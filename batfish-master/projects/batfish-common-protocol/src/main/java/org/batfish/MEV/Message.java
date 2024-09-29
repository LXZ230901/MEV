package org.batfish.MEV;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message {
    public Prefix _prefix;
    public Attribute _attribute;
    public List<Dependency> _routingDependency;
    public List<Dependency> _forwardingDependency;
    public RedistributionTag _redistributionTag;
    public Nexthop _nexthop;
    public Reason _reason;
    public MessageType _type;


    public List<Integer> _visitedNode;
    public boolean _reflector;








    public Message(Prefix prefix, Attribute attribute, Nexthop nexthop, MessageType type, Reason reason){
        this._prefix = prefix;
        this._attribute = attribute;
        this._nexthop = nexthop;
        this._type = type;
        this._reason = reason;
        this._routingDependency = new ArrayList<>();
        this._forwardingDependency = new ArrayList<>();
        this._visitedNode = new ArrayList<>();
        this._reflector = false;
    }

    public Message(Prefix prefix, Attribute attribute, List<Dependency> routingDependency, List<Dependency> forwardingDependency, RedistributionTag redistributionTag, Nexthop nexthop, MessageType type, List<Integer> visitNodes, Reason reason, boolean reflector)
    {
        this._prefix = prefix;
        this._attribute = attribute;
        this._routingDependency = routingDependency;
        this._forwardingDependency = forwardingDependency;
        this._redistributionTag = redistributionTag;
        this._nexthop = nexthop;
        this._reason = reason;
        this._visitedNode = new ArrayList<>(visitNodes);
        this._type = type;
        this._reflector = reflector;
    }

    public void setReason(Reason reason)
    {
        this._reason = reason;
    }



    public Prefix getPrefix() {
        return this._prefix;
    }

    public Attribute getAttribute() {
        return this._attribute;
    }

    public List<Dependency> getRoutingDependency() {
        return this._routingDependency;
    }

    public List<Dependency> getForwardingDependency() {return this._forwardingDependency;}

    public Nexthop getNexthop() {
        return this._nexthop;
    }

    public Reason getReason()
    {
        return this._reason;
    }

    public List<Integer> getVisitedNode()
    {
        return this._visitedNode;
    }

    public RedistributionTag getRedistributionTag()
    {
        return this._redistributionTag;
    }

    public void setPrefix(Prefix prefix) {
        this._prefix = prefix;
    }

    public void setAttribute(Attribute attribute) {
        this._attribute = attribute;
    }

    public void setRoutingDependency(List<Dependency> dependency) {
        this._routingDependency = dependency;
    }

    public void setForwardingDependency(List<Dependency> dependency) {this._forwardingDependency = dependency;}

    public void setNexthop(Nexthop nexthop) {
        this._nexthop = nexthop;
    }

    public void setRedistributionTag(RedistributionTag redistributionTag)
    {
        this._redistributionTag = redistributionTag;
    }

    public MessageType getMessageType()
    {
        return this._type;
    }

    public void addVisitNode(Integer visitNodeId)
    {
        this._visitedNode.add(visitNodeId);
    }

    public List<Integer> getVisitNodes()
    {
        return this._visitedNode;
    }

    public boolean getReflector()
    {
        return this._reflector;
    }

    public void setReflector(boolean reflector)
    {
        this._reflector = reflector;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Message message = (Message) o;
        return Objects.equals(_prefix, message.getPrefix()) &&
                Objects.equals(_attribute, message.getAttribute()) &&
                Objects.equals(_routingDependency, message.getRoutingDependency()) &&
                Objects.equals(_forwardingDependency, message.getForwardingDependency()) &&
                Objects.equals(_redistributionTag, message.getRedistributionTag()) &&
                Objects.equals(_nexthop, message.getNexthop()) &&
                Objects.equals(_reflector, message.getReflector());
     }

     @Override
     public int hashCode()
     {
         int result = _prefix.hashCode();
         result = result * 31 + _attribute.hashCode();
         result = result * 31 + _routingDependency.hashCode();
         result = result * 31 + _forwardingDependency.hashCode();
         result = result * 31 + _redistributionTag.hashCode();
         result = result * 31 + _nexthop.hashCode();
         return result;
     }

    // Builder class
    public static class Builder {
        private Prefix _prefix;
        private Attribute _attribute;
        private List<Dependency> _routingDependency;
        private List<Dependency> _forwardingDependency;
        private RedistributionTag _redistributionTag;
        private Nexthop _nexthop;
        private Reason _reason;
        private MessageType _type;
        private List<Integer> _visitNode;
        private boolean _reflector;

        public Builder() {}

        public Builder setVisitNode(List<Integer> visitNodes)
        {
            this._visitNode = new ArrayList<>(visitNodes);
            return this;
        }

        public Builder setPrefix(Prefix prefix) {
            this._prefix = prefix;
            return this;
        }

        public Builder setAttribute(Attribute attribute) {
            this._attribute = attribute;
            return this;
        }

        public Builder setRoutingDependency(List<Dependency> dependency) {
            this._routingDependency = new ArrayList<>(dependency);
            return this;
        }

        public Builder setForwardingDependency(List<Dependency> dependency)
        {
            this._forwardingDependency = new ArrayList<>(dependency);
            return this;
        }

        public Builder addRoutingDependency(List<Dependency> dependency) {
            this._routingDependency.addAll(dependency);
            return this;
        }

        public Builder addForwardingDependency(List<Dependency> dependency)
        {
            this._forwardingDependency.addAll(dependency);
            return this;
        }

        public Builder setNexthop(Nexthop nexthop) {
            this._nexthop = new Nexthop(nexthop._type, nexthop._router, nexthop._Ip);
            return this;
        }

        public Builder setReason(Reason reason)
        {
            this._reason = reason;
            return this;
        }

        public Builder setRedistributionTag(RedistributionTag redistributionTag)
        {
            this._redistributionTag = redistributionTag;
            return this;
        }

        public Builder setMessageType(MessageType type)
        {
            this._type = type;
            return this;
        }

        public Builder setReflector(boolean reflector)
        {
            this._reflector = reflector;
            return this;
        }

        public boolean getReflector()
        {
            return this._reflector;
        }

        public Prefix getPrefix()
        {
            return this._prefix;
        }

        public Attribute getAttribute()
        {
            return this._attribute;
        }

        public Nexthop getNexthop()
        {
            return this._nexthop;
        }

        public RedistributionTag getRedistributionTag()
        {
            return this._redistributionTag;
        }

        public MessageType getMessageType()
        {
            return this._type;
        }

        public Message build() {
            return new Message(_prefix, _attribute, _routingDependency, _forwardingDependency, _redistributionTag, _nexthop, _type, _visitNode, _reason, _reflector);
        }
    }

    public void setMessageType(MessageType messageType)
    {
        this._type = messageType;
    }

    public Message.Builder toBuilder() {
        return new Message.Builder()
                .setPrefix(_prefix)
                .setAttribute(_attribute.toBuilder().build())
                .setRoutingDependency(_routingDependency)
                .setForwardingDependency(_forwardingDependency)
                .setRedistributionTag(_redistributionTag)
                .setNexthop(_nexthop)
                .setReason(_reason)
                .setMessageType(_type)
                .setVisitNode(_visitedNode)
                .setReflector(_reflector);
    }
}
