package org.batfish.MEV;



import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NodeUpdateMessage {
    public HashMap<Prefix, List<Message>> _receivedMessage;

    public HashSet<Prefix> _changedPrefix;

    public List<Message> _updatedMessage;

    public Integer _vni;

    public Set<Prefix> _networkPrefix;

    public NodeUpdateMessage() {
        this._receivedMessage = new HashMap<>();
        this._updatedMessage = new ArrayList<>();
    }

    public List<Message> getUpdatedMessage()
    {
        return this._updatedMessage;
    }

    public void mergeMessage(Message message)
    {

        if (message.getReason().equals(Reason.WITHDRAW))
        {
            if (this._receivedMessage.containsKey(message.getPrefix()))
            {
                this._receivedMessage.get(message.getPrefix()).remove(message);
            }
            _updatedMessage.add(message);
        }else if(message.getReason().equals(Reason.ADD))
        {
            Prefix addPrefix = message.getPrefix();
            List<Message> messages = _receivedMessage.computeIfAbsent(addPrefix, k -> new ArrayList<>());
            if (messages.isEmpty())
            {
                Message newAddMessage = message.toBuilder().setReason(Reason.NORMAL).build();
                messages.add(newAddMessage);
                this._updatedMessage.add(message);
            }else{
                Message ribMessage = messages.get(0);
                if (message.getAttribute().comparePriority(ribMessage.getAttribute()) > 0)
                {
                    Iterator<Message> iterator = messages.iterator();
                    while (iterator.hasNext())
                    {
                        Message deleteMessage = iterator.next();
                        Message withDrawMessage = deleteMessage.toBuilder().setReason(Reason.WITHDRAW).build();
                        this._updatedMessage.add(withDrawMessage);
                        iterator.remove();
                    }
                    Message newAddMessage = message.toBuilder().setReason(Reason.NORMAL).build();
                    this._receivedMessage.get(addPrefix).add(newAddMessage);
                    this._updatedMessage.add(message);
                }
            }
        }
    }


    public HashMap<Prefix, List<Message>> getReceivedMessage()
    {
        return this._receivedMessage;
    }
}
