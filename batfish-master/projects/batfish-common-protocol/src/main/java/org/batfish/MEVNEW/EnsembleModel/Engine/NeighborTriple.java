package org.batfish.MEVNEW.EnsembleModel.Engine;

import org.batfish.MEVNEW.EnsembleModel.Edge.EdgeOperation;
import org.batfish.datamodel.Ip;


public abstract class NeighborTriple {
    private Ip _neighborIp;
    private String _neighborRouter;
    private String _neighborVrf;
    private EdgeOperation _outOperation;
    private EdgeOperation _inOperation;


    public NeighborTriple(String neighborRouter, String neighborVrf)
    {
        this._neighborRouter = neighborRouter;
        this._neighborVrf = neighborVrf;
    }

    // Getter和Setter方法
    public String getNeighborRouter()
    {
        return this._neighborRouter;
    }

    public String getNeighborVrf()
    {
        return this._neighborVrf;
    }

    public EdgeOperation getOutOperation()
    {
        return this._outOperation;
    }

    public EdgeOperation getInOperation()
    {
        return this._inOperation;
    }

    public Ip getNeighborIp()
    {
        return this._neighborIp;
    }

    public void setNeighborRouter(String neighborRouter)
    {
        this._neighborRouter = neighborRouter;
    }

    public void setNeighborVrf(String neighborVrf)
    {
        this._neighborVrf = neighborVrf;
    }

    public void setOutOperation(EdgeOperation outOperation)
    {
        this._outOperation = outOperation;
    }

    public void setInOperation(EdgeOperation inOperation)
    {
        this._inOperation = inOperation;
    }

    public void setNeighborIp(Ip neighborIp)
    {
        this._neighborIp = neighborIp;
    }

    @Override
    public String toString() {
        return "NeighborTriple{" +
                "router='" + _neighborRouter + '\'' +
                ", vrf='" + _neighborVrf + '\'' +
                ", ip=" + _neighborIp +
                '}';
    }

    // 重写equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeighborTriple that = (NeighborTriple) o;

        if (!_neighborIp.equals(that._neighborIp)) return false;
        if (!_neighborRouter.equals(that._neighborRouter)) return false;
        return _neighborVrf.equals(that._neighborVrf);
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        int result = _neighborIp.hashCode();
        result = 31 * result + _neighborRouter.hashCode();
        result = 31 * result + _neighborVrf.hashCode();
        return result;
    }
}

