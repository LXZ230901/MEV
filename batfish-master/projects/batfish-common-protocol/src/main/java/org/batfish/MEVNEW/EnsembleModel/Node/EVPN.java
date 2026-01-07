package org.batfish.MEVNEW.EnsembleModel.Node;

import org.batfish.MEVNEW.EnsembleModel.Message.EvpnAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.Message;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.bgp.RouteDistinguisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EVPN extends Node{
    public long _AS;
    private List<Node> _reflectorNeighbor;
    private Long _reflectorId;
    private HashMap<RouteDistinguisher, VRFRib> _RDRib;
    private List<Message> _outMessage;






    public EVPN(int id, NodeType type, String router, String name, String vrf, long AS) {
        super(id, type, router, name, vrf);
        this._AS = AS;
        this._reflectorNeighbor = new ArrayList<>();
        this._RDRib = new HashMap<>();
        this._outMessage = new ArrayList<>();
    }

    public Long getReflectorId()
    {
        return this._reflectorId;
    }

    public List<Message> getOutMessage()
    {
        return this._outMessage;
    }

    public long getAS()
    {
        return this._AS;
    }

    public List<Node> getReflectorNeighbor()
    {
        return this._reflectorNeighbor;
    }

    public void setAS(long AS)
    {
        this._AS = AS;
    }

    public void addReflectorNeighbor(Node neighbor)
    {
        this._reflectorNeighbor.add(neighbor);
    }

    public void setReflectorId(Long reflectorId)
    {
        this._reflectorId = reflectorId;
    }

    public void addMessage(Message message, boolean incremental)
    {
        EvpnAttribute evpnAttribute = (EvpnAttribute) message.getAttribute();
        RouteDistinguisher rd = evpnAttribute.getRd();
        if (this._RDRib.containsKey(rd))
        {
            this._RDRib.get(rd).addMessage(message, incremental);
        } else {
            this._RDRib.put(rd, new VRFRib());
            this._RDRib.get(rd).addMessage(message, incremental);
        }
    }

    @Override
    public void attributeMessage(Message.Builder message)
    {
        message.addVisitNode(getID());
        if (message.getAttribute() instanceof EvpnAttribute)
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.addAsPath(AsSet.of(this._AS));
            if (this._reflectorId != null)
            {
                attribute.addRouterIdList(this._reflectorId);
            }
        }
        return;
    }

    @Override
    public List<Message.Builder> attributeMessage(List<Message.Builder> messageList)
    {
        for (Message.Builder message : messageList)
        {
            message.addVisitNode(getID());
            if (message.getAttribute() instanceof EvpnAttribute)
            {
                EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
                attribute.addAsPath(AsSet.of(this._AS));
                if (this._reflectorId != null)
                {
                    attribute.addRouterIdList(this._reflectorId);
                }
            }
        }
        return messageList;
    }

    public void computeOutMessage()
    {
        this._outMessage.clear();;
        for (VRFRib vrfrib : this._RDRib.values())
        {
            vrfrib.computeOutMessage();
            this._outMessage.addAll(vrfrib.getOutMessage());
        }
    }
}
