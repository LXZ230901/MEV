package org.batfish.MEVNEW.EnsembleModel.Edge;


import org.batfish.MEVNEW.EnsembleModel.Message.BgpAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.EvpnAttribute;
import org.batfish.MEVNEW.EnsembleModel.Message.Message;
import org.batfish.MEVNEW.EnsembleModel.Message.MessageType;
import org.batfish.MEVNEW.EnsembleModel.Message.Nexthop;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

import java.util.HashSet;
import java.util.Set;

public class EdgeOperation {
    public Environment.Direction _direction;
    public RoutingPolicy _policies;
    public Set<ExtendedCommunity> _permitRTs;
    public Set<ExtendedCommunity> _attachRTs;
    public RouteDistinguisher _attachRD;
    public Integer _attachVni;
    public Set<MessageType> _blockMessageType;
    public Long _addAS;
    public Nexthop _setNexthop;
    public boolean _reSetEBGP;
    public boolean _reSetIBGP;
    public boolean _reflector;

    // Default constructor
    public EdgeOperation() {
        this._permitRTs = new HashSet<>();
        this._attachRTs = new HashSet<>();
        this._attachRD = null;
        this._attachVni = null;
        this._policies = null;
        this._direction = null;
        this._blockMessageType = new HashSet<>();
        this._setNexthop = null;
        this._reSetEBGP = false;
        this._reSetIBGP = false;
        this._addAS = null;
    }

    // Parameterized constructor
    public EdgeOperation(Set<ExtendedCommunity> permitRTs, Set<ExtendedCommunity> attachRTs, RouteDistinguisher attachRD,
                         Set<Integer> permitVnis, Integer attachVni, RoutingPolicy policies, Environment.Direction direction) {
        this._permitRTs = permitRTs != null ? new HashSet<>(permitRTs) : new HashSet<>();
        this._attachRTs = attachRTs != null ? new HashSet<>(attachRTs) : new HashSet<>();
        this._attachRD = attachRD;
        this._attachVni = attachVni != null ? attachVni : null;
        this._policies = policies != null ? policies : null;
        this._direction = direction;
        this._reflector = false;
    }




    //getter

    public Set<ExtendedCommunity> getPermitRTs() {
        return _permitRTs;
    }

    public Set<ExtendedCommunity> getAttachRTs() {
        return _attachRTs;
    }

    public RouteDistinguisher getAttachRD() {
        return _attachRD;
    }

    public boolean getReflector()
    {
        return this._reflector;
    }

    public Integer getAttachVni() {
        return _attachVni;
    }

    public RoutingPolicy getPolicies() {
        return _policies;
    }

    public Nexthop getNexthop()
    {
        return _setNexthop;
    }

    public Set<MessageType> getBlockMessageType()
    {
        return _blockMessageType;
    }

    public boolean getReSetEBGP()
    {
        return this._reSetEBGP;
    }

    public boolean getReSetIBGP()
    {
        return this._reSetIBGP;
    }

    public Long getAddAS()
    {
        return this._addAS;
    }

    // setter
    public void setPermitRTs(Set<ExtendedCommunity> permitRTs) {
        this._permitRTs = permitRTs;
    }

    public void setAttachRTs(Set<ExtendedCommunity> attachRTs) {
        this._attachRTs = attachRTs;
    }

    public void setReflector(boolean reflector)
    {
        this._reflector = reflector;
    }

    public void setAttachRD(RouteDistinguisher attachRD) {
        this._attachRD = attachRD;
    }

    public void setAttachVnis(Integer attachVni) {
        this._attachVni = attachVni;
    }

    public void setPolicies(RoutingPolicy policies) {
        this._policies = policies;
    }

    public void setAddAS(Long addAS)
    {
        this._addAS = addAS;
    }

    public void setAttachVni(int vni) {
        _attachVni = vni;
    }

    public void setNexthop(Nexthop nexthop)
    {
        _setNexthop = nexthop;
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

    public void addBlockMessageType(MessageType messageType)
    {
        _blockMessageType.add(messageType);
    }

    public void reSetEBGP()
    {
        this._reSetEBGP = true;
    }

    public void reSetIBGP()
    {
        this._reSetIBGP = true;
    }

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
        }
        OperationAnswer operationAnswer = new OperationAnswer();
        operationAnswer.setMessage(message);
        operationAnswer.setFilter(false);
        if (!this._blockMessageType.isEmpty())
        {
            if (this._blockMessageType.contains(message.getMessageType()) && !this._reflector)
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
        if (this._attachVni != null && (message.getAttribute() instanceof BgpAttribute))
        {
            message.getAttribute().setVni(this._attachVni);
        } else if (this._attachVni != null && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.setVni(this._attachVni);
        } else {
            message.getAttribute().setVni(0);
        }
        if (!this._attachRTs.isEmpty() && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.setRTs(this._attachRTs);
        }
        if (this._setNexthop != null)
        {
            message.setNexthop(this._setNexthop);
        }
        if (this._policies != null && !this._policies.process(message.build(), message, this._direction))
        {
            operationAnswer.setFilter(true);
        }
        operationAnswer.setMessage(message);
        if (this._attachRD != null && (message.getAttribute() instanceof EvpnAttribute))
        {
            EvpnAttribute attribute = (EvpnAttribute) message.getAttribute();
            attribute.setRD(this._attachRD);
        }
        return operationAnswer;
    }

    public void mergeOperation(EdgeOperation operation)
    {
        this._attachRTs.addAll(operation.getAttachRTs());
        this._permitRTs.addAll(operation.getPermitRTs());
        this._attachVni = operation.getAttachVni();
    }

    @Override
    public String toString() {
        return "EdgeOperation{" +
                "_permitRTs=" + _permitRTs +
                ", _attachRTs=" + _attachRTs +
                ", _attachRD=" + _attachRD +
                ", _attachVnis=" + _attachVni +
                ", _policies=" + _policies +
                '}';
    }
}
