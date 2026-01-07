package org.batfish.MEVNEW.EnsembleVerification;

import org.batfish.MEVNEW.EnsembleModel.Dependency;
import org.batfish.MEVNEW.EnsembleVerification.FDG.FDG;
import org.batfish.MEVNEW.EnsembleVerification.FDG.FDGNode;
import org.batfish.MEVNEW.EnsembleVerification.FDG.FDGNodeDependencyPair;
import org.batfish.MEVNEW.EnsembleVerification.FDG.FDGNodeSubPrefixPair;
import org.batfish.MEVNEW.EnsembleVerification.FIB.DependencyFIB;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FIBRadixNode;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthopRouter;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthopType;
import org.batfish.MEVNEW.EnsembleVerification.ForwardingTopology.ForwardingTopology;
import org.batfish.MEVNEW.EnsembleVerification.ForwardingTopology.PrefixParser;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropertyVerificationEngine {

    public ForwardingTopology _forwardingTopology;

    public Map<String, FDG> _fdgMap;

    public Map<String, Configuration> _configurations;

    public Map<String, Map<String, Integer>> _vrfToVniList;




    public Map<String, HashMap<String, String>> _violationSubTree;
    public List<FDGNode> _allViolatedTopLevelPropertyNode;
    public PropertyVerificationEngine(Map<String, Configuration> configurations, ForwardingTopology forwardingTopology) {
        this._forwardingTopology = forwardingTopology;
        this._configurations = configurations;
        this._vrfToVniList = new HashMap<>();
        this._fdgMap = new HashMap<>();
        this._violationSubTree = new HashMap<>();
        this._allViolatedTopLevelPropertyNode = new ArrayList<>();
        computeVrfToVniList(configurations);
    }







    public void computeVrfToVniList(Map<String, Configuration> configurations)
    {
        for (String router : configurations.keySet())
        {
            if (!this._vrfToVniList.containsKey(router))
            {
                this._vrfToVniList.put(router, new HashMap<>());
            }
            for (Vrf vrf : configurations.get(router).getVrfs().values())
            {
                String vrfName = vrf.getName();
                if (vrf.getLayer3Vnis().size() != 0)
                {
                    for (Integer vni : vrf.getLayer3Vnis().keySet())
                    {
                        this._vrfToVniList.get(router).put(vrfName, vni);
                    }
                } else if (vrfName.equals("default"))
                {
                    this._vrfToVniList.get(router).put(vrfName, 0);
                }
            }
        }
    }

    public void propertyVerifierWithoutSymbolicWithSplitPDG(Map<String, List<Property>> tobeVerifiedProperty) {
        for (String sourceRouter : tobeVerifiedProperty.keySet())
        {
            FDG currentFDG = null;
            if (_fdgMap.containsKey(sourceRouter))
            {
                currentFDG = _fdgMap.get(sourceRouter);
            } else {
                currentFDG = new FDG();
                _fdgMap.put(sourceRouter, currentFDG);
            }

            currentFDG.addPropertyNodes(tobeVerifiedProperty.get(sourceRouter));

            while (!currentFDG.getToBeVerifiedIpNodes().isEmpty() || !currentFDG.getToBeVerifiedSubnetNodes().isEmpty()) {
                Set<FDGNodeDependencyPair> FDGNodePropertyPairs = new HashSet<>();
                // 这里分一下类，符号化和非符号化，符号化的部分需要重新构建符号化的转发表，非符号化的部分不需要，符号化的部分需要使用同一个BDDFactory，并且，需要拷贝对应的转发表
                List<PropertyVerificationResultPair> propertySubnetVerificationResultPairs = currentFDG
                        .getToBeVerifiedSubnetNodes()
                        .stream()
//                        .parallelStream()
                        .flatMap(this::forwardingPacket)
                        .collect(Collectors.toList());
                List<PropertyVerificationResultPair> propertyIpVerificationResultPairs = currentFDG
                        .getToBeVerifiedIpNodes()
//                        .stream()
                        .parallelStream()
                        .flatMap(this::forwardingPacket)
                        .collect(Collectors.toList());

                List<PropertyVerificationResultPair> propertyVerificationResultPairs = new ArrayList<>();
                propertyVerificationResultPairs.addAll(propertyIpVerificationResultPairs);
                propertyVerificationResultPairs.addAll(propertySubnetVerificationResultPairs);
                currentFDG.FDGClear();
                for (PropertyVerificationResultPair propertyVerificationResultPair : propertyVerificationResultPairs) {
                    if (propertyVerificationResultPair.getVerifiedResult()) {
                        if (propertyVerificationResultPair.getFdgNodeSubPrefixPairs() != null) {
                            for (FDGNodeSubPrefixPair fdgNodeSubPrefixPair : propertyVerificationResultPair.getFdgNodeSubPrefixPairs()) {
                                if (fdgNodeSubPrefixPair.getVerified()) {
                                    List<Integer> graphIndex = currentFDG.addSubPrefixProperties(fdgNodeSubPrefixPair);
                                    Set<FDGNodeDependencyPair> dependencies = new HashSet<>();
                                    for (Set<Dependency> dependencyList : fdgNodeSubPrefixPair.getDep().getNestedDependency()) {
                                        for (Dependency dependency : dependencyList) {
                                            dependencies.add(new FDGNodeDependencyPair(graphIndex, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
                                        }
                                    }
                                    FDGNodePropertyPairs.addAll(dependencies);
                                    for (Integer nodeIndex : graphIndex) {
                                        currentFDG.getGraph().get(nodeIndex).setVerifiedResult(1);
                                        currentFDG.addRelatedVrf(nodeIndex, fdgNodeSubPrefixPair.getRelatedVrf());
                                    }
                                } else {
                                    List<Integer> graphIndex = currentFDG.addSubPrefixProperties(fdgNodeSubPrefixPair);
                                    for (Integer nodeIndex : graphIndex) {
                                        currentFDG.getGraph().get(nodeIndex).setVerifiedResult(0);
                                        currentFDG.addRelatedVrf(nodeIndex, fdgNodeSubPrefixPair.getRelatedVrf());
                                    }
                                }
                            }
                        } else if (propertyVerificationResultPair.getFdgNodeDependencyPairs() != null) {
                            FDGNodePropertyPairs.addAll(propertyVerificationResultPair.getFdgNodeDependencyPairs());
                        }
                        currentFDG.getGraph().get(propertyVerificationResultPair.getGraphIndex()).setVerifiedResult(1);
                        currentFDG.getGraph().get(propertyVerificationResultPair.getGraphIndex()).addDep(propertyVerificationResultPair.getDep());
                        currentFDG.addRelatedVrf(propertyVerificationResultPair.getGraphIndex(), propertyVerificationResultPair.getRelatedVrf());
                    } else {
                        if (propertyVerificationResultPair.getFdgNodeSubPrefixPairs() != null) {
                            for (FDGNodeSubPrefixPair fdgNodeSubPrefixPair : propertyVerificationResultPair.getFdgNodeSubPrefixPairs()) {
                                if (fdgNodeSubPrefixPair.getVerified()) {
                                    List<Integer> graphIndex = currentFDG.addSubPrefixProperties(fdgNodeSubPrefixPair);
                                    Set<FDGNodeDependencyPair> dependencies = new HashSet<>();
                                    for (Set<Dependency> dependencyList : fdgNodeSubPrefixPair.getDep().getNestedDependency()) {
                                        for (Dependency dependency : dependencyList) {
                                            dependencies.add(new FDGNodeDependencyPair(graphIndex, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
                                        }
                                    }
                                    FDGNodePropertyPairs.addAll(dependencies);
                                    for (Integer nodeIndex : graphIndex) {
                                        currentFDG.getGraph().get(nodeIndex).setVerifiedResult(1);
                                        currentFDG.addRelatedVrf(nodeIndex, fdgNodeSubPrefixPair.getRelatedVrf());
                                    }
                                } else {
                                    List<Integer> graphIndex = currentFDG.addSubPrefixProperties(fdgNodeSubPrefixPair);
                                    for (Integer nodeIndex : graphIndex) {
                                        currentFDG.getGraph().get(nodeIndex).setVerifiedResult(0);
                                        currentFDG.addRelatedVrf(nodeIndex, fdgNodeSubPrefixPair.getRelatedVrf());
                                    }
                                }
                            }
                        }
                        currentFDG.getGraph().get(propertyVerificationResultPair.getGraphIndex()).setVerifiedResult(0);
                        currentFDG.addRelatedVrf(propertyVerificationResultPair.getGraphIndex(), propertyVerificationResultPair.getRelatedVrf());
                    }
                }
                currentFDG.addDependentProperties(new ArrayList<>(FDGNodePropertyPairs));
            }
        }
    }

    public Stream<PropertyVerificationResultPair> forwardingPacket(FDGNode property)
    {
        String srcRouter = property.getSrcRouter();
        String srcVrf = property.getSrcVrf();
        String dstRouter = property.getDstRouter();
        String dstVrf = property.getDstVrf();
        Prefix dstPrefix = property.getDstPrefix();

        ForwardingTopology forwardingTopology = _forwardingTopology;

        String dstIp = PrefixParser.convertToBinaryIP(dstPrefix.toString());
        Packet initialPacket = new Packet(srcRouter, srcVrf, dstPrefix);
        Integer vni = forwardingTopology.getForwardingTopology().get(srcRouter).getVrfToVni().get(srcVrf);
        initialPacket.setVni(vni);
        List<Packet> receivedPackets = new ArrayList<>();
        Queue<Packet> forwardingPackets = new LinkedList<>();
        forwardingPackets.add(initialPacket);

        while (!forwardingPackets.isEmpty())
        {
            List<Packet> tempForwardingPacket = new ArrayList<>();
            for (Packet packet : forwardingPackets)
            {
                String stateRouter = packet.getStateRouter();
                Integer forwardingVni = packet.getVni();
                String stateVrf = forwardingTopology.getForwardingTopology().get(stateRouter).getVniToVrf().get(forwardingVni);
                DependencyFIB dependencyFIB = forwardingTopology.getForwardingTopology().get(stateRouter).getDependencyFIB(forwardingVni);
                RouterVrfPair stateForwardingNode = new RouterVrfPair(stateRouter, stateVrf);
                if (packet.getForwardingPath().contains(stateForwardingNode))
                {
                    packet.setVerified(false);
                } else {
                    packet.addForwardingPath(stateForwardingNode);
                    if (stateRouter.equals(dstRouter) && stateVrf.equals(dstVrf))
                    {
                        packet.setVerified(true);
                        receivedPackets.add(packet);
                    } else if (dependencyFIB != null)
                    {
                        FIBRadixNode matchedFIBEntry = dependencyFIB.getFIBRadixTrie().getMatchForwardingResult(dstIp);
                        if (matchedFIBEntry.getNexthop() == null)
                        {
                            packet.setVerified(false);
                        } else if (matchedFIBEntry.getNexthop().getNexthopType().equals(ForwardingNexthopType.ROUTER))
                        {
                            ForwardingNexthopRouter nexthopRouter = (ForwardingNexthopRouter) matchedFIBEntry.getNexthop();
                            packet.addDep(matchedFIBEntry.getDep());
                            packet.setVni(matchedFIBEntry.getForwardingVni());
                            packet.setStateRouter(nexthopRouter.getNexthopRouter());
                            tempForwardingPacket.add(packet);
                        } else if (matchedFIBEntry.getNexthop().getNexthopType().equals(ForwardingNexthopType.LOCAL) || matchedFIBEntry.getNexthop().getNexthopType().equals(ForwardingNexthopType.ERROR)){
                            packet.setVerified(false);
                        }
                    } else {
                        packet.setVerified(false);
                    }
                }
            }
            forwardingPackets.clear();
            forwardingPackets.addAll(tempForwardingPacket);
        }

        if (receivedPackets.isEmpty())
        {
            return Stream.of(new PropertyVerificationResultPair(false, property.getGraphIndex()));
        } else {
            Set<FDGNodeDependencyPair> dependencies = new HashSet<>();
            Packet packet = receivedPackets.get(0);
            for (Set<Dependency> dependencyList : packet.getDep().getNestedDependency())
            {
                for (Dependency dependency : dependencyList)
                {
                    List<Integer> nodeList = new ArrayList<>();
                    nodeList.add(property.getGraphIndex());
                    dependencies.add(new FDGNodeDependencyPair(nodeList, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
                }
            }
            return Stream.of(new PropertyVerificationResultPair(true, property.getGraphIndex(), packet.getDep(), dependencies, packet.getRelatedVrf()));
        }
    }

    public Map<String, FDG> getFDG()
    {
        return this._fdgMap;
    }


    public void computeFinalVerificationResult()
    {
        for (FDG fdg : _fdgMap.values())
        {
            fdg.findRootNodes();
            fdg.computeFinalVerificationResult();
        }
    }
    public void computeViolationTree()
    {
        for (String treeName : _fdgMap.keySet())
        {
            _fdgMap.get(treeName).findViolatedRoot();
            if (!_violationSubTree.containsKey(treeName))
            {
                _violationSubTree.put(treeName, new HashMap<>());
            }
            _violationSubTree.get(treeName).putAll(_fdgMap.get(treeName).getViolationTree());
        }
    }





    public void printViolationTree(String srcRouter, String srcVrf, String dstRouter, String dstVrf, String dstPrefix)
    {
        boolean print = false;
        if (_violationSubTree.containsKey(srcRouter))
        {
            String treeName = srcRouter + "--" + srcVrf + "--" + dstRouter + "--" + dstVrf + "--" + dstPrefix;
            if (_violationSubTree.get(srcRouter).containsKey(treeName))
            {
                try {
                    Path outDir = Paths.get("/home/liuxz/MEV/Final-Version/batfish-master/violationTree");
                    if (!Files.exists(outDir)) {
                        Files.createDirectories(outDir);
                    }

                    // DOT 临时文件
                    Path dotFile = Files.createTempFile("graph", ".dot");
                    Files.write(dotFile, _violationSubTree.get(srcRouter).get(treeName) .getBytes(StandardCharsets.UTF_8));

                    // PNG 输出文件
                    Path pngFile = outDir.resolve(treeName.split("/")[0] + ".png");

                    // 调用 Graphviz dot
                    Process process = new ProcessBuilder(
                            "dot", "-Tpng", dotFile.toString(), "-o", pngFile.toString()
                    ).redirectErrorStream(true).start();

                    // 打印 dot 输出，便于调试
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.err.println("[dot] " + line);
                        }
                    }

                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        System.err.println("Graphviz dot failed, exit code = " + exitCode);
                        return;
                    }

                    System.out.println("Graph saved to: " + pngFile.toAbsolutePath());

                    // 删除临时 DOT 文件
                    Files.deleteIfExists(dotFile);
                    print = true;
                } catch (IOException e)
                {
                    System.err.println("I/O error while rendering Graphviz graph:");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 正确做法
                    System.err.println("Graphviz rendering was interrupted");
                }
            }
        }
        if (!print)
        {
            System.out.println("There is no such violation tree!!!");
        }
    }

    public void computeAllViolatedTopLevelPropertyNode()
    {
        for (FDG fdg : _fdgMap.values())
        {
            _allViolatedTopLevelPropertyNode.addAll(fdg.getViolatedRootNode());
        }
    }



    public void printTopViolationTable() {
        // 表头
        String[] headers = {
                "SRC Router", "SRC VRF", "DST Router", "DST VRF", "DST Prefix"
        };

        // 求每一列最大长度用于对齐
        int[] colWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }

        // 计算列宽
        for (FDGNode r : _allViolatedTopLevelPropertyNode) {
            colWidths[0] = Math.max(colWidths[0], r.getSrcRouter().length());
            colWidths[1] = Math.max(colWidths[1], r.getSrcVrf().length());
            colWidths[2] = Math.max(colWidths[2], r.getDstRouter().length());
            colWidths[3] = Math.max(colWidths[3], r.getDstVrf().length());
            colWidths[4] = Math.max(colWidths[4], r.getDstPrefix().toString().length());
        }

        // 打印水平分隔线
        printLine(colWidths);

        // 打印表头
        printRow(headers, colWidths);

        printLine(colWidths);

        // 打印内容
        for (FDGNode r : _allViolatedTopLevelPropertyNode) {
            printRow(new String[]{
                    r.getSrcRouter(), r.getSrcVrf(), r.getDstRouter(), r.getDstVrf(), r.getDstPrefix().toString()
            }, colWidths);
        }

        printLine(colWidths);
    }

    private void printLine(int[] colWidths) {
        StringBuilder sb = new StringBuilder();
        sb.append("+");
        for (int w : colWidths) {
            for (int i = 0; i < w + 2; i++) {
                sb.append("-");
            }
            sb.append("+");
        }
        System.out.println(sb);
    }

    private static void printRow(String[] items, int[] colWidths) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int i = 0; i < items.length; i++) {
            sb.append(" ");
            sb.append(String.format("%-" + colWidths[i] + "s", items[i]));
            sb.append(" |");
        }
        System.out.println(sb);
    }
}
