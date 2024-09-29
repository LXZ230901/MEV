package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.MEV.ControlPlaneGraphModelConstructEngine;
import org.batfish.MEV.DestinationPair;
import org.batfish.MEV.GraphType;
import org.batfish.MEV.Property;
import org.batfish.MEV.PropertyVerificationEngine;
import org.batfish.MEV.RouterVrfPair;
import org.batfish.MEV.VRFAdd;
import org.batfish.common.BdpOscillationException;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.HybridL3Adjacencies;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.topology.TunnelTopology;
import org.batfish.common.topology.broadcast.BroadcastL3Adjacencies;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.IncrementalBdpAnswerElement;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.eigrp.EigrpTopologyUtils;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.tracking.GenericTrackMethodVisitor;
import org.batfish.datamodel.tracking.NegatedTrackMethod;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackAll;
import org.batfish.datamodel.tracking.TrackInterface;
import org.batfish.datamodel.tracking.TrackMethodReference;
import org.batfish.datamodel.tracking.TrackReachability;
import org.batfish.datamodel.tracking.TrackRoute;
import org.batfish.datamodel.tracking.TrackTrue;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.batfish.dataplane.ibdp.DataplaneTrackEvaluator.DataPlaneTrackMethodEvaluatorProvider;
import org.batfish.dataplane.ibdp.TrackRouteUtils.GetRoutesForPrefix;
import org.batfish.dataplane.ibdp.schedule.IbdpSchedule;
import org.batfish.dataplane.ibdp.schedule.IbdpSchedule.Schedule;
import org.batfish.dataplane.rib.RibDelta;
import org.batfish.version.BatfishVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.batfish.common.topology.TopologyUtil.computeLayer2Topology;
import static org.batfish.common.topology.TopologyUtil.computeLayer3Topology;
import static org.batfish.common.topology.TopologyUtil.computeRawLayer3Topology;
import static org.batfish.common.topology.TopologyUtil.pruneUnreachableTunnelEdges;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.common.util.IpsecUtil.retainReachableIpsecEdges;
import static org.batfish.common.util.IpsecUtil.toEdgeSet;
import static org.batfish.common.util.StreamUtil.toListInRandomOrder;
import static org.batfish.datamodel.bgp.BgpTopologyUtils.initBgpTopology;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.computeNextVxlanTopologyModuloReachability;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.prunedVxlanTopology;
import static org.batfish.datamodel.vxlan.VxlanTopologyUtils.vxlanTopologyToLayer3Edges;
import static org.batfish.dataplane.ibdp.TrackReachabilityUtils.evaluateTrackReachability;
import static org.batfish.dataplane.rib.AbstractRib.importRib;

/** Computes the entire dataplane by executing a fixed-point computation. */
final class IncrementalBdpEngine {

  private static final Logger LOGGER = LogManager.getLogger(IncrementalBdpEngine.class);

  /**
   * Maximum amount of topology iterations to do before deciding that the dataplane computation
   * cannot converge (there is some sort of flap)
   */
  private static final int MAX_TOPOLOGY_ITERATIONS = 10;

  private int _numIterations;
  private final IncrementalDataPlaneSettings _settings;

  IncrementalBdpEngine(IncrementalDataPlaneSettings settings) {
    _settings = settings;
  }

  /**
   * Returns the {@link PartialDataplane} corresponding to the given topology and nodes. FIBs,
   * ForwardingAnalysis, and other internals are recomputed based on the updated state in the {@code
   * nodes} and {@code vrs}.
   */
  private PartialDataplane nextDataplane(
      TopologyContext currentTopologyContext,
      SortedMap<String, Node> nodes,
      List<VirtualRouter> vrs,
      IpOwners currentIpOwners) {
    LOGGER.info("Updating dataplane");
    computeFibs(vrs);

    return PartialDataplane.builder()
        .setNodes(nodes)
        .setIpOwners(currentIpOwners)
        .setLayer3Topology(currentTopologyContext.getLayer3Topology())
        .setL3Adjacencies(currentTopologyContext.getL3Adjacencies())
        .build();
  }

  /**
   * Performs the iterative step in dataplane computations as topology changes.
   *
   * <p>The {@code currentTopologyContext} contains the connectivity learned so far in the network,
   * specifically for things like VXLAN, BGP, and others, and {@code nodes} contains the current
   * routing and forwarding tables.
   *
   * <p>Given these inputs, primarily the current Layer3 topology, the possible edges for each other
   * topology (obtained from {@code initialTopologyContext}) are pruned down based on which sessions
   * can be established given the current L3 topology and dataplane state. The resulting {@code
   * TopologyContext} for the next iteration of dataplane is returned.
   */
  private static TopologyContext nextTopologyContext(
      TopologyContext currentTopologyContext,
      PartialDataplane currentDataplane,
      TopologyContext initialTopologyContext,
      NetworkConfigurations networkConfigurations,
      Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    // Update topologies
    LOGGER.info("Updating dynamic topologies");

    Map<String, Configuration> configurations = networkConfigurations.getMap();
    TracerouteEngine trEngCurrentL3Topology =
        new TracerouteEngineImpl(
            currentDataplane, currentTopologyContext.getLayer3Topology(), configurations);

    // IPsec
    LOGGER.info("Updating IPsec topology");
    // Note: this uses the initial context since it is pruning down the potential edges initially
    // established.
    IpsecTopology newIpsecTopology =
        retainReachableIpsecEdges(
            initialTopologyContext.getIpsecTopology(), configurations, trEngCurrentL3Topology);

    // VXLAN
    LOGGER.info("Updating VXLAN topology");
    VxlanTopology newVxlanTopology =
        prunedVxlanTopology(
            computeNextVxlanTopologyModuloReachability(
                currentDataplane.getLayer2Vnis(), currentDataplane.getLayer3Vnis()),
            configurations,
            trEngCurrentL3Topology);

    // Tunnel topology
    LOGGER.info("Updating Tunnel topology");
    TunnelTopology newTunnelTopology =
        pruneUnreachableTunnelEdges(
            initialTopologyContext.getTunnelTopology(), // like IPsec, pruning initial tunnels
            networkConfigurations,
            trEngCurrentL3Topology);

    // EIGRP topology
    LOGGER.info("Updating EIGRP topology");
    EigrpTopology newEigrpTopology =
        EigrpTopologyUtils.initEigrpTopology(
            configurations, currentTopologyContext.getLayer3Topology());

    // Initialize BGP topology
    LOGGER.info("Updating BGP topology");
    BgpTopology newBgpTopology =
        initBgpTopology(
            configurations,
            ipVrfOwners,
            false,
            true,
            trEngCurrentL3Topology,
            currentDataplane.getFibs(),
            currentTopologyContext.getL3Adjacencies());

    // Update L3 adjacencies if necessary.
    L3Adjacencies newAdjacencies;
    if (!currentTopologyContext
        .getVxlanTopology()
        .getLayer2VniEdges()
        .collect(ImmutableSet.toImmutableSet())
        .equals(newVxlanTopology.getLayer2VniEdges().collect(ImmutableSet.toImmutableSet()))) {
      LOGGER.info("Updating Layer 3 adjacencies");
      if (L3Adjacencies.USE_NEW_METHOD) {
        newAdjacencies =
            BroadcastL3Adjacencies.create(
                initialTopologyContext.getLayer1Topologies(), newVxlanTopology, configurations);
      } else {
        Layer1Topologies topologies = initialTopologyContext.getLayer1Topologies();
        if (topologies.getCombinedL1().isEmpty()) {
          newAdjacencies = GlobalBroadcastNoPointToPoint.instance();
        } else {
          Layer2Topology l2 =
              computeLayer2Topology(
                  topologies.getActiveLogicalL1(), newVxlanTopology, configurations);
          newAdjacencies = HybridL3Adjacencies.create(topologies, l2, configurations);
        }
      }
    } else {
      newAdjacencies = currentTopologyContext.getL3Adjacencies();
    }

    // Layer-3
    Topology newLayer3Topology;
    if (!newIpsecTopology.equals(currentTopologyContext.getIpsecTopology())
        || !newTunnelTopology.equals(currentTopologyContext.getTunnelTopology())
        || !newAdjacencies.equals(currentTopologyContext.getL3Adjacencies())
        || !newVxlanTopology
            .getLayer3VniEdges()
            .collect(ImmutableSet.toImmutableSet())
            .equals(
                currentTopologyContext
                    .getVxlanTopology()
                    .getLayer3VniEdges()
                    .collect(ImmutableSet.toImmutableSet()))) {
      LOGGER.info("Updating Layer 3 topology");
      newLayer3Topology =
          computeLayer3Topology(
              computeRawLayer3Topology(newAdjacencies, configurations),
              // Overlay edges consist of "plain" tunnels and IPSec tunnels
              ImmutableSet.<Edge>builder()
                  .addAll(toEdgeSet(newIpsecTopology, configurations))
                  .addAll(newTunnelTopology.asEdgeSet())
                  .addAll(vxlanTopologyToLayer3Edges(newVxlanTopology, configurations))
                  .build());
    } else {
      newLayer3Topology = currentTopologyContext.getLayer3Topology();
    }

    return currentTopologyContext.toBuilder()
        .setBgpTopology(newBgpTopology)
        .setLayer3Topology(newLayer3Topology)
        .setL3Adjacencies(newAdjacencies)
        .setVxlanTopology(newVxlanTopology)
        .setIpsecTopology(newIpsecTopology)
        .setTunnelTopology(newTunnelTopology)
        .setEigrpTopology(newEigrpTopology)
        .build();
  }

  /** Helper method used to sample the change in tracks across iterations. */
  @VisibleForTesting
  static <T> @Nonnull Optional<String> compareTracks(
      Table<String, T, Boolean> current, Table<String, T, Boolean> next) {
    if (current.equals(next)) {
      return Optional.empty();
    }
    Set<String> currentTrue =
        current.cellSet().stream()
            .filter(Cell::getValue)
            .map(c -> String.format("%s > %s", c.getRowKey(), c.getColumnKey()))
            .collect(toSet());
    Set<String> nextTrue =
        next.cellSet().stream()
            .filter(Cell::getValue)
            .map(c -> String.format("%s > %s", c.getRowKey(), c.getColumnKey()))
            .collect(toSet());
    List<String> gained = ImmutableList.copyOf(Sets.difference(nextTrue, currentTrue));
    List<String> lost = ImmutableList.copyOf(Sets.difference(currentTrue, nextTrue));
    if (gained.isEmpty()) {
      return Optional.ofNullable(
          String.format(
              "lost %d including %s", lost.size(), lost.size() > 3 ? lost.subList(0, 3) : lost));
    } else if (lost.isEmpty()) {
      return Optional.ofNullable(
          String.format(
              "gained %d including %s",
              gained.size(), gained.size() > 3 ? gained.subList(0, 3) : gained));
    }
    return Optional.ofNullable(
        String.format(
            "gained %d including %s, lost %d including %s",
            gained.size(),
            gained.size() > 3 ? gained.subList(0, 3) : gained,
            lost.size(),
            lost.size() > 3 ? lost.subList(0, 3) : lost));
  }

