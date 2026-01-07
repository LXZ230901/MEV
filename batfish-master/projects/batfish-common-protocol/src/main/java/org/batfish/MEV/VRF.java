package org.batfish.MEV;

//import org.batfish.dataplane.rib.Rib;

import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class VRF extends Node{

    public HashMap<Prefix, List<Message>> _rib;

    public HashSet<Prefix> _changedPrefix;

    public HashMap<Prefix, List<Message>> _connectMessages;

    public Integer _vni;

    public Set<Prefix> _networkPrefix;

    public VRF(int id, NodeType type, String router, String name) {
        super(id, type, router, name);
        this._rib = new HashMap<>();
        this._changedPrefix = new HashSet<>();
        this._connectMessages = new HashMap<>();
        this._networkPrefix = new HashSet<>();
    }

    public HashMap<Prefix,List<Message>> getRib()
    {
        return this._rib;
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

    @Override
    public void attributeMessage(Message message)
    {
        return;
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
                this.setIncrementChange(incremental);
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
                    this.setIncrementChange(incremental);
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

    public HashMap<Prefix, List<Message>> getConnectMessages()
    {
        return this._connectMessages;
    }

    public void setAssociatedVNI(Integer vni)
    {
        this._vni = vni;
    }

    public Integer getAssociatedVNI()
    {
        return this._vni;
    }

    public void addNetworkPrefix(Prefix prefix)
    {
        this._networkPrefix.add(prefix);
    }

    public void addNetworkPrefix(List<Prefix> prefixes)
    {
        this._networkPrefix.addAll(prefixes);
    }

    public Set<Prefix> getNetworkPrefix()
    {
        return this._networkPrefix;
    }

    public void initRib(GraphType graphType)
    {
        if (graphType.equals(GraphType.BGP))
        {
            for (Prefix prefix : _networkPrefix)
            {
                BgpAttribute bgpAttribute = new BgpAttribute(0, new HashSet<>(), AsPath.of(new ArrayList<>()), Long.valueOf(100), 0);
                bgpAttribute.setType(MessageType.eBGP);
                bgpAttribute.setAD(20);
                Nexthop nexthop = new Nexthop(NexthopType.Local, getName(), null);
                Message message = new Message(prefix, bgpAttribute, new ArrayList<>(), new ArrayList<>(), new RedistributionTag(), nexthop, MessageType.Connected, new ArrayList<>(getID()), Reason.ADD, false);
                addMessage(message, false);
            }
        }else if (graphType.equals(GraphType.EVPNL3VPN))
        {
            for (Prefix prefix : _networkPrefix)
            {
                EvpnAttribute evpnAttribute = new EvpnAttribute(0, new HashSet<>(), AsPath.of(new ArrayList<>()), Long.valueOf(100), 0, new HashSet<>(), _vni);
                evpnAttribute.setType(MessageType.eBGP);
                evpnAttribute.setAD(20);
                Nexthop nexthop = new Nexthop(NexthopType.Local, getName(), null);
                Message message = new Message(prefix, evpnAttribute, new ArrayList<>(), new ArrayList<>(), new RedistributionTag(), nexthop, MessageType.Connected, new ArrayList<>(getID()), Reason.ADD, false);
                addMessage(message, false);
            }
        }
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

    public void initIsolation()
    {
        for (Prefix prefix : getNetworkPrefix())
        {
            EvpnAttribute evpnAttribute = new EvpnAttribute(20, new HashSet<>(), AsPath.of(new ArrayList<>()), Long.valueOf(100), 0, new HashSet<>(), _vni);
            evpnAttribute.setType(MessageType.eBGP);
            IsoaltionMessage isoaltionMessage = new IsoaltionMessage(prefix, evpnAttribute, getRouter(), getName(), MessageType.eBGP);
            isoaltionMessage.getVisitedNode().add(getID());
            addIsolationReceivedMessage(isoaltionMessage);
            addIsolationUpdatedMessage(isoaltionMessage);
        }
    }
}
