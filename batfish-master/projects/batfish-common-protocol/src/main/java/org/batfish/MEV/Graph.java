package org.batfish.MEV;


import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

public class Graph {
    public HashMap<Node, List<Edge>> _graph;

    public HashMap<Node, List<Edge>> _inGraph;
    public List<Node> _nodes;
    public List<Edge> _edges;
    public GraphType _type;
    public Integer _index;

    public Graph(GraphType type)
    {
        this._type = type;
        this._graph = new HashMap<>();
        this._nodes = new ArrayList<>();
        this._edges = new ArrayList<>();
        this._inGraph = new HashMap<>();
        this._index = 0;
    }

    public void addNode(Node node)
    {
        if (this._graph.containsKey(node))
        {
            return;
        }
        this._graph.put(node, new ArrayList<>());
        this._inGraph.put(node, new ArrayList<>());
        this._nodes.add(node);
        this._index++;
    }

    public void addEdge(Edge edge)
    {
        if (this._graph.get(edge.getSrcNode()).contains(edge))
        {
            mergeEdge(edge);
            return;
        }
        this._graph.get(edge.getSrcNode()).add(edge);
        this._graph.get(edge.getDstNode()).add(edge);
        this._edges.add(edge);
        this._inGraph.get(edge.getDstNode()).add(edge);
    }

    public List<Node> getNodes()
    {
        return this._nodes;
    }

    public List<Edge> getEdges()
    {
        return this._edges;
    }

    public void mergeEdge(Edge edge)
    {
        int srcEdgeIndex = this._graph.get(edge.getSrcNode()).indexOf(edge);
        this._graph.get(edge.getSrcNode()).get(srcEdgeIndex).mergeEdge(edge);

        int dstEdgeIndex = this._graph.get(edge.getDstNode()).indexOf(edge);
        this._graph.get(edge.getDstNode()).get(dstEdgeIndex).mergeEdge(edge);

        int inEdgeIndex = this._inGraph.get(edge.getDstNode()).indexOf(edge);
        this._inGraph.get(edge.getDstNode()).get(inEdgeIndex).mergeEdge(edge);
    }

    public void reasoningControlPlane(boolean incremental)
    {
//        每个节点设置一个出消息链路列表，然后所有节点并行进行，取到邻居的出消息链路列表，然后更新自己的，然后最后再统一更新成目标出消息链路列表，最后再执行下一轮更新
        if (this._type == GraphType.BGP)
        {
            reasoningBgpGraph(incremental);
        } else if (this._type == GraphType.EVPNL3VPN)
        {
            reasoningEvpnGraph(incremental);
        } else if (this._type == GraphType.ISIS)
        {
            reasoningIsisGraph(incremental);
        } else {
            // unimplemented
        }
    }

