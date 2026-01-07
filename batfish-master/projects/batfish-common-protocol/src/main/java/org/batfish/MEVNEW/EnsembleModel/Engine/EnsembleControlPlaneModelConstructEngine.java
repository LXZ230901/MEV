package org.batfish.MEVNEW.EnsembleModel.Engine;

import com.google.common.graph.Network;
import org.batfish.MEVNEW.EnsembleModel.Dependency;
import org.batfish.MEVNEW.EnsembleModel.Edge.Edge;
import org.batfish.MEVNEW.EnsembleModel.Edge.EdgeOperation;
import org.batfish.MEVNEW.EnsembleModel.Graph.Graph;
import org.batfish.MEVNEW.EnsembleModel.Graph.GraphType;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.AddCustomer;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.AddVPC;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.UpdateRouterVrfPrefixPair;
import org.batfish.MEVNEW.EnsembleModel.Message.ConnectAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.Message;
import org.batfish.MEVNEW.EnsembleModel.Message.MessageType;
import org.batfish.MEVNEW.EnsembleModel.Message.Nexthop;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopIp;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopLocal;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopRouter;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopTunnel;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopType;
import org.batfish.MEVNEW.EnsembleModel.Message.Reason;
import org.batfish.MEVNEW.EnsembleModel.Message.StaticAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.TunnelType;
import org.batfish.MEVNEW.EnsembleModel.Node.BGP;
import org.batfish.MEVNEW.EnsembleModel.Node.EVPN;
import org.batfish.MEVNEW.EnsembleModel.Node.ISIS;
import org.batfish.MEVNEW.EnsembleModel.Node.Node;
import org.batfish.MEVNEW.EnsembleModel.Node.NodeType;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4ToEvpnVrfLeakConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EvpnToBgpv4VrfLeakConfig;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Nve;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.RouteDistinguisher;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;



public class EnsembleControlPlaneModelConstructEngine {























    private Graph _model = new Graph(GraphType.HYBRID);
    private Map<String, Map<String, List<Message>>> _connectedMessages = new HashMap<>();
    private Map<String, Map<String, List<Message>>> _staticMessages = new HashMap<>();
    private List<Graph> _ensembleModel = new ArrayList<>();
    private Set<Graph> _updateModel = new HashSet<>();
    public EnsembleControlPlaneModelConstructEngine() { }

    public List<Graph> getEnsembleModel()
    {
        return this._ensembleModel;
    }

    public Map<String, Map<String, List<Message>>> getConnectMessage()
    {
        return this._connectedMessages;
    }

    public Map<String, Map<String, List<Message>>> getStaticMessage()
    {
        return this._staticMessages;
    }

    public Graph getModel()
    {
        return this._model;
    }

    public void controlPlaneConstructor(Map<String, Configuration> configurations, IsisTopology isisTopology, Topology L3Topology)
    {
        Graph model = new Graph(GraphType.HYBRID);
        int nodeId = 0;
        HashMap<Ip, List<EvpnNeighborTriple>> unConstructedBgpEvpnEdges = new HashMap<>();
        HashMap<Ip, List<BgpNeighborTriple>> unConstructedBgpEdges = new HashMap<>();
        HashMap<IsisNode, HashMap<IsisNode, IsisNeighborTriple>> unConstructedIsisEdges = new HashMap<>();
        Map<String, org.batfish.datamodel.Edge> interfaceToEdgeList = L3Topology.computeInterfaceEdges();
        Map<String, Map<String, List<Message>>> connectMessages = new HashMap<>();
        Map<String, Map<String, List<Message>>> staticMessages = new HashMap<>();
        Network<IsisNode, IsisEdge> isisNetwork = isisTopology.getNetwork();
        //Construct Graph
        for (String routerName : configurations.keySet())
        {
            Configuration routerConfig = configurations.get(routerName);
            List<Ip> hostIpList = new ArrayList<>();

            HashMap<String, List<Message>> connectMessageList = new HashMap<>();
            HashMap<String, List<Message>> staticMessageList = new HashMap<>();
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
                    String nexthopInter = null;
                    if (interfaceToEdgeList.containsKey(routerName + "-" + inter.getName()))
                    {
                        nexthopRouter = interfaceToEdgeList.get(routerName + "-" + inter.getName()).getNode2();
                        nexthopInter = interfaceToEdgeList.get(routerName + "-" + inter.getName()).getHead().getInterface();
                    }
                    if (nexthopRouter == null)
                    {
                        Prefix prefix = inter.getConcreteAddress().getPrefix();
                        Message message = new Message(prefix, new ConnectAttribute(0), new ArrayList<>(), new ArrayList<>(), null, new NexthopLocal(NexthopType.Local, routerName), MessageType.Connected, new ArrayList<>(), Reason.ADD, false);
                        message.setReason(Reason.ADD);
                        connectMessageList.get(vrfName).add(message);
                    } else {
                        Vrf vrf = configurations.get(nexthopRouter).getAllInterfaces().get(nexthopInter).getVrf();
                        Integer vni = 0;
                        if (!vrf.getLayer3Vnis().keySet().isEmpty())
                        {
                            vni = vrf.getLayer3Vnis().keySet().iterator().next();
                        }
                        Prefix prefix = inter.getConcreteAddress().getPrefix();
                        Message message = new Message(prefix, new ConnectAttribute(0), new ArrayList<>(), new ArrayList<>(), null, new NexthopRouter(NexthopType.Router, nexthopRouter, vni), MessageType.Connected, new ArrayList<>(), Reason.ADD, false);
                        message.setReason(Reason.ADD);
                        connectMessageList.get(vrfName).add(message);
                    }
                }
            }


