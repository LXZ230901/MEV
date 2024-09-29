package org.batfish.MEV;

import java.util.List;

public class EVPN extends Node{

    public EVPN(int id, NodeType type, String router, String name) {
        super(id, type, router, name);
    }

    @Override
    public void attributeMessage(Message message)
    {
        return;
    }

    @Override
    public List<Message> attributeMessage(List<Message> messageList) {
        // 使用 parallelStream 进行并行处理
        messageList.parallelStream().forEach(message -> message.addVisitNode(getID()));
        return messageList;
    }
}
