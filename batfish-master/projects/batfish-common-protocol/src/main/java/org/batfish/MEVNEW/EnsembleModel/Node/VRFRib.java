package org.batfish.MEVNEW.EnsembleModel.Node;
import org.batfish.MEVNEW.EnsembleModel.Message.BgpAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.EvpnAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.Message;
import org.batfish.MEVNEW.EnsembleModel.Message.MessageType;
import org.batfish.MEVNEW.EnsembleModel.Message.Nexthop;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopLocal;
import org.batfish.MEVNEW.EnsembleModel.Message.NexthopType;
import org.batfish.MEVNEW.EnsembleModel.Message.Reason;
import org.batfish.MEVNEW.EnsembleModel.Message.RedistributionTag;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class VRFRib {
    private HashMap<Prefix, List<Message>> _rib;
    private HashSet<Prefix> _changedPrefix;
    private HashMap<Prefix, List<Message>> _connectMessages;
    private Integer _vni;
    private List<Message> _outMessage;
    private List<Message> _stageOutMessage;
    private boolean _incremental;
    private Set<Prefix> _incrementPrefix;
    private String _router;
    public VRFRib() {
        this._rib = new HashMap<>();
        this._changedPrefix = new HashSet<>();
        this._connectMessages = new HashMap<>();
        this._outMessage = new ArrayList<>();
        this._stageOutMessage = new ArrayList<>();
        this._incremental = false;
        this._incrementPrefix = new HashSet<>();
    }

    public HashMap<Prefix,List<Message>> getRib()
    {
        return this._rib;
    }

    public List<Message> getStageOutMessage()
    {
        return this._stageOutMessage;
    }

    public List<Message> getMessageList()
    {
        List<Message> messageList = new ArrayList<>();
        for (Prefix prefix : _rib.keySet())
        {
            messageList.addAll(_rib.get(prefix));
        }
        return messageList;
    }

    public List<Message> getOutMessage()
    {
        return this._outMessage;
    }

    public Set<Prefix> getIncrementalPrefix()
    {
        return this._incrementPrefix;
    }

    public HashMap<Prefix, List<Message>> getConnectMessages()
    {
        return this._connectMessages;
    }

    public boolean getIncrementalTag()
    {
        return this._incremental;
    }

    public Integer getAssociatedVNI()
    {
        return this._vni;
    }

    public void setAssociatedVNI(Integer vni)
    {
        this._vni = vni;
    }

    public void addStageOutMessage(Message message)
    {
        this._stageOutMessage.add(message.toBuilder().build());
    }

    public void clearStageOutMessage()
    {
        this._stageOutMessage.clear();
    }

    public void addMessage(Message message, boolean incremental)
    {
        if (message.getReason().equals(Reason.WITHDRAW))
        {
            if (this._rib.containsKey(message.getPrefix()))
            {
                this._rib.get(message.getPrefix()).remove(message);
            }
            this.getStageOutMessage().remove(message);
            this.addStageOutMessage(message);
        }else if(message.getReason().equals(Reason.ADD))
        {
            Prefix addPrefix = message.getPrefix();
            if (!this._rib.containsKey(addPrefix))
            {
                this._rib.put(addPrefix, new ArrayList<>());
            }
            if (this._rib.get(addPrefix).isEmpty())
            {
                this._rib.get(addPrefix).add(message.toBuilder().build());
                this.addStageOutMessage(message.toBuilder().build());
                this._changedPrefix.add(addPrefix);
                this._incremental = incremental;
                if (incremental)
                {
                    this._incrementPrefix.add(addPrefix);
                }
            }else{
                Message ribMessage = this._rib.get(addPrefix).get(0);
                if (message.getAttribute().comparePriority(ribMessage.getAttribute()) == 0)
                {
//                    this._rib.get(addPrefix).add(message.toBuilder().build());
//                    this.addStageOutMessgae(message.toBuilder().build());
//                    this._changedPrefix.add(addPrefix);
                } else if (message.getAttribute().comparePriority(ribMessage.getAttribute()) > 0)
                {
                    Iterator<Message> iterator = this._rib.get(addPrefix).iterator();
                    while (iterator.hasNext())
                    {
                        Message deleteMessage = iterator.next();
                        if (deleteMessage.getReason().equals(Reason.ADD))
                        {
                            this.getStageOutMessage().remove(deleteMessage);
                        } else {
                            Message withDrawMessage = deleteMessage.toBuilder().build();
                            withDrawMessage.setReason(Reason.WITHDRAW);
                            this.addStageOutMessage(withDrawMessage);
                        }
                        iterator.remove();
                    }
                    this._rib.get(addPrefix).add(message.toBuilder().build());
                    this.addStageOutMessage(message.toBuilder().build());
                    this._changedPrefix.add(addPrefix);
                    this._incremental = incremental;
                    if (incremental)
                    {
                        this._incrementPrefix.add(addPrefix);
                    }
                }
            }
        }
        return ;
    }


    public void reInitRib()
    {
        for (Prefix prefix : this._changedPrefix)
        {
            for (Message message : this._rib.get(prefix))
            {
                message.setReason(Reason.NORMAL);
            }
        }
        this._changedPrefix.clear();
        clearStageOutMessage();
    }

    public void mergeMessageList(List<Message> messageList)
    {
        for (Message message : messageList)
        {
            Prefix addPrefix = message.getPrefix();
            if (!this._rib.containsKey(addPrefix))
            {
                this._rib.put(addPrefix, new ArrayList<>());
            }
            if (this._rib.get(addPrefix).isEmpty())
            {
                this._rib.get(addPrefix).add(message.toBuilder().build());
                this.addStageOutMessage(message.toBuilder().build());
                this._changedPrefix.add(addPrefix);
            }else{
                Message ribMessage = this._rib.get(addPrefix).get(0);
                if (message.getAttribute().comparePriority(ribMessage.getAttribute()) == 0)
                {
//                    this._rib.get(addPrefix).add(message.toBuilder().build());
//                    this.addStageOutMessgae(message.toBuilder().build());
//                    this._changedPrefix.add(addPrefix);
                } else if (message.getAttribute().comparePriority(ribMessage.getAttribute()) > 0)
                {
                    Iterator<Message> iterator = this._rib.get(addPrefix).iterator();
                    while (iterator.hasNext())
                    {
                        Message deleteMessage = iterator.next();
                        if (deleteMessage.getReason().equals(Reason.ADD))
                        {
                            this.getStageOutMessage().remove(deleteMessage);
                        } else {
                            Message withDrawMessage = deleteMessage.toBuilder().build();
                            withDrawMessage.setReason(Reason.WITHDRAW);
                            this.addStageOutMessage(withDrawMessage);
                        }
                        iterator.remove();
                    }
                    this._rib.get(addPrefix).add(message.toBuilder().build());
                    this.addStageOutMessage(message.toBuilder().build());
                    this._changedPrefix.add(addPrefix);
                }
            }
        }
        return ;
    }

    public void setConnectMessages(List<Message> connectMessages)
    {
        for (Message connectMessage : connectMessages)
        {
            if (!this._connectMessages.containsKey(connectMessage.getPrefix()))
            {
                this._connectMessages.put(connectMessage.getPrefix(), new ArrayList<>());
            }
            this._connectMessages.get(connectMessage.getPrefix()).add(connectMessage);
        }
    }

    public void initRib(NodeType nodeType, Integer nodeId, Set<Prefix> networkPrefix)
    {
        if (nodeType.equals(NodeType.BGP))
        {
            for (Prefix prefix : networkPrefix)
            {
                BgpAttribute bgpAttribute = new BgpAttribute(0, new HashSet<>(), new HashSet<>(), AsPath.of(new ArrayList<>()), Long.valueOf(100), 0);
                bgpAttribute.setType(MessageType.eBGP);
                bgpAttribute.setAD(20);
                Nexthop nexthop = new NexthopLocal(NexthopType.Local, _router);
                Message message = new Message(prefix, bgpAttribute, new ArrayList<>(), new ArrayList<>(), new RedistributionTag(), nexthop, MessageType.Connected, new ArrayList<>(nodeId), Reason.ADD, false);
                addMessage(message, false);
            }
        }else if (nodeType.equals(NodeType.EVPN))
        {
            for (Prefix prefix : networkPrefix)
            {
                EvpnAttribute evpnAttribute = new EvpnAttribute(0, new HashSet<>(), AsPath.of(new ArrayList<>()), Long.valueOf(100), 0, new HashSet<>(), _vni, null);
                evpnAttribute.setType(MessageType.eBGP);
                evpnAttribute.setAD(20);
                Nexthop nexthop = new NexthopLocal(NexthopType.Local, _router);
                Message message = new Message(prefix, evpnAttribute, new ArrayList<>(), new ArrayList<>(), new RedistributionTag(), nexthop, MessageType.Connected, new ArrayList<>(nodeId), Reason.ADD, false);
                addMessage(message, false);
            }
        }
    }

    public void computeOutMessage()
    {
        this._outMessage.clear();
        this._outMessage.addAll(this._stageOutMessage);
        this._stageOutMessage.clear();
    }
}
