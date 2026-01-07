package org.batfish.MEV;



import java.util.Objects;


public class IsisAttribute extends Attribute{


    public MessageType _type;
    public Long _cost;
    public IsisAttribute(int AD, Long cost)
    {
        super(AD);
        this._cost = cost;
    }

    public Long getCost() {
        return this._cost;
    }

    public void setCost(Long cost)
    {
        this._cost = cost;
    }


    @Override
    public int comparePriority(Attribute attribute) {
        if (!attribute.getClass().equals(getClass())) {
            return Integer.compare(attribute.getAD(), getAD());
        }
        IsisAttribute attr = (IsisAttribute) attribute;

        // Compare MED values
        return Long.compare(attr.getCost(), this._cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IsisAttribute that = (IsisAttribute) o;
        return Objects.equals(_cost, that.getCost()) &&
                Objects.equals(getAD(), that.getAD());
    }

    @Override
    public int hashCode() {
        int result = getAD();
        result = result * 31 + _cost.hashCode();
        return result;
    }

    public static class Builder extends Attribute.Builder<Builder>{
        public Integer _AD;
        public Long _cost;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder(Integer AD)
        {
         this._AD = AD;
        }

        public Long getCost() {
            return _cost;
        }


        public Builder setCost(Long cost) {
            this._cost = cost;
            return this;
        }

        public IsisAttribute build() {
            return new IsisAttribute(_AD, _cost);
        }
    }

    @Override
    public Builder toBuilder()
    {
        return new Builder(this._AD)
                .setCost(this._cost);
    }

    public void setType(MessageType type)
    {
        this._type = type;
    }

    public MessageType getType()
    {
        return this._type;
    }
}
