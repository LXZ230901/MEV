package org.batfish.MEVNEW.EnsembleVerification.ForwardingTopology;



import org.batfish.MEVNEW.EnsembleModel.Dependency;
import org.batfish.MEVNEW.EnsembleModel.Graph.Graph;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.AddCustomer;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.UpdateRouterVrfPrefixPair;
import org.batfish.MEVNEW.EnsembleModel.Message.Message;
import org.batfish.MEVNEW.EnsembleModel.Message.Nexthop;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopIp;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopRouter;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopTunnel;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopType;
import org.batfish.MEVNEW.EnsembleModel.Node.Node;
import org.batfish.MEVNEW.EnsembleModel.Node.VRFRib;
import org.batfish.MEVNEW.EnsembleVerification.FIB.DependencyFIB;
import org.batfish.MEVNEW.EnsembleVerification.FIB.DependencyFIBEntry;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthop;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthopError;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthopLocal;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthopRouter;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthopType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForwardingTopologyConstructorEngine {
    private ForwardingTopology _forwardingTopology;
    public ForwardingTopologyConstructorEngine() {}
    public ForwardingTopology getForwardingTopology()
    {
        return this._forwardingTopology;
    }
    public void constructForwardingTopology(Map<String, Configuration> configurations, List<Graph> ensembleModel, Map<String, Map<String, List<Message>>> connectMessage, Map<String, Map<String, List<Message>>> staticMessage)
    {
        _forwardingTopology = new ForwardingTopology();
        _forwardingTopology.initForwardingTopology(configurations);
        Map<String, Map<String, VRFRib>> routerVrfUnifiedRib = computeRouterVrfUnifiedRib(ensembleModel, connectMessage, staticMessage);
        for (String router : routerVrfUnifiedRib.keySet())
        {
            for (String vrf : routerVrfUnifiedRib.get(router).keySet())
            {
                if (!_forwardingTopology.getVrfToVniList().containsKey(router) || !_forwardingTopology.getVrfToVniList().get(router).containsKey(vrf))
                {
                    System.out.println("FIB Error on Router " + router + " and VRF " + vrf);
                    return;
                }

                Integer vni = _forwardingTopology.getVrfToVniList().get(router).get(vrf);
                ForwardingNode forwardingNode = _forwardingTopology.getForwardingTopology().get(router);
                synchronized (forwardingNode) {
                    forwardingNode.getDependencyFIB().computeIfAbsent(vni, k -> new DependencyFIB());
                    forwardingNode.putVrfAndVni(vrf, vni);
                }
                DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB().get(vni);

                MessageRadixTrie messageRadixTrie = new MessageRadixTrie();
                routerVrfUnifiedRib.get(router).get(vrf).getRib().forEach((prefix, messageList)->{
                    messageRadixTrie.addNodes(PrefixParser.convertToBinaryIP(prefix.toString()), PrefixParser.extractPrefixLength(prefix.toString()), messageList);
                });




























                for (Prefix messagePrefix : routerVrfUnifiedRib.get(router).get(vrf).getRib().keySet())
                {
                    String prefixBinaryString = PrefixParser.convertToBinaryIP(messagePrefix.toString());
                    Integer prefixLength = PrefixParser.extractPrefixLength(messagePrefix.toString());

                    List<Message> messageList = routerVrfUnifiedRib.get(router).get(vrf).getRib().get(messagePrefix);
                    for (Message message : messageList)
                    {
                        List<Dependency> FDep = message.getRoutingDependency();
                        ForwardingNexthop forwardingNexthop = null;
                        Nexthop nexthop = message.getNexthop();
                        Integer matchVni = 0;
                        if (nexthop.getNexthopType().equals(NexthopType.Original))
                        {
                            NexthopIp originalNexthop = (NexthopIp) nexthop;
                            Ip nexthopIp = originalNexthop.getNexthopIp();
                            matchVni = originalNexthop.getVni();
                            while (nexthopIp!=null)
                            {
                                List<Message> matchMessageList = messageRadixTrie.getMatchMessage(IpParser.convertToBinaryIP(nexthopIp.toString()));
                                if (!matchMessageList.isEmpty())
                                {
                                    Message matchMessage = matchMessageList.get(0);
                                    Nexthop matchNexthop = matchMessage.getNexthop();
                                    // 这里其实应该考虑多个Message的，但是目前只考虑了一个，后面这一部分需要修改
                                    if (matchNexthop.getNexthopType().equals(NexthopType.Original))
                                    {
                                        NexthopIp specificMatchNexthop = (NexthopIp) matchNexthop;
                                        FDep.addAll(matchMessage.getRoutingDependency());
                                        nexthopIp = specificMatchNexthop.getNexthopIp();
                                    } else if (matchNexthop.getNexthopType().equals(NexthopType.Router)){
                                        NexthopRouter specificMatchNexthop = (NexthopRouter) matchNexthop;
                                        FDep.addAll(matchMessage.getRoutingDependency());
                                        forwardingNexthop = new ForwardingNexthopRouter(ForwardingNexthopType.ROUTER, specificMatchNexthop.getNexthopRouter());
                                        break;
                                    } else if (matchNexthop.getNexthopType().equals(NexthopType.Local))
                                    {
                                        forwardingNexthop = new ForwardingNexthopLocal(ForwardingNexthopType.LOCAL);
                                    }
                                } else {
                                    forwardingNexthop = new ForwardingNexthopError(ForwardingNexthopType.ERROR);
                                    break;
                                    // unimplemented
                                }
                            }
                        } else if (nexthop.getNexthopType().equals(NexthopType.Router)){
                            NexthopRouter nexthopRouter = (NexthopRouter) nexthop;
                            matchVni = nexthopRouter.getVni();
                            forwardingNexthop = new ForwardingNexthopRouter(ForwardingNexthopType.ROUTER, nexthopRouter.getNexthopRouter());
                        } else if (nexthop.getNexthopType().equals(NexthopType.Encap)){
                            NexthopTunnel originalNexthop = (NexthopTunnel) nexthop;
                            String nextHopRouter = originalNexthop.getEncapRouter();
                            forwardingNexthop = new ForwardingNexthopRouter(ForwardingNexthopType.ROUTER, nextHopRouter);
                            Dependency forwardingDependency = new Dependency(router, "default", nextHopRouter, "default", originalNexthop.getEncapIp());
                            FDep.add(forwardingDependency);
                            matchVni = originalNexthop.getVni();
                        } else if (nexthop.getNexthopType().equals(NexthopType.Local)){
                            forwardingNexthop = new ForwardingNexthopLocal(ForwardingNexthopType.LOCAL);
                        } else {
                            forwardingNexthop = new ForwardingNexthopError(ForwardingNexthopType.ERROR);
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
            }
        }
    }

    public Map<String, Map<String, VRFRib>> computeRouterVrfUnifiedRib(List<Graph> ensembleModel, Map<String, Map<String, List<Message>>> connectMessage, Map<String, Map<String, List<Message>>> staticMessage)
    {
        Map<String, Map<String, VRFRib>> routerVrfUnifiedRib = new HashMap<>();
        for (String router : connectMessage.keySet())
        {
            for (String vrf : connectMessage.get(router).keySet())
            {
                if (!routerVrfUnifiedRib.containsKey(router))
                {
                    routerVrfUnifiedRib.put(router, new HashMap<>());
                }
                if (!routerVrfUnifiedRib.get(router).containsKey(vrf))
                {
                    routerVrfUnifiedRib.get(router).put(vrf, new VRFRib());
                }
                routerVrfUnifiedRib.get(router).get(vrf).mergeMessageList(connectMessage.get(router).get(vrf));
            }
        }
        for (String router : staticMessage.keySet())
        {
            for (String vrf : staticMessage.get(router).keySet())
            {
                if (!routerVrfUnifiedRib.containsKey(router))
                {
                    routerVrfUnifiedRib.put(router, new HashMap<>());
                }
                if (!routerVrfUnifiedRib.get(router).containsKey(vrf))
                {
                    routerVrfUnifiedRib.get(router).put(vrf, new VRFRib());
                }
                routerVrfUnifiedRib.get(router).get(vrf).mergeMessageList(staticMessage.get(router).get(vrf));
            }
        }
        for (Graph graph : ensembleModel)
        {
            for (Node node : graph.getNodes())
            {
                if (!routerVrfUnifiedRib.containsKey(node.getRouter()))
                {
                    routerVrfUnifiedRib.put(node.getRouter(), new HashMap<>());
                }
                if (!routerVrfUnifiedRib.get(node.getRouter()).containsKey(node.getVrf()))
                {
                    routerVrfUnifiedRib.get(node.getRouter()).put(node.getVrf(), new VRFRib());
                }
                routerVrfUnifiedRib.get(node.getRouter()).get(node.getVrf()).mergeMessageList(node.getVrfrib().getMessageList());
            }
        }
        return routerVrfUnifiedRib;
    }

    public void updateForwardingTopology(List<AddCustomer> addCustomerList, Graph model, Map<String, Map<String, UpdateRouterVrfPrefixPair>> updateRouterVrfPrefixPair)
    {
        _forwardingTopology.updateVrfToVniList(addCustomerList);
        for (String router : updateRouterVrfPrefixPair.keySet())
        {
            for (String vrf : updateRouterVrfPrefixPair.get(router).keySet())
            {
                List<Node> updateModelNode = model.getNodes().stream()
                        .filter(n -> n.getRouter().equals(router))
                        .filter(n -> n.getVrf().equals(vrf))
                        .collect(Collectors.toList());

                VRFRib unifiedVrfRib = new VRFRib();
                for (Node node : updateModelNode)
                {
                    unifiedVrfRib.mergeMessageList(node.getVrfrib().getMessageList());
                }

                if (!_forwardingTopology.getVrfToVniList().containsKey(router) || !_forwardingTopology.getVrfToVniList().get(router).containsKey(vrf))
                {
                    System.out.println("FIB Error on Router " + router + " and VRF " + vrf);
                    return;
                }

                Integer vni = _forwardingTopology.getVrfToVniList().get(router).get(vrf);
                ForwardingNode forwardingNode = _forwardingTopology.getForwardingTopology().get(router);
                synchronized (forwardingNode) {
                    forwardingNode.getDependencyFIB().computeIfAbsent(vni, k -> new DependencyFIB());
                    forwardingNode.putVrfAndVni(vrf, vni);
                }
                DependencyFIB dependencyFIB = forwardingNode.getDependencyFIB().get(vni);

                MessageRadixTrie messageRadixTrie = new MessageRadixTrie();
                unifiedVrfRib.getRib().forEach((prefix, messageList)->{
                    messageRadixTrie.addNodes(PrefixParser.convertToBinaryIP(prefix.toString()), PrefixParser.extractPrefixLength(prefix.toString()), messageList);
                });
                for (Prefix messagePrefix : unifiedVrfRib.getRib().keySet())
                {
                    String prefixBinaryString = PrefixParser.convertToBinaryIP(messagePrefix.toString());
                    Integer prefixLength = PrefixParser.extractPrefixLength(messagePrefix.toString());

                    List<Message> messageList = unifiedVrfRib.getRib().get(messagePrefix);
                    for (Message message : messageList)
                    {
                        List<Dependency> FDep = message.getRoutingDependency();
                        ForwardingNexthop forwardingNexthop = null;
                        Nexthop nexthop = message.getNexthop();
                        Integer matchVni = 0;
                        if (nexthop.getNexthopType().equals(NexthopType.Original))
                        {
                            NexthopIp originalNexthop = (NexthopIp) nexthop;
                            Ip nexthopIp = originalNexthop.getNexthopIp();
                            while (nexthopIp!=null)
                            {
                                List<Message> matchMessageList = messageRadixTrie.getMatchMessage(IpParser.convertToBinaryIP(nexthopIp.toString()));
                                if (!matchMessageList.isEmpty())
                                {
                                    Message matchMessage = matchMessageList.get(0);
                                    Nexthop matchNexthop = matchMessage.getNexthop();
                                    // 这里其实应该考虑多个Message的，但是目前只考虑了一个，后面这一部分需要修改
                                    if (matchNexthop.getNexthopType().equals(NexthopType.Original))
                                    {
                                        NexthopIp specificMatchNexthop = (NexthopIp) matchNexthop;
                                        FDep.addAll(matchMessage.getRoutingDependency());
                                        nexthopIp = specificMatchNexthop.getNexthopIp();
                                    } else if (matchNexthop.getNexthopType().equals(NexthopType.Router)){
                                        NexthopRouter specificMatchNexthop = (NexthopRouter) matchNexthop;
                                        FDep.addAll(matchMessage.getRoutingDependency());
                                        forwardingNexthop = new ForwardingNexthopRouter(ForwardingNexthopType.ROUTER, specificMatchNexthop.getNexthopRouter());
                                        break;
                                    } else if (matchNexthop.getNexthopType().equals(NexthopType.Local))
                                    {
                                        forwardingNexthop = new ForwardingNexthopLocal(ForwardingNexthopType.LOCAL);
                                    }
                                } else {
                                    forwardingNexthop = new ForwardingNexthopError(ForwardingNexthopType.ERROR);
                                    break;
                                    // unimplemented
                                }
                            }
                        } else if (nexthop.getNexthopType().equals(NexthopType.Router)){
                            NexthopRouter nexthopRouter = (NexthopRouter) nexthop;
                            forwardingNexthop = new ForwardingNexthopRouter(ForwardingNexthopType.ROUTER, nexthopRouter.getNexthopRouter());
                        } else if (nexthop.getNexthopType().equals(NexthopType.Encap)){
                            NexthopTunnel originalNexthop = (NexthopTunnel) nexthop;
                            String nextHopRouter = originalNexthop.getEncapRouter();
                            forwardingNexthop = new ForwardingNexthopRouter(ForwardingNexthopType.ROUTER, nextHopRouter);
                            Dependency forwardingDependency = new Dependency(router, "default", nextHopRouter, "default", originalNexthop.getEncapIp());
                            FDep.add(forwardingDependency);
                            matchVni = originalNexthop.getVni();
                        } else if (nexthop.getNexthopType().equals(NexthopType.Local)){
                            forwardingNexthop = new ForwardingNexthopLocal(ForwardingNexthopType.LOCAL);
                        } else {
                            forwardingNexthop = new ForwardingNexthopError(ForwardingNexthopType.ERROR);
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
            }
        }
    }
}