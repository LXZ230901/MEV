package org.batfish.MEV;

import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForwardingTopology {

    public HashMap<String, ForwardingNode> _topology;

    public HashMap<String, ForwardingNode> getForwardingTopology()
    {
        return this._topology;
    }

    public Map<String, Map<String, Integer>> _vrfToVniList;

//    因为大多数的属性，其实都是单个的IP转发验证操作，所以可以设置两套转发系统，如果是具体的IP的话就用第二套具体的转发系统；而如果需要验证的属性，是一个前缀的话，那么就用第一套符号化的转发系统

    public ForwardingTopology(Map<String, Configuration> configurations)
    {
        this._topology = new HashMap<>();
        this._vrfToVniList = new HashMap<>();
        for (String node : configurations.keySet())
        {
            ForwardingNode forwardingNode = new ForwardingNode(node);
            for (String vrf : configurations.get(node).getVrfs().keySet())
            {
                forwardingNode.getVrf().add(vrf);
            }
            this._topology.put(node, forwardingNode);
        }
    }














    public void generateDependencyFib(List<VRF> vrfNodeList)
    {



        vrfNodeList
//                .parallelStream()
                .forEach(node -> {
                    String routerName = node.getRouter();
                    String vrfName = node.getName();
                    if (!this._vrfToVniList.containsKey(routerName) || !this._vrfToVniList.get(routerName).containsKey(vrfName))
                    {
                        return;
                    }
                    Integer vni = this._vrfToVniList.get(routerName).get(vrfName);
                    ForwardingNode forwardingNode = _topology.get(routerName);
                    synchronized (forwardingNode) {
                        forwardingNode.getDependencyFIB().computeIfAbsent(vni, k -> new DependencyFIB());
                        forwardingNode.putVrfAndVni(vrfName, vni);
                    }
                    DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB().get(vni);
                    MessageRadixTrie messageRadixTrie = new MessageRadixTrie();
                    node.getRib().forEach((prefix, messageList)->{
                        messageRadixTrie.addNodes(PrefixParser.convertToBinaryIP(prefix.toString()), PrefixParser.extractPrefixLength(prefix.toString()), messageList);
                    });
                    for (Prefix messagePrefix : node.getRib().keySet())
                    {
                        String prefixBinaryString = PrefixParser.convertToBinaryIP(messagePrefix.toString());
                        Integer prefixLength = PrefixParser.extractPrefixLength(messagePrefix.toString());
//                if (!dependencyFIB.getFib().containsKey(messagePrefix))
//                {
//                    dependencyFIB.getFib().put(messagePrefix, new ArrayList<>());
//                }
//                List<DependencyFIBEntry> dependencyFIBEntryList = dependencyFIB.getFib().get(messagePrefix);

                        List<Message> messageList = node.getRib().get(messagePrefix);
                        for (Message message : messageList)
                        {
                            List<Dependency> FDep = message.getForwardingDependency();
                            ForwardingNexthop forwardingNexthop = new ForwardingNexthop();
                            Nexthop nexthop = message.getNexthop();
                            if (message.getRedistributionTag() != null)
                            {
                                String nexthopRouter = message.getRedistributionTag().getRouter();
                                String nexthopVrf = message.getRedistributionTag().getVrf();
                                String layer = message.getRedistributionTag().getProtocolLayer();
                                forwardingNexthop.setNexthopType(ForwardingNexthopType.PROTOCOLLAYER);
                                forwardingNexthop.setNexthopRouter(nexthopRouter);
                                forwardingNexthop.setNexthopLayer(layer);
                            }
                            if (nexthop.getNexthopType().equals(NexthopType.Original))
                            {
                                Ip nexthopIp = nexthop.getNexthopIp();
                                while (nexthopIp!=null)
                                {
                                    List<Message> matchMessageList = messageRadixTrie.getMatchMessage(IpParser.convertToBinaryIP(nexthopIp.toString()));
                                    if (!matchMessageList.isEmpty())
                                    {
                                        Message matchMessage = matchMessageList.get(0);
                                        // 这里其实应该考虑多个Message的，但是目前只考虑了一个，后面这一部分需要修改
                                        if (nexthop.getNexthopType().equals(NexthopType.Original))
                                        {
                                            FDep.addAll(matchMessage.getForwardingDependency());
                                            nexthopIp = matchMessage.getNexthop().getNexthopIp();
                                            forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
                                            forwardingNexthop.setNexthopRouter(matchMessage.getNexthop().getNexthopRouter());
                                        } else {
                                            // unimplemented
                                        }
                                    } else {
                                        forwardingNexthop.setNexthopType(ForwardingNexthopType.ERROR);
                                        break;
                                        // unimplemented
                                    }
                                }
                            } else if (nexthop.getNexthopType().equals(NexthopType.Local)){
                                forwardingNexthop.setNexthopType(ForwardingNexthopType.LOCAL);
                                forwardingNexthop.setNexthopRouter("");
                            } else{
                                String nextHopRouter = nexthop.getNexthopRouter();
                                forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
                                forwardingNexthop.setNexthopRouter(nextHopRouter);
                            }
                            Integer matchVni = 0;
                            if (message.getAttribute() instanceof EvpnAttribute)
                            {
                                EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
                                matchVni = attribute.getVni();
                            }
                            DependencyFIBEntry dependencyFIBEntry = new DependencyFIBEntry(messagePrefix, FDep, forwardingNexthop, matchVni);
                            if (dependencyFIB.getFib().containsKey(messagePrefix))
                            {
                                dependencyFIB.getFib().get(messagePrefix).mergeDependencyFibEntry(dependencyFIBEntry);
                            } else {
                                dependencyFIB.getFib().put(messagePrefix, dependencyFIBEntry);
                            }
                            dependencyFIB.getFIBRadixTrie().addNodes(prefixBinaryString,prefixLength,dependencyFIBEntry);
                        }
                    }
                });

//        for (VRF node : vrfNodeList)
//        {
//            String routerName = node.getRouter();
//            String vrfName = node.getName();
//            if (!this._vrfToVniList.containsKey(routerName) || !this._vrfToVniList.get(routerName).containsKey(vrfName))
//            {
//                continue;
//            }
//            Integer vni = this._vrfToVniList.get(routerName).get(vrfName);
//            ForwardingNode forwardingNode = _topology.get(routerName);
//            if (!forwardingNode.getDependencyFIB().containsKey(vni))
//            {
//                forwardingNode.getDependencyFIB().put(vni, new DependencyFIB());
//                forwardingNode.putVrfAndVni(vrfName, vni);
//            }
//            DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB().get(vni);
//            MessageRadixTrie messageRadixTrie = new MessageRadixTrie();
//            node.getRib().forEach((prefix, messageList)->{
//                messageRadixTrie.addNodes(PrefixParser.convertToBinaryIP(prefix.toString()), PrefixParser.extractPrefixLength(prefix.toString()), messageList);
//            });
//            for (Prefix messagePrefix : node.getRib().keySet())
//            {
//                String prefixBinaryString = PrefixParser.convertToBinaryIP(messagePrefix.toString());
//                Integer prefixLength = PrefixParser.extractPrefixLength(messagePrefix.toString());
////                if (!dependencyFIB.getFib().containsKey(messagePrefix))
////                {
////                    dependencyFIB.getFib().put(messagePrefix, new ArrayList<>());
////                }
////                List<DependencyFIBEntry> dependencyFIBEntryList = dependencyFIB.getFib().get(messagePrefix);
//
//                List<Message> messageList = node.getRib().get(messagePrefix);
//                for (Message message : messageList)
//                {
//                    List<Dependency> RDep = message.getRoutingDependency();
//                    List<Dependency> FDep = message.getForwardingDependency();
//                    ForwardingNexthop forwardingNexthop = new ForwardingNexthop();
//                    Nexthop nexthop = message.getNexthop();
//                    if (message.getRedistributionTag() != null)
//                    {
//                        String nexthopRouter = message.getRedistributionTag().getRouter();
//                        String nexthopVrf = message.getRedistributionTag().getVrf();
//                        String layer = message.getRedistributionTag().getProtocolLayer();
//                        forwardingNexthop.setNexthopType(ForwardingNexthopType.PROTOCOLLAYER);
//                        forwardingNexthop.setNexthopRouter(nexthopRouter);
//                        forwardingNexthop.setNexthopLayer(layer);
//                    }
//                    if (nexthop.getNexthopType().equals(NexthopType.Original))
//                    {
//                        Ip nexthopIp = nexthop.getNexthopIp();
//                        while (nexthopIp!=null)
//                        {
//                            List<Message> matchMessageList = messageRadixTrie.getMatchMessage(IpParser.convertToBinaryIP(nexthopIp.toString()));
//                            if (!matchMessageList.isEmpty())
//                            {
//                                Message matchMessage = matchMessageList.get(0);
//                                // 这里其实应该考虑多个Message的，但是目前只考虑了一个，后面这一部分需要修改
//                                if (nexthop.getNexthopType().equals(NexthopType.Original))
//                                {
//                                    RDep.addAll(matchMessage.getRoutingDependency());
//                                    FDep.addAll(matchMessage.getForwardingDependency());
//                                    nexthopIp = matchMessage.getNexthop().getNexthopIp();
//                                    forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
//                                    forwardingNexthop.setNexthopRouter(matchMessage.getNexthop().getNexthopRouter());
//                                } else {
//                                    // unimplemented
//                                }
//                            } else {
//                                forwardingNexthop.setNexthopType(ForwardingNexthopType.ERROR);
//                                break;
//                                // unimplemented
//                            }
//                        }
//                    } else if (nexthop.getNexthopType().equals(NexthopType.Local)){
//                        forwardingNexthop.setNexthopType(ForwardingNexthopType.LOCAL);
//                        forwardingNexthop.setNexthopRouter("");
//                    } else{
//                        String nextHopRouter = nexthop.getNexthopRouter();
//                        forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
//                        forwardingNexthop.setNexthopRouter(nextHopRouter);
//                    }
//                    Integer matchVni = 0;
//                    if (message.getAttribute() instanceof EvpnAttribute)
//                    {
//                        EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
//                        matchVni = attribute.getVni();
//                    }
//                    DependencyFIBEntry dependencyFIBEntry = new DependencyFIBEntry(messagePrefix, RDep, FDep, forwardingNexthop, matchVni);
//                    if (dependencyFIB.getFib().containsKey(messagePrefix))
//                    {
//                        dependencyFIB.getFib().get(messagePrefix).mergeDependencyFibEntry(dependencyFIBEntry);
//                    } else {
//                        dependencyFIB.getFib().put(messagePrefix, dependencyFIBEntry);
//                    }
//                    dependencyFIB.getFIBRadixTrie().addNodes(prefixBinaryString,prefixLength,dependencyFIBEntry);
//                }
//            }
//        }
    }

    public void generateDependencyFib(HashMap<GraphType, List<VRF>> vrfNodeSet)
    {

        HashMap<String, VRF> unidifedVRFList = new HashMap<>();
        for (GraphType graphType : vrfNodeSet.keySet())
        {
            List<VRF> vrfNodeList = vrfNodeSet.get(graphType);
            for (VRF node : vrfNodeList)
            {
                String routerName = node.getRouter();
                String vrfName = node.getName();
                String indexName = routerName + "-" + vrfName;
                if (!unidifedVRFList.containsKey(indexName))
                {
                    unidifedVRFList.put(indexName, new VRF(0, NodeType.VRF, routerName, vrfName));
                }
                VRF unifiedVRF = unidifedVRFList.get(indexName);
                unifiedVRF.mergeMessageList(node.getMessageList());
            }
        }
        unidifedVRFList
                .values()
//                .parallelStream()
                .forEach(node -> {
                    String routerName = node.getRouter();
                    String vrfName = node.getName();
                    if (!this._vrfToVniList.containsKey(routerName) || !this._vrfToVniList.get(routerName).containsKey(vrfName))
                    {
                        return;
                    }
                    Integer vni = this._vrfToVniList.get(routerName).get(vrfName);
                    ForwardingNode forwardingNode = _topology.get(routerName);
                    synchronized (forwardingNode) {
                        forwardingNode.getDependencyFIB().computeIfAbsent(vni, k -> new DependencyFIB());
                        forwardingNode.putVrfAndVni(vrfName, vni);
                    }

                    DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB().get(vni);
                    MessageRadixTrie messageRadixTrie = new MessageRadixTrie();
                    node.getRib().forEach((prefix, messageList)->{
                        messageRadixTrie.addNodes(PrefixParser.convertToBinaryIP(prefix.toString()), PrefixParser.extractPrefixLength(prefix.toString()), messageList);
                    });
                    for (Prefix messagePrefix : node.getRib().keySet())
                    {
                        String prefixBinaryString = PrefixParser.convertToBinaryIP(messagePrefix.toString());
                        Integer prefixLength = PrefixParser.extractPrefixLength(messagePrefix.toString());
//                if (!dependencyFIB.getFib().containsKey(messagePrefix))
//                {
//                    dependencyFIB.getFib().put(messagePrefix, new ArrayList<>());
//                }
//                List<DependencyFIBEntry> dependencyFIBEntryList = dependencyFIB.getFib().get(messagePrefix);

                        List<Message> messageList = node.getRib().get(messagePrefix);
                        for (Message message : messageList)
                        {
                            List<Dependency> FDep = message.getForwardingDependency();
                            ForwardingNexthop forwardingNexthop = new ForwardingNexthop();
                            Nexthop nexthop = message.getNexthop();
                            if (message.getRedistributionTag() != null)
                            {
                                String nexthopRouter = message.getRedistributionTag().getRouter();
                                String nexthopVrf = message.getRedistributionTag().getVrf();
                                String layer = message.getRedistributionTag().getProtocolLayer();
                                forwardingNexthop.setNexthopType(ForwardingNexthopType.PROTOCOLLAYER);
                                forwardingNexthop.setNexthopRouter(nexthopRouter);
                                forwardingNexthop.setNexthopLayer(layer);
                            }
                            if (nexthop.getNexthopType().equals(NexthopType.Original))
                            {
                                Ip nexthopIp = nexthop.getNexthopIp();
                                if (nexthopIp == null)
                                {
                                    forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
                                    forwardingNexthop.setNexthopRouter(nexthop.getNexthopRouter());
                                }
                                int iteration = 0;
                                while (nexthopIp!=null)
                                {
                                    iteration++;
                                    if (iteration>5)
                                    {
                                        System.out.println("isolate");
                                    }
                                    List<Message> matchMessageList = messageRadixTrie.getMatchMessage(IpParser.convertToBinaryIP(nexthopIp.toString()));
                                    if (!matchMessageList.isEmpty())
                                    {
                                        Message matchMessage = matchMessageList.get(0);
                                        // 这里其实应该考虑多个Message的，但是目前只考虑了一个，后面这一部分需要修改
                                        if (nexthop.getNexthopType().equals(NexthopType.Original))
                                        {
                                            FDep.addAll(matchMessage.getForwardingDependency());
                                            nexthopIp = matchMessage.getNexthop().getNexthopIp();
                                            forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
                                            forwardingNexthop.setNexthopRouter(matchMessage.getNexthop().getNexthopRouter());
                                        } else {
                                            // unimplemented
                                        }
                                    } else {
                                        forwardingNexthop.setNexthopType(ForwardingNexthopType.ERROR);
                                        break;
                                        // unimplemented
                                    }
                                }
                            }else if (nexthop.getNexthopType().equals(NexthopType.Local)){
                                forwardingNexthop.setNexthopType(ForwardingNexthopType.LOCAL);
                                forwardingNexthop.setNexthopRouter("");
                            } else {
                                String nextHopRouter = nexthop.getNexthopRouter();
                                forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
                                forwardingNexthop.setNexthopRouter(nextHopRouter);
                            }
                            Integer matchVni = 0;
                            if (message.getAttribute() instanceof EvpnAttribute)
                            {
                                EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
                                matchVni = attribute.getVni();
                            }
                            DependencyFIBEntry dependencyFIBEntry = new DependencyFIBEntry(messagePrefix, FDep, forwardingNexthop, matchVni);
                            if (dependencyFIB.getFib().containsKey(messagePrefix))
                            {
                                dependencyFIB.getFib().get(messagePrefix).mergeDependencyFibEntry(dependencyFIBEntry);
                            } else {
                                dependencyFIB.getFib().put(messagePrefix, dependencyFIBEntry);
                            }
                            dependencyFIB.getFIBRadixTrie().addNodes(prefixBinaryString,prefixLength,dependencyFIBEntry);
                        }
                    }
                });
//        for (VRF node : unidifedVRFList.values())
//        {
//            String routerName = node.getRouter();
//            String vrfName = node.getName();
//            if (!this._vrfToVniList.containsKey(routerName) || !this._vrfToVniList.get(routerName).containsKey(vrfName))
//            {
//                continue;
//            }
//            Integer vni = this._vrfToVniList.get(routerName).get(vrfName);
//            ForwardingNode forwardingNode = _topology.get(routerName);
//            if (!forwardingNode.getDependencyFIB().containsKey(vni))
//            {
//                forwardingNode.getDependencyFIB().put(vni, new DependencyFIB());
//                forwardingNode.putVrfAndVni(vrfName, vni);
//            }
//
//            DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB().get(vni);
//            MessageRadixTrie messageRadixTrie = new MessageRadixTrie();
//            node.getRib().forEach((prefix, messageList)->{
//                messageRadixTrie.addNodes(PrefixParser.convertToBinaryIP(prefix.toString()), PrefixParser.extractPrefixLength(prefix.toString()), messageList);
//            });
//            for (Prefix messagePrefix : node.getRib().keySet())
//            {
//                String prefixBinaryString = PrefixParser.convertToBinaryIP(messagePrefix.toString());
//                Integer prefixLength = PrefixParser.extractPrefixLength(messagePrefix.toString());
////                if (!dependencyFIB.getFib().containsKey(messagePrefix))
////                {
////                    dependencyFIB.getFib().put(messagePrefix, new ArrayList<>());
////                }
////                List<DependencyFIBEntry> dependencyFIBEntryList = dependencyFIB.getFib().get(messagePrefix);
//
//                List<Message> messageList = node.getRib().get(messagePrefix);
//                for (Message message : messageList)
//                {
//                    List<Dependency> RDep = message.getRoutingDependency();
//                    List<Dependency> FDep = message.getForwardingDependency();
//                    ForwardingNexthop forwardingNexthop = new ForwardingNexthop();
//                    Nexthop nexthop = message.getNexthop();
//                    if (message.getRedistributionTag() != null)
//                    {
//                        String nexthopRouter = message.getRedistributionTag().getRouter();
//                        String nexthopVrf = message.getRedistributionTag().getVrf();
//                        String layer = message.getRedistributionTag().getProtocolLayer();
//                        forwardingNexthop.setNexthopType(ForwardingNexthopType.PROTOCOLLAYER);
//                        forwardingNexthop.setNexthopRouter(nexthopRouter);
//                        forwardingNexthop.setNexthopLayer(layer);
//                    }
//                    if (nexthop.getNexthopType().equals(NexthopType.Original))
//                    {
//                        Ip nexthopIp = nexthop.getNexthopIp();
//                        if (nexthopIp == null)
//                        {
//                            forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
//                            forwardingNexthop.setNexthopRouter(nexthop.getNexthopRouter());
//                        }
//                        int iteration = 0;
//                        while (nexthopIp!=null)
//                        {
//                            iteration++;
//                            if (iteration>5)
//                            {
//                                System.out.println("isolate");
//                            }
//                            List<Message> matchMessageList = messageRadixTrie.getMatchMessage(IpParser.convertToBinaryIP(nexthopIp.toString()));
//                            if (!matchMessageList.isEmpty())
//                            {
//                                Message matchMessage = matchMessageList.get(0);
//                                // 这里其实应该考虑多个Message的，但是目前只考虑了一个，后面这一部分需要修改
//                                if (nexthop.getNexthopType().equals(NexthopType.Original))
//                                {
//                                    RDep.addAll(matchMessage.getRoutingDependency());
//                                    FDep.addAll(matchMessage.getForwardingDependency());
//                                    nexthopIp = matchMessage.getNexthop().getNexthopIp();
//                                    forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
//                                    forwardingNexthop.setNexthopRouter(matchMessage.getNexthop().getNexthopRouter());
//                                } else {
//                                    // unimplemented
//                                }
//                            } else {
//                                forwardingNexthop.setNexthopType(ForwardingNexthopType.ERROR);
//                                break;
//                                // unimplemented
//                            }
//                        }
//                    }else if (nexthop.getNexthopType().equals(NexthopType.Local)){
//                        forwardingNexthop.setNexthopType(ForwardingNexthopType.LOCAL);
//                        forwardingNexthop.setNexthopRouter("");
//                    } else {
//                        String nextHopRouter = nexthop.getNexthopRouter();
//                        forwardingNexthop.setNexthopType(ForwardingNexthopType.ROUTER);
//                        forwardingNexthop.setNexthopRouter(nextHopRouter);
//                    }
//                    Integer matchVni = 0;
//                    if (message.getAttribute() instanceof EvpnAttribute)
//                    {
//                        EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
//                        matchVni = attribute.getVni();
//                    }
//                    DependencyFIBEntry dependencyFIBEntry = new DependencyFIBEntry(messagePrefix, RDep, FDep, forwardingNexthop, matchVni);
//                    if (dependencyFIB.getFib().containsKey(messagePrefix))
//                    {
//                        dependencyFIB.getFib().get(messagePrefix).mergeDependencyFibEntry(dependencyFIBEntry);
//                    } else {
//                        dependencyFIB.getFib().put(messagePrefix, dependencyFIBEntry);
//                    }
//                    dependencyFIB.getFIBRadixTrie().addNodes(prefixBinaryString,prefixLength,dependencyFIBEntry);
//                }
//            }
//        }
    }

    public void convertSymbolicFIB(BDDFactory factory)
    {
        _topology.values().forEach(
                forwardingNode -> {
                    forwardingNode.convertSymbolicFIB(factory);
                }
        );
    }
    public void setVrfToVniList(Map<String, Map<String, Integer>> vrfToVniList)
    {
        this._vrfToVniList.putAll(vrfToVniList);
    }




}
