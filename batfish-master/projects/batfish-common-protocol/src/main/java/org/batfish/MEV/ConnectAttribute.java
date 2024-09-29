package org.batfish.MEV;

public class ConnectAttribute extends Attribute{


    public ConnectAttribute(int AD)
    {
        super(AD);
    }

    @Override
    public int comparePriority(Attribute attribute) {
        if (!attribute.getClass().equals(getClass())) {
            return Integer.compare(attribute.getAD(), getAD());
        }
        return 0;
    }

    public static class Builder extends Attribute.Builder<Builder>{

        public Builder(Integer AD)
        {
            this._AD = AD;
        }

             @Override
             protected Builder self() {
                 return this;
             }

             @Override
             public ConnectAttribute build() {
                 return new ConnectAttribute(_AD);
             }
         }
    @Override
    public Builder toBuilder()
    {
        return new Builder(this._AD);
    }
}
