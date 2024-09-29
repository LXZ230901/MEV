package org.batfish.MEV;

import org.batfish.datamodel.Ip;

import java.util.List;

public class NVE extends Node{

    public EncapType _encapType;
    public Ip _encapIP;

    public NVE(int id, NodeType type, String router, String name) {
        super(id, type, router, name);
    }

    public void setEncapType(EncapType type)
    {
        this._encapType = type;
    }

    public void setEncapIP(Ip encapIP)
    {
        this._encapIP = encapIP;
    }

    public EncapType getEncapType()
    {
        return this._encapType;
    }

    public Ip getEncapIP()
    {
        return this._encapIP;
    }



    @Override
    public void attributeMessage(Message message)
    {
        if (message.getAttribute() instanceof EvpnAttribute)
        {
            Nexthop nexthop = new Nexthop(NexthopType.Encap, getRouter(), this._encapIP);
            nexthop.setEncapType(this._encapType);
            message.setNexthop(nexthop);
        }
        return;
    }

    @Override
    public List<Message> attributeMessage(List<Message> messageList)
    {
        for (Message message : messageList)
        {
            message.addVisitNode(getID());
            if (message.getAttribute() instanceof EvpnAttribute)
            {
                if (message.getNexthop() != null && message.getNexthop().getNexthopType() == NexthopType.Encap)
                {
                    Dependency dependency = new Dependency(getRouter(), "default", message.getNexthop().getNexthopRouter(), "default", message.getNexthop().getNexthopIp());
                    message.getForwardingDependency().add(dependency);
                } else {
                    Nexthop nexthop = new Nexthop(NexthopType.Encap, getRouter(), this._encapIP);
                    nexthop.setEncapType(this._encapType);
                    message.setNexthop(nexthop);
                }
            }
        }
        return messageList;
    }
}
