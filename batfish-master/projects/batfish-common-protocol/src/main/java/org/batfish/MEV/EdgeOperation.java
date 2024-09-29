package org.batfish.MEV;




import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

import java.util.HashSet;
import java.util.Set;

public class EdgeOperation {
    public Set<ExtendedCommunity> _permitRTs;
    public Set<ExtendedCommunity> _attachRTs;
    public RouteDistinguisher _attachRD;
    public Set<Integer> _permitVnis;
    public Integer _attachVni;
    public RoutingPolicy _policies;
    public Environment.Direction _direction;
    public Set<MessageType> _blockMessageType;
    public Ip _setNexthopIp;
    public boolean _reSetEBGP;
    public boolean _reSetIBGP;
    public boolean _reflector;

    // Default constructor
    public EdgeOperation() {
        this._permitRTs = new HashSet<>();
        this._attachRTs = new HashSet<>();
        this._attachRD = null;
        this._permitVnis = new HashSet<>();
        this._attachVni = null;
        this._policies = null;
        this._direction = null;
        this._blockMessageType = new HashSet<>();
        this._setNexthopIp = null;
        this._reSetEBGP = false;
        this._reSetIBGP = false;
    }

    // Parameterized constructor
    public EdgeOperation(Set<ExtendedCommunity> permitRTs, Set<ExtendedCommunity> attachRTs, RouteDistinguisher attachRD,
                         Set<Integer> permitVnis, Integer attachVni, RoutingPolicy policies, Environment.Direction direction) {
        this._permitRTs = permitRTs != null ? new HashSet<>(permitRTs) : new HashSet<>();
        this._attachRTs = attachRTs != null ? new HashSet<>(attachRTs) : new HashSet<>();
        this._attachRD = attachRD;
        this._permitVnis = permitVnis != null ? new HashSet<>(permitVnis) : new HashSet<>();
        this._attachVni = attachVni != null ? attachVni : null;
        this._policies = policies != null ? policies : null;
        this._direction = direction;
        this._reflector = false;
    }




    public Set<ExtendedCommunity> getPermitRTs() {
        return _permitRTs;
    }

    public void setPermitRTs(Set<ExtendedCommunity> permitRTs) {
        this._permitRTs = permitRTs;
    }

    public Set<ExtendedCommunity> getAttachRTs() {
        return _attachRTs;
    }

    public void setAttachRTs(Set<ExtendedCommunity> attachRTs) {
        this._attachRTs = attachRTs;
    }

    public RouteDistinguisher getAttachRD() {
        return _attachRD;
    }

    public void setReflector(boolean reflector)
    {
        this._reflector = reflector;
    }


    public boolean getReflector()
    {
        return this._reflector;
    }

    public void setAttachRD(RouteDistinguisher attachRD) {
        this._attachRD = attachRD;
    }

    public Set<Integer> getPermitVnis() {
        return _permitVnis;
    }

    public void setPermitVnis(Set<Integer> permitVnis) {
        this._permitVnis = permitVnis;
    }

    public Integer getAttachVni() {
        return _attachVni;
    }

    public void setAttachVnis(Integer attachVni) {
        this._attachVni = attachVni;
    }

    public RoutingPolicy getPolicies() {
        return _policies;
    }

    public void setPolicies(RoutingPolicy policies) {
        this._policies = policies;
    }

    // Business logic methods
    public void addBlockRT(ExtendedCommunity rt) {
        _permitRTs.add(rt);
    }

    public void addPermitRTs(Set<ExtendedCommunity> rt) {
        _permitRTs.addAll(rt);
    }

    public void removeBlockRT(ExtendedCommunity rt) {
        _permitRTs.remove(rt);
    }

    public void addAttachRT(ExtendedCommunity rt) {
        _attachRTs.add(rt);
    }

    public void addAttachRTs(Set<ExtendedCommunity> rt) {
        _attachRTs.addAll(rt);
    }

    public void removeAttachRT(ExtendedCommunity rt) {
        _attachRTs.remove(rt);
    }

    public void addBlockVni(int vni) {
        _permitVnis.add(vni);
    }

    public void removeBlockVni(int vni) {
        _permitVnis.remove(vni);
    }

    public void setAttachVni(int vni) {
        _attachVni = vni;
    }

    public void addBlockMessageType(MessageType messageType)
    {
        _blockMessageType.add(messageType);
    }

    public void setNexthopIp(Ip nexthopIp)
    {
        _setNexthopIp = nexthopIp;
    }

    public Ip getNexthopIp()
    {
        return _setNexthopIp;
    }

    public Set<MessageType> getBlockMessageType()
    {
        return _blockMessageType;
    }

//    public void removeAttachVni(int vni) {
//        _attachVnis.remove(vni);
//    }

