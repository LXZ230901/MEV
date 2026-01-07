package org.batfish.MEVNEW.EnsembleModel.Message;

import java.util.Objects;

public abstract class Attribute {

    public int _AD;

    public Integer _vni = 0;

    public Attribute (int AD, Integer vni)
    {
        this._AD = AD;
        this._vni = vni;
    }


    public int getAD()
    {
        return this._AD;
    }

    public void setAD(int AD)
    {
        this._AD = AD;
    }

    public void setVni(Integer vni)
    {
        this._vni = vni;
    }

    public Integer getVni() {
        return _vni;
    }

    public abstract int comparePriority(Attribute attribute);

    public static abstract class Builder<T extends Builder<T>> {
        protected int _AD;

        protected Integer _vni = 0;

        public T setAD(int AD) {
            this._AD = AD;
            return self();
        }

        protected abstract T self();

        public abstract Attribute build();

        public T setVni(int vni)
        {
            this._vni = vni;
            return self();
        }
    }

    public abstract Builder<? extends Builder<?>> toBuilder();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return _AD == attribute._AD && Objects.equals(_vni, attribute._vni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_AD) + 32 * Objects.hash(_vni);
    }
}
