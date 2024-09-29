package org.batfish.MEV;

import org.batfish.datamodel.isis.IsisLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Edge {
    private Node _srcNode;
    private Node _dstNode;
    private List<Dependency> _dependency;
    private Long _cost;
    private EdgeOperation _outOperation;
    private EdgeOperation _inOperation;
    private String _vrf;

    private IsisLevel _isisType;


    // 无参构造方法
    public Edge() {}

    // 有参构造方法
    public Edge(Node srcNode, Node dstNode, List<Dependency> dependency, Long cost) {
        this._srcNode = srcNode;
        this._dstNode = dstNode;
        this._dependency = new ArrayList<>(dependency);  // 深度复制
        this._cost = cost;
        this._outOperation = new EdgeOperation();
        this._inOperation = new EdgeOperation();
    }

    // Getter方法
    public Node getSrcNode() {
        return this._srcNode;
    }

    public IsisLevel getTsisType()
    {
        return this._isisType;
    }

    public Node getDstNode() {
        return this._dstNode;
    }

    public List<Dependency> getDependency() {
        return new ArrayList<>(this._dependency);  // 返回深度复制
    }

    public Long getCost() {
        return this._cost;
    }

    // Setter方法
    public void setSrcNode(Node srcNode) {
        this._srcNode = srcNode;
    }

    public void setIsisType(IsisLevel isisType)
    {
        this._isisType = isisType;
    }

    public void setDstNode(Node dstNode) {
        this._dstNode = dstNode;
    }

    public void setDependency(List<Dependency> dependency) {
        this._dependency = new ArrayList<>(dependency);  // 深度复制
    }

    public void setCost(Long cost) {
        this._cost = cost;
    }

    public void setOutOperation(EdgeOperation outOperation)
    {
        this._outOperation = outOperation;
    }

    public EdgeOperation getOutOperation()
    {
        return this._outOperation;
    }

    public void setInOperation(EdgeOperation inOperation)
    {
        this._inOperation = inOperation;
    }

    public EdgeOperation getInOperation()
    {
        return this._inOperation;
    }

    public void setVrfName(String vrf)
    {
        this._vrf = vrf;
    }

    public String getVrfName()
    {
        return this._vrf;
    }

    public void mergeEdge(Edge edge)
    {
        this._outOperation.mergeOperation(edge.getOutOperation());
        this._inOperation.mergeOperation(edge.getInOperation());
    }

    public OperationAnswer processMessage(Message.Builder message, boolean maintainReflector)
    {
        message.addRoutingDependency(this._dependency);
        OperationAnswer outOperationAnswer = this._outOperation.processMessage(message);
        if (!maintainReflector)
        {
            outOperationAnswer.getMessage().setReflector(false);
        }
        if (outOperationAnswer.getFilter())
        {
            return outOperationAnswer;
        }
        if (this._outOperation.getNexthopIp() != null)
        {
            message.getNexthop().setType(NexthopType.Original);
            message.getNexthop().setIp(this._outOperation.getNexthopIp());
        }

        OperationAnswer inOperationAnswer = this._inOperation.processMessage(outOperationAnswer.getMessage());
        if (!maintainReflector)
        {
            inOperationAnswer.getMessage().setReflector(this._inOperation.getReflector());
        }
        return inOperationAnswer;
    }
    public IsolationOperationAnswer processIsolationMessage(IsoaltionMessage.Builder message)
    {
        IsolationOperationAnswer outOperationAnswer = this._outOperation.processMessage(message);
        outOperationAnswer.getMessage().setReflector(false);
        if (outOperationAnswer.getFilter())
        {
            return outOperationAnswer;
        }



        IsolationOperationAnswer inOperationAnswer = this._inOperation.processMessage(outOperationAnswer.getMessage());
        return inOperationAnswer;
    }
    // 重写toString方法
    @Override
    public String toString() {
        return "Edge{" +
                "srcNode=" + _srcNode +
                ", dstNode=" + _dstNode +
                ", dependency=" + _dependency +
                ", cost=" + _cost +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge that = (Edge) o;
        return Objects.equals(_srcNode, that._srcNode) &&
                Objects.equals(_dstNode, that._dstNode);
    }

    // 重写 hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(_srcNode, _dstNode);
    }
}
