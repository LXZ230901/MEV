package org.batfish.MEVNEW.EnsembleModel.Message;

import java.util.Objects;

public abstract class Nexthop {
    public NexthopType _type;

    public Integer _vni = 0;

    public Nexthop() {}

    public Nexthop(NexthopType type, Integer vni)
    {
        this._type = type;
        this._vni = vni;
    }

    public void setType(NexthopType type)
    {
        this._type = type;
    }

    public void setVni(Integer vni)
    {
        this._vni = vni;
    }

    public NexthopType getNexthopType()
    {
        return this._type;
    }

    public Integer getVni()
    {
        return this._vni;
    }

    public abstract Builder<? extends Builder<?>> toBuilder();

    public static abstract class Builder<T extends Builder<T>>
    {
        protected NexthopType _type;

        protected Integer _vni;

        public T setNexthopType(NexthopType type)
        {
            this._type = type;
            return self();
        }

        public T setVni(Integer vni)
        {
            this._vni = vni;
            return self();
        }

        protected abstract T self();

        public abstract Nexthop build();
    }



    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nexthop nexthop = (Nexthop) o;
        return Objects.equals(this._type, nexthop.getNexthopType()) && Objects.equals(this._vni, nexthop.getVni());
    }

    @Override
    public int hashCode()
    {
        int result = _type.hashCode() + 32 * _vni.hashCode();
        return result;
    }
}
