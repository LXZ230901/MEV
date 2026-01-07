package org.batfish.MEVNEW.EnsembleModel.Message;



public class StaticAttribute extends Attribute {


    public StaticAttribute(int AD)
    {
        super(AD, 0);
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
             public StaticAttribute build() {
                 return new StaticAttribute(_AD);
             }
         }
    @Override
    public Builder toBuilder()
    {
        return new Builder(this._AD);
    }
}
