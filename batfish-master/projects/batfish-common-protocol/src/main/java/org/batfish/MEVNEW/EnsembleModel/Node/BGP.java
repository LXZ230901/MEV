package org.batfish.MEVNEW.EnsembleModel.Node;

import org.batfish.MEVNEW.EnsembleModel.Message.BgpAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.Message;
import org.batfish.datamodel.AsSet;

import java.util.ArrayList;
import java.util.List;

public class BGP extends Node {
    public long _AS;
    private List<Node> _reflectorNeighbor;
    private Long _reflectorId;
    public BGP(int id, NodeType type, String router, String name, String vrf, long AS) {
        super(id, type, router, name, vrf);
        this._AS = AS;
        this._reflectorNeighbor = new ArrayList<>();
    }

    public Long getReflectorId()
    {
        return this._reflectorId;
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

    @Override
    public void attributeMessage(Message.Builder message)
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
        return;
    }

    @Override
    public List<Message.Builder> attributeMessage(List<Message.Builder> messageList)
    {
        for (Message.Builder message : messageList)
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
        }
        return messageList;
    }
}