            for (Vrf vrf : routerConfig.getVrfs().values()) {
                // Skip management VRF
                if ("management".equals(vrf.getName())) {
                    continue;
                }
                if (!staticMessageList.containsKey(vrf.getName()))
                {
                    staticMessageList.put(vrf.getName(), new ArrayList<>());
                }
                for (StaticRoute staticRoute : vrf.getStaticRoutes()) {
                    if (staticRoute.getNextHop() instanceof org.batfish.datamodel.route.nh.NextHopIp) {

                        Message message =
                                new Message(
                                        staticRoute.getNetwork(),
                                        new StaticAttribute(1),
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        null,
                                        new NexthopIp(
                                                NexthopType.Original,
                                                staticRoute.getNextHopIp(),
                                                0),
                                        MessageType.Static,
                                        new ArrayList<>(),
                                        Reason.ADD,
                                        false);

                        message.setReason(Reason.ADD);
                        staticMessageList
                                .get(vrf.getName())
                                .add(message);
                    }
                }
            }

            String hostName = routerConfig.getHostname();
            HashMap<String, EVPN> localBgpEvpnNodes = new HashMap<>();
            HashMap<String, BGP> localBgpNodes = new HashMap<>();

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

                long localAsNumber = bgpProcess.getLocalAs();

                BGP localBgpNode = null;
                EVPN localBgpEvpnNode = null;

                BGP bgpNode = new BGP(nodeId++, NodeType.BGP, routerName, "BGP", vrfName, localAsNumber);
                if (!vrf.getBgpProcess().getNetworkPrefixs().isEmpty())
                {
                    bgpNode.setAnnouncePrefix(vrf.getBgpProcess().getNetworkPrefixs().get(AddressFamily.Type.IPV4_UNICAST));
                }
                model.addNode(bgpNode);
                localBgpNodes.put(vrfName, bgpNode);
                localBgpNode = bgpNode;

