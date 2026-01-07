package org.batfish.MEVNEW.EnsembleModel.Message;


import org.batfish.datamodel.Ip;

public class NexthopIp extends Nexthop{
    private Ip _ip;

    public NexthopIp(NexthopType type, Ip ip, Integer vni)
    {
        super(type, vni);
        this._ip = ip;
    }

    public Ip getNexthopIp()
    {
        return this._ip;
    }

    public void setNexthopIp(Ip ip)
    {
        this._ip = ip;
    }

    public static class Builder extends Nexthop.Builder<Builder>
    {
        private NexthopType _type;
        private Ip _ip;
        private Integer _vni;
        @Override
        protected Builder self()
        {
            return this;
        }

        public Ip getNexthopIp()
        {
            return _ip;
        }

        public Builder setType(NexthopType type)
        {
            this._type = type;
            return this;
        }

        public Builder setNexthopIp(Ip ip)
        {
            this._ip = ip;
            return this;
        }

        public Builder setVni(Integer vni)
        {
            this._vni = vni;
            return this;
        }

        @Override
        public NexthopIp build()
        {
            return new NexthopIp(_type, _ip, _vni);
        }
    }

    @Override
    public NexthopIp.Builder toBuilder()
    {
        return new NexthopIp.Builder()
                .setType(_type)
                .setNexthopIp(_ip)
                .setVni(_vni);
    }
}
