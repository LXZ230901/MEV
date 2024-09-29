package org.batfish.MEV;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class Node {
    private int _id;
    private NodeType _type;
    private String _router;
    private String _name;
    private List<Message> _outMessage;
    private List<Message> _stageOutMessage;

    private Set<IsoaltionMessage> _isolationReceivedMessage;
    private Set<IsoaltionMessage> _stageIsolationUpdateMessage;
    private Set<IsoaltionMessage> _isolationUpdateMessage;

    private boolean _incrementChange;

    // 有参构造方法
    public Node(int id, NodeType type, String router, String name) {
        this._id = id;
        this._type = type;
        this._router = router;
        this._name = name;
        this._outMessage = new ArrayList<>();
        this._stageOutMessage = new ArrayList<>();
        this._isolationReceivedMessage = new HashSet<>();
        this._stageIsolationUpdateMessage = new HashSet<>();
        this._isolationUpdateMessage = new HashSet<>();

        this._incrementChange = false;
    }

    // Getter方法
    public List<Message> getStageOutMessage()
    {
        return this._stageOutMessage;
    }

    public int getID() {
        return this._id;
    }

    public NodeType getNodeType() {
        return this._type;
    }

    public String getRouter() {
        return this._router;
    }

    public String getName() {
        return this._name;
    }

    public List<Message> getOutMessage()
    {
        return this._outMessage;
    }

    // Setter方法
    public void setID(int id) {
        this._id = id;
    }

    public void setNodeType(NodeType type) {
        this._type = type;
    }

    public void setRouter(String router) {
        this._router = router;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void addStageOutMessage(Message message)
    {
        this._stageOutMessage.add(message.toBuilder().build());
    }

    public void clearStageOutMessage()
    {
        this._stageOutMessage.clear();
    }

    public abstract void attributeMessage(Message message);

    public abstract List<Message> attributeMessage(List<Message> message);

    public Set<IsoaltionMessage> getIsolationUpdateMessage()
    {
        return this._isolationUpdateMessage;
    }

    public Set<IsoaltionMessage> getIsolationReceivedMessage()
    {
        return this._isolationReceivedMessage;
    }


    public void convertOutMessage()
    {
        // 直接使用属性消息列表
        List<Message> updatedMessages = attributeMessage(new ArrayList<>(this._stageOutMessage));
        // 使用 copy-on-write 代替 clear 和 addAll，减少潜在的性能瓶颈
        this._outMessage = new ArrayList<>(updatedMessages);
    }

    public void convertOutMessgae(List<Message> messageList)
    {
        List<Message> updatedMessages = attributeMessage(new ArrayList<>(messageList));
        // 使用 copy-on-write 代替 clear 和 addAll，减少潜在的性能瓶颈
        this._outMessage = new ArrayList<>(updatedMessages);
    }

    public void clearOutMessage()
    {
        this._outMessage.clear();
    }

    // 重写toString方法
    @Override
    public String toString() {
        return "Node{" +
                "id=" + _id +
                ", type=" + _type +
                ", router='" + _router + '\'' +
                ", name='" + _name + '\'' +
                '}';
    }

    // 重写equals方法

    public void convertIsolationMessage()
    {
        this._isolationUpdateMessage.clear();
        this._isolationUpdateMessage.addAll(this._stageIsolationUpdateMessage);
        this._stageIsolationUpdateMessage.clear();
    }
    @Override

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return _id == node._id &&
                Objects.equals(_type, node._type) &&
                Objects.equals(_router, node._router) &&
                Objects.equals(_name, node._name);
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        int result = Integer.hashCode(_id);
        result = 31 * result + (_type != null ? _type.hashCode() : 0);
        result = 31 * result + (_router != null ? _router.hashCode() : 0);
        result = 31 * result + (_name != null ? _name.hashCode() : 0);
        return result;
    }

    public void addIsolationReceivedMessage(Set<IsoaltionMessage> message)
    {
        this._isolationReceivedMessage.addAll(message);
    }

    // 添加抽象方法
    public void addStageIsolationUpdatedMessage(Set<IsoaltionMessage> message)
    {
        this._stageIsolationUpdateMessage.addAll(message);
    }

    public void addIsolationUpdatedMessage(Set<IsoaltionMessage> message)
    {
        this._isolationUpdateMessage.addAll(message);
    }

    public void addIsolationReceivedMessage(IsoaltionMessage message)
    {
        this._isolationReceivedMessage.add(message);
    }

    // 添加抽象方法
    public void addStageIsolationUpdatedMessage(IsoaltionMessage message)
    {
        this._stageIsolationUpdateMessage.add(message);
    }

    public void addIsolationUpdatedMessage(IsoaltionMessage message)
    {
        this._isolationUpdateMessage.add(message);
    }


    public void setIncrementChange(boolean incrementChange)
    {
        this._incrementChange = incrementChange;
    }


    public boolean getIncrementChange()
    {
        return this._incrementChange;
    }
}