                for (Ip neighborIP : bgpProcess.getActiveNeighbors().keySet())
                {
                    BgpActivePeerConfig neighborBgpConfig = bgpProcess.getActiveNeighbors().get(neighborIP);
                    if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN) != null)
                    {
                        if (localBgpEvpnNode == null)
                        {
                            EVPN evpnNode = new EVPN(nodeId++, NodeType.EVPN, routerName, "EVPN", vrfName, localAsNumber);
                            model.addNode(evpnNode);
                            localBgpEvpnNodes.put(vrfName, evpnNode);
                            localBgpEvpnNode = evpnNode;
                        }
                        long reflectorId = neighborBgpConfig.getClusterId();
                        localBgpEvpnNode.setReflectorId(reflectorId);
                        boolean reflector = neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getRouteReflectorClient();
                        long neighborAs = neighborBgpConfig.getRemoteAsns().least();

                        boolean hasConstructed = false;
                        for (Ip hostIp : hostIpList)
                        {
                            if (unConstructedBgpEvpnEdges.containsKey(hostIp))
                            {
                                for (EvpnNeighborTriple neighborTriple : unConstructedBgpEvpnEdges.get(hostIp))
                                {
                                    if (neighborTriple.getLocalIpList().contains(neighborIP))
                                    {
                                        Dependency neighborToLocalDep = new Dependency(neighborTriple.getNeighborRouter(), neighborTriple.getNeighborVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
                                        Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getNeighborRouter(), neighborTriple.getNeighborVrf(), neighborIP);
                                        List<Dependency> deps = new ArrayList<>();
                                        deps.add(neighborToLocalDep);
                                        deps.add(localToNeighborDep);

                                        EVPN neighborNode = neighborTriple.getNeighborEvpnNode();
                                        Edge neighborToLocalEdge = new Edge(neighborNode, localBgpEvpnNode, deps, Long.valueOf(0));
                                        Edge localToNeighborEdge = new Edge(localBgpEvpnNode, neighborNode, deps, Long.valueOf(0));


                                        if (neighborTriple.getReflector())
                                        {
                                            neighborNode.addReflectorNeighbor(localBgpEvpnNode);
                                        }

                                        if (reflector)
                                        {
                                            localBgpEvpnNode.addReflectorNeighbor(neighborNode);
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

                                        if (localAsNumber == neighborAs)
                                        {
                                            outOperation.addBlockMessageType(MessageType.iBGP);
                                            inOperation.reSetIBGP();
                                            outOperation.setReflector(reflector);
                                        } else
                                        {
                                            inOperation.reSetEBGP();
                                        }

                                        neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
                                        neighborToLocalEdge.setInOperation(inOperation);
                                        localToNeighborEdge.setOutOperation(outOperation);
                                        localToNeighborEdge.setInOperation(neighborTriple.getInOperation());

                                        model.addEdge(neighborToLocalEdge);
                                        model.addEdge(localToNeighborEdge);
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
                            EvpnNeighborTriple neighborTriple = new EvpnNeighborTriple(hostName, vrfName, neighborIP, localBgpEvpnNode);
                            neighborTriple.setLocalIpList(hostIpList);
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
                            if (localAsNumber == neighborAs)
                            {
                                outOperation.addBlockMessageType(MessageType.iBGP);
                                inOperation.reSetIBGP();
                                outOperation.setReflector(reflector);
                            } else {
                                inOperation.reSetEBGP();
                            }
                            neighborTriple.setOutOperation(outOperation);
                            neighborTriple.setInOperation(inOperation);
                            if (!unConstructedBgpEvpnEdges.containsKey(neighborIP))
                            {
                                unConstructedBgpEvpnEdges.put(neighborIP, new ArrayList<>());
                            }
                            unConstructedBgpEvpnEdges.get(neighborIP).add(neighborTriple);
                        }
                    }
                    if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST) != null)
                    {
                        Long reflectorId = neighborBgpConfig.getClusterId();
                        localBgpNode.setReflectorId(reflectorId);
                        long localAs = neighborBgpConfig.getLocalAs();
                        long neighborAs = neighborBgpConfig.getRemoteAsns().least();
                        boolean reflector = neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getRouteReflectorClient();

                        boolean hasConstructed = false;
                        Integer vni = 0;
                        if (!vrf.getLayer3Vnis().keySet().isEmpty())
                        {
                            vni = vrf.getLayer3Vnis().keySet().iterator().next();
                        }
                        for (Ip hostIp : hostIpList)
                        {
                            if (unConstructedBgpEdges.containsKey(hostIp))
                            {
                                for (BgpNeighborTriple neighborTriple : unConstructedBgpEdges.get(hostIp))
                                {
                                    if (neighborTriple.getLocalIpList().contains(neighborIP))
                                    {
                                        Dependency neighborToLocalDep = new Dependency(neighborTriple.getNeighborRouter(), neighborTriple.getNeighborVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
                                        Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getNeighborRouter(), neighborTriple.getNeighborVrf(), neighborIP);
                                        List<Dependency> deps = new ArrayList<>();
                                        deps.add(neighborToLocalDep);
                                        deps.add(localToNeighborDep);

                                        BGP neighborNode = neighborTriple.getNeighborBgpNode();
                                        Edge neighborToLocalEdge = new Edge(neighborNode, localBgpNode, deps, Long.valueOf(0));
                                        Edge localToNeighborEdge = new Edge(localBgpNode, neighborNode, deps, Long.valueOf(0));

                                        EdgeOperation outOperation = new EdgeOperation();
                                        EdgeOperation inOperation = new EdgeOperation();

                                        RoutingPolicy outRoutingPolicy = null;
                                        RoutingPolicy inRoutingPolicy = null;

                                        if (reflector)
                                        {
                                            localBgpNode.addReflectorNeighbor(neighborNode);
                                        }


                                        outOperation.setAttachVni(vni);

                                        if (neighborTriple.getReflector())
                                        {
                                            neighborNode.addReflectorNeighbor(localBgpNode);
                                        }

                                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicySources().isEmpty()) {
                                            outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicy());
                                            outOperation.setPolicies(outRoutingPolicy);
                                        }

                                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicySources().isEmpty()) {
                                            inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicy());
                                            inOperation.setPolicies(inRoutingPolicy);
                                        }

                                        outOperation.setNexthop(new NexthopIp(NexthopType.Original, neighborTriple.getNeighborIp(), vni));
                                        neighborTriple.getOutOperation().setNexthop(new NexthopIp(NexthopType.Original, neighborIP, neighborTriple.getVni()));

                                        if (localAs == neighborAs)
                                        {
                                            outOperation.addBlockMessageType(MessageType.iBGP);
                                            inOperation.reSetIBGP();
                                            outOperation.setReflector(reflector);
                                        } else {
                                            inOperation.reSetEBGP();
                                        }

                                        neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
                                        neighborToLocalEdge.setInOperation(inOperation);
                                        localToNeighborEdge.setOutOperation(outOperation);
                                        localToNeighborEdge.setInOperation(neighborTriple.getInOperation());

                                        model.addEdge(neighborToLocalEdge);
                                        model.addEdge(localToNeighborEdge);
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
                            BgpNeighborTriple neighborTriple = new BgpNeighborTriple(hostName, vrfName, neighborIP, localBgpNode);
                            neighborTriple.setLocalIpList(hostIpList);
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
                                outOperation.setReflector(reflector);
                            } else {
                                inOperation.reSetEBGP();
                            }
                            neighborTriple.setOutOperation(outOperation);
                            neighborTriple.setInOperation(inOperation);
                            neighborTriple.setVni(vni);
                            if (!unConstructedBgpEdges.containsKey(neighborIP))
                            {
                                unConstructedBgpEdges.put(neighborIP, new ArrayList<>());
                            }
                            unConstructedBgpEdges.get(neighborIP).add(neighborTriple);
                        }
                    }
                }


            }

            HashMap<Integer, Nve> vrfAssociateWithNves = new HashMap<>();
            for (Nve nve : routerConfig.getNves().values())
            {
                nve.getMemberVnis().forEach(
                        (vniNumber, vniConfig) ->{
                            if(vniConfig.isAssociateVrf())
                            {
                                vrfAssociateWithNves.put(vniNumber, nve);
                            } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.BGP)){
//                                L2VPN to implement
                            } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.STATIC))
                            {
//                                Static to implement
                            }
                        }
                );
            }

            for (Vrf vrf : routerConfig.getVrfs().values())
            {
                String vrfName = vrf.getName();
                if(vrf.getVrfLeakConfig() != null && vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs() != null)
                {

                    for (Bgpv4ToEvpnVrfLeakConfig bgpv4ToEvpnVrfLeakConfig : vrf.getVrfLeakConfig().getBgpv4ToEvpnVrfLeakConfigs())
                    {
                        Set<ExtendedCommunity> attachCommunity = new HashSet<>();
                        attachCommunity.addAll(bgpv4ToEvpnVrfLeakConfig.getAttachRouteTargets());
                        RouteDistinguisher rd =bgpv4ToEvpnVrfLeakConfig.getSrcVrfRouteDistinguisher();
                        for (Integer vni : vrf.getLayer3Vnis().keySet())
                        {
                            if (!vrfAssociateWithNves.containsKey(vni))
                            {
                                System.out.println("Dont have conrresponding NVEs");
                                continue;
                            }
                            Nve nve = vrfAssociateWithNves.get(vni);
                            Ip encapIp = getInterfaceIp(routerConfig.getAllInterfaces(),nve.getSourceInterface());
                            NexthopTunnel nexthopTunnel = new NexthopTunnel(NexthopType.Encap, TunnelType.VXLAN, encapIp, routerName, vni);

                            Layer3Vni vniConfig = vrf.getLayer3Vnis().get(vni);

                            if (vniConfig.getSourceAddress().equals(encapIp))
                            {
                                EVPN localEvpnNode = localBgpEvpnNodes.get("default");
                                BGP localBgpNode = localBgpNodes.get(vrfName);
                                Edge bgpToEvpnEdge = new Edge(localBgpNode, localEvpnNode, new ArrayList<>(), Long.valueOf(0));
                                EdgeOperation bgpToEvpnOperation = new EdgeOperation();
                                bgpToEvpnOperation.setAttachVni(vni);
                                bgpToEvpnOperation.setAttachRD(rd);
                                bgpToEvpnOperation.setAttachRTs(attachCommunity);
                                bgpToEvpnOperation.setNexthop(nexthopTunnel);
                                bgpToEvpnEdge.setOutOperation(bgpToEvpnOperation);
                                model.addEdge(bgpToEvpnEdge);
                            }
                        }
                    }
                    for (EvpnToBgpv4VrfLeakConfig evpnToBgpv4VrfLeakConfig : vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs())
                    {
                        Set<ExtendedCommunity> blockCommunity = new HashSet<>();
                        blockCommunity.addAll(evpnToBgpv4VrfLeakConfig.getAttachRouteTargets());

                        for (Integer vni : vrf.getLayer3Vnis().keySet())
                        {
                            if (!vrfAssociateWithNves.containsKey(vni))
                            {
                                System.out.println("Dont have corresponding NVEs");
                                continue;
                            }
                            Nve nve = vrfAssociateWithNves.get(vni);
                            Ip encapIp = getInterfaceIp(routerConfig.getAllInterfaces(),nve.getSourceInterface());

                            Layer3Vni vniConfig = vrf.getLayer3Vnis().get(vni);

                            if (vniConfig.getSourceAddress().equals(encapIp))
                            {
                                EVPN localEvpnNode = localBgpEvpnNodes.get("default");
                                BGP localBgpNode = localBgpNodes.get(vrfName);
                                Edge evpnToBgpEdge = new Edge(localEvpnNode, localBgpNode, new ArrayList<>(), Long.valueOf(0));
                                EdgeOperation evpnToBgpOperation = new EdgeOperation();
                                evpnToBgpOperation.setPermitRTs(blockCommunity);
                                evpnToBgpEdge.setOutOperation(evpnToBgpOperation);
                                model.addEdge(evpnToBgpEdge);
                            }
                        }
                    }
                }
                if (vrf.getIsisProcess() != null)
                {
                    List<Prefix> announcePrefix = new ArrayList<>();
                    Map<String, Interface> routerInterfaceMap = routerConfig.getAllInterfaces(vrfName);
                    ISIS constructedIsisNode = new ISIS(nodeId++, NodeType.ISIS, hostName, "ISIS", vrfName);
                    constructedIsisNode.setAnnouncePrefix(announcePrefix);
                    model.addNode(constructedIsisNode);
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
                            NexthopIp nexthop = new NexthopIp(NexthopType.Original, nexthopIp, 0);
                            EdgeOperation outOperation = new EdgeOperation();
                            outOperation.setNexthop(nexthop);
                            if (unConstructedIsisEdges.containsKey(adjacentIsisNode) && unConstructedIsisEdges.get(adjacentIsisNode).containsKey(isisNode))
                            {
                                IsisNeighborTriple isisNeighborTriple = unConstructedIsisEdges.get(adjacentIsisNode).get(isisNode);
                                ISIS remoteConstructedIsisNode = isisNeighborTriple.getIsisNeighborNode();
                                EdgeOperation remoteOutOperation = isisNeighborTriple.getOutOperation();
                                IsisLevel remoteIsisType = isisNeighborTriple.getIsisLevel();
                                Long remoteIsisCost = isisNeighborTriple.getIsisCost();
                                Edge localToNeighborEdge = new Edge(constructedIsisNode, remoteConstructedIsisNode, new ArrayList<>(), isisCost);
                                Edge neighborToLocalEdge = new Edge(remoteConstructedIsisNode, constructedIsisNode, new ArrayList<>(), remoteIsisCost);
                                localToNeighborEdge.setIsisType(isisType);
                                localToNeighborEdge.setOutOperation(outOperation);
                                neighborToLocalEdge.setIsisType(remoteIsisType);
                                neighborToLocalEdge.setOutOperation(remoteOutOperation);

                                model.addEdge(localToNeighborEdge);
                                model.addEdge(neighborToLocalEdge);
                            } else {
                                IsisNeighborTriple isisNeighborTriple = new IsisNeighborTriple(hostName, vrfName, constructedIsisNode, isisType, isisCost);
                                isisNeighborTriple.setOutOperation(outOperation);
                                unConstructedIsisEdges.get(isisNode).put(adjacentIsisNode, isisNeighborTriple);
                            }
                        }
                    }
                    constructedIsisNode.setAnnouncePrefix(announcePrefix);
                }
            }
            if (!connectMessages.containsKey(routerName))
            {
                connectMessages.put(routerName, connectMessageList);
            }
            if (!staticMessages.containsKey(routerName))
            {
                staticMessages.put(routerName, staticMessageList);
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
        _model = model;
        _connectedMessages = connectMessages;
        _staticMessages = staticMessages;
    }

    private @Nullable Ip getInterfaceIp(
            Map<String, Interface> interfaces, String ifaceName) {
        Interface iface = interfaces.get(ifaceName);
        if (iface == null) {
            return null;
        }
        ConcreteInterfaceAddress concreteInterfaceAddress = iface.getConcreteAddress();
        if (concreteInterfaceAddress == null) {
            return null;
        }
        return concreteInterfaceAddress.getIp();
    }

    public void clearGraph()
    {
        this._model.clearGraph();
    }

    public void controlPlaneReasoning()
    {
        _ensembleModel
//                .parallelStream()
                .forEach(
                        graph -> {
                            graph.reasoningControlPlane(false);
                        }
                );
    }

    public void splitModel()
    {
        HashSet<Node> visitedNodes = new HashSet<>();
        for (Node node : _model.getNodes())
        {
            Graph currentGraph = new Graph(GraphType.HYBRID);
            if (visitedNodes.contains(node))
            {
                continue;
            }
            Queue<Node> currentNodes = new LinkedList<>();
            currentNodes.add(node);
            currentGraph.addNode(node);
            while (!currentNodes.isEmpty())
            {
                Node currentNode = currentNodes.poll();
                visitedNodes.add(currentNode);
                for (Edge edge : _model.getInGraph().get(currentNode))
                {
                    if (edge.getSrcNode().equals(currentNode))
                    {
                        if (! visitedNodes.contains(edge.getDstNode()))
                        {
                            currentNodes.add(edge.getDstNode());
                        }
                        currentGraph.addNode(edge.getDstNode());
                        currentGraph.addEdge(edge);
                    }

                    if (edge.getDstNode().equals(currentNode))
                    {
                        if (! visitedNodes.contains(edge.getSrcNode()))
                        {
                            currentNodes.add(edge.getSrcNode());
                        }
                        currentGraph.addNode(edge.getSrcNode());
                        currentGraph.addEdge(edge);
                    }
                }
            }
            _ensembleModel.add(currentGraph);
        }
        for (Graph graph : _ensembleModel)
        {
            for (Node node : graph.getNodes())
            {
                if (node.getNodeType().equals(NodeType.BGP) || node.getNodeType().equals(NodeType.EVPN))
                {
                    graph.setGraphType(GraphType.HYBRID);
                    break;
                } else if (node.getNodeType().equals(NodeType.ISIS))
                {
                    graph.setGraphType(GraphType.ISIS);
                    break;
                }
            }
        }
    }

    public void incrementalEnsembleModelUnderVPCAdd(AddVPC addVPC) {
        Set<Graph> updateModel = new HashSet<>();
        for (AddCustomer addCustomer : addVPC.getAddCustomers()) {
            Integer id = _model.getNodes().size();
            BGP addNode = new BGP(id, NodeType.BGP, addCustomer.getAddRouter(), "BGP", addCustomer.getAddVrf(), addCustomer.getAs());
            _model.addNode(addNode);

            List<Prefix> announcePrefix = new ArrayList<>();
            announcePrefix.add(addCustomer.getAddPrefix());
            addNode.setAnnouncePrefix(announcePrefix);

            addNode.getVrfrib().initRib(NodeType.BGP, id, addNode.getAnnouncePrefix());
            addNode.getVrfrib().computeOutMessage();

            Node connectNode = null;
            for (Node node : _model.getNodes()) {
                if (node.getRouter().equals(addCustomer.getAddRouter()) && node.getNodeType().equals(NodeType.EVPN)) {
                    connectNode = node;
                    break;
                }
            }
            if (connectNode != null) {
                Graph connectSubGraph = null;
                for (Graph subGraph : _ensembleModel) {
                    if (subGraph.getNodes().contains(connectNode)) {
                        connectSubGraph = subGraph;
                        break;
                    }
                }
                Nexthop setNexthop = null;
                for (Edge inEdge : connectSubGraph.getInGraph().get(connectNode))
                {
                    if (inEdge.getOutOperation().getNexthop() != null)
                    {
                        setNexthop = inEdge.getOutOperation().getNexthop().toBuilder().build();
                        break;
                    }
                }
                NexthopTunnel setNexthopTunnel = (NexthopTunnel) setNexthop;
                setNexthopTunnel.setVni(addCustomer.getAddVni());
                Set<ExtendedCommunity> attachRT = new HashSet<>();
                attachRT.add(addCustomer.getAddRT());
                Integer attachVni = addCustomer.getAddVni();
                RouteDistinguisher rd = addCustomer.getRd();
                EdgeOperation addToConnectOutOperation = new EdgeOperation();
                EdgeOperation addToConnectInOperation = new EdgeOperation();
                EdgeOperation connectToAddOutOperation = new EdgeOperation();
                EdgeOperation connectToAddInOperation = new EdgeOperation();
                addToConnectOutOperation.setNexthop(setNexthopTunnel);
                addToConnectOutOperation.setAttachRTs(attachRT);
                addToConnectInOperation.setAttachRD(rd);
                addToConnectOutOperation.setAttachVni(attachVni);
                connectToAddInOperation.setPermitRTs(attachRT);

                Edge addNodeToConnect = new Edge(addNode, connectNode, new ArrayList<>(), Long.valueOf(0));
                Edge connectNodeToAdd = new Edge(connectNode, addNode, new ArrayList<>(), Long.valueOf(0));
                addNodeToConnect.setInOperation(addToConnectInOperation);
                addNodeToConnect.setOutOperation(addToConnectOutOperation);
                connectNodeToAdd.setInOperation(connectToAddInOperation);
                connectNodeToAdd.setOutOperation(connectToAddOutOperation);

                _model.addEdge(addNodeToConnect);
                _model.addEdge(connectNodeToAdd);
                connectSubGraph.addNode(addNode);
                connectSubGraph.addEdge(connectNodeToAdd);
                connectSubGraph.addEdge(addNodeToConnect);

                updateModel.add(connectSubGraph);
            } else {
                System.out.println("Adding customer network is error!");
            }
        }
        _updateModel = updateModel;
    }

    public void incrementalControlPlaneReasoning()
    {
        _updateModel
//                .parallelStream()
                .forEach(
                        graph -> {
                            graph.reasoningControlPlane(true);
                        }
                );
    }

    public Map<String, Map<String, UpdateRouterVrfPrefixPair>> computeUpdateRouterVrfPrefixPair()
    {
        Map<String, Map<String, UpdateRouterVrfPrefixPair>> updateRouterVrfPrefixPairHashMap = new HashMap<>();

        for (Graph model : _updateModel)
        {
            for (Node node : model.getNodes())
            {
                if (node.getVrfrib().getIncrementalTag())
                {
                    UpdateRouterVrfPrefixPair updateRouterVrfPrefixPair = new UpdateRouterVrfPrefixPair(node.getRouter(), node.getVrf());
                    if (!updateRouterVrfPrefixPairHashMap.containsKey(node.getRouter()))
                    {
                        updateRouterVrfPrefixPairHashMap.put(node.getRouter(), new HashMap<>());
                    }
                    if (!updateRouterVrfPrefixPairHashMap.get(node.getRouter()).containsKey(node.getVrf()))
                    {
                        updateRouterVrfPrefixPairHashMap.get(node.getRouter()).put(node.getVrf(), updateRouterVrfPrefixPair);
                    }
                    updateRouterVrfPrefixPairHashMap.get(node.getRouter()).get(node.getVrf()).addUpdatePrefixes(node.getVrfrib().getIncrementalPrefix());
                }
            }
        }
        return updateRouterVrfPrefixPairHashMap;
    }
}