package org.batfish.MEV;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;

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

    public HashMap<String, ForwardingTopology> _forwardingEngine;

    public List<String> _priority = new ArrayList<>();

    public PDG _pdg;

    public Map<String, Configuration> _configurations;

    public BDDFactory _bddFactory;

    public Map<String, Map<String, Integer>> _vrfToVniList;

    public Set<RouterVrfPair> _incrementVrf;

    public PropertyVerificationEngine(Map<String, Configuration> configurations) {
        this._priority = new ArrayList<>();
        this._priority.add("EVPNL3VPN");
        this._priority.add("Underlay");
        this._pdg = new PDG();
        this._configurations = configurations;
        this._forwardingEngine = new HashMap<>();
        this._vrfToVniList = new HashMap<>();
        generateVrfToVniList(configurations);

    }







    public void generateVrfToVniList(Map<String, Configuration> configurations)
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











    public void incrementVrfToVniList(List<VRFAdd> incrementVrf) {
        for (VRFAdd vrfAdd : incrementVrf) {
            String router = vrfAdd.getRouter();
            String vrf = vrfAdd.getVrf();
            Integer vni = vrfAdd.getVni();
            if (!this._vrfToVniList.containsKey(router)) {
                this._vrfToVniList.put(router, new HashMap<>());
            }
            this._vrfToVniList.get(router).put(vrf, vni);
        }
        this._forwardingEngine.get("EVPNL3VPN").setVrfToVniList(this._vrfToVniList);
    }








    public void incrementForwardingTopology(Set<RouterVrfPair> incrementVrf, ControlPlaneGraphModelConstructEngine controlPlaneGraphModelConstructEngin)
    {
        List<VRF> incrementVRFFib = new ArrayList<>();
        for (RouterVrfPair vrfAdd : incrementVrf)
        {
            String router = vrfAdd.getRouter();
            String vrf = vrfAdd.getVrf();
            if (!this._forwardingEngine.get("EVPNL3VPN").getForwardingTopology().containsKey(router))
            {
                this._forwardingEngine.get("EVPNL3VPN").getForwardingTopology().put(router, new ForwardingNode(router));
            }
            Node vrfNode =  controlPlaneGraphModelConstructEngin.getEVPNL3vpnGraph().getVRF(router, vrf);
            if (vrfNode != null)
            {
                incrementVRFFib.add((VRF)vrfNode);
            }
        }
        this._forwardingEngine.get("EVPNL3VPN").generateDependencyFib(incrementVRFFib);
    }
    public void incrementFowardingEngine(List<VRFAdd> addVrf, Set<RouterVrfPair> incrementVrf, ControlPlaneGraphModelConstructEngine engine)
    {
        this.incrementVrfToVniList(addVrf);
        this.incrementForwardingTopology(incrementVrf, engine);
    }
    public void generateForwardingTopology(ControlPlaneGraphModelConstructEngine controlPlaneGraphModelConstructEngine)
    {
        this._forwardingEngine.put("Underlay", new ForwardingTopology(_configurations));
        this._forwardingEngine.put("EVPNL3VPN", new ForwardingTopology(_configurations));
        this._forwardingEngine.put("VXLANL2VPN", new ForwardingTopology(_configurations));
        this._forwardingEngine.get("Underlay").setVrfToVniList(this._vrfToVniList);
        this._forwardingEngine.get("EVPNL3VPN").setVrfToVniList(this._vrfToVniList);
        this._forwardingEngine.get("Underlay").generateDependencyFib(controlPlaneGraphModelConstructEngine.getUnderlayVrfNodeSet());
        this._forwardingEngine.get("EVPNL3VPN").generateDependencyFib(controlPlaneGraphModelConstructEngine.getEVPNL3vpnGraph().getVRFNodes());
//        this._forwardingEngine.get("VXLANL2VPN").generateDependencyFib(controlPlaneGraphModelConstructEngine.getVXLANL2VPNGraph().getVRFNodes());
    }
    public void addProtocolLayerEngine(String protocolLayer, ForwardingTopology forwardingTopology)
    {
        _forwardingEngine.put(protocolLayer, forwardingTopology);
    }




