package org.batfish.MEV;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.isis.IsisLevel;

import java.util.List;

public class NeighborTriple {
    private Ip _remoteIP;
    private String _router;
    private String _vrf;
    private EdgeOperation _outOperation;
    private EdgeOperation _inOperation;
    private BGP _bgpNode;
    private NVE _nveNode;
    private ISIS _isisNode;
    private IsisLevel _isisType;
    private Long _isisCost;
    private boolean _reflector;
    private List<Ip> _localIpList;
    // 无参构造方法
    public NeighborTriple() {}

    // 有参构造方法
    public NeighborTriple(String router, String vrf, ISIS node, IsisLevel isisType, Long isisCost)
    {
        this._router = router;
        this._vrf = vrf;
        this._isisNode = node;
        this._isisType = isisType;
        this._isisCost = isisCost;
        this._reflector = false;
    }

    public NeighborTriple(String router, String vrf, Ip ip, BGP node) {
        this._router = router;
        this._vrf = vrf;
        this._remoteIP = ip;
        this._bgpNode = node;
        this._reflector = false;
    }

    public NeighborTriple(String router, String vrf, Ip ip, NVE node) {
        this._router = router;
        this._vrf = vrf;
        this._remoteIP = ip;
        this._nveNode = node;
        this._reflector = false;
    }

    // Getter和Setter方法
    public Ip getNeighborIp() {
        return _remoteIP;
    }

    public void setNeighborIp(Ip ip) {
        this._remoteIP = ip;
    }

    public String getRouter() {
        return _router;
    }

    public void setRouter(String router) {
        this._router = router;
    }

    public String getVrf() {
        return _vrf;
    }

    public void setVrf(String vrf) {
        this._vrf = vrf;
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

    public void setBgpNode(BGP node)
    {
        this._bgpNode = node;
    }

    public BGP getBgpNode()
    {
        return this._bgpNode;
    }

    public void setNveNode(NVE node)
    {
        this._nveNode = node;
    }

    public NVE getNveNode()
    {
        return this._nveNode;
    }

    public ISIS getIsisNode()
    {
        return this._isisNode;
    }

    public IsisLevel getIsisType()
    {
        return this._isisType;
    }

    // 重写toString方法
    public Long getIsisCost()
    {
        return this._isisCost;
    }

    public List<Ip> getLocalIp()
    {
        return this._localIpList;
    }

    public void setLocalIp(List<Ip> localIp)
    {
        this._localIpList = localIp;
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
    public String toString() {
        return "NeighborTriple{" +
                "router='" + _router + '\'' +
                ", vrf='" + _vrf + '\'' +
                ", ip=" + _remoteIP +
                '}';
    }

    // 重写equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeighborTriple that = (NeighborTriple) o;

        if (!_remoteIP.equals(that._remoteIP)) return false;
        if (!_router.equals(that._router)) return false;
        return _vrf.equals(that._vrf);
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        int result = _remoteIP.hashCode();
        result = 31 * result + _router.hashCode();
        result = 31 * result + _vrf.hashCode();
        return result;
    }
}

