package org.batfish.MEV;

import java.util.Objects;

public abstract class Attribute {

    public int _AD;
    public Attribute (int AD)
    {
        this._AD = AD;
    }
    public void setAD(int AD)
    {
        this._AD = AD;
    }

    public int getAD()
    {
        return this._AD;
    }

    public abstract int comparePriority(Attribute attribute);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return _AD == attribute._AD;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_AD);
    }



    public static abstract class Builder<T extends Builder<T>> {
        protected int _AD;

        public T setAD(int AD) {
            this._AD = AD;
            return self();
        }

        protected abstract T self();

        public abstract Attribute build();
    }

    public abstract Builder<? extends Builder<?>> toBuilder();
}
