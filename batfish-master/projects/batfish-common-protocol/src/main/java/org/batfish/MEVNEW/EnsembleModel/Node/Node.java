package org.batfish.MEVNEW.EnsembleModel.Node;

import org.batfish.MEVNEW.EnsembleModel.Message.Message;
import org.batfish.datamodel.Prefix;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class Node {
    private Integer _id;
    private NodeType _nodeType;
    private String _router;
    private String _vrf;
    private String _name;
    private VRFRib _vrfrib;
    private boolean _incrementChange;
    private Set<Prefix> _announcePrefix;
    // 有参构造方法
    public Node(int id, NodeType type, String router, String name, String vrf) {
        this._id = id;
        this._nodeType = type;
        this._router = router;
        this._name = name;
        this._vrf = vrf;
        this._vrfrib = new VRFRib();
        this._incrementChange = false;
        this._announcePrefix = new HashSet<>();
    }

    // Getter方法

    public int getID() {
        return this._id;
    }

    public NodeType getNodeType() {
        return this._nodeType;
    }

    public String getRouter() {
        return this._router;
    }

    public String getName() {
        return this._name;
    }

    public String getVrf()
    {
        return this._vrf;
    }

    public VRFRib getVrfrib()
    {
        return this._vrfrib;
    }

    public boolean getIncrementChange()
    {
        return this._incrementChange;
    }

    public Set<Prefix> getAnnouncePrefix()
    {
        return this._announcePrefix;
    }

    // Setter方法
    public void setID(int id) {
        this._id = id;
    }

    public void setNodeType(NodeType type) {
        this._nodeType = type;
    }

    public void setRouter(String router) {
        this._router = router;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setVrf(String vrf)
    {
        this._vrf = vrf;
    }

    public void setIncrementChange(boolean incrementChange)
    {
        this._incrementChange = incrementChange;
    }

    public void setAnnouncePrefix(List<Prefix> announcePrefix)
    {
        this._announcePrefix.addAll(announcePrefix);
    }

    public void addAnnouncePrefix(Prefix prefix)
    {
        this._announcePrefix.add(prefix);
    }

    public abstract void attributeMessage(Message.Builder message);

    public abstract List<Message.Builder> attributeMessage(List<Message.Builder> message);

    // 重写toString方法
    @Override
    public String toString() {
        return "Node{" +
                "id=" + _id +
                ", type=" + _nodeType +
                ", router='" + _router + '\'' +
                ", vrf='" + _vrf + '\'' +
                ", name='" + _name + '\'' +
                '}';
    }

    // 重写equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return _id.equals(node._id) &&
                Objects.equals(_nodeType, node._nodeType) &&
                Objects.equals(_router, node._router) &&
                Objects.equals(_vrf, node._vrf) &&
                Objects.equals(_name, node._name);
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        int result = Integer.hashCode(_id);
        result = 31 * result + (_nodeType != null ? _nodeType.hashCode() : 0);
        result = 31 * result + (_router != null ? _router.hashCode() : 0);
        result = 31 * result + (_vrf != null ? _vrf.hashCode() : 0);
        result = 31 * result + (_name != null ? _name.hashCode() : 0);
        return result;
    }
}
