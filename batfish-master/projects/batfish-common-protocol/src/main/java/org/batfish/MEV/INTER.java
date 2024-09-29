package org.batfish.MEV;

import java.util.ArrayList;
import java.util.List;

public class INTER extends Node{

    public INTER(int id, NodeType type, String router, String name) {
        super(id, type, router, name);
    }

    @Override
    public void attributeMessage(Message message)
    {
        return;
    }

    @Override
    public List<Message> attributeMessage(List<Message> message)
    {
        return new ArrayList<>();
    }
}