    public void reasoningBgpGraph(boolean incremental)
    {
        _nodes.parallelStream().forEach(node -> {
            if (node.getNodeType().equals(NodeType.VRF))
            {
                VRF vrf = (VRF) node;
                vrf.initRib(GraphType.BGP);
                vrf.convertOutMessage();
                vrf.reInitRib();
            }
        });
        boolean converaged = false;
        while (!converaged)
        {
            converaged = true;
            _nodes
                    .parallelStream()
                    .forEach(node ->{
                if (node.getNodeType().equals(NodeType.VRF))
                {
                    VRF vrf = (VRF) node;
                    for (Edge adjacentEdge : this._inGraph.get(node))
                    {
                        Node adjacentNode = adjacentEdge.getSrcNode();
                        if (adjacentNode instanceof BGP)
                        {
                            BGP adjacentBgpNode = (BGP) adjacentNode;
                            for (Message message : adjacentBgpNode.getOutVrfMessage())
                            {
                                if (message.getVisitedNode().contains(node.getID()))
                                {
                                    continue;
                                }
                                Message.Builder inMessageBuiler = message.toBuilder();
                                OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuiler, true);
                                if (operationAnswer.getFilter())
                                {
                                    continue;
                                }
                                vrf.addMessage(operationAnswer.getMessage().build(), incremental);
                            }
                        }
                    }
                } else if (node.getNodeType().equals(NodeType.BGP)){
                    BGP bgpNode = (BGP) node;
                    for (Edge adjacentEdge : this._inGraph.get(bgpNode))
                    {
                        Node adjacentNode = adjacentEdge.getSrcNode();
                        if (adjacentNode instanceof VRF)
                        {
                            VRF adjacentVrfNode = (VRF) adjacentNode;
                            for (Message message : adjacentVrfNode.getOutMessage())
                            {
                                if (message.getVisitedNode().contains(bgpNode.getID()))
                                {
                                    continue;
                                }

                                Message.Builder inMessageBuiler = message.toBuilder();
                                OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuiler, true);
                                if (operationAnswer.getFilter())
                                {
                                    continue;
                                }
                                Message inMessage = inMessageBuiler.build();
                                bgpNode.getStageOutMessage().add(inMessage);
                            }
                        } else {
                            boolean reflector = bgpNode.getReflectorNeighbor().contains(adjacentNode);
                            for (Message message : adjacentNode.getOutMessage())
                            {
                                if (message.getVisitedNode().contains(node.getID()))
                                {
                                    continue;
                                }

                                Message.Builder inMessageBuiler = message.toBuilder();
                                OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuiler, false);
                                if (operationAnswer.getFilter())
                                {
                                    continue;
                                }
                                Message inMessage = inMessageBuiler.build();
                                BgpAttribute bgpAttribute = (BgpAttribute) inMessage.getAttribute();
                                bgpAttribute.setReflector(false);
                                bgpNode.getStageOutVrfMessage().add(inMessage);
                            }
                        }
                    }
                }
            });

            _nodes
                    .parallelStream()
                    .forEach( node -> {
                node.convertOutMessage();
                node.clearStageOutMessage();
                if (node instanceof BGP)
                {
                    BGP bgpNode = (BGP) node;
                    bgpNode.convertOutVrfMessage();
                } else if (node instanceof VRF)
                {
                    ((VRF) node).reInitRib();
                }
            });

            for (Node node : _nodes)
            {
                if (!node.getOutMessage().isEmpty())
                {
                    converaged = false;
                    break;
                }
                if (node.getNodeType().equals(NodeType.BGP))
                {
                    BGP bgpNode = (BGP) node;
                    if (!bgpNode.getOutVrfMessage().isEmpty())
                    {
                        converaged = false;
                        break;
                    }
                }
            }
        }
        System.out.println("converaged!");
    }