  ComputeDataPlaneResult computeDataPlane(
      Map<String, Configuration> configurations,
      TopologyContext initialTopologyContext,
      Set<BgpAdvertisement> externalAdverts,
      IpOwners initialIpOwners) {
    LOGGER.info("Computing Data Plane using iBDP");

    Map<Ip, Map<String, Set<String>>> initialIpVrfOwners = initialIpOwners.getIpVrfOwners();

    // Generate our nodes, keyed by name, sorted for determinism
    String a = "(R1,VRF1,R2,VRF2)";
    int hashCode = a.hashCode();
    System.out.println("Hash Code: " + hashCode % 100);
    String b = "(R1,DVRF,R2,DVRF)";
    int hashCodea = b.hashCode();
    System.out.println("Hash Code: " + hashCodea % 100);

    SortedMap<String, Node> nodes =
        toImmutableSortedMap(configurations.values(), Configuration::getHostname, Node::new);
    long startTime1 = System.currentTimeMillis();
    List<DestinationPair> destinationPairList = new ArrayList<>();
    for (String router : configurations.keySet())
    {
      Configuration config = configurations.get(router);
      for (String vrfName : config.getVrfs().keySet())
      {
        Vrf vrf = config.getVrfs().get(vrfName);
        if (vrf.getBgpProcess() != null)
        {
            List<Prefix> prefixList = vrf.getBgpProcess().getNetworkPrefixs().get(AddressFamily.Type.IPV4_UNICAST);
            if (prefixList != null)
            {
              destinationPairList.add(new DestinationPair(router, vrfName, prefixList));
            }
        }
      }
    }
    List<Property> propertyToBeVerifiedList = new ArrayList<>();
    for (DestinationPair src : destinationPairList)
    {
      String srcRouter = src.getDstRouter();
      String srcVrf = src.getDstVrf();
      if (!srcVrf.equals("defualt"))
      {
        for (DestinationPair dst : destinationPairList)
        {
          String dstRouter = dst.getDstRouter();
          String dstVrf = dst.getDstVrf();
          if (dstVrf.equals("default") || (srcRouter.equals(dstRouter) && srcVrf.equals(dstVrf)))
          {
            continue;
          }
          if (!srcVrf.equals(dstVrf))
          {
            continue;
          }
          for (Prefix dstPrefix : dst.getDstPrefix())
          {
            Property property = new Property(srcRouter, srcVrf, dstRouter, dstVrf, dstPrefix);
            propertyToBeVerifiedList.add(property);
          }
        }
      }
    }
    //模型构建部分
    ControlPlaneGraphModelConstructEngine engine = new ControlPlaneGraphModelConstructEngine();
    engine.controlPlaneConstructor(configurations, initialTopologyContext.getIsisTopology(), initialTopologyContext.getLayer3Topology());
    long endTime1 = System.currentTimeMillis();
    System.out.println("model-constructor-time:" + (endTime1 - startTime1));
    engine.clearGraph();
//    engine.reasoningControlPlane(GraphType.ISIS);
//    long endTime1 = System.currentTimeMillis();
//    System.out.println("isis-time:" + (endTime1 - startTime));
//    engine.reasoningControlPlane(GraphType.BGP);
//    long endTime2 = System.currentTimeMillis();
//    System.out.println("BGP-time:" + (endTime2 - startTime));

    //模型推理部分
    long startTime2 = System.currentTimeMillis();
//    engine.reasoningControlPlane();
//    engine.reasoningControlPlane(GraphType.EVPNL3VPN, false);
    engine.reasoningControlPlane(GraphType.ISIS, false);
    long startTime2_1 = System.currentTimeMillis();
    engine.reasoningControlPlane(GraphType.BGP, false);
    long startTime2_2 = System.currentTimeMillis();
    engine.reasoningControlPlane(GraphType.EVPNL3VPN, false);
    long endTime2 = System.currentTimeMillis();
    System.out.println("ISIS-time:" + (startTime2_1 - startTime2));
    System.out.println("BGP-time:" + (startTime2_2 - startTime2_1));
    System.out.println("EVPNL3VPN-time:" + (endTime2 - startTime2_2));
    System.out.println("simulation-time:" + (endTime2 - startTime2));


    //同一个切片内的All Pair可达性验证
    BDDFactory prefixFactory = JFactory.init(10000,1000);
    prefixFactory.setVarNum(32);
    long startTime3 = System.currentTimeMillis();
    PropertyVerificationEngine propertyVerificationEngine = new PropertyVerificationEngine(configurations);
    propertyVerificationEngine.generateForwardingTopology(engine);
    long endTime3_1 = System.currentTimeMillis();
    propertyVerificationEngine.propertyVerifier(prefixFactory, propertyToBeVerifiedList, false);
    long endTime3 = System.currentTimeMillis();
    System.out.println("FIB-Generation-time:" + (endTime3_1 - startTime3));
    System.out.println("reachability-verification-time:" + (endTime3 - startTime3));


    //增量推理部分 - 增加一个对应的Subnet用户
//    Set<ExtendedCommunity> attachRT = new HashSet<>();
//    Set<Prefix> prefixSet = new HashSet<>();
//    attachRT.add(ExtendedCommunity.parse("target:100:1"));
//    prefixSet.add(Prefix.parse("20.75.100.0/24"));
//    engine.incrementalReasoning(new VRFAdd("edge-2", "slice10086", attachRT, null,1, prefixSet));














    //增量推理部分 - 增加一个对应的Subnet用户
    long startTime4 = System.currentTimeMillis();
    String addRouter = "abbeville";
    String addVrf = "slice10086";
    ExtendedCommunity addRT = ExtendedCommunity.parse("target:100:1");
    Prefix addPrefix = Prefix.parse("30.75.100.0/24");
    Integer addVni = 10086;

    Set<ExtendedCommunity> attachRT = new HashSet<>();
    attachRT.add(addRT);
    Set<Prefix> prefixSet = new HashSet<>();
    prefixSet.add(addPrefix);
    VRFAdd vrfAdd = new VRFAdd(addRouter, addVrf, attachRT, null,addVni, prefixSet);
    engine.incrementalReasoning(vrfAdd);

    long endTime4 = System.currentTimeMillis();
    System.out.println("increment-simulation-time:" + (endTime4 - startTime4));



    //增量验证部分 - 验证上面增量导致的属性变化
    long startTime5 = System.currentTimeMillis();
    List<Property> incrementProperty = new ArrayList<>();
    for (DestinationPair src : destinationPairList)
    {
      String srcRouter = src.getDstRouter();
      String srcVrf = src.getDstVrf();
      if (!srcVrf.equals("defualt"))
      {
        if (srcRouter.equals(addRouter))
        {
          continue;
        }


        for (Prefix dstPrefix : src.getDstPrefix())
        {
          Property newVrfToProperty = new Property(addRouter, addVrf, srcRouter, srcVrf, dstPrefix);
          Property toNewVrfProperty = new Property(srcRouter, srcVrf, addRouter, addVrf, addPrefix);
          incrementProperty.add(newVrfToProperty);
          incrementProperty.add(toNewVrfProperty);
        }
      }
    }
    List<VRFAdd> inVrf = new ArrayList<>();
    inVrf.add(vrfAdd);
    Set<RouterVrfPair> incrementVrf = engine.getEVPNL3vpnGraph().getIncrementVrf();
    propertyVerificationEngine.incrementFowardingEngine(inVrf, incrementVrf, engine);
    propertyVerificationEngine.incrementVerification(prefixFactory, incrementVrf, incrementProperty);

    long endTime5 = System.currentTimeMillis();
    System.out.println("increment-verification-time:" + (endTime5 - startTime5));


//    Property property = new Property("r1", "tenant-a", "r4", "tenant-a", Prefix.parse("192.168.6.0/24"));
//
//    propertyToBeVerifiedList.add(property);





//    long startTime1 = System.currentTimeMillis();
//
//    //模型构建部分
//    ControlPlaneGraphModelConstructEngine engine = new ControlPlaneGraphModelConstructEngine();
//    engine.controlPlaneConstructor(configurations, initialTopologyContext.getIsisTopology(), initialTopologyContext.getLayer3Topology());
//    long endTime1 = System.currentTimeMillis();
//    System.out.println("model-constructor-time:" + (endTime1 - startTime1));
//    engine.clearGraph();
////    engine.reasoningControlPlane(GraphType.ISIS);
////    long endTime1 = System.currentTimeMillis();
////    System.out.println("isis-time:" + (endTime1 - startTime));
////    engine.reasoningControlPlane(GraphType.BGP);
////    long endTime2 = System.currentTimeMillis();
////    System.out.println("BGP-time:" + (endTime2 - startTime));
//
//    //模型推理部分
//    long startTime2 = System.currentTimeMillis();
//    engine.reasoningIsolation(GraphType.EVPNL3VPN);
//    long endTime2 = System.currentTimeMillis();
//    System.out.println("isolation-time:" + (endTime2 - startTime2));
//
//
//    //同一个切片内的All Pair可达性验证
//    BDDFactory prefixFactory = JFactory.init(10000,1000);
//    prefixFactory.setVarNum(32);
//    long startTime3 = System.currentTimeMillis();
//    String addRouter = "abbeville";
//    String addVrf = "slice10086";
//    ExtendedCommunity addRT = ExtendedCommunity.parse("target:100:1");
//    Prefix addPrefix = Prefix.parse("30.75.100.0/24");
//    Integer addVni = 10086;
//
//    Set<ExtendedCommunity> attachRT = new HashSet<>();
//    attachRT.add(addRT);
//    Set<Prefix> prefixSet = new HashSet<>();
//    prefixSet.add(addPrefix);
//    VRFAdd vrfAdd = new VRFAdd(addRouter, addVrf, attachRT, null,addVni, prefixSet);
//    engine.incrementalIsolation(vrfAdd);
//    long endTime3 = System.currentTimeMillis();
//    System.out.println("incremental-isolation-time:" + (endTime3 - startTime3));


    //增量推理部分 - 增加一个对应的Subnet用户
//    Set<ExtendedCommunity> attachRT = new HashSet<>();
//    Set<Prefix> prefixSet = new HashSet<>();
//    attachRT.add(ExtendedCommunity.parse("target:100:1"));
//    prefixSet.add(Prefix.parse("20.75.100.0/24"));
//    engine.incrementalReasoning(new VRFAdd("edge-2", "slice10086", attachRT, null,1, prefixSet));












    System.out.println("Coveraged!");
//    PDGNode node = propertyVerificationEngine.getPDG().getGraph().get(124102);
//    System.out.println("Coverage!");
    //    设置直连路由
//    Map<Prefix, List<Interface>> prefixInterfaces = TopologyUtil.computeInterfacesBucketByPrefix(configurations);
//    for (Entry<Prefix, List<Interface>> bucketEntry : prefixInterfaces.entrySet()) {
//      Prefix p = bucketEntry.getKey();
//      Set<Interface> candidateInterfaces = TopologyUtil.candidateInterfacesForPrefix(prefixInterfaces, p);
//
//      for (Interface iface1 : bucketEntry.getValue()) {
//        for (Interface iface2 : candidateInterfaces) {
//          // No device self-adjacencies in the same VRF.
//          if (!TopologyUtil.isValidLayer3Adjacency(iface1, iface2)) {
//            continue;
//          }
//          // Additionally, don't connect if any of the two endpoint interfaces have Tunnel or VPN
//          // interfaceTypes
//          if (TUNNEL_INTERFACE_TYPES.contains(iface1.getInterfaceType())
//                  || TUNNEL_INTERFACE_TYPES.contains(iface2.getInterfaceType())) {
//            continue;
//          }
//          iface1.addNeighborInterface(p, iface2);
//        }
//      }
//    }
//
////    构建EVPN的模型
//    Graph evpnL3vpnGraph = new Graph(GraphType.EVPNL3VPN);
//    Graph bgpGraph = new Graph(GraphType.BGP);
//    Graph vxlanL2vpnGraph = new Graph(GraphType.VXLANL2VPN);
//    int evpnL3vpnNodeId = 0;
//    int bgpNodeId = 0;
//    int vxlanL2vpnNodeId = 0;
//    HashMap<Ip, NVE> nveL3vpnNodes = new HashMap<>();
//    HashMap<Long, BGP> bgpL3vpnNodes = new HashMap<>();
//    HashMap<Ip, NVE> nveL2vpnNodes = new HashMap<>();
//    HashMap<Long, BGP> bgpL2vpnNodes = new HashMap<>();
//    HashMap<Long, BGP> bgpNodes = new HashMap<>();
//    HashMap<Long, HashMap<Long, List<NeighborTriple>>> unConstructedL3vpnBgpEdges = new HashMap<>();
//    HashMap<Long, HashMap<Long, List<NeighborTriple>>> unConstructedL2vpnBgpEdges = new HashMap<>();
//    HashMap<Long, HashMap<Long, List<NeighborTriple>>> unConstructedBgpEdges = new HashMap<>();
//    HashMap<Ip, HashMap<Ip, NeighborTriple>> unConstructedNveEdges = new HashMap<>();
//
//    //Construct Graph
//    for (Configuration routerConfig : configurations.values())
//    {
//      HashMap<String, List<Message>> connectedMessages = new HashMap<>();
//      for (Interface inter : routerConfig.getAllInterfaces().values())
//      {
//        String vrfName = inter.getVrfName();
//        if (!connectedMessages.containsKey(vrfName))
//        {
//          connectedMessages.put(vrfName, new ArrayList<>());
//        }
//        for (Prefix prefix : inter.getNeighborInterfaces().keySet())
//        {
//          Interface neighborInterface = inter.getNeighborInterfaces().get(prefix);
//          Nexthop nexthop = new Nexthop(NexthopType.Original, neighborInterface.getHumanName(), null);
//          Message message = new Message(prefix, new ConnectAttribute(100), null, null, null, nexthop, MessageType.Connected, new ArrayList<>(), null);
//          connectedMessages.get(vrfName).add(message);
//        }
//      }
//
//      String hostName = routerConfig.getHostname();
//      List<BGP> evpnL3vpnBgpNodes = new ArrayList<>();
//      List<BGP> vxlanL2vpnBgpNodes = new ArrayList<>();
//      HashMap<String, BGP> bgpConstructedNodes = new HashMap<>();
//      HashMap<Integer, VLAN> vlanConstructedNodes = new HashMap<>();
//      HashMap<Nve, NVE> nveL2vpnConstructedNodes = new HashMap<>();
//      for(Vrf vrf : routerConfig.getVrfs().values())
//      {
//        String vrfName = vrf.getName();
//        if (vrfName.equals("management"))
//        {
//          continue;
//        }
//        BgpProcess bgpProcess = vrf.getBgpProcess();
//        long ASNumber = bgpProcess.getLocalAs();
//        BGP bgpNode = new BGP(bgpNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
//        BGP bgpL3vpnNode = new BGP(evpnL3vpnNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
//        BGP bgpL2vpnNode = new BGP(vxlanL2vpnNodeId++, NodeType.BGP, hostName, vrfName, ASNumber);
//        bgpGraph.addNode(bgpNode);
//        evpnL3vpnGraph.addNode(bgpL3vpnNode);
//        vxlanL2vpnGraph.addNode(bgpL2vpnNode);
//        bgpNodes.put(ASNumber, bgpNode);
//        bgpL3vpnNodes.put(ASNumber, bgpL3vpnNode);
//        bgpL2vpnNodes.put(ASNumber, bgpL2vpnNode);
//        unConstructedBgpEdges.put(ASNumber, new HashMap<>());
//        unConstructedL3vpnBgpEdges.put(ASNumber, new HashMap<>());
//        unConstructedL2vpnBgpEdges.put(ASNumber, new HashMap<>());
//
//        bgpConstructedNodes.put(vrfName, bgpNode);
//
//        evpnL3vpnBgpNodes.add(bgpL3vpnNode);
//        vxlanL2vpnBgpNodes.add(bgpL2vpnNode);
//
//        bgpProcess.getActiveNeighbors().forEach(
//                (neighborIP, neighborBgpConfig) -> {
//                  if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN) != null)
//                  {
//                    long localAs = neighborBgpConfig.getLocalAs();
//                    for (Long neighborAs : neighborBgpConfig.getRemoteAsns().enumerate())
//                    {
//                      if (unConstructedL3vpnBgpEdges.containsKey(neighborAs)&&unConstructedL3vpnBgpEdges.get(neighborAs).containsKey(localAs))
//                      {
//                        for (NeighborTriple neighborTriple : unConstructedL3vpnBgpEdges.get(neighborAs).get(localAs))
//                        {
//                          Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
//                          Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getRouter(), neighborTriple.getVrf(), neighborIP);
//                          List<Dependency> deps = new ArrayList<>();
//                          deps.add(neighborToLocalDep);
//                          deps.add(localToNeighborDep);
//
//                          BGP neighborNode = neighborTriple.getBgpNode();
//                          org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborNode, bgpNode, deps, 0);
//                          org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(bgpNode, neighborNode, deps, 0);
//
//                          EdgeOperation outOperation = new EdgeOperation();
//                          EdgeOperation inOperation = new EdgeOperation();
//
//                          RoutingPolicy outRoutingPolicy = null;
//                          RoutingPolicy inRoutingPolicy = null;
//
//                          if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                          {
//                            outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                            outOperation.setPolicies(outRoutingPolicy);
//                          }
//
//                          if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                          {
//                            inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                            inOperation.setPolicies(inRoutingPolicy);
//                          }
//
//                          neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
//                          neighborToLocalEdge.setInOperation(inOperation);
//                          localToNeighborEdge.setOutOperation(outOperation);
//                          localToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//
//                          evpnL3vpnGraph.addEdge(neighborToLocalEdge);
//                          evpnL3vpnGraph.addEdge(localToNeighborEdge);
//
//                          vxlanL2vpnGraph.addEdge(neighborToLocalEdge);
//                          vxlanL2vpnGraph.addEdge(localToNeighborEdge);
//                        }
//                      } else {
//                        NeighborTriple neighborTriple = new NeighborTriple(hostName, vrfName, neighborIP, bgpNode);
//                        EdgeOperation outOperation = new EdgeOperation();
//                        EdgeOperation inOperation = new EdgeOperation();
//                        RoutingPolicy outRoutingPolicy = null;
//                        RoutingPolicy inRoutingPolicy = null;
//                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                        {
//                          outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                          outOperation.setPolicies(outRoutingPolicy);
//                        }
//                        if(!neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                        {
//                          inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                          inOperation.setPolicies(inRoutingPolicy);
//                        }
//                        neighborTriple.setOutOperation(outOperation);
//                        neighborTriple.setInOperation(inOperation);
//                        unConstructedL3vpnBgpEdges.get(localAs).put(neighborAs, new ArrayList<>());
//                        unConstructedL3vpnBgpEdges.get(localAs).get(neighborAs).add(neighborTriple);
//                      }
//                    }
//                  }
//                  if (neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST) != null)
//                  {
//                    long localAs = neighborBgpConfig.getLocalAs();
//                    for (Long neighborAs : neighborBgpConfig.getRemoteAsns().enumerate()) {
//                      if (unConstructedBgpEdges.containsKey(neighborAs)&&unConstructedBgpEdges.get(neighborAs).containsKey(localAs)) {
//                        for (NeighborTriple neighborTriple : unConstructedBgpEdges.get(neighborAs).get(localAs)) {
//                          Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, vrfName, neighborTriple.getNeighborIp());
//                          Dependency localToNeighborDep = new Dependency(hostName, vrfName, neighborTriple.getRouter(), neighborTriple.getVrf(), neighborIP);
//                          List<Dependency> deps = new ArrayList<>();
//                          deps.add(neighborToLocalDep);
//                          deps.add(localToNeighborDep);
//
//                          BGP neighborNode = neighborTriple.getBgpNode();
//                          org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborNode, bgpNode, deps, 0);
//                          org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(bgpNode, neighborNode, deps, 0);
//
//                          EdgeOperation outOperation = new EdgeOperation();
//                          EdgeOperation inOperation = new EdgeOperation();
//
//                          RoutingPolicy outRoutingPolicy = null;
//                          RoutingPolicy inRoutingPolicy = null;
//
//                          if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicySources().isEmpty()) {
//                            outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicy());
//                            outOperation.setPolicies(outRoutingPolicy);
//                          }
//
//                          if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicySources().isEmpty()) {
//                            inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicy());
//                            inOperation.setPolicies(inRoutingPolicy);
//                          }
//
//                          neighborToLocalEdge.setOutOperation(neighborTriple.getOutOperation());
//                          neighborToLocalEdge.setInOperation(inOperation);
//                          localToNeighborEdge.setOutOperation(outOperation);
//                          localToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//
//                          bgpGraph.addEdge(neighborToLocalEdge);
//                          bgpGraph.addEdge(localToNeighborEdge);
//                        }
//                      } else {
//                        NeighborTriple neighborTriple = new NeighborTriple(hostName, vrfName, neighborIP, bgpNode);
//                        EdgeOperation outOperation = new EdgeOperation();
//                        EdgeOperation inOperation = new EdgeOperation();
//                        RoutingPolicy outRoutingPolicy = null;
//                        RoutingPolicy inRoutingPolicy = null;
//                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicySources().isEmpty()) {
//                          outRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getExportPolicy());
//                          outOperation.setPolicies(outRoutingPolicy);
//                        }
//                        if (!neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicySources().isEmpty()) {
//                          inRoutingPolicy = routerConfig.getRoutingPolicies().get(neighborBgpConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST).getImportPolicy());
//                          inOperation.setPolicies(inRoutingPolicy);
//                        }
//                        neighborTriple.setOutOperation(outOperation);
//                        neighborTriple.setInOperation(inOperation);
//                        unConstructedBgpEdges.get(localAs).put(neighborAs, new ArrayList<>());
//                        unConstructedBgpEdges.get(localAs).get(neighborAs).add(neighborTriple);
//                      }
//                    }
//                  }
//                }
//        );
//      }
//
//      for (Nve nve : routerConfig.getNves().values())
//      {
//        NVE evpnL3vpnNveNode = new NVE(evpnL3vpnNodeId++, NodeType.NVE, hostName, nve.toString());
//        NVE vxlanL2vpnNveNode = new NVE(vxlanL2vpnNodeId++, NodeType.NVE, hostName, nve.toString());
//        Ip nveIP = getInterfaceIp(routerConfig.getAllInterfaces(),nve.getSourceInterface());
//        evpnL3vpnNveNode.setEncapType(EncapType.VXLAN);
//        vxlanL2vpnNveNode.setEncapType(EncapType.VXLAN);
//        evpnL3vpnNveNode.setEncapIP(nveIP);
//        vxlanL2vpnNveNode.setEncapIP(nveIP);
//        nveL3vpnNodes.put(nveIP, evpnL3vpnNveNode);
//        nveL2vpnNodes.put(nveIP, vxlanL2vpnNveNode);
//        evpnL3vpnGraph.addNode(evpnL3vpnNveNode);
//        vxlanL2vpnGraph.addNode(vxlanL2vpnNveNode);
//
//        nveL2vpnConstructedNodes.put(nve, vxlanL2vpnNveNode);
//
//        nve.getMemberVnis().forEach(
//                (vniNumber, vniConfig) ->{
//                  if(vniConfig.isAssociateVrf())
//                  {
//                    for (BGP evpnL3vpnBgpNode : evpnL3vpnBgpNodes)
//                    {
//                      org.batfish.MEV.Edge nveToBgpEdge = new org.batfish.MEV.Edge(evpnL3vpnNveNode, evpnL3vpnBgpNode, new ArrayList<>(), 0);
//                      org.batfish.MEV.Edge bgpToNveEdge = new org.batfish.MEV.Edge(evpnL3vpnBgpNode, evpnL3vpnNveNode, new ArrayList<>(), 0);
//                      EdgeOperation nveToBgpOperation = new EdgeOperation();
//                      EdgeOperation bgpToNveOperation = new EdgeOperation();
//                      nveToBgpOperation.addBlockVni(vniNumber);
//                      bgpToNveOperation.addBlockVni(vniNumber);
//                      evpnL3vpnGraph.addEdge(nveToBgpEdge);
//                      evpnL3vpnGraph.addEdge(bgpToNveEdge);
//                    }
//                  } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.BGP)){
//                    for (BGP vxlanL2vpnBgpNode : vxlanL2vpnBgpNodes)
//                    {
//                      org.batfish.MEV.Edge nveToBgpEdge = new org.batfish.MEV.Edge(vxlanL2vpnNveNode, vxlanL2vpnNveNode, new ArrayList<>(), 0);
//                      org.batfish.MEV.Edge bgpToNveEdge = new org.batfish.MEV.Edge(vxlanL2vpnBgpNode, vxlanL2vpnNveNode, new ArrayList<>(), 0);
//                      EdgeOperation nveToBgpOperation = new EdgeOperation();
//                      EdgeOperation bgpToNveOperation = new EdgeOperation();
//                      nveToBgpOperation.addBlockVni(vniNumber);
//                      bgpToNveOperation.addBlockVni(vniNumber);
//                      vxlanL2vpnGraph.addEdge(nveToBgpEdge);
//                      vxlanL2vpnGraph.addEdge(bgpToNveEdge);
//                    }
//                  } else if (vniConfig.getIngressReplicationProtocol().equals(Nve.IngressReplicationProtocol.STATIC))
//                  {
//                    for (Ip peerIp : vniConfig.getPeerIps())
//                    {
//                      if (unConstructedNveEdges.containsKey(peerIp) && unConstructedNveEdges.get(peerIp).containsKey(nveIP))
//                      {
//                        NeighborTriple neighborTriple = unConstructedNveEdges.get(peerIp).get(nveIP);
//                        org.batfish.MEV.Edge localToNeighborEdge = new org.batfish.MEV.Edge(vxlanL2vpnNveNode, neighborTriple.getNveNode(), new ArrayList<>(), 0);
//                        org.batfish.MEV.Edge neighborToLocalEdge = new org.batfish.MEV.Edge(neighborTriple.getNveNode(), vxlanL2vpnNveNode, new ArrayList<>(), 0);
//                        Dependency localToNeighborDep = new Dependency(hostName, "default", neighborTriple.getRouter(), neighborTriple.getVrf(), neighborTriple.getNeighborIp());
//                        Dependency neighborToLocalDep = new Dependency(neighborTriple.getRouter(), neighborTriple.getVrf(), hostName, "default", nveIP);
//                        List<Dependency> deps = new ArrayList<>();
//                        deps.add(localToNeighborDep);
//                        deps.add(neighborToLocalDep);
//
//                        localToNeighborEdge.setDependency(deps);
//                        neighborToLocalEdge.setDependency(deps);
//                      } else {
//                        NeighborTriple neighborTriple = new NeighborTriple(hostName, "default", nveIP, vxlanL2vpnNveNode);
//                        if (!unConstructedNveEdges.containsKey(nveIP))
//                        {
//                          unConstructedNveEdges.put(nveIP, new HashMap<>());
//                        }
//                        unConstructedNveEdges.get(nveIP).put(peerIp, neighborTriple);
//                      }
//                    }
//                  }
//                }
//        );
//      }
//
//
//      for (Vrf vrf : routerConfig.getVrfs().values())
//      {
//        String vrfName = vrf.getName();
//        VRF evpnL3vpnVrfNode = new VRF(evpnL3vpnNodeId++, NodeType.VRF, hostName, vrfName);
//        VRF vrfNode = new VRF(bgpNodeId++, NodeType.VRF, hostName, vrfName);
//        evpnL3vpnVrfNode.setConnectMessages(connectedMessages.get(vrfName));
//        vrfNode.setConnectMessages(connectedMessages.get(vrfName));
//
//        if(vrf.getVrfLeakConfig() != null)
//        {
//          Set<ExtendedCommunity> attachCommunity = new HashSet<>();
//          Set<ExtendedCommunity> blockCommunity = new HashSet<>();
//          for (Bgpv4ToEvpnVrfLeakConfig bgpv4ToEvpnVrfLeakConfig : vrf.getVrfLeakConfig().getBgpv4ToEvpnVrfLeakConfigs())
//          {
//            attachCommunity.addAll(bgpv4ToEvpnVrfLeakConfig.getAttachRouteTargets());
//          }
//          for (EvpnToBgpv4VrfLeakConfig evpnToBgpv4VrfLeakConfig : vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs())
//          {
//            blockCommunity.addAll(evpnToBgpv4VrfLeakConfig.getAttachRouteTargets());
//          }
//          for (Integer vni : vrf.getLayer3Vnis().keySet())
//          {
//            Layer3Vni vniConfig = vrf.getLayer3Vnis().get(vni);
//            if (nveL3vpnNodes.containsKey(vniConfig.getSourceAddress()))
//            {
//              NVE adjacencyNveNode = nveL3vpnNodes.get(vniConfig.getSourceAddress());
//              org.batfish.MEV.Edge vrfToNveEdge = new org.batfish.MEV.Edge(evpnL3vpnVrfNode, adjacencyNveNode, new ArrayList<>(), 0);
//              org.batfish.MEV.Edge nveToVrfEdge = new org.batfish.MEV.Edge(adjacencyNveNode, evpnL3vpnVrfNode, new ArrayList<>(), 0);
//
//              EdgeOperation vrfToNveOperation = new EdgeOperation();
//              EdgeOperation nveToVrfOperation = new EdgeOperation();
//              vrfToNveOperation.setAttachVni(vni);
//              vrfToNveOperation.addAttachRTs(attachCommunity);
//              nveToVrfOperation.addBlockRTs(blockCommunity);
//
//              vrfToNveEdge.setOutOperation(vrfToNveOperation);
//              nveToVrfEdge.setOutOperation(nveToVrfOperation);
//
//              evpnL3vpnGraph.addEdge(vrfToNveEdge);
//              evpnL3vpnGraph.addEdge(nveToVrfEdge);
//            }
//          }
//        }
//
//        if (vrf.getBgpProcess() != null)
//        {
//          BGP vrfToBgpNode = bgpConstructedNodes.get(vrfName);
//          bgpGraph.addEdge(new org.batfish.MEV.Edge(vrfNode, vrfToBgpNode, new ArrayList<>(), 0));
//          bgpGraph.addEdge(new org.batfish.MEV.Edge(vrfToBgpNode, vrfNode, new ArrayList<>(), 0));
//        }
//      }
//
//      for (org.batfish.datamodel.Vlan vlan : routerConfig.getVlans())
//      {
//        int vlanId = vlan.getId();
//        int vlanVni = vlan.getVni();
//        VLAN vlanNode = new VLAN(vxlanL2vpnNodeId++, NodeType.VLAN, hostName, vlan.toString());
//        vxlanL2vpnGraph.addNode(vlanNode);
//        vlanConstructedNodes.put(vlanId, vlanNode);
//
//        for (Nve nve : routerConfig.getNves().values())
//        {
//          if (nve.getMemberVni(vlanVni) != null)
//          {
//            org.batfish.MEV.Edge vlanToNveEdge = new org.batfish.MEV.Edge(vlanNode, nveL2vpnConstructedNodes.get(nve), new ArrayList<>(), 0);
//            org.batfish.MEV.Edge nveToVlanEdge = new org.batfish.MEV.Edge(nveL2vpnConstructedNodes.get(nve), vlanNode, new ArrayList<>(), 0);
//            EdgeOperation outOperation = new EdgeOperation();
//            EdgeOperation inOperation = new EdgeOperation();
//            outOperation.setAttachVni(vlanVni);
//            inOperation.addBlockVni(vlanVni);
//            vlanToNveEdge.setOutOperation(outOperation);
//            nveToVlanEdge.setInOperation(inOperation);
//            vxlanL2vpnGraph.addEdge(vlanToNveEdge);
//            vxlanL2vpnGraph.addEdge(nveToVlanEdge);
//          }
//        }
//      }
//
//      for (Interface interfaceConfig : routerConfig.getAllInterfaces().values())
//      {
//        String interfaceName = interfaceConfig.getName();
//        if (interfaceConfig.getSwitchportTrunkEncapsulation().equals(SwitchportEncapsulationType.DOT1Q))
//        {
//          INTER interfaceNode = new INTER(vxlanL2vpnNodeId++, NodeType.INTER, hostName, interfaceName);
//          vxlanL2vpnGraph.addNode(interfaceNode);
//          VLAN vlanNode = vlanConstructedNodes.get(interfaceConfig.getVlan());
//          org.batfish.MEV.Edge interfaceToVlanEdge = new org.batfish.MEV.Edge(interfaceNode, vlanNode, new ArrayList<>(), 0);
//          org.batfish.MEV.Edge vlanToInterfaceEdge = new org.batfish.MEV.Edge(vlanNode, interfaceNode, new ArrayList<>(), 0);
//          vxlanL2vpnGraph.addEdge(interfaceToVlanEdge);
//          vxlanL2vpnGraph.addEdge(vlanToInterfaceEdge);
//        }
//      }
//    }

//    int id = 0;
//    Graph graph = new Graph();
//    HashMap<Ip, NVE> nveNodes = new HashMap<>();
//    HashMap<Long, BGP> bgpNodes = new HashMap<>();
//    HashMap<Long, HashMap<Long, NeighborTriple>> unConstructedBgpEdges = new HashMap<>();
////    首先把一个router内的节点建模出来
//    for (Configuration config : configurations.values())
//    {
//      BgpProcess bgpProcess = config.getDefaultVrf().getBgpProcess();
//      String hostName = config.getHostname();
//      BGP bgpNode = new BGP(id++, NodeType.BGP, hostName, "default");
//      bgpNode.setAS(bgpProcess.getLocalAs());
//      graph.addNode(bgpNode);
//      bgpNodes.put(bgpNode.getAS(),bgpNode);
//      unConstructedBgpEdges.put(bgpProcess.getLocalAs(), new HashMap<>());
//
//      bgpProcess.getActiveNeighbors().forEach(
//              (neighborIp, neighborConfig) ->{
//                if (neighborConfig.getAddressFamily(AddressFamily.Type.EVPN) != null)
//                {
//                  long localAs = neighborConfig.getLocalAs();
//                  for (Long neighborAs : neighborConfig.getRemoteAsns().enumerate())
//                  {
//                    if (bgpNodes.containsKey(neighborAs))
//                    {
//                      NeighborTriple neighborTriple = unConstructedBgpEdges.get(neighborAs).get(localAs);
//                      Dependency neighborToHostDep = new Dependency(neighborTriple.getRouter(),neighborTriple.getVrf(),hostName,"default",neighborTriple.getNeighborIp());
//                      Dependency hostToNeighborDep = new Dependency(hostName,"default",neighborTriple.getRouter(),neighborTriple.getVrf(),neighborIp);
//                      List<Dependency> dep = new ArrayList<>();
//                      dep.add(neighborToHostDep);
//                      dep.add(hostToNeighborDep);
//                      BGP neighborNode = bgpNodes.get(neighborAs);
//                      org.batfish.MEV.Edge neighborToHostEdge = new org.batfish.MEV.Edge(neighborNode, bgpNode, dep, 0);
//                      org.batfish.MEV.Edge hostToNeighborEdge = new org.batfish.MEV.Edge(bgpNode, neighborNode, dep, 0);
//
//                      EdgeOperation outOperation = new EdgeOperation();
//                      EdgeOperation inOperation = new EdgeOperation();
//                      RoutingPolicy outRoutingPolicy = null;
//                      RoutingPolicy inRoutingPolicy = null;
//
//                      if (!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                      {
//                        outRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                        outOperation.setPolicies(outRoutingPolicy);
//                      }
//                      if(!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                      {
//                        inRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                        inOperation.setPolicies(inRoutingPolicy);
//                      }
//                      neighborToHostEdge.setOutOperation(neighborTriple.getOutOperation());
//                      neighborToHostEdge.setInOperation(inOperation);
//                      hostToNeighborEdge.setOutOperation(outOperation);
//                      hostToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//                      graph.addEdge(neighborToHostEdge);
//                      graph.addEdge(hostToNeighborEdge);
//                    }else {
//                      NeighborTriple neighborTriple = new NeighborTriple(hostName, "default", neighborIp);
//                      EdgeOperation outOperation = new EdgeOperation();
//                      EdgeOperation inOperation = new EdgeOperation();
//                      RoutingPolicy outRoutingPolicy = null;
//                      RoutingPolicy inRoutingPolicy = null;
//                      if (!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                      {
//                          outRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                          outOperation.setPolicies(outRoutingPolicy);
//                      }
//                      if(!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                      {
//                          inRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                          inOperation.setPolicies(inRoutingPolicy);
//                      }
//                      neighborTriple.setOutOperation(outOperation);
//                      neighborTriple.setInOperation(inOperation);
//                      unConstructedBgpEdges.get(localAs).put(neighborAs, neighborTriple);
//                    }
//                  }
//                }
//              }
//      );
//
//      for (Nve nve : config.getNves().values())
//      {
//        NVE nveNode = new NVE(id++, NodeType.NVE, config.getHostname(), nve.toString());
//        Ip nveIP = getInterfaceIp(config.getAllInterfaces(),nve.getSourceInterface());
//        nveNode.setEncapType(EncapType.VXLAN);
//        nveNode.setEncapIP(nveIP);
//        nveNodes.put(nveIP, nveNode);
//        graph.addNode(nveNode);
//
//        nve.getMemberVnis().forEach(
//                (vniNumber, vniConfig) ->{
//                  if(vniConfig.isAssociateVrf())
//                  {
//                    org.batfish.MEV.Edge nveToBgpEdge = new org.batfish.MEV.Edge(nveNode, bgpNode, new ArrayList<>(), 0);
//                    org.batfish.MEV.Edge bgpToNveEdge = new org.batfish.MEV.Edge(bgpNode, nveNode, new ArrayList<>(), 0);
//                    EdgeOperation nveToBgpOperation = new EdgeOperation();
//                    EdgeOperation bgpToNveOperation = new EdgeOperation();
//                    nveToBgpOperation.addBlockVni(vniNumber);
//                    bgpToNveOperation.addBlockVni(vniNumber);
//                    graph.addEdge(nveToBgpEdge);
//                    graph.addEdge(bgpToNveEdge);
//                  }
//                }
//        );
//      }
//
//      for (String vrfName : config.getVrfs().keySet())
//      {
//        if (vrfName.equals("default") || vrfName.equals("management"))
//        {
//          continue;
//        }
//        Vrf vrf = config.getVrfs().get(vrfName);
//        VRF vrfNode = new VRF(id++, NodeType.VRF, config.getHostname(), vrfName);
//        graph.addNode(vrfNode);
//        if (vrf.getVrfLeakConfig()!=null)
//        {
//          Set<ExtendedCommunity> attachCommunity = new HashSet<>();
//          Set<ExtendedCommunity> blockCommunity = new HashSet<>();
//          for (Bgpv4ToEvpnVrfLeakConfig bgpv4ToEvpnVrfLeakConfig : vrf.getVrfLeakConfig().getBgpv4ToEvpnVrfLeakConfigs())
//          {
//            attachCommunity.addAll(bgpv4ToEvpnVrfLeakConfig.getAttachRouteTargets());
//          }
//          for (EvpnToBgpv4VrfLeakConfig evpnToBgpv4VrfLeakConfig : vrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs())
//          {
//            blockCommunity.addAll(evpnToBgpv4VrfLeakConfig.getAttachRouteTargets());
//          }
//          for (Integer vni : vrf.getLayer3Vnis().keySet())
//          {
//            Layer3Vni vniConfig = vrf.getLayer3Vnis().get(vni);
//            if (nveNodes.containsKey(vniConfig.getSourceAddress()))
//            {
//              NVE adjacencyNveNode = nveNodes.get(vniConfig.getSourceAddress());
//              org.batfish.MEV.Edge vrfToNveEdge = new org.batfish.MEV.Edge(vrfNode, adjacencyNveNode, new ArrayList<>(), 0);
//              org.batfish.MEV.Edge nveToVrfEdge = new org.batfish.MEV.Edge(adjacencyNveNode, vrfNode, new ArrayList<>(), 0);
//
//              EdgeOperation vrfToNveOperation = new EdgeOperation();
//              EdgeOperation nveToVrfOperation = new EdgeOperation();
//              vrfToNveOperation.addAttachVni(vni);
//              vrfToNveOperation.addAttachRTs(attachCommunity);
//              nveToVrfOperation.addBlockRTs(blockCommunity);
//
//              vrfToNveEdge.setOutOperation(vrfToNveOperation);
//              nveToVrfEdge.setOutOperation(nveToVrfOperation);
//
//              graph.addEdge(vrfToNveEdge);
//              graph.addEdge(nveToVrfEdge);
//            }
//          }
//        }
//      }
//    }




//  构建BGP的模型
//    int id = 0;
//    Graph graph = new Graph();
//    HashMap<Ip, NVE> nveNodes = new HashMap<>();
//    HashMap<Long, BGP> bgpNodes = new HashMap<>();
//    HashMap<Long, HashMap<Long, NeighborTriple>> unConstructedBgpEdges = new HashMap<>();
////    首先把一个router内的节点建模出来
//    for (Configuration config : configurations.values())
//    {
//      BgpProcess bgpProcess = config.getDefaultVrf().getBgpProcess();
//      String hostName = config.getHostname();
//      BGP bgpNode = new BGP(id++, NodeType.BGP, hostName, "default");
//      bgpNode.setAS(bgpProcess.getLocalAs());
//      graph.addNode(bgpNode);
//      bgpNodes.put(bgpNode.getAS(),bgpNode);
//      unConstructedBgpEdges.put(bgpProcess.getLocalAs(), new HashMap<>());
//
//      bgpProcess.getActiveNeighbors().forEach(
//              (neighborIp, neighborConfig) ->{
//                if (neighborConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST) != null)
//                {
//                  long localAs = neighborConfig.getLocalAs();
//                  for (Long neighborAs : neighborConfig.getRemoteAsns().enumerate())
//                  {
//                    if (bgpNodes.containsKey(neighborAs))
//                    {
//                      NeighborTriple neighborTriple = unConstructedBgpEdges.get(neighborAs).get(localAs);
//                      Dependency neighborToHostDep = new Dependency(neighborTriple.getRouter(),neighborTriple.getVrf(),hostName,"default",neighborTriple.getNeighborIp());
//                      Dependency hostToNeighborDep = new Dependency(hostName,"default",neighborTriple.getRouter(),neighborTriple.getVrf(),neighborIp);
//                      List<Dependency> dep = new ArrayList<>();
//                      dep.add(neighborToHostDep);
//                      dep.add(hostToNeighborDep);
//                      BGP neighborNode = bgpNodes.get(neighborAs);
//                      org.batfish.MEV.Edge neighborToHostEdge = new org.batfish.MEV.Edge(neighborNode, bgpNode, dep, 0);
//                      org.batfish.MEV.Edge hostToNeighborEdge = new org.batfish.MEV.Edge(bgpNode, neighborNode, dep, 0);
//
//                      EdgeOperation outOperation = new EdgeOperation();
//                      EdgeOperation inOperation = new EdgeOperation();
//                      RoutingPolicy outRoutingPolicy = null;
//                      RoutingPolicy inRoutingPolicy = null;
//
//                      if (!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                      {
//                        outRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                        outOperation.setPolicies(outRoutingPolicy);
//                      }
//                      if(!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                      {
//                        inRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                        inOperation.setPolicies(inRoutingPolicy);
//                      }
//                      neighborToHostEdge.setOutOperation(neighborTriple.getOutOperation());
//                      neighborToHostEdge.setInOperation(inOperation);
//                      hostToNeighborEdge.setOutOperation(outOperation);
//                      hostToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//                      graph.addEdge(neighborToHostEdge);
//                      graph.addEdge(hostToNeighborEdge);
//                    } else {
//                      NeighborTriple neighborTriple = new NeighborTriple(hostName, "default", neighborIp);
//                      EdgeOperation outOperation = new EdgeOperation();
//                      EdgeOperation inOperation = new EdgeOperation();
//                      RoutingPolicy outRoutingPolicy = null;
//                      RoutingPolicy inRoutingPolicy = null;
//                      if (!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                      {
//                        outRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                        outOperation.setPolicies(outRoutingPolicy);
//                      }
//                      if(!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                      {
//                        inRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                        inOperation.setPolicies(inRoutingPolicy);
//                      }
//                      neighborTriple.setOutOperation(outOperation);
//                      neighborTriple.setInOperation(inOperation);
//                      unConstructedBgpEdges.get(localAs).put(neighborAs, neighborTriple);
//                    }
//                  }
//                }
//              }
//      );
//
//      for (String vrfName : config.getVrfs().keySet())
//      {
//        if (vrfName.equals("management"))
//        {
//          continue;
//        }
//        Vrf vrf = config.getVrfs().get(vrfName);
//        VRF vrfNode = new VRF(id++, NodeType.VRF, config.getHostname(), vrfName);
//        graph.addNode(vrfNode);
//        if (vrf.getBgpProcess() != null)
//        {
//          BgpProcess vrfBgp = vrf.getBgpProcess();
//          BGP vrfToBgpNode = bgpNodes.get(vrfBgp.getLocalAs());
//          graph.addEdge(new org.batfish.MEV.Edge(vrfNode, vrfToBgpNode, new ArrayList<>(), 0));
//          graph.addEdge(new org.batfish.MEV.Edge(vrfToBgpNode, vrfNode, new ArrayList<>(), 0));
//        }
//      }
//    }

//    int id = 0;
//    Graph graph = new Graph();
//    HashMap<Ip, NVE> nveNodes = new HashMap<>();
//    HashMap<Long, BGP> bgpNodes = new HashMap<>();
//    HashMap<Long, HashMap<Long, NeighborTriple>> unConstructedBgpEdges = new HashMap<>();
////    首先把一个router内的节点建模出来
//    for (Configuration config : configurations.values())
//    {
////      config.getAllInterfaces()
//      BgpProcess bgpProcess = config.getDefaultVrf().getBgpProcess();
//      String hostName = config.getHostname();
//      BGP bgpNode = new BGP(id++, NodeType.BGP, hostName, "default");
//      bgpNode.setAS(bgpProcess.getLocalAs());
//      graph.addNode(bgpNode);
//      bgpNodes.put(bgpNode.getAS(),bgpNode);
//      unConstructedBgpEdges.put(bgpProcess.getLocalAs(), new HashMap<>());
//
//      bgpProcess.getActiveNeighbors().forEach(
//              (neighborIp, neighborConfig) ->{
//                if (neighborConfig.getAddressFamily(AddressFamily.Type.IPV4_UNICAST) != null)
//                {
//                  long localAs = neighborConfig.getLocalAs();
//                  for (Long neighborAs : neighborConfig.getRemoteAsns().enumerate())
//                  {
//                    if (bgpNodes.containsKey(neighborAs))
//                    {
//                      NeighborTriple neighborTriple = unConstructedBgpEdges.get(neighborAs).get(localAs);
//                      Dependency neighborToHostDep = new Dependency(neighborTriple.getRouter(),neighborTriple.getVrf(),hostName,"default",neighborTriple.getNeighborIp());
//                      Dependency hostToNeighborDep = new Dependency(hostName,"default",neighborTriple.getRouter(),neighborTriple.getVrf(),neighborIp);
//                      List<Dependency> dep = new ArrayList<>();
//                      dep.add(neighborToHostDep);
//                      dep.add(hostToNeighborDep);
//                      BGP neighborNode = bgpNodes.get(neighborAs);
//                      org.batfish.MEV.Edge neighborToHostEdge = new org.batfish.MEV.Edge(neighborNode, bgpNode, dep, 0);
//                      org.batfish.MEV.Edge hostToNeighborEdge = new org.batfish.MEV.Edge(bgpNode, neighborNode, dep, 0);
//
//                      EdgeOperation outOperation = new EdgeOperation();
//                      EdgeOperation inOperation = new EdgeOperation();
//                      RoutingPolicy outRoutingPolicy = null;
//                      RoutingPolicy inRoutingPolicy = null;
//
//                      if (!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                      {
//                        outRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                        outOperation.setPolicies(outRoutingPolicy);
//                      }
//                      if(!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                      {
//                        inRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                        inOperation.setPolicies(inRoutingPolicy);
//                      }
//                      neighborToHostEdge.setOutOperation(neighborTriple.getOutOperation());
//                      neighborToHostEdge.setInOperation(inOperation);
//                      hostToNeighborEdge.setOutOperation(outOperation);
//                      hostToNeighborEdge.setInOperation(neighborTriple.getInOperation());
//                      graph.addEdge(neighborToHostEdge);
//                      graph.addEdge(hostToNeighborEdge);
//                    } else {
//                      NeighborTriple neighborTriple = new NeighborTriple(hostName, "default", neighborIp);
//                      EdgeOperation outOperation = new EdgeOperation();
//                      EdgeOperation inOperation = new EdgeOperation();
//                      RoutingPolicy outRoutingPolicy = null;
//                      RoutingPolicy inRoutingPolicy = null;
//                      if (!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicySources().isEmpty())
//                      {
//                        outRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getExportPolicy());
//                        outOperation.setPolicies(outRoutingPolicy);
//                      }
//                      if(!neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicySources().isEmpty())
//                      {
//                        inRoutingPolicy = config.getRoutingPolicies().get(neighborConfig.getAddressFamily(AddressFamily.Type.EVPN).getImportPolicy());
//                        inOperation.setPolicies(inRoutingPolicy);
//                      }
//                      neighborTriple.setOutOperation(outOperation);
//                      neighborTriple.setInOperation(inOperation);
//                      unConstructedBgpEdges.get(localAs).put(neighborAs, neighborTriple);
//                    }
//                  }
//                }
//              }
//      );
//
//      for (String vrfName : config.getVrfs().keySet())
//      {
//        if (vrfName.equals("management"))
//        {
//          continue;
//        }
//        Vrf vrf = config.getVrfs().get(vrfName);
//        VRF vrfNode = new VRF(id++, NodeType.VRF, config.getHostname(), vrfName);
//        graph.addNode(vrfNode);
//        if (vrf.getBgpProcess() != null)
//        {
//          BgpProcess vrfBgp = vrf.getBgpProcess();
//          BGP vrfToBgpNode = bgpNodes.get(vrfBgp.getLocalAs());
//          graph.addEdge(new org.batfish.MEV.Edge(vrfNode, vrfToBgpNode, new ArrayList<>(), 0));
//          graph.addEdge(new org.batfish.MEV.Edge(vrfToBgpNode, vrfNode, new ArrayList<>(), 0));
//        }
//      }
//    }


    // A collection of all the virtual routers in random order enables parallelization across all
    // VRs, and likely spreads nodes with similar hostnames across different cores. In contrast,
    // nodes.values().parallelStream().flatMap(get vrs stream) is only node-parallel and clusters
    // nodes by hostname. See https://github.com/batfish/batfish/pull/7054 description.
    List<VirtualRouter> vrs =
        toListInRandomOrder(nodes.values().stream().flatMap(n -> n.getVirtualRouters().stream()));
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);

