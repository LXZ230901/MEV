package org.batfish.MEVNEW.EnsembleModel.Node;



import org.batfish.MEVNEW.EnsembleModel.Message.Message;

import java.util.List;

public class ISIS extends Node{
    public ISIS (int id, NodeType type, String router, String name, String vrf)
    {
        super(id, type, router, name, vrf);
    }

    @Override
    public void attributeMessage(Message.Builder message){}

    @Override
    public List<Message.Builder> attributeMessage(List<Message.Builder> messageList)
    {
        for (Message.Builder message : messageList)
        {
            message.addVisitNode(getID());
        }
        return messageList;
    }
}