//    public void reasoningEvpnGraph(boolean incremental)
//    {
//        if (!incremental)
//        {
//            _nodes.parallelStream().forEach(node -> {
//                if (node.getNodeType().equals(NodeType.VRF))
//                {
//                    VRF vrf = (VRF) node;
//                    vrf.initRib(GraphType.EVPNL3VPN);
//                    vrf.convertOutMessage();
//                    vrf.reInitRib();
//                }
//            });
//        }
//        boolean converaged = false;
//
//
//
//
//
//
//
//
//
//        while (!converaged)
//        {
//            converaged = true;
//            _nodes
//                    .parallelStream()
//                    .forEach(node ->{
//                        if (node.getNodeType().equals(NodeType.VRF)) {
//                            VRF vrf = (VRF) node;
//
//                            // Parallel stream for processing adjacent nodes
//                            _inGraph.get(node)
////                                    .parallelStream()
//                                    .forEach(adjacentEdge -> {
//                                Node adjacentNode = adjacentEdge.getSrcNode();
//
//                                // Filter messages before processing and use parallel stream for message processing
//                                List<Message> filteredMessages = adjacentNode.getOutMessage().stream()
//                                        .filter(message -> !message.getVisitedNode().contains(node.getID()))
//                                        .collect(Collectors.toList());
//                                List<Message.Builder> processedMessages = filteredMessages
//                                        .parallelStream()
//                                        .map(message -> {
//                                            Message.Builder inMessageBuilder = message.toBuilder();
//                                            // Process message using adjacent edge, receiving operation answer
//                                            OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuilder, false);
//                                            if (!operationAnswer.getFilter()) {
//                                                return operationAnswer.getMessage();
//                                            }
//                                            return null; // Or use Optional.empty() and filter later if preferred
//                                        })
//                                        .filter(Objects::nonNull) // Remove null values if any
//                                        .collect(Collectors.toList());
//                                for (Message.Builder processMessage : processedMessages)
//                                {
//                                    vrf.addMessage(processMessage.build(), incremental);
//                                }
//                            });
//                        }
//
//                        if (node.getNodeType().equals(NodeType.BGP)) {
//                            BGP bgp = (BGP) node;
//                            List<Message> stageOutMessages = new ArrayList<>();
//
//                            // Parallel stream for processing adjacent nodes in BGP
//                            _inGraph.get(node)
////                                    .parallelStream()
//                                    .forEach(adjacentEdge -> {
//                                Node adjacentNode = adjacentEdge.getSrcNode();
//
//                                // Filter messages before processing and use parallel stream for message processing
//                                List<Message> filteredMessages = adjacentNode.getOutMessage().stream()
//                                        .filter(message -> !message.getVisitedNode().contains(node.getID()))
//                                        .collect(Collectors.toList());
//                                List<Message.Builder> processedMessages = filteredMessages
//                                        .parallelStream()
//                                        .map(message -> {
//                                            Message.Builder inMessageBuilder = message.toBuilder();
//                                            // Process message using adjacent edge, receiving operation answer
//                                            OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuilder, false);
//                                            if (!operationAnswer.getFilter()) {
//                                                return operationAnswer.getMessage();
//                                            }
//                                            return null; // Or use Optional.empty() and filter later if preferred
//                                        })
//                                        .filter(Objects::nonNull) // Remove null values if any
//                                        .collect(Collectors.toList());
//                                for (Message.Builder processMessage : processedMessages)
//                                {
//                                    bgp.getNodeUpdateMessage().mergeMessage(processMessage.build());
//                                }
//                            });
//                        }
//
//                        if (node.getNodeType().equals(NodeType.NVE))
//                        {
//                            _inGraph.get(node)
////                                    .parallelStream()
//                                    .forEach(adjacentEdge -> {
//                                        Node adjacentNode = adjacentEdge.getSrcNode();
//
//                                        // Filter messages before processing and use parallel stream for message processing
//                                        List<Message> filteredMessages = adjacentNode.getOutMessage().stream()
//                                                .filter(message -> !message.getVisitedNode().contains(node.getID()))
//                                                .collect(Collectors.toList());
//                                        List<Message.Builder> processedMessages = filteredMessages
//                                                .parallelStream()
//                                                .map(message -> {
//                                                    Message.Builder inMessageBuilder = message.toBuilder();
//                                                    // Process message using adjacent edge, receiving operation answer
//                                                    OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuilder, false);
//                                                    if (!operationAnswer.getFilter()) {
//                                                        return operationAnswer.getMessage();
//                                                    }
//                                                    return null; // Or use Optional.empty() and filter later if preferred
//                                                })
//                                                .filter(Objects::nonNull) // Remove null values if any
//                                                .collect(Collectors.toList());
//                                        for (Message.Builder processMessage : processedMessages)
//                                        {
//                                            node.addStageOutMessage(processMessage.build());
//                                        }
//                                    });
//                        }
//            });
//
//
//
//            _nodes
//                    .parallelStream()
//                    .forEach( node -> {
//                node.convertOutMessage();
//                node.clearStageOutMessage();
//
//                if (node instanceof BGP)
//                {
//                    BGP bgpNode = (BGP) node;
//                    bgpNode.convertOutMessgae(bgpNode.getNodeUpdateMessage().getUpdatedMessage());
//                    bgpNode.getNodeUpdateMessage().getUpdatedMessage().clear();
//                }
//
//            });
//
//            for (Node node : _nodes)
//            {
//                if (!node.getOutMessage().isEmpty())
//                {
//                    converaged = false;
//                    break;
//                }
//            }
//        }
//    }

    public void reasoningEvpnGraph(boolean incremental)
    {
        if (!incremental)
        {
            _nodes.parallelStream().forEach(node -> {
                if (node.getNodeType().equals(NodeType.VRF))
                {
                    VRF vrf = (VRF) node;
                    vrf.initRib(GraphType.EVPNL3VPN);
                    vrf.convertOutMessage();
                    vrf.reInitRib();
                }
            });
        }
        boolean converaged = false;






        while (!converaged)
        {
            converaged = true;
            _nodes
                    .parallelStream()
                    .forEach(node ->{
                if (node.getNodeType().equals(NodeType.VRF))
                {
                    VRF vrf = (VRF) node;
                    for (Edge adjacentEdge : this._inGraph.get(node))
                    {
                        Node adjacentNode = adjacentEdge.getSrcNode();
                        for (Message message : adjacentNode.getOutMessage())
                        {
                            if (message.getVisitedNode().contains(node.getID()))
                            {
                                continue;
                            }
                            Message.Builder inMessageBuiler = message.toBuilder();
                            OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuiler, true);
                            if (operationAnswer.getFilter())
                            {
                                continue;
                            }
                            vrf.addMessage(operationAnswer.getMessage().build(), incremental);
                        }
                    }
                } else if (node.getNodeType().equals(NodeType.BGP)){
                    for (Edge adjacentEdge : this._inGraph.get(node))
                    {
                        Node adjacentNode = adjacentEdge.getSrcNode();
                        BGP bgpNode = (BGP) node;
                        for (Message message : adjacentNode.getOutMessage())
                        {
                            if (message.getVisitedNode().contains(node.getID()))
                            {
                                continue;
                            }
                            Message.Builder inMessageBuiler = message.toBuilder();
                            OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuiler, false);
                            if (operationAnswer.getFilter())
                            {
                                continue;
                            }
                            Message inMessage = inMessageBuiler.build();
                            ((BGP) node).getNodeUpdateMessage().mergeMessage(inMessage);
                        }
                    }
                } else {
                    for (Edge adjacentEdge : this._inGraph.get(node))
                    {
                        Node adjacentNode = adjacentEdge.getSrcNode();
                        for (Message message : adjacentNode.getOutMessage())
                        {
                            if (message.getVisitedNode().contains(node.getID()))
                            {
                                continue;
                            }
                            Message.Builder inMessageBuiler = message.toBuilder();
                            OperationAnswer operationAnswer = adjacentEdge.processMessage(inMessageBuiler, false);
                            if (operationAnswer.getFilter())
                            {
                                continue;
                            }
                            Message inMessage = inMessageBuiler.build();
                            node.addStageOutMessage(inMessage);
                        }
                    }
                }
            });



            _nodes
                    .parallelStream()
                    .forEach( node -> {
                node.convertOutMessage();
                node.clearStageOutMessage();

                if (node instanceof VRF)
                {
                    ((VRF) node).reInitRib();
                }

                if (node instanceof BGP)
                {
                    BGP bgpNode = (BGP) node;
                    bgpNode.convertOutMessgae(bgpNode.getNodeUpdateMessage().getUpdatedMessage());
                    bgpNode.getNodeUpdateMessage().getUpdatedMessage().clear();
                }

            });

            for (Node node : _nodes)
            {
                if (!node.getOutMessage().isEmpty())
                {
                    converaged = false;
                    break;
                }
            }
        }
    }

    public List<VRF> getVRFNodes()
    {
        List<VRF> vrfNodes = new ArrayList<>();
        for (Node node : this._nodes)
        {
            if (node.getNodeType().equals(NodeType.VRF))
            {
                vrfNodes.add((VRF)node);
            }
        }
        return vrfNodes;
    }

    public void reasoningIsisGraph(boolean incremental)
    {
        List<ISIS> isisNodeList = new ArrayList<>();
        for (Node node : _nodes)
        {
            if (node.getNodeType().equals(NodeType.ISIS))
            {
                isisNodeList.add((ISIS) node);
            }
        }
        Map<ISIS, DijkstraIsisResult> shortestPathResult = isisNodeList.parallelStream()
                .collect(Collectors.toMap(
                        node -> node,
                        node -> dijkstraIsisComputation(node, isisNodeList)
                ));










        for (ISIS isisNode : shortestPathResult.keySet()) {
            Map<ISIS, Long> dist = shortestPathResult.get(isisNode).getDist();
            Map<ISIS, ISIS> nexthop = shortestPathResult.get(isisNode).getNexthop();
            VRF vrfNode = null;
            for (Edge edge : this._inGraph.get(isisNode))
            {
                if (edge.getSrcNode().getNodeType() == NodeType.VRF)
                {
                    vrfNode = (VRF) edge.getSrcNode();
                    break;
                }
            }
            for (ISIS dstNode : nexthop.keySet()) {
                if (isisNode.equals(dstNode))
                {
                    continue;
                }
                List<Prefix> announcePrefix = dstNode.getAnnouncePrefix();
                ISIS nextHopNode = nexthop.get(dstNode);
                Edge nexthopEdge = getEdge(nextHopNode, isisNode);
                if (nexthopEdge == null)
                {
                    System.out.println("ISIS Nexthop Error!");
                    return;
                }
                Ip nexthopIp = nexthopEdge.getOutOperation().getNexthopIp();
                for (Prefix prefix : announcePrefix)
                {
                    IsisAttribute isisAttribute = new IsisAttribute(115, dist.get(dstNode));
                    Nexthop routingNexthop = new Nexthop(NexthopType.Original, nextHopNode.getRouter(), nexthopIp);
                    Message routingMessage = new Message(prefix, isisAttribute, routingNexthop, MessageType.ISIS, Reason.ADD);
                    vrfNode.addMessage(routingMessage, incremental);
                }
            }
        }
    }





    public Edge getEdge(Node srcNode, Node dstNode)
    {
        for (Edge tempEdge : this._inGraph.get(dstNode))
        {
            if (tempEdge.getSrcNode().equals(srcNode))
            {
                return tempEdge;
            }
        }
        return null;
    }

    public DijkstraIsisResult dijkstraIsisComputation(ISIS startNode, List<ISIS> dstIsisNode)
    {
        Map<ISIS, Long> dist = new HashMap<>();
        Map<ISIS, ISIS> nexthop = new HashMap<>();
        PriorityQueue<ISIS> pq = new PriorityQueue<>(Comparator.comparingLong(dist::get));
        for (ISIS node : dstIsisNode)
        {
            dist.put(node, Long.MAX_VALUE);
            nexthop.put(node, null);
        }
        dist.put(startNode, Long.valueOf(0));
        pq.add(startNode);

        while(!pq.isEmpty())
        {
            ISIS u = pq.poll();
            Long currentDist = dist.get(u);

            for (Edge edge : _inGraph.get(u))
            {
                Node v = edge.getSrcNode();
                if (v.getNodeType().equals(NodeType.ISIS))
                {
                    ISIS isisNode = (ISIS) v;
                    Long weight = edge.getCost();

                    if (currentDist + weight < dist.get(isisNode))
                    {
                        dist.put(isisNode, currentDist + weight);
                        if (u.equals(startNode)) {
                            nexthop.put(isisNode, isisNode);
                        } else {
                            nexthop.put(isisNode, nexthop.get(u));
                        }
                        pq.add(isisNode);
                    }
                }
            }
        }
        return new DijkstraIsisResult(nexthop, dist);
    }


    public void clearGraph()
    {
        List<Node> deleteNode = new ArrayList<>();
        for (Node node : this._graph.keySet())
        {
            if (this._graph.get(node).isEmpty())
            {
                deleteNode.add(node);
            }
        }
        for (Node node : deleteNode)
        {
            this._graph.remove(node);
            this._inGraph.remove(node);
            this._nodes.remove(node);
        }
    }

    public Integer getIndex()
    {
        return this._index;
    }

    public HashMap<Node, List<Edge>> getInGraph()
    {
        return this._inGraph;
    }

    public HashMap<Node, List<Edge>> getGraph()
    {
        return this._graph;
    }



    public void initIsolationMessage()
    {
        for (Node node : this.getNodes())
        {
            if (node instanceof VRF)
            {
                VRF vrfNode = (VRF) node;
                for (Prefix prefix : vrfNode.getNetworkPrefix())
                {
                    EvpnAttribute evpnAttribute = new EvpnAttribute(20, new HashSet<>(), AsPath.of(new ArrayList<>()), Long.valueOf(100), 0, new HashSet<>(), vrfNode.getAssociatedVNI());
                    evpnAttribute.setType(MessageType.eBGP);
                    IsoaltionMessage isoaltionMessage = new IsoaltionMessage(prefix, evpnAttribute, vrfNode.getRouter(), vrfNode.getName(), MessageType.eBGP);
                    isoaltionMessage.getVisitedNode().add(vrfNode.getID());
                    vrfNode.addIsolationReceivedMessage(isoaltionMessage);
                    vrfNode.addIsolationUpdatedMessage(isoaltionMessage);
                }
            }
        }
    }

    public void incrementIsoaltion()
    {

    }

    public void reasoningIsolation(GraphType type)
    {
        if (type.equals(GraphType.EVPNL3VPN))
        {



            boolean converaged = false;




            while (!converaged)
            {
                converaged = true;
                _nodes
                        .parallelStream()
                        .forEach(node ->{
                            for (Edge adjacentEdge : this._inGraph.get(node))
                            {
                                Node adjacentNode = adjacentEdge.getSrcNode();
                                for (IsoaltionMessage isoaltionMessage : adjacentNode.getIsolationUpdateMessage())
                                {
                                    if (isoaltionMessage.getVisitedNode().contains(node.getID()))
                                    {
                                        continue;
                                    }
                                    IsoaltionMessage.Builder inMessageBuiler = isoaltionMessage.toBuilder();
                                    IsolationOperationAnswer operationAnswer = adjacentEdge.processIsolationMessage(inMessageBuiler);
                                    if (operationAnswer.getFilter())
                                    {
                                        continue;
                                    }
                                    IsoaltionMessage inMessage = operationAnswer.getMessage().build();
                                    inMessage.addVisitNode(node.getID());
                                    if (node.getIsolationReceivedMessage().contains(inMessage))
                                    {
                                        continue;
                                    }
                                    int receivedMessageLength = node.getIsolationReceivedMessage().size();
                                    node.addIsolationReceivedMessage(inMessage);
                                    if (node.getIsolationReceivedMessage().size() != receivedMessageLength)
                                    {
                                        node.addStageIsolationUpdatedMessage(inMessage);
                                    }
                                }
                            }
                        });



                _nodes
                        .parallelStream()
                        .forEach(Node::convertIsolationMessage);

                for (Node node : _nodes)
                {
                    if (!node.getIsolationUpdateMessage().isEmpty())
                    {
                        converaged = false;
                        break;
                    }
                }
            }
        }
    }





    public Set<RouterVrfPair> getIncrementVrf()
    {
        Set<RouterVrfPair> incrementVrf = new HashSet<>();
        List<Node> incrementNode = this._nodes
                .stream()
                .filter(Node::getIncrementChange)
                .collect(Collectors.toList());
        for (Node node : incrementNode)
        {
            if (node instanceof VRF)
            {
                incrementVrf.add(new RouterVrfPair(node.getRouter(),node.getName()));
            }
            node.setIncrementChange(false);
        }
        return incrementVrf;
    }


    public Node getVRF(String router, String vrf)
    {
        Node node = null;
        for (Node tempNode : this._nodes)
        {
            if (tempNode.getRouter().equals(router) && tempNode.getName().equals(vrf) && tempNode instanceof VRF)
            {
                node = tempNode;
            }
        }
        return node;
    }
}