    /*
     * Run the data plane computation here:
     * - First, let the IGP routes converge
     * - Second, re-init BGP neighbors with reachability checks
     * - Third, let the EGP routes converge
     * - Finally, compute FIBs, return answer
     */
    IncrementalBdpAnswerElement answerElement = new IncrementalBdpAnswerElement();
    // TODO: eventually, IGP needs to be part of fixed-point below, because tunnels.
    computeIgpDataPlane(nodes, vrs, initialTopologyContext, answerElement);

    LOGGER.info("Initialize virtual routers before topology fixed point");
    vrs.parallelStream()
        .forEach(
            vr -> vr.initForEgpComputationBeforeTopologyLoop(externalAdverts, initialIpVrfOwners));

    /*
     * Perform a fixed-point computation, in which every round the topology is updated based
     * on what we have learned in the previous round.
     */
    // Since the topology iterations are incremental, clear fields that are pruned to get the real
    // topology. They are not actually yet included in topologies.

//    描述的是对应的Vxlan隧道以及Ipsec隧道等等都是随着计算过程的变化而变化的，所以他创建了一个迭代的拓扑结构
    TopologyContext priorTopologyContext =
        initialTopologyContext.toBuilder()
            .setIpsecTopology(IpsecTopology.EMPTY)
            .setTunnelTopology(TunnelTopology.EMPTY)
            .setVxlanTopology(VxlanTopology.EMPTY)
            .build();
    PartialDataplane currentDataplane =
        nextDataplane(priorTopologyContext, nodes, vrs, initialIpOwners);

