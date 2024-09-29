package org.batfish.MEV;

import org.batfish.datamodel.Ip;

import java.util.Objects;

public class Nexthop {
    public NexthopType _type;
    public EncapType _encapType;
    public String _router;
    public Ip _Ip;

    public Nexthop() {}
    public Nexthop(NexthopType type, String router, Ip ip)
    {
        this._type = type;
        this._router = router;
        this._Ip = ip;
    }

    public void setType(NexthopType type)
    {
        this._type = type;
    }

    public void setRouter(String router)
    {
        this._router = router;
    }

    public void setIp(Ip ip)
    {
        this._Ip = ip;
    }

    public NexthopType getNexthopType()
    {
        return this._type;
    }

    public String getNexthopRouter()
    {
        return this._router;
    }

    public Ip getNexthopIp()
    {
        return this._Ip;
    }

    public void setEncapType(EncapType encapType)
    {
        this._encapType = encapType;
    }

    public EncapType getEncapType()
    {
        return this._encapType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nexthop nexthop = (Nexthop) o;
        return Objects.equals(this._type, nexthop.getNexthopType()) &&
                Objects.equals(this._router, nexthop.getNexthopRouter()) &&
                Objects.equals(this._Ip, nexthop.getNexthopIp());
    }

    @Override
    public int hashCode()
    {
        int result = _type.hashCode();
        result = result * 31 + _router.hashCode();
        result = result * 31 + _Ip.hashCode();
        return result;
    }
}