//    使用非并行的对应BDD库
    public void propertyVerifier(BDDFactory prefixFactory, List<Property> tobeVerifiedProperty, boolean increment)
    {
        _pdg.addPropertyNodes(tobeVerifiedProperty);
        List<Property> unverifiedProperty = new ArrayList<>();



        List<Property> addProperty = new ArrayList<>();
        if (increment)
        {
            ForwardingTopology forwardingTopology = this._forwardingEngine.get("EVPNL3VPN");
            for (RouterVrfPair nodeVrfPair : this._incrementVrf)
            {
                String nodeName = nodeVrfPair.getRouter();
                String vrfName = nodeVrfPair.getVrf();
                ForwardingNode forwardingNode = forwardingTopology.getForwardingTopology().get(nodeName);
                Integer vni = forwardingNode.getVrfToVni().get(vrfName);
                if (vni == null || !forwardingNode.getVniToVrf().containsKey(vni))
                {
                    continue;
                }
                DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB(vni);
                dependencyFIB.convertSymbolicFIB(prefixFactory);
            }
        } else {
            for (String forwardingEngineName : this._forwardingEngine.keySet())
            {
                ForwardingTopology forwardingTopology = this._forwardingEngine.get(forwardingEngineName);
                for (String nodeName : forwardingTopology.getForwardingTopology().keySet())
                {
                    ForwardingNode forwardingNode = forwardingTopology.getForwardingTopology().get(nodeName);
                    for (DependencyFIB dependencyFIB : forwardingNode.getDependencyFIB().values())
                    {
                        dependencyFIB.convertSymbolicFIB(prefixFactory);
                    }
                }
            }
        }
        int propertyNum = 0;
        while (!_pdg.getToBeVerifiedIpNodes().isEmpty() || !_pdg.getToBeVerifiedSubnetNodes().isEmpty())
        {

            List<PropertyVerificationResultPair> propertySubnetVerificationResultPairs = _pdg
                    .getToBeVerifiedSubnetNodes()
                    .stream()
//                    .parallelStream()
                    .flatMap(propertyNode -> {
                        // 这里分一下类，符号化和非符号化，符号化的部分需要重新构建符号化的转发表，非符号化的部分不需要，符号化的部分需要使用同一个BDDFactory，并且，需要拷贝对应的转发表
                        if (propertyNode.getDstPrefix().getPrefixLength() != 32 || propertyNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            Integer longestLength = 0;
                            String srcRotuer = propertyNode.getSrcRouter();
                            String srcVrf = propertyNode.getSrcVrf();
                            Prefix dstPrefix = propertyNode.getDstPrefix();
                            String forwardingEngineName = "";
                            BDD dstBDDPrefix = PrefixToBDDConverter.convertPrefixToBDD(prefixFactory, dstPrefix.toString());
                            ForwardingTopology forwardingTopology = null;
                            for (String forwardingEngine : _forwardingEngine.keySet())
                            {
                                forwardingTopology = _forwardingEngine.get(forwardingEngine);
                                ForwardingNode forwardingNode = _forwardingEngine.get(forwardingEngine).getForwardingTopology().get(srcRotuer);
                                if (forwardingNode == null)
                                {
                                    continue;
                                }
                                Integer vni = 0;
                                if (forwardingNode.getVrfToVni().containsKey(srcVrf) && forwardingNode.getVrfToVni().get(srcVrf) != null)
                                {
                                    vni = forwardingNode.getVrfToVni().get(srcVrf);
                                } else {
                                    continue;
                                }
                                DependencyFIB fib = forwardingNode.getDependencyFIB(vni);
                                for (BDD prefix : fib.getSymbolicFib().keySet())
                                {
                                    if (dstBDDPrefix.and(prefix).isZero())
                                    {
                                        continue;
                                    }
                                    forwardingEngineName = forwardingEngine;
                                    break;
                                }
                                if (!forwardingEngineName.isEmpty())
                                {
                                    break;
                                }
                            }
                            if (!forwardingEngineName.isEmpty())
                            {
                                return forwardingPacket(forwardingTopology, propertyNode, prefixFactory);
                            } else {
                                return Stream.of(new PropertyVerificationResultPair(false, propertyNode.getGraphIndex()));
                            }
                        } else {
                            return forwardingPacket(propertyNode);
                        }
                    })
                    .collect(Collectors.toList());
//            List<PropertyVerificationResultPair> propertyIpVerificationResultPairs = _pdg
//                    .getToBeVerifiedIpNodes()
////                    .stream()
//                    .parallelStream()
//                    .flatMap(propertyNode -> {
//                        // 这里分一下类，符号化和非符号化，符号化的部分需要重新构建符号化的转发表，非符号化的部分不需要，符号化的部分需要使用同一个BDDFactory，并且，需要拷贝对应的转发表
//                        if (propertyNode.getDstPrefix().getPrefixLength() != 32)
//                        {
//                            Integer longestLength = 0;
//                            String srcRotuer = propertyNode.getSrcRouter();
//                            String srcVrf = propertyNode.getSrcVrf();
//                            Prefix dstPrefix = propertyNode.getDstPrefix();
//                            String forwardingEngineName = "";
//                            BDD dstBDDPrefix = PrefixToBDDConverter.convertPrefixToBDD(prefixFactory, dstPrefix.toString());
//                            ForwardingTopology forwardingTopology = null;
//                            for (String forwardingEngine : _forwardingEngine.keySet())
//                            {
//                                forwardingTopology = _forwardingEngine.get(forwardingEngine);
//                                ForwardingNode forwardingNode = _forwardingEngine.get(forwardingEngine).getForwardingTopology().get(srcRotuer);
//                                if (forwardingNode == null)
//                                {
//                                    continue;
//                                }
//                                Integer vni = 0;
//                                if (forwardingNode.getVrfToVni().containsKey(srcVrf) && forwardingNode.getVrfToVni().get(srcVrf) != null)
//                                {
//                                    vni = forwardingNode.getVrfToVni().get(srcVrf);
//                                } else {
//                                    continue;
//                                }
//                                DependencyFIB fib = forwardingNode.getDependencyFIB(vni);
//                                for (BDD prefix : fib.getSymbolicFib().keySet())
//                                {
//                                    if (dstBDDPrefix.and(prefix).isZero())
//                                    {
//                                        continue;
//                                    }
//                                    forwardingEngineName = forwardingEngine;
//                                    break;
//                                }
//                                if (!forwardingEngineName.isEmpty())
//                                {
//                                    break;
//                                }
//                            }
//                            if (!forwardingEngineName.isEmpty())
//                            {
//                                return forwardingPacket(forwardingTopology, propertyNode, prefixFactory);
//                            } else {
//                                return Stream.of(new PropertyVerificationResultPair(false, propertyNode.getGraphIndex()));
//                            }
//                        } else {
//                            return forwardingPacket(propertyNode);
//                        }
//                    })
//                    .collect(Collectors.toList());
            List<PropertyVerificationResultPair> propertyVerificationResultPairs = new ArrayList<>();
//            propertyVerificationResultPairs.addAll(propertyIpVerificationResultPairs);
            propertyVerificationResultPairs.addAll(propertySubnetVerificationResultPairs);
            propertyNum = propertyNum + propertyVerificationResultPairs.size();
            _pdg.PDGClear();
            for (PropertyVerificationResultPair propertyVerificationResultPair : propertyVerificationResultPairs)
            {
                if (propertyVerificationResultPair.getVerifiedResult())
                {
                    for (PDGNodeDependencyPair pair : propertyVerificationResultPair.getPdgNodeDependencyPairs())
                    {
                        _pdg._toBeVerifiedSubnetNodes.add(new PDGNode(pair.getProperty().getSrcRouter(), pair.getProperty().getSrcVrf(), pair.getProperty().getDstRouter(), pair.getProperty().getDstVrf(), pair.getProperty().getDstPrefix(), 1));
//                        if (pair.getProperty().getDstPrefix().getPrefixLength()!=32)
//                        {
//                            _pdg._toBeVerifiedSubnetNodes.add(new PDGNode(pair.getProperty().getSrcRouter(), pair.getProperty().getSrcVrf(), pair.getProperty().getDstRouter(), pair.getProperty().getDstVrf(), pair.getProperty().getDstPrefix(), 1));
//                        } else {
//                            _pdg._toBeVerifiedIpNodes.add(new PDGNode(pair.getProperty().getSrcRouter(), pair.getProperty().getSrcVrf(), pair.getProperty().getDstRouter(), pair.getProperty().getDstVrf(), pair.getProperty().getDstPrefix(), 1));
//                        }
                    }
                }
            }
        }
        System.out.println("verification-property-num:" + propertyNum);
//        while (!_pdg._toBeVerifiedNodes.isEmpty())
//        {
//
//
//
//            Set<PDGNodeDependencyPair> PDGNodePropertyPairs = new HashSet<>();
//            List<PropertyVerificationResultPair> propertyVerificationResultPairs = _pdg
//                    .getToBeVerifiedNodes()
//                    .stream()
////                    .parallelStream()
//                    .flatMap(propertyNode -> {
//                        // 这里分一下类，符号化和非符号化，符号化的部分需要重新构建符号化的转发表，非符号化的部分不需要，符号化的部分需要使用同一个BDDFactory，并且，需要拷贝对应的转发表
//                        if (propertyNode.getDstPrefix().getPrefixLength() != 32)
//                        {
//                            Integer longestLength = 0;
//                            String srcRotuer = propertyNode.getSrcRouter();
//                            String srcVrf = propertyNode.getSrcVrf();
//                            Prefix dstPrefix = propertyNode.getDstPrefix();
//                            String forwardingEngineName = "";
//                            BDD dstBDDPrefix = PrefixToBDDConverter.convertPrefixToBDD(prefixFactory, dstPrefix.toString());
//                            ForwardingTopology forwardingTopology = null;
//                            for (String forwardingEngine : _forwardingEngine.keySet())
//                            {
//                                forwardingTopology = _forwardingEngine.get(forwardingEngine);
//                                ForwardingNode forwardingNode = _forwardingEngine.get(forwardingEngine).getForwardingTopology().get(srcRotuer);
//                                if (forwardingNode == null)
//                                {
//                                    continue;
//                                }
//                                Integer vni = 0;
//                                if (forwardingNode.getVrfToVni().containsKey(srcVrf) && forwardingNode.getVrfToVni().get(srcVrf) != null)
//                                {
//                                    vni = forwardingNode.getVrfToVni().get(srcVrf);
//                                } else {
//                                    continue;
//                                }
//                                DependencyFIB fib = forwardingNode.getDependencyFIB(vni);
//                                for (BDD prefix : fib.getSymbolicFib().keySet())
//                                {
//                                    if (dstBDDPrefix.and(prefix).isZero())
//                                    {
//                                        continue;
//                                    }
//                                    forwardingEngineName = forwardingEngine;
//                                    break;
//                                }
//                                if (!forwardingEngineName.isEmpty())
//                                {
//                                    break;
//                                }
//                            }
//                            if (!forwardingEngineName.isEmpty())
//                            {
//                                return forwardingPacket(forwardingTopology, propertyNode, prefixFactory);
//                            } else {
//                                return Stream.of(new PropertyVerificationResultPair(false, propertyNode.getGraphIndex()));
//                            }
//                        } else {
//                            return forwardingPacket(propertyNode);
//                        }
//                    })
//                    .collect(Collectors.toList());
//            _pdg.PDGClear();
//            for (PropertyVerificationResultPair propertyVerificationResultPair : propertyVerificationResultPairs)
//            {
//                if (propertyVerificationResultPair.getVerifiedResult())
//                {
//                    if (propertyVerificationResultPair.getPdgNodeSubPrefixPairs() != null)
//                    {
//                        for (PDGNodeSubPrefixPair pdgNodeSubPrefixPair : propertyVerificationResultPair.getPdgNodeSubPrefixPairs())
//                        {
//                            if (pdgNodeSubPrefixPair.getVerified())
//                            {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                Set<PDGNodeDependencyPair> dependencies = new HashSet<>();
//                                for (Set<Dependency> dependencyList : pdgNodeSubPrefixPair.getDep().getNestedDependency())
//                                {
//                                    for (Dependency dependency : dependencyList)
//                                    {
//                                        dependencies.add(new PDGNodeDependencyPair(graphIndex, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
//                                    }
//                                }
//                                PDGNodePropertyPairs.addAll(dependencies);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(1);
//                                }
//                            } else {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(0);
//                                }
//                            }
//                        }
//                    } else if (propertyVerificationResultPair.getPdgNodeDependencyPairs() != null)
//                    {
//                        PDGNodePropertyPairs.addAll(propertyVerificationResultPair.getPdgNodeDependencyPairs());
//                    }
//                    _pdg.getGraph().get(propertyVerificationResultPair.getGraphIndex()).setVerifiedResult(1);
//                } else {
//                    if (propertyVerificationResultPair.getPdgNodeSubPrefixPairs() != null)
//                    {
//                        for (PDGNodeSubPrefixPair pdgNodeSubPrefixPair : propertyVerificationResultPair.getPdgNodeSubPrefixPairs())
//                        {
//                            if (pdgNodeSubPrefixPair.getVerified())
//                            {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                Set<PDGNodeDependencyPair> dependencies = new HashSet<>();
//                                for (Set<Dependency> dependencyList : pdgNodeSubPrefixPair.getDep().getNestedDependency())
//                                {
//                                    for (Dependency dependency : dependencyList)
//                                    {
//                                        dependencies.add(new PDGNodeDependencyPair(graphIndex, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
//                                    }
//                                }
//                                PDGNodePropertyPairs.addAll(dependencies);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(1);
//                                }
//                            } else {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(0);
//                                }
//                            }
//                        }
//                    }
//                    _pdg.getGraph().get(propertyVerificationResultPair.getGraphIndex()).setVerifiedResult(0);
//                }
//            }
//            _pdg.addDependentProperties(new ArrayList<>(PDGNodePropertyPairs));
//        }
//    }
//
//    public void propertyVerifier(List<Property> tobeVerifiedProperty)
//    {
//        _pdg.addPropertyNodes(tobeVerifiedProperty);
//        List<Property> unverifiedProperty = new ArrayList<>();
//        List<Property> addProperty = new ArrayList<>();
//        while (!_pdg._toBeVerifiedNodes.isEmpty())
//        {
//
//
//
//            Set<PDGNodeDependencyPair> PDGNodePropertyPairs = new HashSet<>();
//            List<PropertyVerificationResultPair> propertyVerificationResultPairs = _pdg
//                    .getToBeVerifiedNodes()
//                    .parallelStream()
////                    .stream()
//                    .flatMap(propertyNode -> {
//                        // 这里分一下类，符号化和非符号化，符号化的部分需要重新构建符号化的转发表，非符号化的部分不需要，符号化的部分需要使用同一个BDDFactory，并且，需要拷贝对应的转发表
//                        if (propertyNode.getDstPrefix().getPrefixLength() != 32)
//                        {
//                            BDDFactory prefixFactory = JFactory.init(10000, 1000);
//                            prefixFactory.setVarNum(32);
//                            Integer longestLength = 0;
//                            String srcRotuer = propertyNode.getSrcRouter();
//                            String srcVrf = propertyNode.getSrcVrf();
//                            Prefix dstPrefix = propertyNode.getDstPrefix();
//                            String forwardingEngineName = "";
//                            for (String forwardingEngine : _forwardingEngine.keySet())
//                            {
//                                ForwardingTopology forwardingTopology = _forwardingEngine.get(forwardingEngine);
//                                ForwardingNode forwardingNode = _forwardingEngine.get(forwardingEngine).getForwardingTopology().get(srcRotuer);
//                                if (forwardingNode == null)
//                                {
//                                    continue;
//                                }
//                                Integer vni = 0;
//                                if (forwardingNode.getVrfToVni().containsKey(srcVrf) && forwardingNode.getVrfToVni().get(srcVrf) != null)
//                                {
//                                    vni = forwardingNode.getVrfToVni().get(srcVrf);
//                                } else {
//                                    continue;
//                                }
//                                DependencyFIB fib = forwardingNode.getDependencyFIB(vni);
//                                FIBRadixNode matchedFibNode = fib.getFIBRadixTrie().getMatchForwardingResult(PrefixParser.convertToBinaryIP(dstPrefix.toString()));
//                                if (matchedFibNode != null && matchedFibNode.getNexthop() != null)
//                                {
//                                    forwardingEngineName = forwardingEngine;
//                                    break;
//                                }
//                            }
//                            long endTime = System.currentTimeMillis();
//                            if (forwardingEngineName != "")
//                            {
//                                ForwardingTopology forwardingTopology = _forwardingEngine.get(forwardingEngineName);
//                                ForwardingTopology newForwardingTopology = new ForwardingTopology(_configurations);
//                                for (String nodeName : forwardingTopology.getForwardingTopology().keySet())
//                                {
//                                    ForwardingNode forwardingNode = forwardingTopology.getForwardingTopology().get(nodeName);
//                                    ForwardingNode newForwardingNode = newForwardingTopology.getForwardingTopology().get(nodeName);
//                                    newForwardingNode.getVrfToVni().putAll(forwardingNode.getVrfToVni());
//                                    newForwardingNode.getVniToVrf().putAll(forwardingNode.getVniToVrf());
//                                    for (Integer vni : forwardingNode.getDependencyFIB().keySet())
//                                    {
//                                        DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB(vni);
//                                        DependencyFIB newDependencyFIB = new DependencyFIB();
//                                        for (Prefix prefix : dependencyFIB.getFib().keySet())
//                                        {
//                                            BDD bddPrefix = PrefixToBDDConverter.convertPrefixToBDD(prefixFactory, prefix.toString());
//                                            newDependencyFIB.addSymbolicEntry(bddPrefix, dependencyFIB.getFib().get(prefix));
//                                        }
//                                        newForwardingNode.addDependencyFIBEntry(vni, newDependencyFIB);
//                                    }
//                                }
//                                long endTime1 = System.currentTimeMillis();
//                                System.out.println("fib-time:" + (endTime1 - endTime));
//                                Stream<PropertyVerificationResultPair> ans = forwardingPacket(newForwardingTopology, propertyNode, prefixFactory);
//                                long endTime2 = System.currentTimeMillis();
//                                System.out.println("forward-time:" + (endTime2 - endTime1));
//                                return ans;
//                            } else {
//                                return Stream.of(new PropertyVerificationResultPair(false, propertyNode.getGraphIndex()));
//                            }
//                        } else {
//                            return forwardingPacket(propertyNode);
//                        }
//                    })
//                    .collect(Collectors.toList());
//            _pdg.PDGClear();
//            for (PropertyVerificationResultPair propertyVerificationResultPair : propertyVerificationResultPairs)
//            {
//                if (propertyVerificationResultPair.getVerifiedResult())
//                {
//                    if (propertyVerificationResultPair.getPdgNodeSubPrefixPairs() != null)
//                    {
//                        for (PDGNodeSubPrefixPair pdgNodeSubPrefixPair : propertyVerificationResultPair.getPdgNodeSubPrefixPairs())
//                        {
//                            if (pdgNodeSubPrefixPair.getVerified())
//                            {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                Set<PDGNodeDependencyPair> dependencies = new HashSet<>();
//                                for (Set<Dependency> dependencyList : pdgNodeSubPrefixPair.getDep().getNestedDependency())
//                                {
//                                    for (Dependency dependency : dependencyList)
//                                    {
//                                        dependencies.add(new PDGNodeDependencyPair(graphIndex, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
//                                    }
//                                }
//                                PDGNodePropertyPairs.addAll(dependencies);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(1);
//                                }
//                            } else {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(0);
//                                }
//                            }
//                        }
//                    } else if (propertyVerificationResultPair.getPdgNodeDependencyPairs() != null)
//                    {
//                        PDGNodePropertyPairs.addAll(propertyVerificationResultPair.getPdgNodeDependencyPairs());
//                    }
//                    _pdg.getGraph().get(propertyVerificationResultPair.getGraphIndex()).setVerifiedResult(1);
//                } else {
//                    if (propertyVerificationResultPair.getPdgNodeSubPrefixPairs() != null)
//                    {
//                        for (PDGNodeSubPrefixPair pdgNodeSubPrefixPair : propertyVerificationResultPair.getPdgNodeSubPrefixPairs())
//                        {
//                            if (pdgNodeSubPrefixPair.getVerified())
//                            {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                Set<PDGNodeDependencyPair> dependencies = new HashSet<>();
//                                for (Set<Dependency> dependencyList : pdgNodeSubPrefixPair.getDep().getNestedDependency())
//                                {
//                                    for (Dependency dependency : dependencyList)
//                                    {
//                                        dependencies.add(new PDGNodeDependencyPair(graphIndex, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
//                                    }
//                                }
//                                PDGNodePropertyPairs.addAll(dependencies);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(1);
//                                }
//                            } else {
//                                List<Integer> graphIndex = _pdg.addSubPrefixProperties(pdgNodeSubPrefixPair);
//                                for (Integer nodeIndex : graphIndex)
//                                {
//                                    _pdg.getGraph().get(nodeIndex).setVerifiedResult(0);
//                                }
//                            }
//                        }
//                    }
//                    _pdg.getGraph().get(propertyVerificationResultPair.getGraphIndex()).setVerifiedResult(0);
//                }
//            }
//            _pdg.addDependentProperties(new ArrayList<>(PDGNodePropertyPairs));
//        }
    }


    public Stream<PropertyVerificationResultPair> forwardingPacket(ForwardingTopology forwardingTopology, PDGNode property, BDDFactory factory)
    {
        String srcRouter = property.getSrcRouter();
        String srcVrf = property.getSrcVrf();
        String dstRouter = property.getDstRouter();
        String dstVrf = property.getDstVrf();
        Prefix dstPrefix = property.getDstPrefix();
        Integer vni = forwardingTopology.getForwardingTopology().get(srcRouter).getVrfToVni().get(srcVrf);
        Packet initialPacket = new Packet(srcRouter, srcVrf, dstPrefix);
        initialPacket.setSymbolicDstPrefix(PrefixToBDDConverter.convertPrefixToBDD(factory, dstPrefix.toString()));
        initialPacket.setVni(vni);
        BDD symbolicPropertyPrefix = PrefixToBDDConverter.convertPrefixToBDD(factory, dstPrefix.toString());
        List<Packet> receivedPackets = new ArrayList<>();

        if (dstPrefix.getPrefixLength()!=32)
        {
            PacketWrapper wrapper = new PacketWrapper(initialPacket, "1", "new");
            PacketWrapper wrapper1 = new PacketWrapper(initialPacket, "1", "new");
        }





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
                BDD forwardingPrefix = packet.getSymbolicDstPrefix();
                DependencyFIB dependencyFIB = forwardingTopology.getForwardingTopology().get(stateRouter).getDependencyFIB(forwardingVni);
                packet.addRelatedVrf(stateRouter + "-" + stateVrf);
                if (stateRouter.equals(dstRouter) && stateVrf.equals(dstVrf))
                {
                    packet.setVerified(true);
                    receivedPackets.add(packet);
                } else if (dependencyFIB != null)
                {
                    for (BDD fibPrefix : dependencyFIB.getSymbolicFib().keySet())
                    {
                        DependencyFIBEntry matchedFIBEntry = dependencyFIB.getSymbolicFib().get(fibPrefix);
                        BDD tempSymbolicPrefix = fibPrefix.and(forwardingPrefix);
                        if (!tempSymbolicPrefix.isZero())
                        {
                            if (matchedFIBEntry.getNexthop().isType(ForwardingNexthopType.ROUTER))
                            {
                                Packet tempPacket = packet.toNewPacket();
                                tempPacket.setSymbolicDstPrefix(tempSymbolicPrefix);
                                tempPacket.addDep(matchedFIBEntry.getDep());
                                tempPacket.setVni(matchedFIBEntry.getVni());
                                tempPacket.setStateRouter(matchedFIBEntry.getNexthop().getNexthopRouter());
                                tempForwardingPacket.add(tempPacket);
                                forwardingPrefix = forwardingPrefix.and(fibPrefix.not());
                            } else if (matchedFIBEntry.getNexthop().isType(ForwardingNexthopType.LOCAL)) {
                                // 路由重分布的情况，待实现
                                packet.setVerified(false);
                                receivedPackets.add(packet);
                            } else {

                            }
                        }
                        if (forwardingPrefix.isZero())
                        {
                            break;
                        }
                    }
                } else {
                    packet.setVerified(false);
                    receivedPackets.add(packet);
                    // 错误目的地，不处理
                }
            }
            forwardingPackets.clear();
            forwardingPackets.addAll(tempForwardingPacket);
        }

        if (receivedPackets.isEmpty())
        {
            return Stream.of(new PropertyVerificationResultPair(false, property.getGraphIndex()));
        } else {
            if (receivedPackets.size() == 1)
            {
                Packet receivedPacket = receivedPackets.get(0);
                if (receivedPacket.getVerified())
                {
                    if (receivedPacket.getSymbolicDstPrefix().equals(symbolicPropertyPrefix))
                    {
                        Set<PDGNodeDependencyPair> dependencies = new HashSet<>();
                        for (Set<Dependency> dependencyList : receivedPacket.getDep().getNestedDependency())
                        {
                            for (Dependency dependency : dependencyList)
                            {
                                List<Integer> parentNode = new ArrayList<>();
                                parentNode.add(property.getGraphIndex());
                                dependencies.add(new PDGNodeDependencyPair(parentNode, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
                            }
                        }
                        return Stream.of(new PropertyVerificationResultPair(true, property.getGraphIndex(), receivedPacket.getDep(), dependencies, receivedPacket.getRelatedVrf()));
                    } else {
                        List<String> prefixesString = BDDPrefixExtractor.extractAllPrefixes(receivedPacket.getSymbolicDstPrefix(),32);
                        List<Property> subProperties = new ArrayList<>();
                        for (String prefixString : prefixesString)
                        {
                            Prefix prefix = Prefix.parse(prefixString);
                            subProperties.add(new Property(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), prefix));
                        }
                        PDGNodeSubPrefixPair pdgNodeSubPrefixPair = new PDGNodeSubPrefixPair(property.getGraphIndex(), subProperties, receivedPacket.getDep(), true, receivedPacket.getRelatedVrf());
                        Set<PDGNodeSubPrefixPair> pdgNodeSubPrefixPairs = new HashSet<>();
                        pdgNodeSubPrefixPairs.add(pdgNodeSubPrefixPair);
                        return Stream.of(new PropertyVerificationResultPair(false, property.getGraphIndex(), pdgNodeSubPrefixPairs, receivedPacket.getRelatedVrf()));
                    }
                } else {
                    Set<PDGNodeDependencyPair> dependencies = new HashSet<>();
                    return Stream.of(new PropertyVerificationResultPair(false, property.getGraphIndex(), new NestedDependency(), dependencies, receivedPacket.getRelatedVrf()));
                }
            } else {
                BDD allReceivedPrefix = factory.zero();
                Set<PDGNodeSubPrefixPair> pdgNodeSubPrefixPairs = new HashSet<>();
                boolean verified = true;
                Set<String> relatedVrf = new HashSet<>();
                for (Packet receivedPacket : receivedPackets)
                {
                    relatedVrf.addAll(receivedPacket.getRelatedVrf());
                    if (receivedPacket.getVerified())
                    {
                        List<String> prefixesString = BDDPrefixExtractor.extractAllPrefixes(receivedPacket.getSymbolicDstPrefix(),32);
                        List<Property> subProperties = new ArrayList<>();
                        for (String prefixString : prefixesString)
                        {
                            Prefix prefix = Prefix.parse(prefixString);
                            subProperties.add(new Property(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), prefix));
                        }
                        PDGNodeSubPrefixPair pdgNodeSubPrefixPair = new PDGNodeSubPrefixPair(property.getGraphIndex(), subProperties, receivedPacket.getDep(), true, receivedPacket.getRelatedVrf());
                        pdgNodeSubPrefixPairs.add(pdgNodeSubPrefixPair);
                    } else {
                        verified = false;
                        List<String> prefixesString = BDDPrefixExtractor.extractAllPrefixes(receivedPacket.getSymbolicDstPrefix(),32);
                        List<Property> subProperties = new ArrayList<>();
                        for (String prefixString : prefixesString)
                        {
                            Prefix prefix = Prefix.parse(prefixString);
                            subProperties.add(new Property(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), prefix));
                        }
                        PDGNodeSubPrefixPair pdgNodeSubPrefixPair = new PDGNodeSubPrefixPair(property.getGraphIndex(), subProperties, receivedPacket.getDep(), false, receivedPacket.getRelatedVrf());
                        pdgNodeSubPrefixPairs.add(pdgNodeSubPrefixPair);
                    }
                }
                if (verified)
                {
                    return Stream.of(new PropertyVerificationResultPair(true, property.getGraphIndex(), pdgNodeSubPrefixPairs, relatedVrf));
                } else {
                    return Stream.of(new PropertyVerificationResultPair(false, property.getGraphIndex(), pdgNodeSubPrefixPairs, relatedVrf));
                }
            }
        }
    }

    public Stream<PropertyVerificationResultPair> forwardingPacket(PDGNode property)
    {
        String srcRouter = property.getSrcRouter();
        String srcVrf = property.getSrcVrf();
        String dstRouter = property.getDstRouter();
        String dstVrf = property.getDstVrf();
        Prefix dstPrefix = property.getDstPrefix();

        ForwardingTopology forwardingTopology = null;

        for (String layer : _priority)
        {
            ForwardingNode node = _forwardingEngine.get(layer).getForwardingTopology().get(srcRouter);
            Integer associatedVni = node.getVrfToVni().get(srcVrf);
            DependencyFIB dependencyFIB = node.getDependencyFIB().get(associatedVni);
            if (dependencyFIB == null)
            {
                continue;
            }
            FIBRadixNode matchFibNode = dependencyFIB.getFIBRadixTrie().getMatchForwardingResult(PrefixParser.convertToBinaryIP(dstPrefix.toString()));
            if (matchFibNode.getNexthop() != null)
            {
                forwardingTopology = _forwardingEngine.get(layer);
                break;
            }
            if (forwardingTopology != null)
            {
                break;
            }
        }

        if (forwardingTopology == null)
        {
            return Stream.of(new PropertyVerificationResultPair(false, property.getGraphIndex()));
        }

        String dstIp = PrefixParser.convertToBinaryIP(dstPrefix.toString());
        Packet initialPacket = new Packet(srcRouter, srcVrf, dstPrefix);
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
                Prefix forwardingPrefix = packet.getPrefix();
                DependencyFIB dependencyFIB = forwardingTopology.getForwardingTopology().get(stateRouter).getDependencyFIB(forwardingVni);
                if (stateRouter.equals(dstRouter) && stateVrf.equals(dstVrf))
                {
                    packet.setVerified(true);
                    receivedPackets.add(packet);
                } else if (dependencyFIB != null)
                {
                    FIBRadixNode matchedFIBEntry = dependencyFIB.getFIBRadixTrie().getMatchForwardingResult(dstIp);
                    if (matchedFIBEntry.getNexthop().isType(ForwardingNexthopType.ROUTER))
                    {
                        packet.addDep(matchedFIBEntry.getDep());
                        packet.setVni(matchedFIBEntry.getForwardingVni());
                        packet.setStateRouter(matchedFIBEntry.getNexthop().getNexthopRouter());
                        tempForwardingPacket.add(packet);
                    } else {
                        // 对应路由重分布的情况，待实现
                    }
                } else {
                    packet.setVerified(false);
                    receivedPackets.add(packet);
                    // 错误目的地，不处理
                }
            }
            forwardingPackets.clear();
            forwardingPackets.addAll(tempForwardingPacket);
        }

        if (receivedPackets.isEmpty())
        {
            return Stream.of(new PropertyVerificationResultPair(false, property.getGraphIndex()));
        } else {
            Set<PDGNodeDependencyPair> dependencies = new HashSet<>();
            Packet packet = receivedPackets.get(0);
            for (Set<Dependency> dependencyList : packet.getDep().getNestedDependency())
            {
                for (Dependency dependency : dependencyList)
                {
                    List<Integer> nodeList = new ArrayList<>();
                    nodeList.add(property.getGraphIndex());
                    dependencies.add(new PDGNodeDependencyPair(nodeList, new Property(dependency._srcRouter, dependency._srcVrf, dependency._dstRouter, dependency._dstVrf, dependency._dstIP.toPrefix())));
                }
            }
            return Stream.of(new PropertyVerificationResultPair(true, property.getGraphIndex(), packet.getDep(), dependencies, packet.getRelatedVrf()));
        }
    }

    public void convertSymbolicFIB(BDDFactory factory)
    {
        this._forwardingEngine.values().forEach(
                forwardingTopology -> {
                    forwardingTopology.convertSymbolicFIB(factory);
                }
        );
    }





    public PDG getPDG()
    {
        return this._pdg;
    }






    public void incrementVerification(BDDFactory prefixFactory, Set<RouterVrfPair> inrementVrf, List<Property> newProperty)
    {
        this._incrementVrf = new HashSet<>(inrementVrf);
        long startTime5 = System.currentTimeMillis();
        this._pdg.incrementVrf(inrementVrf);
        long endTime5 = System.currentTimeMillis();
        System.out.println("Incremental-verification-time:" + (endTime5 - startTime5));
        this.propertyVerifier(prefixFactory, newProperty, true);
    }
}