    TopologyContext currentTopologyContext =
        nextTopologyContext(
            priorTopologyContext,
            currentDataplane,
            initialTopologyContext,
            networkConfigurations,
            initialIpVrfOwners);
    Map<String, Collection<TrackRoute>> trackRoutesByHostname = collectTrackRoutes(configurations);
    Map<String, Collection<TrackReachability>> trackReachabilitiesByHostname =
        collectTrackReachabilities(configurations);
    Table<String, TrackReachability, Boolean> currentTrackReachabilityResults =
        nextTrackReachabilityResults(
            currentDataplane,
            currentTopologyContext,
            configurations,
            trackReachabilitiesByHostname);
    Table<String, TrackRoute, Boolean> currentTrackRouteResults =
        nextTrackRouteResults(trackRoutesByHostname, nodes);
    DataPlaneTrackMethodEvaluatorProvider currentTrackMethodEvaluatorProvider =
        nextTrackMethodEvaluatorProvider(currentTrackReachabilityResults, currentTrackRouteResults);
    DataPlaneIpOwners currentIpOwners =
        new DataPlaneIpOwners(
            configurations,
            currentTopologyContext.getL3Adjacencies(),
            currentTrackMethodEvaluatorProvider);
    int topologyIterations = 0;
    boolean converged = false;
    while (!converged && topologyIterations++ < MAX_TOPOLOGY_ITERATIONS) {
      LOGGER.info("Starting topology iteration {}", topologyIterations);
      boolean isOscillating =
          computeNonMonotonicPortionOfDataPlane(
              nodes,
              vrs,
              answerElement,
              currentTopologyContext,
              initialTopologyContext.getLayer3Topology(),
              currentIpOwners,
              networkConfigurations,
              currentTrackMethodEvaluatorProvider);
      if (isOscillating) {
        // If we are oscillating here, network has no stable solution.
        LOGGER.error("Network has no stable solution");
        throw new BdpOscillationException("Network has no stable solution");
      }

      updateLayer3Vnis(vrs);
      currentDataplane = null; // free the old one
      currentDataplane = nextDataplane(currentTopologyContext, nodes, vrs, currentIpOwners);
      TopologyContext nextTopologyContext =
          nextTopologyContext(
              currentTopologyContext,
              currentDataplane,
              initialTopologyContext,
              networkConfigurations,
              currentIpOwners.getIpVrfOwners());

      Table<String, TrackReachability, Boolean> nextTrackReachabilityResults =
          nextTrackReachabilityResults(
              currentDataplane,
              currentTopologyContext,
              configurations,
              trackReachabilitiesByHostname);
      Table<String, TrackRoute, Boolean> nextTrackRouteResults =
          nextTrackRouteResults(trackRoutesByHostname, nodes);
      currentTrackMethodEvaluatorProvider =
          nextTrackMethodEvaluatorProvider(nextTrackReachabilityResults, nextTrackRouteResults);
      DataPlaneIpOwners nextIpOwners =
          new DataPlaneIpOwners(
              configurations,
              nextTopologyContext.getL3Adjacencies(),
              currentTrackMethodEvaluatorProvider);
      converged = true;
      if (!currentTopologyContext.equals(nextTopologyContext)) {
        converged = false;
        LOGGER.info("Topologies changed in this iteration");
      }
      Optional<String> reachabilityDiff =
          compareTracks(currentTrackReachabilityResults, nextTrackReachabilityResults);
      Optional<String> routesDiff = compareTracks(currentTrackRouteResults, nextTrackRouteResults);
      if (reachabilityDiff.isPresent() || routesDiff.isPresent()) {
        converged = false;
        LOGGER.info("Tracks changed in this iteration");
        reachabilityDiff.ifPresent(s -> LOGGER.info("Reachability tracks: {}", s));
        routesDiff.ifPresent(s -> LOGGER.info("Route tracks: {}", s));
      }
      if (!currentIpOwners.equals(nextIpOwners)) {
        converged = false;
        LOGGER.info("IP ownership changed in this iteration");
      }
      currentTopologyContext = nextTopologyContext;
      currentTrackReachabilityResults = nextTrackReachabilityResults;
      currentTrackRouteResults = nextTrackRouteResults;
      currentIpOwners = nextIpOwners;
    }

