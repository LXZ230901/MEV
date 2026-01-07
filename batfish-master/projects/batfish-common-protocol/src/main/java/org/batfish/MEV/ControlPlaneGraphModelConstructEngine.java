package org.batfish.MEV;

import com.google.common.graph.Network;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4ToEvpnVrfLeakConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EvpnToBgpv4VrfLeakConfig;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Nve;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.vxlan.Layer3Vni;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public class ControlPlaneGraphModelConstructEngine {
//    public static List<Graph> _evpnL3vpnGraph = new ArrayList<>();
//    public static List<Graph> _bgpGraph = new ArrayList<>();
//    public static List<Graph> _vxlanL2vpnGraph = new ArrayList<>();

    public Graph _evpnL3vpnGraph = new Graph(GraphType.EVPNL3VPN);
    public Graph _bgpGraph = new Graph(GraphType.BGP);
    public Graph _vxlanL2vpnGraph = new Graph(GraphType.VXLANL2VPN);
    public Graph _connectGraph = new Graph(GraphType.CONNECT);
    public Graph _isisGraph = new Graph(GraphType.ISIS);








    public ControlPlaneGraphModelConstructEngine()
    {
        _evpnL3vpnGraph = new Graph(GraphType.EVPNL3VPN);
        _bgpGraph = new Graph(GraphType.BGP);
        _vxlanL2vpnGraph = new Graph(GraphType.VXLANL2VPN);
        _connectGraph = new Graph(GraphType.CONNECT);
        _isisGraph = new Graph(GraphType.ISIS);
    }
    public void controlPlaneConstructor(Map<String, Configuration> configurations, IsisTopology isisTopology, Topology L3Topology)
    {
        Graph evpnL3vpnGraph = new Graph(GraphType.EVPNL3VPN);
        Graph bgpGraph = new Graph(GraphType.BGP);
        Graph vxlanL2vpnGraph = new Graph(GraphType.VXLANL2VPN);
        Graph isisGraph = new Graph(GraphType.ISIS);
        Graph connectGraph = new Graph(GraphType.CONNECT);
        int evpnL3vpnNodeId = 0;
        int bgpNodeId = 0;
        int vxlanL2vpnNodeId = 0;
        int isisNodeId = 0;
        int connectNodeId = 0;
        HashMap<Ip, NVE> nveL3vpnNodes = new HashMap<>();
        HashMap<Long, BGP> bgpL3vpnNodes = new HashMap<>();
        HashMap<Ip, NVE> nveL2vpnNodes = new HashMap<>();
        HashMap<Long, BGP> bgpL2vpnNodes = new HashMap<>();
        HashMap<Long, BGP> bgpNodes = new HashMap<>();
        HashMap<Ip, List<NeighborTriple>> unConstructedL3vpnBgpEdges = new HashMap<>();
        HashMap<Ip, List<NeighborTriple>> unConstructedL2vpnBgpEdges = new HashMap<>();
        HashMap<Ip, List<NeighborTriple>> unConstructedBgpEdges = new HashMap<>();
        HashMap<IsisNode, HashMap<IsisNode, NeighborTriple>> unConstructedIsisEdges = new HashMap<>();
        HashMap<Ip, HashMap<Ip, NeighborTriple>> unConstructedNveEdges = new HashMap<>();
        Map<String, org.batfish.datamodel.Edge> interfaceToEdgeList = L3Topology.computeInterfaceEdges();


        Network<IsisNode, IsisEdge> isisNetwork = isisTopology.getNetwork();
        //Construct Graph
        for (String routerName : configurations.keySet())
        {
            Configuration routerConfig = configurations.get(routerName);
            List<Ip> hostIpList = new ArrayList<>();
            // 构建直连路由
            HashMap<String, List<Message>> connectMessageList = new HashMap<>();
            for (Interface inter : routerConfig.getAllInterfaces().values())
            {
                String vrfName = inter.getVrfName();
                if (!connectMessageList.containsKey(vrfName))
                {
                    connectMessageList.put(vrfName, new ArrayList<>());
                }

                if (inter.getConcreteAddress() != null)
                {
                    hostIpList.add(inter.getConcreteAddress().getIp());
                    String nexthopRouter = null;
                    if (interfaceToEdgeList.containsKey(routerName + "-" + inter.getName()))
                    {
                        nexthopRouter = interfaceToEdgeList.get(routerName + "-" + inter.getName()).getNode2();
                    }
//                    String nexthopRouter = interfaceToEdgeList.get(routerName + "-" + inter.getName()).getNode2();
                    Prefix prefix = inter.getConcreteAddress().getPrefix();
                    Nexthop nexthop = new Nexthop(NexthopType.Original, nexthopRouter, null);
                    Message message = new Message(prefix, new ConnectAttribute(0), new ArrayList<>(), new ArrayList<>(), null, nexthop, MessageType.Connected, new ArrayList<>(), Reason.ADD, false);
                    message.setReason(Reason.ADD);
                    connectMessageList.get(vrfName).add(message);
                }
            }

            String hostName = routerConfig.getHostname();
            HashMap<String, BGP> evpnL3vpnBgpNodes = new HashMap<>();
            HashMap<String, BGP> vxlanL2vpnBgpNodes = new HashMap<>();
            HashMap<String, BGP> bgpConstructedNodes = new HashMap<>();
            HashMap<Integer, VLAN> vlanConstructedNodes = new HashMap<>();
            HashMap<Nve, NVE> nveL2vpnConstructedNodes = new HashMap<>();
            for(Vrf vrf : routerConfig.getVrfs().values())
            {
                String vrfName = vrf.getName();
                if (vrfName.equals("management"))
                {
                    continue;
                }
                BgpProcess bgpProcess = vrf.getBgpProcess();
                if (bgpProcess.getLocalAs() == null)
                {
                    continue;
                }
                long ASNumber = bgpProcess.getLocalAs();
                BGP bgpNode = new BGP(bgpNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
                BGP bgpL3vpnNode = new BGP(evpnL3vpnNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
                BGP bgpL2vpnNode = new BGP(vxlanL2vpnNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
                bgpGraph.addNode(bgpNode);
                evpnL3vpnGraph.addNode(bgpL3vpnNode);
                vxlanL2vpnGraph.addNode(bgpL2vpnNode);
                bgpNodes.put(ASNumber, bgpNode);
                bgpL3vpnNodes.put(ASNumber, bgpL3vpnNode);
                bgpL2vpnNodes.put(ASNumber, bgpL2vpnNode);
//                if (!unConstructedBgpEdges.containsKey(ASNumber))
//                {
//                    unConstructedBgpEdges.put(ASNumber, new HashMap<>());
//                }
//                if (!unConstructedL3vpnBgpEdges.containsKey(ASNumber))
//                {
//                    unConstructedL3vpnBgpEdges.put(ASNumber, new HashMap<>());
//                }
//                if (!unConstructedL2vpnBgpEdges.containsKey(ASNumber))
//                {
//                    unConstructedL2vpnBgpEdges.put(ASNumber, new HashMap<>());
//                }

                bgpConstructedNodes.put(vrfName, bgpNode);

                evpnL3vpnBgpNodes.put(vrfName, bgpL3vpnNode);
                vxlanL2vpnBgpNodes.put(vrfName, bgpL2vpnNode);

                bgpProcess.getActiveNeighbors().forEach(
                        (neighborIP, neighborBgpConfig) -> {
                            // 构建EVPN模型中的边
                            if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN) != null)
                            {
                                long reflectorId = neighborBgpConfig.getClusterId();
                                bgpL3vpnNode.setReflectorId(reflectorId);
                                long localAs = neighborBgpConfig.getLocalAs();
                                boolean reflector = neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getRouteReflectorClient();
                                long neighborAs = neighborBgpConfig.getRemoteAsns().least();












                                boolean hasConstructed = false;
                                for (Ip hostIp : hostIpList)
                                {
                                    if (unConstructedL3vpnBgpEdges.containsKey(hostIp))
                                    {
                                        for (NeighborTriple neighborTriple : unConstructedL3vpnBgpEdges.get(hostIp))
                                        {
                                            if (neighborTriple.getLocalIp().contains(neighborIP))
                                            {
                                                Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
                                                Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getRouter(), neighborTriple.getVrf(), neighborIP);
                                                List<Dependency> deps = new ArrayList<>();
                                                deps.add(neighborToLocalDep);
                                                deps.add(localToNeighborDep);

                                                BGP neighborNode = neighborTriple.getBgpNode();
                                                org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborNode, bgpL3vpnNode, deps, Long.valueOf(0));
                                                org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(bgpL3vpnNode, neighborNode, deps, Long.valueOf(0));






                                                if (neighborTriple.getReflector())
                                                {
                                                    neighborNode.addReflectorNeighbor(bgpL3vpnNode);
                                                }

                                                if (reflector)
                                                {
                                                    bgpL3vpnNode.addReflectorNeighbor(neighborNode);
                                                }

                                                // 需要在Operation里面设置下一跳，并且设置不将iBGP路由传播给iBGP邻居
                                                EdgeOperation outOperation = new EdgeOperation();
                                                EdgeOperation inOperation = new EdgeOperation();

                                                RoutingPolicy outRoutingPolicy = null;
                                                RoutingPolicy inRoutingPolicy = null;

                                                if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
                                                {
                                                    outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
                                                    outOperation.setPolicies(outRoutingPolicy);
                                                }

                                                if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
                                                {
                                                    inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
                                                    inOperation.setPolicies(inRoutingPolicy);
                                                }

//                                            outOperation.setNexthopIp(neighborTriple.getNeighborIp());
//                                            neighborTriple.getOutOperation().setNexthopIp(neighborIP);

                                                if (localAs == neighborAs)
                                                {
                                                    outOperation.addBlockMessageType(MessageType.iBGP);
                                                    inOperation.reSetIBGP();
                                                    inOperation.setReflector(reflector);
                                                } else
                                                {
                                                    inOperation.reSetEBGP();
                                                }

                                                neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
                                                neighborToLocalEdge.setInOperation(inOperation);
                                                localToNeighborEdge.setOutOperation(outOperation);
                                                localToNeighborEdge.setInOperation(neighborTriple.getInOperation());

                                                evpnL3vpnGraph.addEdge(neighborToLocalEdge);
                                                evpnL3vpnGraph.addEdge(localToNeighborEdge);
                                                hasConstructed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (hasConstructed)
                                    {
                                        break;
                                    }
                                }
                                if (!hasConstructed)
                                {
                                    NeighborTriple neighborTriple = new NeighborTriple(hostName, vrfName, neighborIP, bgpL3vpnNode);
                                    neighborTriple.setLocalIp(hostIpList);
                                    neighborTriple.setReflector(reflector);
                                    EdgeOperation outOperation = new EdgeOperation();
                                    EdgeOperation inOperation = new EdgeOperation();
                                    RoutingPolicy outRoutingPolicy = null;
                                    RoutingPolicy inRoutingPolicy = null;
                                    if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
                                    {
                                        outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
                                        outOperation.setPolicies(outRoutingPolicy);
                                    }
                                    if(!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
                                    {
                                        inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
                                        inOperation.setPolicies(inRoutingPolicy);
                                    }
                                    if (localAs == neighborAs)
                                    {
                                        outOperation.addBlockMessageType(MessageType.iBGP);
                                        inOperation.reSetIBGP();
                                        inOperation.setReflector(reflector);
                                    } else {
                                        inOperation.reSetEBGP();
                                    }
                                    neighborTriple.setOutOperation(outOperation);
                                    neighborTriple.setInOperation(inOperation);
                                    if (!unConstructedL3vpnBgpEdges.containsKey(neighborIP))
                                    {
                                        unConstructedL3vpnBgpEdges.put(neighborIP, new ArrayList<>());
                                    }
                                    unConstructedL3vpnBgpEdges.get(neighborIP).add(neighborTriple);

//                                    NeighborTriple neighborTripleL2 = new NeighborTriple(hostName, vrfName, neighborIP, bgpL2vpnNode);
//                                    EdgeOperation outOperationL2 = new EdgeOperation();
//                                    EdgeOperation inOperationL2 = new EdgeOperation();
//                                    RoutingPolicy outRoutingPolicyL2 = null;
//                                    RoutingPolicy inRoutingPolicyL2 = null;
//                                    if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                                    {
//                                        outRoutingPolicyL2 = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                                        outOperationL2.setPolicies(outRoutingPolicyL2);
//                                    }
//                                    if(!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                                    {
//                                        inRoutingPolicyL2 = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                                        inOperationL2.setPolicies(inRoutingPolicyL2);
//                                    }
//
//                                    neighborTripleL2.setOutOperation(outOperationL2);
//                                    neighborTripleL2.setInOperation(inOperationL2);
//                                    if (!unConstructedL2vpnBgpEdges.get(localAs).containsKey(neighborAs))
//                                    {
//                                        unConstructedL2vpnBgpEdges.get(localAs).put(neighborAs, new ArrayList<>());
//                                    }
//                                    unConstructedL2vpnBgpEdges.get(localAs).get(neighborAs).add(neighborTripleL2);
                                }
                            }
                            if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST) != null)
                            {
                                Long reflectorId = neighborBgpConfig.getClusterId();
                                bgpNode.setReflectorId(reflectorId);
                                long localAs = neighborBgpConfig.getLocalAs();
                                long neighborAs = neighborBgpConfig.getRemoteAsns().least();
                                boolean reflector = neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getRouteReflectorClient();

                                boolean hasConstructed = false;
                                for (Ip hostIp : hostIpList)
                                {
                                    if (unConstructedBgpEdges.containsKey(hostIp))
                                    {
                                        for (NeighborTriple neighborTriple : unConstructedBgpEdges.get(hostIp))
                                        {
                                            if (neighborTriple.getLocalIp().contains(neighborIP))
                                            {
                                                Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
                                                Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getRouter(), neighborTriple.getVrf(), neighborIP);
                                                List<Dependency> deps = new ArrayList<>();
                                                deps.add(neighborToLocalDep);
                                                deps.add(localToNeighborDep);

                                                BGP neighborNode = neighborTriple.getBgpNode();
                                                org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborNode, bgpNode, deps, Long.valueOf(0));
                                                org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(bgpNode, neighborNode, deps, Long.valueOf(0));

                                                EdgeOperation outOperation = new EdgeOperation();
                                                EdgeOperation inOperation = new EdgeOperation();

                                                RoutingPolicy outRoutingPolicy = null;
                                                RoutingPolicy inRoutingPolicy = null;

                                                if (reflector)
                                                {
                                                    bgpNode.addReflectorNeighbor(neighborNode);
                                                }




                                                if (neighborTriple.getReflector())
                                                {
                                                    neighborNode.addReflectorNeighbor(bgpNode);
                                                }

                                                if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicySources().isEmpty()) {
                                                    outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicy());
                                                    outOperation.setPolicies(outRoutingPolicy);
                                                }

                                                if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicySources().isEmpty()) {
                                                    inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicy());
                                                    inOperation.setPolicies(inRoutingPolicy);
                                                }

                                                outOperation.setNexthopIp(neighborTriple.getNeighborIp());
                                                neighborTriple.getOutOperation().setNexthopIp(neighborIP);

                                                if (localAs == neighborAs)
                                                {
                                                    outOperation.addBlockMessageType(MessageType.iBGP);
                                                    inOperation.reSetIBGP();
                                                    inOperation.setReflector(reflector);
                                                } else {
                                                    inOperation.reSetEBGP();
                                                }

                                                neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
                                                neighborToLocalEdge.setInOperation(inOperation);
                                                localToNeighborEdge.setOutOperation(outOperation);
                                                localToNeighborEdge.setInOperation(neighborTriple.getInOperation());

                                                bgpGraph.addEdge(neighborToLocalEdge);
                                                bgpGraph.addEdge(localToNeighborEdge);
                                                hasConstructed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (hasConstructed)
                                    {
                                        break;
                                    }
                                }
                                if (!hasConstructed)
                                {
                                    NeighborTriple neighborTriple = new NeighborTriple(hostName, vrfName, neighborIP, bgpNode);
                                    neighborTriple.setLocalIp(hostIpList);
                                    neighborTriple.setReflector(reflector);
                                    EdgeOperation outOperation = new EdgeOperation();
                                    EdgeOperation inOperation = new EdgeOperation();
                                    RoutingPolicy outRoutingPolicy = null;
                                    RoutingPolicy inRoutingPolicy = null;
                                    if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicySources().isEmpty()) {
                                        outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicy());
                                        outOperation.setPolicies(outRoutingPolicy);
                                    }
                                    if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicySources().isEmpty()) {
                                        inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicy());
                                        inOperation.setPolicies(inRoutingPolicy);
                                    }
                                    if (localAs == neighborAs)
                                    {
                                        outOperation.addBlockMessageType(MessageType.iBGP);
                                        inOperation.reSetIBGP();
                                        inOperation.setReflector(reflector);
                                    } else {
                                        inOperation.reSetEBGP();
                                    }
                                    neighborTriple.setOutOperation(outOperation);
                                    neighborTriple.setInOperation(inOperation);

                                    if (!unConstructedBgpEdges.containsKey(neighborIP))
                                    {
                                        unConstructedBgpEdges.put(neighborIP, new ArrayList<>());
                                    }
                                    unConstructedBgpEdges.get(neighborIP).add(neighborTriple);
                                }
                            }
                        }
                );
            }

            for (Nve nve : routerConfig.getNves().values())
            {
                NVE evpnL3vpnNveNode = new NVE(evpnL3vpnNodeId++, NodeType.NVE, hostName, nve.toString());
                NVE vxlanL2vpnNveNode = new NVE(vxlanL2vpnNodeId++, NodeType.NVE, hostName, nve.toString());
                Ip nveIP = getInterfaceIp(routerConfig.getAllInterfaces(),nve.getSourceInterface());
                evpnL3vpnNveNode.setEncapType(EncapType.VXLAN);
                vxlanL2vpnNveNode.setEncapType(EncapType.VXLAN);
                evpnL3vpnNveNode.setEncapIP(nveIP);
                vxlanL2vpnNveNode.setEncapIP(nveIP);
                nveL3vpnNodes.put(nveIP, evpnL3vpnNveNode);
                nveL2vpnNodes.put(nveIP, vxlanL2vpnNveNode);
                evpnL3vpnGraph.addNode(evpnL3vpnNveNode);
                vxlanL2vpnGraph.addNode(vxlanL2vpnNveNode);

                nveL2vpnConstructedNodes.put(nve, vxlanL2vpnNveNode);

                // 这里是不是只会有一个默认的BGP进程呢？

                nve.getMemberVnis().forEach(
                        (vniNumber, vniConfig) ->{
                            if(vniConfig.isAssociateVrf())
                            {
                                BGP evpnL3vpnBgpNode = evpnL3vpnBgpNodes.get("default");
                                org.batfish.MEV.Edge nveToBgpEdge = new org.batfish.MEV.Edge(evpnL3vpnNveNode, evpnL3vpnBgpNode, new ArrayList<>(), Long.valueOf(0));
                                org.batfish.MEV.Edge bgpToNveEdge = new org.batfish.MEV.Edge(evpnL3vpnBgpNode, evpnL3vpnNveNode, new ArrayList<>(), Long.valueOf(0));
                                EdgeOperation nveToBgpOperation = new EdgeOperation();
                                EdgeOperation bgpToNveOperation = new EdgeOperation();
                                nveToBgpOperation.addBlockVni(vniNumber);
//                                bgpToNveOperation.addBlockVni(vniNumber);
                                nveToBgpEdge.setOutOperation(nveToBgpOperation);
                                bgpToNveEdge.setOutOperation(bgpToNveOperation);
                                evpnL3vpnGraph.addEdge(nveToBgpEdge);
                                evpnL3vpnGraph.addEdge(bgpToNveEdge);
                            } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.BGP)){
                                BGP vxlanL2vpnBgpNode = vxlanL2vpnBgpNodes.get("default");
                                org.batfish.MEV.Edge nveToBgpEdge = new org.batfish.MEV.Edge(vxlanL2vpnNveNode, vxlanL2vpnNveNode, new ArrayList<>(), Long.valueOf(0));
                                org.batfish.MEV.Edge bgpToNveEdge = new org.batfish.MEV.Edge(vxlanL2vpnBgpNode, vxlanL2vpnNveNode, new ArrayList<>(), Long.valueOf(0));
                                EdgeOperation nveToBgpOperation = new EdgeOperation();
                                EdgeOperation bgpToNveOperation = new EdgeOperation();
                                nveToBgpOperation.addBlockVni(vniNumber);
//                                bgpToNveOperation.addBlockVni(vniNumber);
                                nveToBgpEdge.setOutOperation(nveToBgpOperation);
                                bgpToNveEdge.setOutOperation(bgpToNveOperation);
                                vxlanL2vpnGraph.addEdge(nveToBgpEdge);
                                vxlanL2vpnGraph.addEdge(bgpToNveEdge);
                            } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.STATIC))
                            {
                                for (Ip peerIp : vniConfig.getPeerIps())
                                {
                                    if (unConstructedNveEdges.containsKey(peerIp) && unConstructedNveEdges.get(peerIp).containsKey(nveIP))
                                    {
                                        NeighborTriple neighborTriple = unConstructedNveEdges.get(peerIp).get(nveIP);
                                        org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(vxlanL2vpnNveNode, neighborTriple.getNveNode(), new ArrayList<>(), Long.valueOf(0));
                                        org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborTriple.getNveNode(), vxlanL2vpnNveNode, new ArrayList<>(), Long.valueOf(0));
                                        Dependency localToNeighborDep = new Dependency(hostName, "default", neighborTriple.getRouter(), neighborTriple.getVrf(), neighborTriple.getNeighborIp());
                                        Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, "default", nveIP);
                                        List<Dependency> deps = new ArrayList<>();
                                        deps.add(localToNeighborDep);
                                        deps.add(neighborToLocalDep);

                                        localToNeighborEdge.setDependency(deps);
                                        neighborToLocalEdge.setDependency(deps);
                                    } else {
                                        NeighborTriple neighborTriple = new NeighborTriple(hostName, "default", nveIP, vxlanL2vpnNveNode);
                                        if (!unConstructedNveEdges.containsKey(nveIP))
                                        {
                                            unConstructedNveEdges.put(nveIP, new HashMap<>());
                                        }
                                        unConstructedNveEdges.get(nveIP).put(peerIp, neighborTriple);
                                    }
                                }
                            }
                        }
                );
            }


            for (Vrf vrf : routerConfig.getVrfs().values())
            {
                String vrfName = vrf.getName();
                VRF evpnL3vpnVrfNode = new VRF(evpnL3vpnNodeId++, NodeType.VRF, hostName, vrfName);
                VRF vrfNode = new VRF(bgpNodeId++, NodeType.VRF, hostName, vrfName);
                if (connectMessageList.containsKey(vrfName))
                {
                    evpnL3vpnVrfNode.setConnectMessages(connectMessageList.get(vrfName));
                    vrfNode.setConnectMessages(connectMessageList.get(vrfName));
                }

                //设置network命令宣告的前缀
                if ( vrf.getBgpProcess()!=null && !vrf.getBgpProcess().getNetworkPrefixs().isEmpty())
                {
                    vrfNode.addNetworkPrefix(vrf.getBgpProcess().getNetworkPrefixs().get(AddressFamily.Type.IPV4_UNICAST));
                    evpnL3vpnVrfNode.addNetworkPrefix(vrf.getBgpProcess().getNetworkPrefixs().get(AddressFamily.Type.IPV4_UNICAST));
                }

                evpnL3vpnGraph.addNode(evpnL3vpnVrfNode);
                bgpGraph.addNode(vrfNode);

                if(vrf.getVrfLeakConfig() != null && vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs() != null)
                {
                    Set<ExtendedCommunity> attachCommunity = new HashSet<>();
                    Set<ExtendedCommunity> blockCommunity = new HashSet<>();
                    for (Bgpv4ToEvpnVrfLeakConfig bgpv4ToEvpnVrfLeakConfig : vrf.getVrfLeakConfig().getBgpv4ToEvpnVrfLeakConfigs())
                    {
                        attachCommunity.addAll(bgpv4ToEvpnVrfLeakConfig.getAttachRouteTargets());
                    }
                    for (EvpnToBgpv4VrfLeakConfig evpnToBgpv4VrfLeakConfig : vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs())
                    {
                        blockCommunity.addAll(evpnToBgpv4VrfLeakConfig.getAttachRouteTargets());
                    }
                    for (Integer vni : vrf.getLayer3Vnis().keySet())
                    {
                        Layer3Vni vniConfig = vrf.getLayer3Vnis().get(vni);
                        if (nveL3vpnNodes.containsKey(vniConfig.getSourceAddress()))
                        {
                            NVE adjacencyNveNode = nveL3vpnNodes.get(vniConfig.getSourceAddress());
                            org.batfish.MEV.Edge vrfToNveEdge = new org.batfish.MEV.Edge(evpnL3vpnVrfNode, adjacencyNveNode, new ArrayList<>(), Long.valueOf(0));
                            org.batfish.MEV.Edge nveToVrfEdge = new org.batfish.MEV.Edge(adjacencyNveNode, evpnL3vpnVrfNode, new ArrayList<>(), Long.valueOf(0));

                            EdgeOperation vrfToNveOperation = new EdgeOperation();
                            EdgeOperation nveToVrfOperation = new EdgeOperation();
                            vrfToNveOperation.setAttachVni(vni);
                            vrfToNveOperation.addAttachRTs(attachCommunity);
                            nveToVrfOperation.addPermitRTs(blockCommunity);

                            vrfToNveEdge.setOutOperation(vrfToNveOperation);
                            nveToVrfEdge.setOutOperation(nveToVrfOperation);

                            evpnL3vpnGraph.addEdge(vrfToNveEdge);
                            evpnL3vpnGraph.addEdge(nveToVrfEdge);
                        }
                    }
                }

                if (vrf.getBgpProcess() != null && vrf.getBgpProcess().getLocalAs() != null)
                {
                    BGP vrfToBgpNode = bgpConstructedNodes.get(vrfName);
                    bgpGraph.addEdge(new org.batfish.MEV.Edge(vrfNode, vrfToBgpNode, new ArrayList<>(), Long.valueOf(0)));
                    bgpGraph.addEdge(new org.batfish.MEV.Edge(vrfToBgpNode, vrfNode, new ArrayList<>(), Long.valueOf(0)));
                }

                if (vrf.getIsisProcess() != null)
                {
                    List<Prefix> announcePrefix = new ArrayList<>();
                    VRF vrfIsisNode = new VRF(isisNodeId++, NodeType.VRF, hostName, vrfName);
                    Map<String, Interface> routerInterfaceMap = routerConfig.getAllInterfaces(vrfName);
                    ISIS constructedIsisNode = new ISIS(isisNodeId++, NodeType.ISIS, hostName, vrfName);
                    constructedIsisNode.setAnnouncePrefix(announcePrefix);
                    isisGraph.addNode(constructedIsisNode);
                    isisGraph.addNode(vrfIsisNode);
                    isisGraph.addEdge(new Edge(constructedIsisNode, vrfIsisNode, new ArrayList<>(), Long.valueOf(0)));
                    isisGraph.addEdge(new Edge(vrfIsisNode, constructedIsisNode, new ArrayList<>(), Long.valueOf(0)));
                    for (String interfaceName : routerInterfaceMap.keySet())
                    {
                        Interface routerInterface = routerInterfaceMap.get(interfaceName);
                        if (routerInterface.getIsis() != null)
                        {
                            announcePrefix.add(routerInterface.getConcreteAddress().getPrefix());
                        }
                        IsisNode isisNode = new IsisNode(hostName, interfaceName);
                        if (!isisNetwork.nodes().contains(isisNode))
                        {
                            continue;
                        }
                        unConstructedIsisEdges.put(isisNode, new HashMap<>());
                        for (IsisEdge isisEdge : isisNetwork.inEdges(isisNode))
                        {
                            IsisNode adjacentIsisNode = isisEdge.getNode1();
                            IsisLevel isisType = isisEdge.getCircuitType();
                            IsisInterfaceLevelSettings isisSetting = routerInterface.getIsis().getIsisSetting(isisType);
                            Long isisCost = isisSetting.getCost();
                            if (isisCost == null)
                            {
                                isisCost = Long.valueOf(10);
                            }
                            Ip nexthopIp = routerInterface.getConcreteAddress().getIp();
                            EdgeOperation outOperation = new EdgeOperation();
                            outOperation.setNexthopIp(nexthopIp);
                            if (unConstructedIsisEdges.containsKey(adjacentIsisNode) && unConstructedIsisEdges.get(adjacentIsisNode).containsKey(isisNode))
                            {
                                NeighborTriple isisNeighborTriple = unConstructedIsisEdges.get(adjacentIsisNode).get(isisNode);
                                ISIS remoteConstructedIsisNode = isisNeighborTriple.getIsisNode();
                                EdgeOperation remoteOutOperation = isisNeighborTriple.getOutOperation();
                                EdgeOperation remoteInOperation = isisNeighborTriple.getInOperation();
                                IsisLevel remoteIsisType = isisNeighborTriple.getIsisType();
                                Long remoteIsisCost = isisNeighborTriple.getIsisCost();
                                Edge localToNeighborEdge = new org.batfish.MEV.Edge(constructedIsisNode, remoteConstructedIsisNode, new ArrayList<>(), isisCost);
                                Edge neighborToLocalEdge = new org.batfish.MEV.Edge(remoteConstructedIsisNode, constructedIsisNode, new ArrayList<>(), remoteIsisCost);
                                localToNeighborEdge.setIsisType(isisType);
                                localToNeighborEdge.setOutOperation(outOperation);
                                neighborToLocalEdge.setIsisType(remoteIsisType);
                                neighborToLocalEdge.setOutOperation(remoteOutOperation);

                                isisGraph.addEdge(localToNeighborEdge);
                                isisGraph.addEdge(neighborToLocalEdge);
                            } else {


                                NeighborTriple isisNeighborTriple = new NeighborTriple(hostName, vrfName, constructedIsisNode, isisType, isisCost);
                                isisNeighborTriple.setOutOperation(outOperation);
                                unConstructedIsisEdges.get(isisNode).put(adjacentIsisNode, isisNeighborTriple);
                            }
                        }
                    }
                    constructedIsisNode.setAnnouncePrefix(announcePrefix);
                }

                if (connectMessageList.containsKey(vrfName))
                {
                    VRF vrfConnectNode = new VRF(connectNodeId++, NodeType.VRF, hostName, vrfName);
                    for (Message connectMessage : connectMessageList.get(vrfName))
                    {
                        vrfConnectNode.addMessage(connectMessage, false);
                    }
                    connectGraph.addNode(vrfConnectNode);
                }
            }

