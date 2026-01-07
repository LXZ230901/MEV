package org.batfish.MEV;

import java.util.List;

public class VLAN extends Node{
    public VLAN(int id, NodeType type, String router, String name)
    {
        super(id, type, router, name);
    }

    @Override
    public void attributeMessage(Message message)
    {
        return;
    }

    @Override
    public List<Message> attributeMessage(List<Message> messageList)
    {
        for (Message message : messageList)
        {
            message.addVisitNode(getID());
        }
        return messageList;
    }
}
