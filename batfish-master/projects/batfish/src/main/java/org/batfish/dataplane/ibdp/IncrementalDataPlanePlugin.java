package org.batfish.dataplane.ibdp;

import com.google.auto.service.AutoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.IncrementalBdpAnswerElement;
import org.batfish.datamodel.isis.IsisTopology;

import java.util.Map;
import java.util.Set;

/** A batfish plugin that registers the Incremental Batfish Data Plane (ibdp) Engine. */
@AutoService(Plugin.class)
public final class IncrementalDataPlanePlugin extends DataPlanePlugin {

  private static final Logger LOGGER = LogManager.getLogger(IncrementalDataPlanePlugin.class);

  public static final String PLUGIN_NAME = "ibdp";

  private IncrementalBdpEngine _engine;

  public IncrementalDataPlanePlugin() {}

  @Override
  public ComputeDataPlaneResult computeDataPlane(NetworkSnapshot snapshot) {
    Map<String, Configuration> configurations = _batfish.loadConfigurations(snapshot);

    int id = 0;
//    Graph EVPNGraph = new Graph();
//    for (String router : configurations.keySet())
//    {
//      Configuration config = configurations.get(router);
//      for (Nve nve : config.getNves().values())
//      {
//
//      }
//      for (String vrfName : config.getVrfs().keySet())
//      {
//        Vrf vrf = config.getVrfs().get(vrfName);
//        VRF VRFNode = new VRF(id++, NodeType.VRF, router, vrfName);
//        BgpProcess bgpProcess = vrf.getBgpProcess();
//        BGP BGPNode = new BGP(id++, NodeType.BGP, router, "BGP");
//
//        if (!EVPNGraph.getNodes().contains(VRFNode))
//        {
//          EVPNGraph.addNode(VRFNode);
//        }
//
//        if (!EVPNGraph.getNodes().contains(BGPNode))
//        {
//          EVPNGraph.addNode(BGPNode);
//        }
//
//        if (vrf.getVrfLeakConfig().getBgpv4ToEvpnVrfLeakConfigs().size() != 0)
//        {
//          EVPNGraph.addEdge(new Edge(VRFNode,BGPNode,null,0));
//        }
//
//        Iterator<BgpPeerConfig> peerConfig = bgpProcess.getAllPeerConfigs().iterator();
//      }
//    }


    Set<BgpAdvertisement> externalAdverts =
        _batfish.loadExternalBgpAnnouncements(snapshot, configurations);

    LOGGER.info("Building topology for data-plane");
    TopologyProvider topologyProvider = _batfish.getTopologyProvider();

    Topology L3Topology =  topologyProvider.getInitialLayer3Topology(snapshot);

    TopologyContext topologyContext =
        TopologyContext.builder()
            .setIpsecTopology(topologyProvider.getInitialIpsecTopology(snapshot))
            .setIsisTopology(
                IsisTopology.initIsisTopology(
                    configurations, topologyProvider.getInitialLayer3Topology(snapshot)))
            .setLayer3Topology(topologyProvider.getInitialLayer3Topology(snapshot))
            .setLayer1Topologies(topologyProvider.getLayer1Topologies(snapshot))
            .setL3Adjacencies(topologyProvider.getInitialL3Adjacencies(snapshot))
            .setOspfTopology(topologyProvider.getInitialOspfTopology(snapshot))
            .setTunnelTopology(topologyProvider.getInitialTunnelTopology(snapshot))
            .build();

    ComputeDataPlaneResult answer =
        _engine.computeDataPlane(
            configurations,
            topologyContext,
            externalAdverts,
            topologyProvider.getInitialIpOwners(snapshot));
    _logger.infof(
        "Generated data-plane for snapshot:%s; iterations:%s",
        snapshot.getSnapshot(),
        ((IncrementalBdpAnswerElement) answer._answerElement).getDependentRoutesIterations());
    return answer;
  }

  @Override
  protected void dataPlanePluginInitialize() {
    _engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(_batfish.getSettingsConfiguration()));
  }

  @Override
  public String getName() {
    return PLUGIN_NAME;
  }
}