//            for (org.batfish.datamodel.Vlan vlan : routerConfig.getVlans())
//            {
//                int vlanId = vlan.getId();
//                int vlanVni = vlan.getVni();
//                VLAN vlanNode = new VLAN(vxlanL2vpnNodeId++, NodeType.VLAN, hostName, vlan.toString());
//                vxlanL2vpnGraph.addNode(vlanNode);
//                vlanConstructedNodes.put(vlanId, vlanNode);
//
//                for (Nve nve : routerConfig.getNves().values())
//                {
//                    if (nve.getMemberVni(vlanVni) != null)
//                    {
//                        org.batfish.MEV.Edge vlanToNveEdge = new org.batfish.MEV.Edge(vlanNode, nveL2vpnConstructedNodes.get(nve), new ArrayList<>(), 0);
//                        org.batfish.MEV.Edge nveToVlanEdge = new org.batfish.MEV.Edge(nveL2vpnConstructedNodes.get(nve), vlanNode, new ArrayList<>(), 0);
//                        EdgeOperation outOperation = new EdgeOperation();
//                        EdgeOperation inOperation = new EdgeOperation();
//                        outOperation.setAttachVni(vlanVni);
//                        inOperation.addBlockVni(vlanVni);
//                        vlanToNveEdge.setOutOperation(outOperation);
//                        nveToVlanEdge.setInOperation(inOperation);
//                        vxlanL2vpnGraph.addEdge(vlanToNveEdge);
//                        vxlanL2vpnGraph.addEdge(nveToVlanEdge);
//                    }
//                }
//            }
//
//            for (Interface interfaceConfig : routerConfig.getAllInterfaces().values())
//            {
//                String interfaceName = interfaceConfig.getName();
//                if (interfaceConfig.getSwitchportTrunkEncapsulation().equals(SwitchportEncapsulationType.DOT1Q))
//                {
//                    INTER interfaceNode = new INTER(vxlanL2vpnNodeId++, NodeType.INTER, hostName, interfaceName);
//                    vxlanL2vpnGraph.addNode(interfaceNode);
//                    VLAN vlanNode = vlanConstructedNodes.get(interfaceConfig.getVlan());
//                    org.batfish.MEV.Edge interfaceToVlanEdge = new org.batfish.MEV.Edge(interfaceNode, vlanNode, new ArrayList<>(), 0);
//                    org.batfish.MEV.Edge vlanToInterfaceEdge = new org.batfish.MEV.Edge(vlanNode, interfaceNode, new ArrayList<>(), 0);
//                    vxlanL2vpnGraph.addEdge(interfaceToVlanEdge);
//                    vxlanL2vpnGraph.addEdge(vlanToInterfaceEdge);
//                }
//            }
        }
        _evpnL3vpnGraph = evpnL3vpnGraph;
        _bgpGraph = bgpGraph;
        _vxlanL2vpnGraph = vxlanL2vpnGraph;
        _isisGraph = isisGraph;
        _connectGraph = connectGraph;
    }

