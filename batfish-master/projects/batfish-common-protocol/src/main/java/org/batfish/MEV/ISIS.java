package org.batfish.MEV;


import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.List;

public class ISIS  extends Node{
    private List<Prefix> _announcePrefix;
    public ISIS (int id, NodeType type, String router, String name)
    {
        super(id, type, router, name);
        this._announcePrefix = new ArrayList<>();
    }
    @Override
    public void attributeMessage(Message message){}


    public void setAnnouncePrefix(List<Prefix> prefixList)
    {
        this._announcePrefix.addAll(prefixList);
    }


    public List<Prefix> getAnnouncePrefix()
    {
        return this._announcePrefix;
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
