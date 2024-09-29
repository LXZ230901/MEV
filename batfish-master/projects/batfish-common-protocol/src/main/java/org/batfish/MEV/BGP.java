package org.batfish.MEV;

import org.batfish.datamodel.AsSet;

import java.util.ArrayList;
import java.util.List;

public class BGP extends Node{

    public long _AS;
    public String _vrf;
    private List<Message> _outVrfMessage;
    private List<Message> _stageOutVrfMessage;
    private List<Node> _reflectorNeighbor;
    private Long _reflectorId;
    private NodeUpdateMessage _nodeUpdateMessage;
    public BGP(int id, NodeType type, String router, String name, long AS) {
        super(id, type, router, name);
        this._AS = AS;
        this._outVrfMessage = new ArrayList<>();
        this._stageOutVrfMessage = new ArrayList<>();
        this._reflectorNeighbor = new ArrayList<>();
        this._nodeUpdateMessage = new NodeUpdateMessage();
    }

    public void setAS(long AS)
    {
        this._AS = AS;
    }

    public long getAS()
    {
        return this._AS;
    }

    public void setVrf(String vrf)
    {
        this._vrf = vrf;
    }

    public void addReflectorNeighbor(Node neighbor)
    {
        this._reflectorNeighbor.add(neighbor);
    }

    public String getVrf() {
        return this._vrf;
    }

    public void addStageOutVrfMessage(Message message)
    {
        this._stageOutVrfMessage.add(message);
    }

    public List<Message> getStageOutVrfMessage()
    {
        return this._stageOutVrfMessage;
    }

    public void addOutVrfMessage(Message message)
    {
        this._outVrfMessage.add(message);
    }

    public List<Message> getOutVrfMessage()
    {
        return this._outVrfMessage;
    }

    public void convertOutVrfMessage()
    {
        this._outVrfMessage.clear();
        this._outVrfMessage.addAll(this._stageOutVrfMessage);
        this._stageOutVrfMessage.clear();
    }

    public NodeUpdateMessage getNodeUpdateMessage()
    {
        return this._nodeUpdateMessage;
    }

    public List<Node> getReflectorNeighbor()
    {
        return this._reflectorNeighbor;
    }

    @Override
    public void attributeMessage(Message message)
    {
        message.addVisitNode(getID());
        if (message.getAttribute() instanceof BgpAttribute)
        {
            BgpAttribute attribute = (BgpAttribute) message.getAttribute();
            attribute.addAsPath(AsSet.of(this._AS));
            if (this._reflectorId != null)
            {
                attribute.addRouterIdList(this._reflectorId);
            }
        }
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
    public List<Message> attributeMessage(List<Message> messageList)
    {
        for (Message message : messageList)
        {
            message.addVisitNode(getID());
            if (message.getAttribute() instanceof BgpAttribute)
            {
                BgpAttribute attribute = (BgpAttribute) message.getAttribute();
                attribute.addAsPath(AsSet.of(this._AS));
                if (this._reflectorId != null)
                {
                    attribute.addRouterIdList(this._reflectorId);
                }
            }
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

    public void setReflectorId(Long reflectorId)
    {
        this._reflectorId = reflectorId;
    }

    public Long getReflectorId()
    {
        return this._reflectorId;
    }
}