    if (!converged) {
      LOGGER.error(
          "Could not reach a fixed point topology in {} iterations", MAX_TOPOLOGY_ITERATIONS);
      throw new BdpOscillationException(
          String.format(
              "Could not reach a fixed point topology in %d iterations", MAX_TOPOLOGY_ITERATIONS));
    }

    // Generate the answers from the computation, compute final FIBs
    // TODO: Properly finalize topologies, IpOwners, etc.
    LOGGER.info("Finalizing dataplane");
    answerElement.setVersion(BatfishVersion.getVersionStatic());
    IncrementalDataPlane finalDataplane =
        IncrementalDataPlane.builder()
            .setNodes(nodes)
            .setPartialDataplane(currentDataplane)
            .build();
    return new IbdpResult(answerElement, finalDataplane, currentTopologyContext, nodes);
  }

  private @Nonnull Table<String, TrackRoute, Boolean> nextTrackRouteResults(
      Map<String, Collection<TrackRoute>> trackRoutesByHostname, SortedMap<String, Node> nodes) {
    ImmutableTable.Builder<String, TrackRoute, Boolean> trackRouteResults =
        ImmutableTable.builder();
    trackRoutesByHostname.forEach(
        (hostname, trackRoutes) -> {
          Node node = nodes.get(hostname);
          trackRoutes.forEach(
              trackRoute ->
                  trackRouteResults.put(
                      hostname, trackRoute, evaluateTrackRoute(trackRoute, node)));
        });
    return trackRouteResults.build();
  }

  /**
   * Returns map: hostname of config with at least one {@link TrackRoute} -> {@link TrackRoute}s in
   * that config.
   */
  private static @Nonnull Map<String, Collection<TrackReachability>> collectTrackReachabilities(
      Map<String, Configuration> configurations) {
    ImmutableMap.Builder<String, Collection<TrackReachability>> builder = ImmutableMap.builder();
    configurations.forEach(
        (hostname, c) -> {
          Collection<TrackReachability> trackReachabilities =
              c.getTrackingGroups().values().stream()
                  .flatMap(TRACK_REACHABILITY_COLLECTOR::visit)
                  .collect(ImmutableSet.toImmutableSet());
          if (!trackReachabilities.isEmpty()) {
            builder.put(hostname, trackReachabilities);
          }
        });
    return builder.build();
  }

  private static final TrackReachabilityCollector TRACK_REACHABILITY_COLLECTOR =
      new TrackReachabilityCollector();

  private static final class TrackReachabilityCollector
      implements GenericTrackMethodVisitor<Stream<TrackReachability>> {

    @Override
    public Stream<TrackReachability> visitNegatedTrackMethod(
        NegatedTrackMethod negatedTrackMethod) {
      return visit(negatedTrackMethod.getTrackMethod());
    }

    @Override
    public Stream<TrackReachability> visitTrackAll(TrackAll trackAll) {
      return trackAll.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<TrackReachability> visitTrackInterface(TrackInterface trackInterface) {
      return Stream.of();
    }

    @Override
    public Stream<TrackReachability> visitTrackMethodReference(
        TrackMethodReference trackMethodReference) {
      // target will be found elsewhere
      return Stream.of();
    }

    @Override
    public Stream<TrackReachability> visitTrackReachability(TrackReachability trackReachability) {
      return Stream.of(trackReachability);
    }

    @Override
    public Stream<TrackReachability> visitTrackRoute(TrackRoute trackRoute) {
      return Stream.of();
    }

    @Override
    public Stream<TrackReachability> visitTrackTrue(TrackTrue trackTrue) {
      return Stream.of();
    }
  }

  /**
   * Returns map: hostname of config with at least one {@link TrackRoute} -> {@link TrackRoute}s in
   * that config.
   */
  private static @Nonnull Map<String, Collection<TrackRoute>> collectTrackRoutes(
      Map<String, Configuration> configurations) {
    ImmutableMap.Builder<String, Collection<TrackRoute>> builder = ImmutableMap.builder();
    configurations.forEach(
        (hostname, c) -> {
          Collection<TrackRoute> trackRoutes =
              c.getTrackingGroups().values().stream()
                  .flatMap(TRACK_ROUTE_COLLECTOR::visit)
                  .collect(ImmutableSet.toImmutableSet());
          if (!trackRoutes.isEmpty()) {
            builder.put(hostname, trackRoutes);
          }
        });
    return builder.build();
  }

  private static final TrackRouteCollector TRACK_ROUTE_COLLECTOR = new TrackRouteCollector();

  private static final class TrackRouteCollector
      implements GenericTrackMethodVisitor<Stream<TrackRoute>> {

    @Override
    public Stream<TrackRoute> visitNegatedTrackMethod(NegatedTrackMethod negatedTrackMethod) {
      return visit(negatedTrackMethod.getTrackMethod());
    }

    @Override
    public Stream<TrackRoute> visitTrackAll(TrackAll trackAll) {
      return trackAll.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<TrackRoute> visitTrackInterface(TrackInterface trackInterface) {
      return Stream.of();
    }

    @Override
    public Stream<TrackRoute> visitTrackMethodReference(TrackMethodReference trackMethodReference) {
      // target will be found elsewhere
      return Stream.of();
    }

    @Override
    public Stream<TrackRoute> visitTrackReachability(TrackReachability trackReachability) {
      return Stream.of();
    }

    @Override
    public Stream<TrackRoute> visitTrackRoute(TrackRoute trackRoute) {
      return Stream.of(trackRoute);
    }

    @Override
    public Stream<TrackRoute> visitTrackTrue(TrackTrue trackTrue) {
      return Stream.of();
    }
  }

  /**
   * Create a provider for data-plane-based track evaluation, which depends in general on the
   * contents of FIBs and RIBs.
   *
   * <p>Evaluation is currently performed in the following places:
   *
   * <ul>
   *   <li>Constructor of {@link DataPlaneIpOwners}. This happens between iterations, so is thread
   *       safe with respect to RIBs and FIBs.
   *   <li>{@link VirtualRouter#activateStaticRoutes}. This happens during evaluation of a parallel
   *       stream over all {@link VirtualRouter}s that modifies RIBs. In order to achieve
   *       thread-safety in the case where a {@link org.batfish.datamodel.tracking.TrackRoute}
   *       depends on information in a different VRF than that containing the static route, the
   *       evaulator must have an immutable view of the RIB being inspected. So we should depend on
   *       the routes from the beginning of the iteration (note we are only able to supply FIBs from
   *       the beginning of an iteration anyway). Since saving routes of a VRF can be expensive, we
   *       instead use pre-evaluated {@link org.batfish.datamodel.tracking.TrackRoute} and {@link
   *       org.batfish.datamodel.tracking.TrackReachability} results here.
   * </ul>
   */
  private static @Nonnull DataPlaneTrackMethodEvaluatorProvider nextTrackMethodEvaluatorProvider(
      Table<String, TrackReachability, Boolean> trackReachabilityResults,
      Table<String, TrackRoute, Boolean> trackRouteResults) {
    return DataplaneTrackEvaluator.createTrackMethodEvaluatorProvider(
        trackReachabilityResults, trackRouteResults);
  }

  private static @Nonnull Table<String, TrackReachability, Boolean> nextTrackReachabilityResults(
      PartialDataplane dp,
      TopologyContext topologyContext,
      Map<String, Configuration> configurations,
      Map<String, Collection<TrackReachability>> trackReachabilitiesByHostname) {
    TracerouteEngine tr =
        new TracerouteEngineImpl(dp, topologyContext.getLayer3Topology(), configurations);
    ImmutableTable.Builder<String, TrackReachability, Boolean> trackReachabilityResults =
        ImmutableTable.builder();
    trackReachabilitiesByHostname.forEach(
        (hostname, trackReachabilities) -> {
          Configuration config = configurations.get(hostname);
          Map<String, Fib> fibs = dp.getFibs().get(hostname);
          trackReachabilities.forEach(
              trackReachability ->
                  trackReachabilityResults.put(
                      hostname,
                      trackReachability,
                      evaluateTrackReachability(trackReachability, config, fibs, tr)));
        });
    return trackReachabilityResults.build();
  }

  @VisibleForTesting
  static boolean evaluateTrackRoute(TrackRoute trackRoute, Node node) {
    switch (trackRoute.getRibType()) {
      case BGP:
        return TrackRouteUtils.evaluateTrackRoute(
            trackRoute,
            Optional.ofNullable(
                    node.getVirtualRouter(trackRoute.getVrf()).get().getBgpRoutingProcess())
                .<GetRoutesForPrefix<Bgpv4Route>>map(brp -> brp._bgpv4Rib::getRoutes)
                .orElse(TrackRouteUtils::emptyGetRoutesForPrefix));
      case MAIN:
        return TrackRouteUtils.evaluateTrackRoute(
            trackRoute, node.getVirtualRouter(trackRoute.getVrf()).get().getMainRib()::getRoutes);
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported RibType: %s", trackRoute.getRibType()));
    }
  }

  /**
   * Perform one iteration of the "dependent routes" dataplane computation. Dependent routes refers
   * to routes that could change because other routes have changed. For example, this includes:
   *
   * <ul>
   *   <li>static routes with next hop IP
   *   <li>aggregate routes
   *   <li>EGP routes (various protocols)
   * </ul>
   *
   * @param vrs virtual routers that are participating in the computation
   * @param iterationLabel iteration label (for stats tracking)
   * @param allNodes all nodes in the network (for correct neighbor referencing)
   */
  private static void computeDependentRoutesIteration(
      List<VirtualRouter> vrs,
      String iterationLabel,
      Map<String, Node> allNodes,
      NetworkConfigurations networkConfigurations,
      DataPlaneTrackMethodEvaluatorProvider provider,
      int iteration) {
    LOGGER.info("{}: Compute dependent routes", iterationLabel);

    // Static nextHopIp routes
    LOGGER.info("{}: Recompute conditional static routes", iterationLabel);
    vrs.parallelStream()
        .forEach(vr -> vr.activateStaticRoutes(provider.forConfiguration(vr.getConfiguration())));

    // Generated/aggregate routes
    LOGGER.info("{}: Recompute aggregate/generated routes", iterationLabel);
    vrs.parallelStream().forEach(VirtualRouter::recomputeGeneratedRoutes);

    // EIGRP
    LOGGER.info("{}: Propagate EIGRP routes", iterationLabel);
    vrs.parallelStream().forEach(vr -> vr.eigrpIteration(allNodes));
    vrs.parallelStream().forEach(VirtualRouter::mergeEigrpRoutesToMainRib);

    // Re-initialize IS-IS exports.
    LOGGER.info("{}: Recompute IS-IS routes", iterationLabel);
    vrs.parallelStream()
        .forEach(vr -> vr.initIsisExports(iteration, allNodes, networkConfigurations));

    // IS-IS route propagation
    AtomicBoolean isisChanged = new AtomicBoolean(true);
    int isisSubIterations = 0;
    while (isisChanged.get()) {
      isisSubIterations++;
      LOGGER.info("{}: Recompute IS-IS routes: subIteration {}", iterationLabel, isisSubIterations);
      isisChanged.set(false);
      vrs.parallelStream()
          .forEach(
              vr -> {
                Entry<RibDelta<IsisRoute>, RibDelta<IsisRoute>> p =
                    vr.propagateIsisRoutes(networkConfigurations);
                if (p != null
                    && vr.unstageIsisRoutes(
                        allNodes, networkConfigurations, p.getKey(), p.getValue())) {
                  isisChanged.set(true);
                }
              });
    }

    LOGGER.info("{}: Propagate OSPF external", iterationLabel);
    vrs.parallelStream().forEach(vr -> vr.ospfIteration(allNodes));
    vrs.parallelStream().forEach(VirtualRouter::mergeOspfRoutesToMainRib);

    computeIterationOfBgpRoutes(iterationLabel, allNodes, vrs);

    leakAcrossVrfs(vrs, iterationLabel);

    // Tell each VR that a BGP route computation inner round (schedule) has ended.
    vrs.parallelStream().forEach(VirtualRouter::endOfEgpInnerRound);
  }

  private static void updateLayer3Vnis(List<VirtualRouter> vrs) {
    LOGGER.info("Update learned VTEP IPs for Layer3Vnis");
    vrs.parallelStream().forEach(VirtualRouter::updateLayer3Vnis);
  }

  private static void computeIterationOfBgpRoutes(
      String iterationLabel, Map<String, Node> allNodes, List<VirtualRouter> vrs) {
    LOGGER.info("{}: Init for new BGP iteration", iterationLabel);
    vrs.parallelStream().forEach(vr -> vr.bgpIteration(allNodes));
    LOGGER.info("{}: Init BGP generated/aggregate routes", iterationLabel);
    // first let's initialize nodes-level generated/aggregate routes
    vrs.parallelStream().forEach(VirtualRouter::initBgpAggregateRoutes);

    LOGGER.info("{}: Propagate BGP v4 routes", iterationLabel);

    // Merge BGP routes from BGP process into the main RIB
    vrs.parallelStream().forEach(VirtualRouter::mergeBgpRoutesToMainRib);
  }

  private static void queueRoutesForCrossVrfLeaking(List<VirtualRouter> vrs) {
    LOGGER.info("Queueing routes to leak across VRFs");
    vrs.parallelStream().forEach(VirtualRouter::queueCrossVrfImports);
  }

  private static void leakAcrossVrfs(List<VirtualRouter> vrs, String iterationLabel) {
    LOGGER.info("{}: Leaking routes across VRFs", iterationLabel);
    vrs.parallelStream().forEach(VirtualRouter::processCrossVrfRoutes);
  }

  /**
   * Run {@link VirtualRouter#computeFib} on all virtual routers
   *
   * @param vrs all virtual routers
   */
  private void computeFibs(List<VirtualRouter> vrs) {
    LOGGER.info("Compute FIBs");
    vrs.parallelStream().forEach(VirtualRouter::computeFib);
  }

  /**
   * Compute the IGP portion of the dataplane.
   *
   * @param nodes A dictionary of configuration-wrapping Bdp nodes keyed by name
   * @param topologyContext The topology context in which various adjacencies are stored
   * @param ae The output answer element in which to store a report of the computation. Also
   */
  private void computeIgpDataPlane(
      SortedMap<String, Node> nodes,
      List<VirtualRouter> vrs,
      TopologyContext topologyContext,
      IncrementalBdpAnswerElement ae) {
    LOGGER.info("Compute IGP");
    int numOspfInternalIterations;

    /*
     * For each virtual router, setup the initial easy-to-do routes, init protocol-based RIBs,
     * queue outgoing messages to neighbors
     */
    LOGGER.info("Initialize for IGP computation");
    vrs.parallelStream().forEach(vr -> vr.initForIgpComputation(topologyContext));

    // OSPF internal routes
    numOspfInternalIterations = initOspfInternalRoutes(nodes, topologyContext.getOspfTopology());

    // RIP internal routes
    initRipInternalRoutes(nodes, vrs, topologyContext.getLayer3Topology());

    // Activate static routes
    LOGGER.info("Compute static routes post IGP convergence");
    vrs.parallelStream()
        .forEach(
            vr -> {
              importRib(vr.getMainRib(), vr._independentRib);
              // Use static evaluator since we don't have dataplane yet
              vr.activateStaticRoutes(new PreDataPlaneTrackMethodEvaluator(vr.getConfiguration()));
            });

    // Set iteration stats in the answer
    ae.setOspfInternalIterations(numOspfInternalIterations);
  }

  /**
   * Compute the EGP portion of the route exchange. Must be called after IGP routing has converged.
   *
   * @param nodes A dictionary of configuration-wrapping Bdp nodes keyed by name
   * @param ae The output answer element in which to store a report of the computation. Also
   *     contains the current recovery iteration.
   * @param topologyContext The various network topologies
   * @return true iff the computation is oscillating
   */
  private boolean computeNonMonotonicPortionOfDataPlane(
      SortedMap<String, Node> nodes,
      List<VirtualRouter> vrs,
      IncrementalBdpAnswerElement ae,
      TopologyContext topologyContext,
      Topology initialLayer3Topology,
      IpOwners ipOwners,
      NetworkConfigurations networkConfigurations,
      DataPlaneTrackMethodEvaluatorProvider provider) {
    LOGGER.info("Compute EGP");
    /*
     * Initialize all routers and their message queues (can be done as parallel as possible)
     */
    LOGGER.info("Initialize virtual routers with updated topologies");
    vrs.parallelStream()
        .forEach(vr -> vr.initForEgpComputationWithNewTopology(topologyContext, provider));

    LOGGER.info("Compute HMM routes");
    Map<String, Map<String, Set<Ip>>> interfaceOwners = ipOwners.getInterfaceOwners(true);
    vrs.parallelStream().forEach(vr -> vr.computeHmmRoutes(initialLayer3Topology, interfaceOwners));

    LOGGER.info("Compute kernel routes");
    vrs.parallelStream()
        .forEach(vr -> vr.computeConditionalKernelRoutes(ipOwners.getIpVrfOwners()));

    /*
     * Setup maps to track iterations. We need this for oscillation detection.
     * Specifically, if we detect that an iteration hashcode (a hash of all the nodes' RIBs)
     * has been previously encountered, we switch our schedule to a more restrictive one.
     */

    Map<Integer, SortedSet<Integer>> iterationsByHashCode = new HashMap<>();

    Schedule currentSchedule = _settings.getScheduleName();

    // Go into iteration mode, until the routes converge (or oscillation is detected)
    do {
      _numIterations++;
      LOGGER.info("Iteration {} begins", _numIterations);
      LOGGER.info("Compute schedule");
      // Compute node schedule
      IbdpSchedule schedule =
          IbdpSchedule.getSchedule(_settings, currentSchedule, nodes, topologyContext);

      // (Re)initialization of dependent route calculation
      //  Since this is a local step, coloring not required.

      LOGGER.info("Re-Init for new route iteration");
      vrs.parallelStream().forEach(VirtualRouter::reinitForNewIteration);

      /*
      Redistribution: take all the routes merged into the main RIB during previous iteration
      and offer them to each routing process.

      This must be called before any `executeIteration` calls on any routing process.
      Since this is a local step, coloring not required.
      */
      LOGGER.info("Redistribute");
      vrs.parallelStream().forEach(VirtualRouter::redistribute);

      // Handle process-specific route resolution and cross-VRF leaking here too.
      vrs.parallelStream().forEach(VirtualRouter::updateResolvableRoutes);
      queueRoutesForCrossVrfLeaking(vrs);

      // compute dependent routes for each allowable set of nodes until we cover all nodes
      int nodeSet = 0;
      while (schedule.hasNext()) {
        Map<String, Node> iterationNodes = schedule.next();
        List<VirtualRouter> iterationVrs =
            toListInRandomOrder(
                iterationNodes.values().stream().flatMap(n -> n.getVirtualRouters().stream()));
        String iterationlabel = String.format("Iteration %d Schedule %d", _numIterations, nodeSet);
        computeDependentRoutesIteration(
            iterationVrs, iterationlabel, nodes, networkConfigurations, provider, _numIterations);
        ++nodeSet;
      }

      // Tell each VR that a route computation round has ended.
      // This must be the last thing called on a VR in a routing round.
      vrs.parallelStream().forEach(VirtualRouter::endOfEgpRound);

      /*
       * Perform various bookkeeping at the end of the iteration:
       * - Collect sizes of certain RIBs this iteration
       * - Compute iteration hashcode
       * - Check for oscillations
       */
      computeIterationStatistics(vrs, ae, _numIterations);

      // This hashcode uniquely identifies the iteration (i.e., network state)
      int iterationHashCode = computeIterationHashCode(vrs);
      SortedSet<Integer> iterationsWithThisHashCode =
          iterationsByHashCode.computeIfAbsent(iterationHashCode, h -> new TreeSet<>());

      if (iterationsWithThisHashCode.isEmpty()) {
        iterationsWithThisHashCode.add(_numIterations);
      } else {
        // If oscillation detected, switch to a more restrictive schedule
        if (currentSchedule != Schedule.NODE_SERIALIZED) {
          LOGGER.debug(
              "Switching to a more restrictive schedule {}, iteration {}",
              Schedule.NODE_SERIALIZED,
              _numIterations);
          currentSchedule = Schedule.NODE_SERIALIZED;
        } else {
          return true; // Found an oscillation
        }
      }
    } while (hasNotReachedRoutingFixedPoint(vrs));

    ae.setDependentRoutesIterations(_numIterations);
    return false; // No oscillations
  }

  /** Check if we have reached a routing fixed point */
  private boolean hasNotReachedRoutingFixedPoint(List<VirtualRouter> vrs) {
    LOGGER.info("Iteration {}: Check if fixed point reached", _numIterations);
    return vrs.parallelStream().anyMatch(VirtualRouter::isDirty);
  }

  /**
   * Compute the hashcode that uniquely identifies the state of the network at a given iteration
   *
   * @param vrs all virtual routers in the network
   * @return integer hashcode
   */
  private int computeIterationHashCode(List<VirtualRouter> vrs) {
    LOGGER.info("Iteration {}: Compute hashCode", _numIterations);
    return vrs.parallelStream().mapToInt(VirtualRouter::computeIterationHashCode).sum();
  }

  private static void computeIterationStatistics(
      List<VirtualRouter> vrs, IncrementalBdpAnswerElement ae, int dependentRoutesIterations) {
    LOGGER.info("Iteration {}: Compute statistics", dependentRoutesIterations);
    int numBgpBestPathRibRoutes =
        vrs.parallelStream().mapToInt(VirtualRouter::getNumBgpBestPaths).sum();
    ae.getBgpBestPathRibRoutesByIteration().put(dependentRoutesIterations, numBgpBestPathRibRoutes);
    int numBgpMultipathRibRoutes =
        vrs.parallelStream().mapToInt(VirtualRouter::getNumBgpPaths).sum();
    ae.getBgpMultipathRibRoutesByIteration()
        .put(dependentRoutesIterations, numBgpMultipathRibRoutes);
    int numMainRibRoutes =
        vrs.parallelStream().mapToInt(vr -> vr.getMainRib().getRoutes().size()).sum();
    ae.getMainRibRoutesByIteration().put(dependentRoutesIterations, numMainRibRoutes);
  }

  /**
   * Return the main RIB routes for each node. Map structure: Hostname -&gt; VRF name -&gt; Set of
   * routes
   */
  @VisibleForTesting
  static SortedMap<String, SortedMap<String, Set<AbstractRoute>>> getRoutes(
      IncrementalDataPlane dp) {
    // Scan through all Nodes and their virtual routers, retrieve main rib routes
    return toImmutableSortedMap(
        dp.getRibsForTesting(),
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue(),
                Entry::getKey,
                vrfEntry -> ImmutableSet.copyOf(vrfEntry.getValue().getUnannotatedRoutes())));
  }

  private static final int MAX_OSPF_INTERNAL_ITERATIONS = 100000;

  /**
   * Run the IGP OSPF computation until convergence.
   *
   * @param allNodes list of nodes for which to initialize the OSPF routes
   * @param ospfTopology graph of OSPF adjacencies
   * @return the number of iterations it took for internal OSPF routes to converge
   */
  private int initOspfInternalRoutes(Map<String, Node> allNodes, OspfTopology ospfTopology) {
    int ospfInternalIterations = 0;
    boolean dirty = true;

    while (dirty) {
      ospfInternalIterations++;
      LOGGER.info("OSPF internal: Iteration {}", ospfInternalIterations);
      // Compute node schedule
      IbdpSchedule schedule =
          IbdpSchedule.getSchedule(
              _settings,
              _settings.getScheduleName(),
              allNodes,
              TopologyContext.builder().setOspfTopology(ospfTopology).build());

      while (schedule.hasNext()) {
        Map<String, Node> scheduleNodes = schedule.next();
        List<VirtualRouter> scheduleVrs =
            toListInRandomOrder(
                scheduleNodes.values().stream().flatMap(n -> n.getVirtualRouters().stream()));
        scheduleVrs.parallelStream()
            .forEach(virtualRouter -> virtualRouter.ospfIteration(allNodes));
        scheduleVrs.parallelStream().forEach(VirtualRouter::mergeOspfRoutesToMainRib);
      }
      dirty =
          allNodes.values().parallelStream()
              .flatMap(n -> n.getVirtualRouters().stream())
              .flatMap(vr -> vr.getOspfProcesses().values().stream())
              .anyMatch(OspfRoutingProcess::isDirty);
      if (ospfInternalIterations > MAX_OSPF_INTERNAL_ITERATIONS) {
        throw new BdpOscillationException(
            "OSPF did not converge after " + MAX_OSPF_INTERNAL_ITERATIONS + " iterations");
      }
    }
    return ospfInternalIterations;
  }

  /**
   * Run the IGP RIP computation until convergence
   *
   * @param nodes nodes for which to initialize the routes, keyed by name
   * @param topology network topology
   */
  private static void initRipInternalRoutes(
      SortedMap<String, Node> nodes, List<VirtualRouter> vrs, Topology topology) {
    /*
     * Consider this method to be a simulation within a simulation. Since RIP routes are not
     * affected by other protocols, we propagate all RIP routes amongst the nodes prior to
     * processing other routing protocols (e.g., OSPF & BGP)
     */
    AtomicBoolean ripInternalChanged = new AtomicBoolean(true);
    int ripInternalIterations = 0;
    while (ripInternalChanged.get()) {
      ripInternalIterations++;
      ripInternalChanged.set(false);
      LOGGER.info("RIP internal: Iteration {}", ripInternalIterations);
      vrs.parallelStream()
          .forEach(
              vr -> {
                if (vr.propagateRipInternalRoutes(nodes, topology)) {
                  ripInternalChanged.set(true);
                }
              });
      LOGGER.info("Unstage RIP internal: Iteration {}", ripInternalIterations);
      vrs.parallelStream().forEach(VirtualRouter::unstageRipInternalRoutes);

      LOGGER.info("Import RIP internal: Iteration {}", ripInternalIterations);
      vrs.parallelStream()
          .forEach(
              vr -> {
                importRib(vr._ripRib, vr._ripInternalRib);
                importRib(vr._independentRib, vr._ripRib, vr.getName());
              });
    }
  }


  // new ADD
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
}