//    public void controlPlaneConstructor(Map<String, Configuration> configurations, IsisTopology isisTopology, Topology L3Topology)
//    {
//        Graph evpnL3vpnGraph = new Graph(GraphType.EVPNL3VPN);
//        Graph bgpGraph = new Graph(GraphType.BGP);
//        Graph vxlanL2vpnGraph = new Graph(GraphType.VXLANL2VPN);
//        Graph isisGraph = new Graph(GraphType.ISIS);
//        Graph connectGraph = new Graph(GraphType.CONNECT);
//        int evpnL3vpnNodeId = 0;
//        int bgpNodeId = 0;
//        int vxlanL2vpnNodeId = 0;
//        int isisNodeId = 0;
//        int connectNodeId = 0;
//        HashMap<Ip, NVE> nveL3vpnNodes = new HashMap<>();
//        HashMap<Long, BGP> bgpL3vpnNodes = new HashMap<>();
//        HashMap<Ip, NVE> nveL2vpnNodes = new HashMap<>();
//        HashMap<Long, BGP> bgpL2vpnNodes = new HashMap<>();
//        HashMap<Long, BGP> bgpNodes = new HashMap<>();
//        HashMap<Long, HashMap<Long, List<NeighborTriple>>> unConstructedL3vpnBgpEdges = new HashMap<>();
//        HashMap<Long, HashMap<Long, List<NeighborTriple>>> unConstructedL2vpnBgpEdges = new HashMap<>();
//        HashMap<Long, HashMap<Long, List<NeighborTriple>>> unConstructedBgpEdges = new HashMap<>();
//        HashMap<IsisNode, HashMap<IsisNode, NeighborTriple>> unConstructedIsisEdges = new HashMap<>();
//        HashMap<Ip, HashMap<Ip, NeighborTriple>> unConstructedNveEdges = new HashMap<>();
//        Map<String, org.batfish.datamodel.Edge> interfaceToEdgeList = L3Topology.computeInterfaceEdges();
//
//
//        Network<IsisNode, IsisEdge> isisNetwork = isisTopology.getNetwork();
//        //Construct Graph
//        for (String routerName : configurations.keySet())
//        {
//            Configuration routerConfig = configurations.get(routerName);
//            // 构建直连路由
//            HashMap<String, List<Message>> connectMessageList = new HashMap<>();
//            for (Interface inter : routerConfig.getAllInterfaces().values())
//            {
//                String vrfName = inter.getVrfName();
//                if (!connectMessageList.containsKey(vrfName))
//                {
//                    connectMessageList.put(vrfName, new ArrayList<>());
//                }
//
//                if (inter.getConcreteAddress() != null)
//                {
//                    String nexthopRouter = null;
//                    if (interfaceToEdgeList.containsKey(routerName + "-" + inter.getName()))
//                    {
//                        nexthopRouter = interfaceToEdgeList.get(routerName + "-" + inter.getName()).getNode2();
//                    }
////                    String nexthopRouter = interfaceToEdgeList.get(routerName + "-" + inter.getName()).getNode2();
//                    Prefix prefix = inter.getConcreteAddress().getPrefix();
//                    Nexthop nexthop = new Nexthop(NexthopType.Original, nexthopRouter, null);
//                    Message message = new Message(prefix, new ConnectAttribute(0), new ArrayList<>(), new ArrayList<>(), null, nexthop, MessageType.Connected, new ArrayList<>(), Reason.ADD);
//                    message.setReason(Reason.ADD);
//                    connectMessageList.get(vrfName).add(message);
//                }
//            }
//
//            String hostName = routerConfig.getHostname();
//            HashMap<String, BGP> evpnL3vpnBgpNodes = new HashMap<>();
//            HashMap<String, BGP> vxlanL2vpnBgpNodes = new HashMap<>();
//            HashMap<String, BGP> bgpConstructedNodes = new HashMap<>();
//            HashMap<Integer, VLAN> vlanConstructedNodes = new HashMap<>();
//            HashMap<Nve, NVE> nveL2vpnConstructedNodes = new HashMap<>();
//            for(Vrf vrf : routerConfig.getVrfs().values())
//            {
//                String vrfName = vrf.getName();
//                if (vrfName.equals("management"))
//                {
//                    continue;
//                }
//                BgpProcess bgpProcess = vrf.getBgpProcess();
//                if (bgpProcess.getLocalAs() == null)
//                {
//                    continue;
//                }
//                long ASNumber = bgpProcess.getLocalAs();
//                BGP bgpNode = new BGP(bgpNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
//                BGP bgpL3vpnNode = new BGP(evpnL3vpnNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
//                BGP bgpL2vpnNode = new BGP(vxlanL2vpnNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
//                bgpGraph.addNode(bgpNode);
//                evpnL3vpnGraph.addNode(bgpL3vpnNode);
//                vxlanL2vpnGraph.addNode(bgpL2vpnNode);
//                bgpNodes.put(ASNumber, bgpNode);
//                bgpL3vpnNodes.put(ASNumber, bgpL3vpnNode);
//                bgpL2vpnNodes.put(ASNumber, bgpL2vpnNode);
//                if (!unConstructedBgpEdges.containsKey(ASNumber))
//                {
//                    unConstructedBgpEdges.put(ASNumber, new HashMap<>());
//                }
//                if (!unConstructedL3vpnBgpEdges.containsKey(ASNumber))
//                {
//                    unConstructedL3vpnBgpEdges.put(ASNumber, new HashMap<>());
//                }
//                if (!unConstructedL2vpnBgpEdges.containsKey(ASNumber))
//                {
//                    unConstructedL2vpnBgpEdges.put(ASNumber, new HashMap<>());
//                }
//
//                bgpConstructedNodes.put(vrfName, bgpNode);
//
//                evpnL3vpnBgpNodes.put(vrfName, bgpL3vpnNode);
//                vxlanL2vpnBgpNodes.put(vrfName, bgpL2vpnNode);
//
//                bgpProcess.getActiveNeighbors().forEach(
//                        (neighborIP, neighborBgpConfig) -> {
//                            // 构建EVPN模型中的边
//                            if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN) != null)
//                            {
//                                long reflectorId = neighborBgpConfig.getClusterId();
//                                bgpL3vpnNode.setReflectorId(reflectorId);
//                                long localAs = neighborBgpConfig.getLocalAs();
//                                boolean reflector = neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getRouteReflectorClient();
//                                for (Long neighborAs : neighborBgpConfig.getRemoteAsns().enumerate())
//                                {
//                                    if (unConstructedL3vpnBgpEdges.containsKey(neighborAs)&&unConstructedL3vpnBgpEdges.get(neighborAs).containsKey(localAs))
//                                    {
//                                        for (NeighborTriple neighborTriple : unConstructedL3vpnBgpEdges.get(neighborAs).get(localAs))
//                                        {
//                                            Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
//                                            Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getRouter(), neighborTriple.getVrf(), neighborIP);
//                                            List<Dependency> deps = new ArrayList<>();
//                                            deps.add(neighborToLocalDep);
//                                            deps.add(localToNeighborDep);
//
//                                            BGP neighborNode = neighborTriple.getBgpNode();
//                                            org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborNode, bgpL3vpnNode, deps, Long.valueOf(0));
//                                            org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(bgpL3vpnNode, neighborNode, deps, Long.valueOf(0));
//
//
//
//
//
//
//                                            if (neighborTriple.getReflector())
//                                            {
//                                                neighborNode.addReflectorNeighbor(bgpL3vpnNode);
//                                            }
//
//                                            if (reflector)
//                                            {
//                                                bgpL3vpnNode.addReflectorNeighbor(neighborNode);
//                                            }
//
//                                            // 需要在Operation里面设置下一跳，并且设置不将iBGP路由传播给iBGP邻居
//                                            EdgeOperation outOperation = new EdgeOperation();
//                                            EdgeOperation inOperation = new EdgeOperation();
//
//                                            RoutingPolicy outRoutingPolicy = null;
//                                            RoutingPolicy inRoutingPolicy = null;
//
//                                            if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                                            {
//                                                outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                                                outOperation.setPolicies(outRoutingPolicy);
//                                            }
//
//                                            if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                                            {
//                                                inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                                                inOperation.setPolicies(inRoutingPolicy);
//                                            }
//
////                                            outOperation.setNexthopIp(neighborTriple.getNeighborIp());
////                                            neighborTriple.getOutOperation().setNexthopIp(neighborIP);
//
//                                            if (localAs == neighborAs)
//                                            {
//                                                outOperation.addBlockMessageType(MessageType.iBGP);
//                                                neighborTriple.getOutOperation().addBlockMessageType(MessageType.iBGP);
//                                                outOperation.reSetIBGP();
//                                            } else
//                                            {
//                                                outOperation.reSetEBGP();
//                                            }
//
//                                            neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
//                                            neighborToLocalEdge.setInOperation(inOperation);
//                                            localToNeighborEdge.setOutOperation(outOperation);
//                                            localToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//
//                                            evpnL3vpnGraph.addEdge(neighborToLocalEdge);
//                                            evpnL3vpnGraph.addEdge(localToNeighborEdge);
//                                        }
//
//                                        for (NeighborTriple neighborTriple : unConstructedL2vpnBgpEdges.get(neighborAs).get(localAs))
//                                        {
//                                            Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
//                                            Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getRouter(), neighborTriple.getVrf(), neighborIP);
//                                            List<Dependency> deps = new ArrayList<>();
//                                            deps.add(neighborToLocalDep);
//                                            deps.add(localToNeighborDep);
//
//                                            BGP neighborNode = neighborTriple.getBgpNode();
//                                            org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborNode, bgpL2vpnNode, deps, Long.valueOf(0));
//                                            org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(bgpL2vpnNode, neighborNode, deps, Long.valueOf(0));
//
//
//                                            // 需要在Operation里面设置下一跳，并且设置不将iBGP路由传播给iBGP邻居
//                                            EdgeOperation outOperation = new EdgeOperation();
//                                            EdgeOperation inOperation = new EdgeOperation();
//
//                                            RoutingPolicy outRoutingPolicy = null;
//                                            RoutingPolicy inRoutingPolicy = null;
//
//                                            if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                                            {
//                                                outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                                                outOperation.setPolicies(outRoutingPolicy);
//                                            }
//
//                                            if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                                            {
//                                                inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                                                inOperation.setPolicies(inRoutingPolicy);
//                                            }
//
////                                            outOperation.setNexthopIp(neighborTriple.getNeighborIp());
////                                            neighborTriple.getOutOperation().setNexthopIp(neighborIP);
//
//                                            if (localAs == neighborAs)
//                                            {
//                                                outOperation.addBlockMessageType(MessageType.iBGP);
//                                                neighborTriple.getOutOperation().addBlockMessageType(MessageType.iBGP);
//                                                outOperation.reSetIBGP();
//                                            } else {
//                                                outOperation.reSetEBGP();
//                                            }
//
//                                            neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
//                                            neighborToLocalEdge.setInOperation(inOperation);
//                                            localToNeighborEdge.setOutOperation(outOperation);
//                                            localToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//
//                                            vxlanL2vpnGraph.addEdge(neighborToLocalEdge);
//                                            vxlanL2vpnGraph.addEdge(localToNeighborEdge);
//                                        }
//                                    } else {
//                                        NeighborTriple neighborTriple = new NeighborTriple(hostName, vrfName, neighborIP, bgpL3vpnNode);
//                                        neighborTriple.setReflector(reflector);
//                                        EdgeOperation outOperation = new EdgeOperation();
//                                        EdgeOperation inOperation = new EdgeOperation();
//                                        RoutingPolicy outRoutingPolicy = null;
//                                        RoutingPolicy inRoutingPolicy = null;
//                                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                                        {
//                                            outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                                            outOperation.setPolicies(outRoutingPolicy);
//                                        }
//                                        if(!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                                        {
//                                            inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                                            inOperation.setPolicies(inRoutingPolicy);
//                                        }
//
//                                        neighborTriple.setOutOperation(outOperation);
//                                        neighborTriple.setInOperation(inOperation);
//                                        if (!unConstructedL3vpnBgpEdges.get(localAs).containsKey(neighborAs))
//                                        {
//                                            unConstructedL3vpnBgpEdges.get(localAs).put(neighborAs, new ArrayList<>());
//                                        }
//                                        unConstructedL3vpnBgpEdges.get(localAs).get(neighborAs).add(neighborTriple);
//
//                                        NeighborTriple neighborTripleL2 = new NeighborTriple(hostName, vrfName, neighborIP, bgpL2vpnNode);
//                                        EdgeOperation outOperationL2 = new EdgeOperation();
//                                        EdgeOperation inOperationL2 = new EdgeOperation();
//                                        RoutingPolicy outRoutingPolicyL2 = null;
//                                        RoutingPolicy inRoutingPolicyL2 = null;
//                                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                                        {
//                                            outRoutingPolicyL2 = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                                            outOperationL2.setPolicies(outRoutingPolicyL2);
//                                        }
//                                        if(!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                                        {
//                                            inRoutingPolicyL2 = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                                            inOperationL2.setPolicies(inRoutingPolicyL2);
//                                        }
//
//                                        neighborTripleL2.setOutOperation(outOperationL2);
//                                        neighborTripleL2.setInOperation(inOperationL2);
//                                        if (!unConstructedL2vpnBgpEdges.get(localAs).containsKey(neighborAs))
//                                        {
//                                            unConstructedL2vpnBgpEdges.get(localAs).put(neighborAs, new ArrayList<>());
//                                        }
//                                        unConstructedL2vpnBgpEdges.get(localAs).get(neighborAs).add(neighborTripleL2);
//                                    }
//                                }
//                            }
//                            if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST) != null)
//                            {
//                                Long reflectorId = neighborBgpConfig.getClusterId();
//                                bgpNode.setReflectorId(reflectorId);
//                                long localAs = neighborBgpConfig.getLocalAs();
//                                boolean reflector = neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getRouteReflectorClient();
//                                for (Long neighborAs : neighborBgpConfig.getRemoteAsns().enumerate()) {
//                                    if (unConstructedBgpEdges.containsKey(neighborAs)&&unConstructedBgpEdges.get(neighborAs).containsKey(localAs)) {
//                                        for (NeighborTriple neighborTriple : unConstructedBgpEdges.get(neighborAs).get(localAs)) {
//                                            Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
//                                            Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getRouter(), neighborTriple.getVrf(), neighborIP);
//                                            List<Dependency> deps = new ArrayList<>();
//                                            deps.add(neighborToLocalDep);
//                                            deps.add(localToNeighborDep);
//
//                                            BGP neighborNode = neighborTriple.getBgpNode();
//                                            org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborNode, bgpNode, deps, Long.valueOf(0));
//                                            org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(bgpNode, neighborNode, deps, Long.valueOf(0));
//
//                                            EdgeOperation outOperation = new EdgeOperation();
//                                            EdgeOperation inOperation = new EdgeOperation();
//
//                                            RoutingPolicy outRoutingPolicy = null;
//                                            RoutingPolicy inRoutingPolicy = null;
//
//                                            if (reflector)
//                                            {
//                                                bgpNode.addReflectorNeighbor(neighborNode);
//                                            }
//
//
//
//
//                                            if (neighborTriple.getReflector())
//                                            {
//                                                neighborNode.addReflectorNeighbor(bgpNode);
//                                            }
//
//                                            if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicySources().isEmpty()) {
//                                                outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicy());
//                                                outOperation.setPolicies(outRoutingPolicy);
//                                            }
//
//                                            if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicySources().isEmpty()) {
//                                                inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicy());
//                                                inOperation.setPolicies(inRoutingPolicy);
//                                            }
//
//                                            outOperation.setNexthopIp(neighborTriple.getNeighborIp());
//                                            neighborTriple.getOutOperation().setNexthopIp(neighborIP);
//
//                                            if (localAs == neighborAs)
//                                            {
//                                                outOperation.addBlockMessageType(MessageType.iBGP);
//                                                neighborTriple.getOutOperation().addBlockMessageType(MessageType.iBGP);
//                                                outOperation.reSetIBGP();
//                                            } else {
//                                                outOperation.reSetEBGP();
//                                            }
//
//                                            neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
//                                            neighborToLocalEdge.setInOperation(inOperation);
//                                            localToNeighborEdge.setOutOperation(outOperation);
//                                            localToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//
//                                            bgpGraph.addEdge(neighborToLocalEdge);
//                                            bgpGraph.addEdge(localToNeighborEdge);
//                                        }
//                                    } else {
//                                        NeighborTriple neighborTriple = new NeighborTriple(hostName, vrfName, neighborIP, bgpNode);
//                                        neighborTriple.setReflector(reflector);
//                                        EdgeOperation outOperation = new EdgeOperation();
//                                        EdgeOperation inOperation = new EdgeOperation();
//                                        RoutingPolicy outRoutingPolicy = null;
//                                        RoutingPolicy inRoutingPolicy = null;
//                                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicySources().isEmpty()) {
//                                            outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicy());
//                                            outOperation.setPolicies(outRoutingPolicy);
//                                        }
//                                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicySources().isEmpty()) {
//                                            inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicy());
//                                            inOperation.setPolicies(inRoutingPolicy);
//                                        }
//                                        neighborTriple.setOutOperation(outOperation);
//                                        neighborTriple.setInOperation(inOperation);
//
//                                        if (!unConstructedBgpEdges.get(localAs).containsKey(neighborAs))
//                                        {
//                                            unConstructedBgpEdges.get(localAs).put(neighborAs, new ArrayList<>());
//                                        }
//                                        unConstructedBgpEdges.get(localAs).get(neighborAs).add(neighborTriple);
//                                    }
//                                }
//                            }
//                        }
//                );
//            }
//
//            for (Nve nve : routerConfig.getNves().values())
//            {
//                NVE evpnL3vpnNveNode = new NVE(evpnL3vpnNodeId++, NodeType.NVE, hostName, nve.toString());
//                NVE vxlanL2vpnNveNode = new NVE(vxlanL2vpnNodeId++, NodeType.NVE, hostName, nve.toString());
//                Ip nveIP = getInterfaceIp(routerConfig.getAllInterfaces(),nve.getSourceInterface());
//                evpnL3vpnNveNode.setEncapType(EncapType.VXLAN);
//                vxlanL2vpnNveNode.setEncapType(EncapType.VXLAN);
//                evpnL3vpnNveNode.setEncapIP(nveIP);
//                vxlanL2vpnNveNode.setEncapIP(nveIP);
//                nveL3vpnNodes.put(nveIP, evpnL3vpnNveNode);
//                nveL2vpnNodes.put(nveIP, vxlanL2vpnNveNode);
//                evpnL3vpnGraph.addNode(evpnL3vpnNveNode);
//                vxlanL2vpnGraph.addNode(vxlanL2vpnNveNode);
//
//                nveL2vpnConstructedNodes.put(nve, vxlanL2vpnNveNode);
//
//                // 这里是不是只会有一个默认的BGP进程呢？
//
//                nve.getMemberVnis().forEach(
//                        (vniNumber, vniConfig) ->{
//                            if(vniConfig.isAssociateVrf())
//                            {
//                                BGP evpnL3vpnBgpNode = evpnL3vpnBgpNodes.get("default");
//                                org.batfish.MEV.Edge nveToBgpEdge = new org.batfish.MEV.Edge(evpnL3vpnNveNode, evpnL3vpnBgpNode, new ArrayList<>(), Long.valueOf(0));
//                                org.batfish.MEV.Edge bgpToNveEdge = new org.batfish.MEV.Edge(evpnL3vpnBgpNode, evpnL3vpnNveNode, new ArrayList<>(), Long.valueOf(0));
//                                EdgeOperation nveToBgpOperation = new EdgeOperation();
//                                EdgeOperation bgpToNveOperation = new EdgeOperation();
//                                nveToBgpOperation.addBlockVni(vniNumber);
//                                bgpToNveOperation.addBlockVni(vniNumber);
//                                nveToBgpEdge.setOutOperation(nveToBgpOperation);
//                                bgpToNveEdge.setOutOperation(bgpToNveOperation);
//                                evpnL3vpnGraph.addEdge(nveToBgpEdge);
//                                evpnL3vpnGraph.addEdge(bgpToNveEdge);
//                            } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.BGP)){
//                                BGP vxlanL2vpnBgpNode = vxlanL2vpnBgpNodes.get("default");
//                                org.batfish.MEV.Edge nveToBgpEdge = new org.batfish.MEV.Edge(vxlanL2vpnNveNode, vxlanL2vpnNveNode, new ArrayList<>(), Long.valueOf(0));
//                                org.batfish.MEV.Edge bgpToNveEdge = new org.batfish.MEV.Edge(vxlanL2vpnBgpNode, vxlanL2vpnNveNode, new ArrayList<>(), Long.valueOf(0));
//                                EdgeOperation nveToBgpOperation = new EdgeOperation();
//                                EdgeOperation bgpToNveOperation = new EdgeOperation();
//                                nveToBgpOperation.addBlockVni(vniNumber);
//                                bgpToNveOperation.addBlockVni(vniNumber);
//                                nveToBgpEdge.setOutOperation(nveToBgpOperation);
//                                bgpToNveEdge.setOutOperation(bgpToNveOperation);
//                                vxlanL2vpnGraph.addEdge(nveToBgpEdge);
//                                vxlanL2vpnGraph.addEdge(bgpToNveEdge);
//                            } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.STATIC))
//                            {
//                                for (Ip peerIp : vniConfig.getPeerIps())
//                                {
//                                    if (unConstructedNveEdges.containsKey(peerIp) && unConstructedNveEdges.get(peerIp).containsKey(nveIP))
//                                    {
//                                        NeighborTriple neighborTriple = unConstructedNveEdges.get(peerIp).get(nveIP);
//                                        org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(vxlanL2vpnNveNode, neighborTriple.getNveNode(), new ArrayList<>(), Long.valueOf(0));
//                                        org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborTriple.getNveNode(), vxlanL2vpnNveNode, new ArrayList<>(), Long.valueOf(0));
//                                        Dependency localToNeighborDep = new Dependency(hostName, "default", neighborTriple.getRouter(), neighborTriple.getVrf(), neighborTriple.getNeighborIp());
//                                        Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, "default", nveIP);
//                                        List<Dependency> deps = new ArrayList<>();
//                                        deps.add(localToNeighborDep);
//                                        deps.add(neighborToLocalDep);
//
//                                        localToNeighborEdge.setDependency(deps);
//                                        neighborToLocalEdge.setDependency(deps);
//                                    } else {
//                                        NeighborTriple neighborTriple = new NeighborTriple(hostName, "default", nveIP, vxlanL2vpnNveNode);
//                                        if (!unConstructedNveEdges.containsKey(nveIP))
//                                        {
//                                            unConstructedNveEdges.put(nveIP, new HashMap<>());
//                                        }
//                                        unConstructedNveEdges.get(nveIP).put(peerIp, neighborTriple);
//                                    }
//                                }
//                            }
//                        }
//                );
//            }
//
//
//            for (Vrf vrf : routerConfig.getVrfs().values())
//            {
//                String vrfName = vrf.getName();
//                VRF evpnL3vpnVrfNode = new VRF(evpnL3vpnNodeId++, NodeType.VRF, hostName, vrfName);
//                VRF vrfNode = new VRF(bgpNodeId++, NodeType.VRF, hostName, vrfName);
//                if (connectMessageList.containsKey(vrfName))
//                {
//                    evpnL3vpnVrfNode.setConnectMessages(connectMessageList.get(vrfName));
//                    vrfNode.setConnectMessages(connectMessageList.get(vrfName));
//                }
//
//                //设置network命令宣告的前缀
//                if ( vrf.getBgpProcess()!=null && !vrf.getBgpProcess().getNetworkPrefixs().isEmpty())
//                {
//                    vrfNode.addNetworkPrefixes(vrf.getBgpProcess().getNetworkPrefixs().get(AddressFamily.Type.IPV4_UNICAST));
//                    evpnL3vpnVrfNode.addNetworkPrefixes(vrf.getBgpProcess().getNetworkPrefixs().get(AddressFamily.Type.IPV4_UNICAST));
//                }
//
//                evpnL3vpnGraph.addNode(evpnL3vpnVrfNode);
//                bgpGraph.addNode(vrfNode);
//
//                if(vrf.getVrfLeakConfig() != null)
//                {
//                    Set<ExtendedCommunity> attachCommunity = new HashSet<>();
//                    Set<ExtendedCommunity> blockCommunity = new HashSet<>();
//                    for (Bgpv4ToEvpnVrfLeakConfig bgpv4ToEvpnVrfLeakConfig : vrf.getVrfLeakConfig().getBgpv4ToEvpnVrfLeakConfigs())
//                    {
//                        attachCommunity.addAll(bgpv4ToEvpnVrfLeakConfig.getAttachRouteTargets());
//                    }
//                    for (EvpnToBgpv4VrfLeakConfig evpnToBgpv4VrfLeakConfig : vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs())
//                    {
//                        blockCommunity.addAll(evpnToBgpv4VrfLeakConfig.getAttachRouteTargets());
//                    }
//                    for (Integer vni : vrf.getLayer3Vnis().keySet())
//                    {
//                        Layer3Vni vniConfig = vrf.getLayer3Vnis().get(vni);
//                        if (nveL3vpnNodes.containsKey(vniConfig.getSourceAddress()))
//                        {
//                            NVE adjacencyNveNode = nveL3vpnNodes.get(vniConfig.getSourceAddress());
//                            org.batfish.MEV.Edge vrfToNveEdge = new org.batfish.MEV.Edge(evpnL3vpnVrfNode, adjacencyNveNode, new ArrayList<>(), Long.valueOf(0));
//                            org.batfish.MEV.Edge nveToVrfEdge = new org.batfish.MEV.Edge(adjacencyNveNode, evpnL3vpnVrfNode, new ArrayList<>(), Long.valueOf(0));
//
//                            EdgeOperation vrfToNveOperation = new EdgeOperation();
//                            EdgeOperation nveToVrfOperation = new EdgeOperation();
//                            vrfToNveOperation.setAttachVni(vni);
//                            vrfToNveOperation.addAttachRTs(attachCommunity);
//                            nveToVrfOperation.addPermitRTs(blockCommunity);
//
//                            vrfToNveEdge.setOutOperation(vrfToNveOperation);
//                            nveToVrfEdge.setOutOperation(nveToVrfOperation);
//
//                            evpnL3vpnGraph.addEdge(vrfToNveEdge);
//                            evpnL3vpnGraph.addEdge(nveToVrfEdge);
//                        }
//                    }
//                }
//
//                if (vrf.getBgpProcess() != null && vrf.getBgpProcess().getLocalAs() != null)
//                {
//                    BGP vrfToBgpNode = bgpConstructedNodes.get(vrfName);
//                    bgpGraph.addEdge(new org.batfish.MEV.Edge(vrfNode, vrfToBgpNode, new ArrayList<>(), Long.valueOf(0)));
//                    bgpGraph.addEdge(new org.batfish.MEV.Edge(vrfToBgpNode, vrfNode, new ArrayList<>(), Long.valueOf(0)));
//                }
//
//                if (vrf.getIsisProcess() != null)
//                {
//                    List<Prefix> announcePrefix = new ArrayList<>();
//                    VRF vrfIsisNode = new VRF(isisNodeId++, NodeType.VRF, hostName, vrfName);
//                    Map<String, Interface> routerInterfaceMap = routerConfig.getAllInterfaces(vrfName);
//                    ISIS constructedIsisNode = new ISIS(isisNodeId++, NodeType.ISIS, hostName, vrfName);
//                    constructedIsisNode.setAnnouncePrefix(announcePrefix);
//                    isisGraph.addNode(constructedIsisNode);
//                    isisGraph.addNode(vrfIsisNode);
//                    isisGraph.addEdge(new Edge(constructedIsisNode, vrfIsisNode, new ArrayList<>(), Long.valueOf(0)));
//                    isisGraph.addEdge(new Edge(vrfIsisNode, constructedIsisNode, new ArrayList<>(), Long.valueOf(0)));
//                    for (String interfaceName : routerInterfaceMap.keySet())
//                    {
//                        Interface routerInterface = routerInterfaceMap.get(interfaceName);
//                        if (routerInterface.getIsis() != null)
//                        {
//                            announcePrefix.add(routerInterface.getConcreteAddress().getPrefix());
//                        }
//                        IsisNode isisNode = new IsisNode(hostName, interfaceName);
//                        if (!isisNetwork.nodes().contains(isisNode))
//                        {
//                            continue;
//                        }
//                        unConstructedIsisEdges.put(isisNode, new HashMap<>());
//                        for (IsisEdge isisEdge : isisNetwork.inEdges(isisNode))
//                        {
//                            IsisNode adjacentIsisNode = isisEdge.getNode1();
//                            IsisLevel isisType = isisEdge.getCircuitType();
//                            IsisInterfaceLevelSettings isisSetting = routerInterface.getIsis().getIsisSetting(isisType);
//                            Long isisCost = isisSetting.getCost();
//                            if (isisCost == null)
//                            {
//                                isisCost = Long.valueOf(10);
//                            }
//                            Ip nexthopIp = routerInterface.getConcreteAddress().getIp();
//                            EdgeOperation outOperation = new EdgeOperation();
//                            outOperation.setNexthopIp(nexthopIp);
//                            if (unConstructedIsisEdges.containsKey(adjacentIsisNode) && unConstructedIsisEdges.get(adjacentIsisNode).containsKey(isisNode))
//                            {
//                                NeighborTriple isisNeighborTriple = unConstructedIsisEdges.get(adjacentIsisNode).get(isisNode);
//                                ISIS remoteConstructedIsisNode = isisNeighborTriple.getIsisNode();
//                                EdgeOperation remoteOutOperation = isisNeighborTriple.getOutOperation();
//                                EdgeOperation remoteInOperation = isisNeighborTriple.getInOperation();
//                                IsisLevel remoteIsisType = isisNeighborTriple.getIsisType();
//                                Long remoteIsisCost = isisNeighborTriple.getIsisCost();
//                                Edge localToNeighborEdge = new org.batfish.MEV.Edge(constructedIsisNode, remoteConstructedIsisNode, new ArrayList<>(), isisCost);
//                                Edge neighborToLocalEdge = new org.batfish.MEV.Edge(remoteConstructedIsisNode, constructedIsisNode, new ArrayList<>(), remoteIsisCost);
//                                localToNeighborEdge.setIsisType(isisType);
//                                localToNeighborEdge.setOutOperation(outOperation);
//                                neighborToLocalEdge.setIsisType(remoteIsisType);
//                                neighborToLocalEdge.setOutOperation(remoteOutOperation);
//
//                                isisGraph.addEdge(localToNeighborEdge);
//                                isisGraph.addEdge(neighborToLocalEdge);
//                            } else {
//
//
//                                NeighborTriple isisNeighborTriple = new NeighborTriple(hostName, vrfName, constructedIsisNode, isisType, isisCost);
//                                isisNeighborTriple.setOutOperation(outOperation);
//                                unConstructedIsisEdges.get(isisNode).put(adjacentIsisNode, isisNeighborTriple);
//                            }
//                        }
//                    }
//                    constructedIsisNode.setAnnouncePrefix(announcePrefix);
//                }
//
//                if (connectMessageList.containsKey(vrfName))
//                {
//                    VRF vrfConnectNode = new VRF(connectNodeId++, NodeType.VRF, hostName, vrfName);
//                    for (Message connectMessage : connectMessageList.get(vrfName))
//                    {
//                        vrfConnectNode.addMessage(connectMessage);
//                    }
//                    connectGraph.addNode(vrfConnectNode);
//                }
//            }
//
////            for (org.batfish.datamodel.Vlan vlan : routerConfig.getVlans())
////            {
////                int vlanId = vlan.getId();
////                int vlanVni = vlan.getVni();
////                VLAN vlanNode = new VLAN(vxlanL2vpnNodeId++, NodeType.VLAN, hostName, vlan.toString());
////                vxlanL2vpnGraph.addNode(vlanNode);
////                vlanConstructedNodes.put(vlanId, vlanNode);
////
////                for (Nve nve : routerConfig.getNves().values())
////                {
////                    if (nve.getMemberVni(vlanVni) != null)
////                    {
////                        org.batfish.MEV.Edge vlanToNveEdge = new org.batfish.MEV.Edge(vlanNode, nveL2vpnConstructedNodes.get(nve), new ArrayList<>(), 0);
////                        org.batfish.MEV.Edge nveToVlanEdge = new org.batfish.MEV.Edge(nveL2vpnConstructedNodes.get(nve), vlanNode, new ArrayList<>(), 0);
////                        EdgeOperation outOperation = new EdgeOperation();
////                        EdgeOperation inOperation = new EdgeOperation();
////                        outOperation.setAttachVni(vlanVni);
////                        inOperation.addBlockVni(vlanVni);
////                        vlanToNveEdge.setOutOperation(outOperation);
////                        nveToVlanEdge.setInOperation(inOperation);
////                        vxlanL2vpnGraph.addEdge(vlanToNveEdge);
////                        vxlanL2vpnGraph.addEdge(nveToVlanEdge);
////                    }
////                }
////            }
////
////            for (Interface interfaceConfig : routerConfig.getAllInterfaces().values())
////            {
////                String interfaceName = interfaceConfig.getName();
////                if (interfaceConfig.getSwitchportTrunkEncapsulation().equals(SwitchportEncapsulationType.DOT1Q))
////                {
////                    INTER interfaceNode = new INTER(vxlanL2vpnNodeId++, NodeType.INTER, hostName, interfaceName);
////                    vxlanL2vpnGraph.addNode(interfaceNode);
////                    VLAN vlanNode = vlanConstructedNodes.get(interfaceConfig.getVlan());
////                    org.batfish.MEV.Edge interfaceToVlanEdge = new org.batfish.MEV.Edge(interfaceNode, vlanNode, new ArrayList<>(), 0);
////                    org.batfish.MEV.Edge vlanToInterfaceEdge = new org.batfish.MEV.Edge(vlanNode, interfaceNode, new ArrayList<>(), 0);
////                    vxlanL2vpnGraph.addEdge(interfaceToVlanEdge);
////                    vxlanL2vpnGraph.addEdge(vlanToInterfaceEdge);
////                }
////            }
//        }
//        _evpnL3vpnGraph = evpnL3vpnGraph;
//        _bgpGraph = bgpGraph;
//        _vxlanL2vpnGraph = vxlanL2vpnGraph;
//        _isisGraph = isisGraph;
//        _connectGraph = connectGraph;
//    }

    private @Nullable Ip getInterfaceIp(
            Map<String, org.batfish.datamodel.Interface> interfaces, String ifaceName) {
        org.batfish.datamodel.Interface iface = interfaces.get(ifaceName);
        if (iface == null) {
            return null;
        }
        ConcreteInterfaceAddress concreteInterfaceAddress = iface.getConcreteAddress();
        if (concreteInterfaceAddress == null) {
            return null;
        }
        return concreteInterfaceAddress.getIp();
    }

    public void reasoningControlPlane(GraphType graphType, boolean incremental)
    {
        if (graphType.equals(GraphType.BGP))
        {
            this._bgpGraph.reasoningControlPlane(incremental);
        } else if (graphType.equals(GraphType.EVPNL3VPN)) {
            this._evpnL3vpnGraph.reasoningControlPlane(incremental);
        } else if (graphType.equals(GraphType.ISIS)) {
            this._isisGraph.reasoningControlPlane(incremental);
        }
    }

    public void reasoningControlPlane() {
        // 创建三个异步任务
        CompletableFuture<Void> bgpTask = CompletableFuture.runAsync(() -> this._bgpGraph.reasoningControlPlane(false));
        CompletableFuture<Void> evpnTask = CompletableFuture.runAsync(() -> this._evpnL3vpnGraph.reasoningControlPlane(false));
        CompletableFuture<Void> isisTask = CompletableFuture.runAsync(() -> this._isisGraph.reasoningControlPlane(false));

        // 等待所有任务完成
        CompletableFuture.allOf(bgpTask, evpnTask, isisTask).join();
    }

    public Graph getBGPGraph()
    {
        return this._bgpGraph;
    }

    public Graph getEVPNL3vpnGraph()
    {
        return this._evpnL3vpnGraph;
    }

    public Graph getVXLANL2VPNGraph()
    {
        return this._vxlanL2vpnGraph;
    }



    public HashMap<GraphType, List<VRF>> getUnderlayVrfNodeSet()
    {
        HashMap<GraphType, List<VRF>> underlayVrfNodeSet = new HashMap<>();
        underlayVrfNodeSet.put(GraphType.BGP, this._bgpGraph.getVRFNodes());
        underlayVrfNodeSet.put(GraphType.ISIS, this._isisGraph.getVRFNodes());
        underlayVrfNodeSet.put(GraphType.CONNECT, this._connectGraph.getVRFNodes());
        return underlayVrfNodeSet;
    }

    public HashMap<GraphType, List<VRF>> getEVPNL3VPNVrfNodeSet()
    {
        HashMap<GraphType, List<VRF>> evpnL3vpnVrfNodeSet = new HashMap<>();
        evpnL3vpnVrfNodeSet.put(GraphType.EVPNL3VPN, this._evpnL3vpnGraph.getVRFNodes());
        return evpnL3vpnVrfNodeSet;
    }


    public void clearGraph()
    {
        this._isisGraph.clearGraph();
        this._bgpGraph.clearGraph();
        this._evpnL3vpnGraph.clearGraph();
    }




    public void addSubNet(VRFAdd vrfAdd, GraphType type)
    {
        String router = vrfAdd.getRouter();
        String vrf = vrfAdd.getVrf();
        if (type.equals(GraphType.EVPNL3VPN))
        {
            NVE nveNode = null;
            for (Node node : this._evpnL3vpnGraph.getNodes())
            {
                if (node instanceof NVE)
                {
                    if (node.getRouter().equals(router))
                    {
                        nveNode = (NVE) node;
                        break;
                    }
                }
            }
            if (nveNode != null)
            {
                VRF vrfNode = new VRF(this._evpnL3vpnGraph.getIndex(), NodeType.VRF, router, vrf);
                List<Prefix> networkPrefix = new ArrayList<>();
                networkPrefix.addAll(vrfAdd.getSubNet());
                vrfNode.addNetworkPrefix(networkPrefix);
                vrfNode.initRib(GraphType.EVPNL3VPN);
                vrfNode.initIsolation();
                vrfNode.convertOutMessage();
                vrfNode.reInitRib();
                nveNode.addIsolationUpdatedMessage(nveNode.getIsolationReceivedMessage());
                this._evpnL3vpnGraph.addNode(vrfNode);
                EdgeOperation vrfToNveOutEdgeOperation = new EdgeOperation();
                EdgeOperation nveToVrfOutEdgeOperation = new EdgeOperation();
                EdgeOperation vrfToNveInEdgeOperation = new EdgeOperation();
                EdgeOperation nveToVrfInEdgeOperation = new EdgeOperation();
                vrfToNveOutEdgeOperation.setAttachVni(vrfAdd.getVni());
                vrfToNveOutEdgeOperation.setAttachRTs(vrfAdd.getRT());
                nveToVrfOutEdgeOperation.setPermitRTs(vrfAdd.getRT());
                Edge vrfToNveEdge = new Edge(vrfNode, nveNode, new ArrayList<>(), Long.valueOf(0));
                Edge nveToVrfEdge = new Edge(nveNode, vrfNode, new ArrayList<>(), Long.valueOf(0));
                vrfToNveEdge.setOutOperation(vrfToNveOutEdgeOperation);
                vrfToNveEdge.setInOperation(vrfToNveInEdgeOperation);
                nveToVrfEdge.setOutOperation(nveToVrfOutEdgeOperation);
                nveToVrfEdge.setInOperation(nveToVrfInEdgeOperation);
                this._evpnL3vpnGraph.addEdge(vrfToNveEdge);
                this._evpnL3vpnGraph.addEdge(nveToVrfEdge);
                BGP nveAdjacentBgpNode = null;
                for (Edge nveAdjacentEdge : this._evpnL3vpnGraph.getGraph().get(nveNode))
                {
                    Node nveAdjacentNode = nveAdjacentEdge.getDstNode();
                    if (nveAdjacentNode instanceof BGP)
                    {
                        nveAdjacentBgpNode = (BGP) nveAdjacentNode;
                        nveAdjacentEdge.getOutOperation().addPermitVni(vrfAdd.getVni());
                    }
                }
                if (nveAdjacentBgpNode != null)
                {
                    for (List<Message> messageList : nveAdjacentBgpNode.getNodeUpdateMessage().getReceivedMessage().values())
                    {
                        for (Message message : messageList)
                        {
                            Message newMessage = message.toBuilder().build();
                            newMessage.setReason(Reason.ADD);
                            nveAdjacentBgpNode.attributeMessage(newMessage);
                            nveAdjacentBgpNode.getOutMessage().add(newMessage);
                        }
                    }
                }

                nveNode.addStageIsolationUpdatedMessage(nveNode.getIsolationReceivedMessage());
            }
        }
    }

    public void addVPC(TenantAdd tenantAdd, GraphType type)
    {
        if (type.equals(GraphType.EVPNL3VPN))
        {
            for (VRFAdd vrfAdd : tenantAdd.getAddUser())
            {
                String router = vrfAdd.getRouter();
                String vrf = vrfAdd.getVrf();
                NVE nveNode = null;
                for (Node node : this._evpnL3vpnGraph.getNodes())
                {
                    if (node instanceof NVE)
                    {
                        if (node.getRouter().equals(router))
                        {
                            nveNode = (NVE) node;
                            break;
                        }
                    }
                }








                if (nveNode != null)
                {
                    VRF vrfNode = new VRF(this._evpnL3vpnGraph.getIndex(), NodeType.VRF, router, vrf);
                    List<Prefix> networkPrefix = new ArrayList<>();
                    networkPrefix.addAll(vrfAdd.getSubNet());
                    vrfNode.addNetworkPrefix(networkPrefix);
                    vrfNode.initRib(GraphType.EVPNL3VPN);
                    vrfNode.convertOutMessage();
                    vrfNode.reInitRib();
                    vrfNode.initIsolation();
                    this._evpnL3vpnGraph.addNode(vrfNode);
                    EdgeOperation vrfToNveOutEdgeOperation = new EdgeOperation();
                    EdgeOperation nveToVrfOutEdgeOperation = new EdgeOperation();
                    EdgeOperation vrfToNveInEdgeOperation = new EdgeOperation();
                    EdgeOperation nveToVrfInEdgeOperation = new EdgeOperation();
                    vrfToNveOutEdgeOperation.setAttachVni(vrfAdd.getVni());
                    vrfToNveOutEdgeOperation.setAttachRTs(vrfAdd.getRT());
                    nveToVrfOutEdgeOperation.setPermitRTs(vrfAdd.getRT());
                    Edge vrfToNveEdge = new Edge(vrfNode, nveNode, new ArrayList<>(), Long.valueOf(0));
                    Edge nveToVrfEdge = new Edge(nveNode, vrfNode, new ArrayList<>(), Long.valueOf(0));
                    vrfToNveEdge.setOutOperation(vrfToNveOutEdgeOperation);
                    vrfToNveEdge.setInOperation(vrfToNveInEdgeOperation);
                    nveToVrfEdge.setOutOperation(nveToVrfOutEdgeOperation);
                    nveToVrfEdge.setInOperation(nveToVrfInEdgeOperation);
                    this._evpnL3vpnGraph.addEdge(vrfToNveEdge);
                    this._evpnL3vpnGraph.addEdge(nveToVrfEdge);
                    BGP nveAdjacentBgpNode = null;
                    for (Edge nveAdjacentEdge : this._evpnL3vpnGraph.getGraph().get(nveNode))
                    {
                        Node nveAdjacentNode = nveAdjacentEdge.getDstNode();
                        if (nveAdjacentNode instanceof BGP)
                        {
                            nveAdjacentBgpNode = (BGP) nveAdjacentNode;
                            nveAdjacentEdge.getOutOperation().addPermitVni(vrfAdd.getVni());
                        }
                    }
                    if (nveAdjacentBgpNode != null)
                    {
                        for (List<Message> messageList : nveAdjacentBgpNode.getNodeUpdateMessage().getReceivedMessage().values())
                        {
                            for (Message message : messageList)
                            {
                                Message newMessage = message.toBuilder().build();
                                newMessage.setReason(Reason.ADD);
                                nveAdjacentBgpNode.getOutMessage().add(newMessage);
                            }
                        }
                    }
                    nveNode.addStageIsolationUpdatedMessage(nveNode.getIsolationReceivedMessage());
                }
            }
        }
    }

    public void reasoningIsolation(GraphType type)
    {
        if (type.equals(GraphType.EVPNL3VPN))
        {
            this._evpnL3vpnGraph.initIsolationMessage();
            this._evpnL3vpnGraph.reasoningIsolation(type);
        }
    }

    public void incrementalReasoning(VRFAdd vrfAdd)
    {
        addSubNet(vrfAdd, GraphType.EVPNL3VPN);
        reasoningControlPlane(GraphType.EVPNL3VPN, true);
    }

    public void incrementalIsolation(VRFAdd vrfAdd)
    {
        addSubNet(vrfAdd, GraphType.EVPNL3VPN);
        this._evpnL3vpnGraph.reasoningIsolation(GraphType.EVPNL3VPN);
    }
}