    public OperationAnswer processMessage(Message.Builder message)
    {
//        message.setReflector(this._reflector);


        if (this._reSetIBGP)
        {
            message.setMessageType(MessageType.iBGP);
            message.getAttribute().setAD(200);
        } else if (this._reSetEBGP)
        {
            message.setMessageType(MessageType.eBGP);
            message.getAttribute().setAD(20);
//            if (message.getAttribute() instanceof BgpAttribute)
//            {
//                BgpAttribute bgpAttribute = (BgpAttribute) message.getAttribute();
//                bgpAttribute.setType(MessageType.eBGP);
//                bgpAttribute.setLocalPreference(Long.valueOf(100));
//                bgpAttribute.setAD(20);
//                message.setMessageType(MessageType.eBGP);
//            } else {
//                EvpnAttribute evpnAttribute = (EvpnAttribute) message.getAttribute();
//                evpnAttribute.setType(MessageType.eBGP);
//                evpnAttribute.setLocalPreference(Long.valueOf(100));
//                evpnAttribute.setAD(20);
//            }
        }
        OperationAnswer operationAnswer = new OperationAnswer();
        operationAnswer.setMessage(message);
        operationAnswer.setFilter(false);
        if (!this._blockMessageType.isEmpty())
        {
            if (this._blockMessageType.contains(message.getMessageType()) && !message.getReflector())
            {
                operationAnswer.setFilter(true);
                return operationAnswer;
            }
//            if (message.getAttribute() instanceof BgpAttribute)
//            {
//                BgpAttribute bgpAttribute = (BgpAttribute) message.getAttribute();
//                if (this._blockMessageType.contains(bgpAttribute.getType()) && !bgpAttribute.getReflector())
//                {
//                    operationAnswer.setFilter(true);
//                    return operationAnswer;
//                }
//            } else {
//                EvpnAttribute evpnAttribute = (EvpnAttribute) message.getAttribute();
//                if (this._blockMessageType.contains(evpnAttribute.getType()) && !evpnAttribute.getReflector())
//                {
//                    operationAnswer.setFilter(true);
//                    return operationAnswer;
//                }
//            }
        }
        if (!this._permitVnis.isEmpty() && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            if (!this._permitVnis.contains(attribute.getVni()))
            {
                operationAnswer.setFilter(true);
                return operationAnswer;
            }
        }
        if (!this._permitRTs.isEmpty() && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            Set<ExtendedCommunity> communities = new HashSet<>(this._permitRTs);
            communities.retainAll(attribute.getRTs());
            if (communities.isEmpty())
            {
                operationAnswer.setFilter(true);
                return operationAnswer;
            }
        }
        if (this._attachVni != null && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.setVni(this._attachVni);
        }
        if (!this._attachRTs.isEmpty() && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.setRTs(this._attachRTs);
        }
        if (this._policies != null && !this._policies.process(message.build(), message, this._direction))
        {
            operationAnswer.setFilter(true);
        }
        operationAnswer.setMessage(message);
        return operationAnswer;
    }

    public IsolationOperationAnswer processMessage(IsoaltionMessage.Builder message)
    {
//        message.setReflector(this._reflector);
        if (this._reSetIBGP)
        {
            message.setMessageType(MessageType.iBGP);
        } else if (this._reSetEBGP)
        {
            message.setMessageType(MessageType.eBGP);
        }
        IsolationOperationAnswer operationAnswer = new IsolationOperationAnswer();
        operationAnswer.setMessage(message);
        operationAnswer.setFilter(false);
        if (!this._blockMessageType.isEmpty())
        {
            if (this._blockMessageType.contains(message.getMessageType()) && !message.getReflector())
            {
                operationAnswer.setFilter(true);
                return operationAnswer;
            }
        }
        if (!this._permitVnis.isEmpty() && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            if (!this._permitVnis.contains(attribute.getVni()))
            {
                operationAnswer.setFilter(true);
                return operationAnswer;
            }
        }
        if (!this._permitRTs.isEmpty() && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            Set<ExtendedCommunity> communities = new HashSet<>(this._permitRTs);
            communities.retainAll(attribute.getRTs());
            if (communities.isEmpty())
            {
                operationAnswer.setFilter(true);
                return operationAnswer;
            }
        }
        if (this._attachVni != null && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.setVni(this._attachVni);
        }
        if (!this._attachRTs.isEmpty() && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.setRTs(this._attachRTs);
        }
//        if (this._policies != null && !this._policies.process(message.build(), message, this._direction))
//        {
//            operationAnswer.setFilter(true);
//        }
        operationAnswer.setMessage(message);
        message.setReflector(this._reflector);
        return operationAnswer;
    }

    public void mergeOperation(EdgeOperation operation)
    {
        this._attachRTs.addAll(operation.getAttachRTs());
        this._permitRTs.addAll(operation.getPermitRTs());
        this._attachVni = operation.getAttachVni();
        this._permitVnis.addAll(operation.getPermitVnis());
    }

    @Override
    public String toString() {
        return "EdgeOperation{" +
                "_permitRTs=" + _permitRTs +
                ", _attachRTs=" + _attachRTs +
                ", _attachRD=" + _attachRD +
                ", _permitVnis=" + _permitVnis +
                ", _attachVnis=" + _attachVni +
                ", _policies=" + _policies +
                '}';
    }

    public void reSetEBGP()
    {
        this._reSetEBGP = true;
    }

    public void reSetIBGP()
    {
        this._reSetIBGP = true;
    }

    public boolean getReSetEBGP()
    {
        return this._reSetEBGP;
    }

    public boolean getReSetIBGP()
    {
        return this._reSetIBGP;
    }

    public void addPermitVni(Integer vni)
    {
        this._permitVnis.add(vni);
    }
}
