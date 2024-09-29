package org.batfish.MEV;


import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IsoaltionMessage {
    public Prefix _prefix;
    public Attribute _attribute;
    public String _srcRouter;

    public List<Integer> _visitedNode;
    public boolean _reflector;


    public String _srcVrf;

    public MessageType _type;


    public IsoaltionMessage(Prefix prefix, Attribute attribute, String srcRouter, String srcVrf, MessageType type){
        this._prefix = prefix;
        this._attribute = attribute;
        this._visitedNode = new ArrayList<>();
        this._reflector = false;
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._type = type;
    }

    public IsoaltionMessage(Prefix prefix, Attribute attribute, List<Integer> visitNodes, boolean reflector, String srcRouter, String srcVrf, MessageType type)
    {
        this._prefix = prefix;
        this._attribute = attribute;
        this._visitedNode = new ArrayList<>(visitNodes);
        this._reflector = reflector;
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._type = type;
    }




    public Prefix getPrefix() {
        return this._prefix;
    }

    public Attribute getAttribute() {
        return this._attribute;
    }

    public List<Integer> getVisitedNode()
    {
        return this._visitedNode;
    }


    public void setPrefix(Prefix prefix) {
        this._prefix = prefix;
    }

    public void setAttribute(Attribute attribute) {
        this._attribute = attribute;
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

    public MessageType getMessageType()
    {
        return this._type;
    }

    public void setMessageType(MessageType type)
    {
        this._type = type;
    }

    public String getSrcRouter()
    {
        return this._srcRouter;
    }

    public String getSrcVrf()
    {
        return this._srcVrf;
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

        IsoaltionMessage message = (IsoaltionMessage) o;
        return Objects.equals(_prefix, message.getPrefix()) &&
                Objects.equals(_srcRouter, message.getSrcRouter()) &&
                Objects.equals(_srcVrf, message.getSrcVrf());
    }

    @Override
    public int hashCode()
    {
        int result = _prefix.hashCode();
        result = result * 31 + _srcRouter.hashCode();
        result = result * 31 + _srcVrf.hashCode();
        return result;
    }

    // Builder class
    public static class Builder {
        private Prefix _prefix;
        private Attribute _attribute;
        private List<Integer> _visitNode;
        private boolean _reflector;
        private String _srcRouter;
        private String _srcVrf;

        private MessageType _type;
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

        public Builder setReflector(boolean reflector)
        {
            this._reflector = reflector;
            return this;
        }

        public Builder setMessageType(MessageType type)
        {
            this._type = type;
            return this;
        }
        public Builder setSrcRotuer(String srcRouter)
        {
            this._srcRouter = srcRouter;
            return this;
        }

        public Builder setSrcVrf(String srcVrf)
        {
            this._srcVrf = srcVrf;
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

        public void addVisitNode(Integer nodeId)
        {
            this._visitNode.add(nodeId);
        }

        public MessageType getMessageType()
        {
            return this._type;
        }

        public IsoaltionMessage build() {
            return new IsoaltionMessage(_prefix, _attribute, _visitNode, _reflector, _srcRouter, _srcVrf, _type);
        }
    }



    public IsoaltionMessage.Builder toBuilder() {
        return new IsoaltionMessage.Builder()
                .setPrefix(_prefix)
                .setAttribute(_attribute.toBuilder().build())
                .setVisitNode(_visitedNode)
                .setReflector(_reflector)
                .setSrcRotuer(_srcRouter)
                .setSrcVrf(_srcVrf)
                .setMessageType(_type);
    }
}
