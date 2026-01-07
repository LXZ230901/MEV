package org.batfish.MEVNEW.EnsembleModel.Message;


import org.batfish.datamodel.Ip;

public class NexthopTunnel extends Nexthop{
    private TunnelType _tunnelType;
    private Ip _encapIp;
    private String _router;
    public NexthopTunnel(NexthopType nexthopType, TunnelType tunnelType, Ip encapIp, String router, Integer vni)
    {
        super(nexthopType, 0);
        this._tunnelType = tunnelType;
        this._encapIp = encapIp;
        this._router = router;
        this._vni = vni;
    }

    public TunnelType getTunnelType()
    {
        return _tunnelType;
    }

    public Ip getEncapIp()
    {
        return _encapIp;
    }

    public Integer getVni()
    {
        return _vni;
    }

    public String getEncapRouter()
    {
        return this._router;
    }

    public void setTunnelType(TunnelType tunnelType)
    {
        this._tunnelType = tunnelType;
    }

    public void setEncapIp(Ip encapIp)
    {
        this._encapIp = encapIp;
    }

    public void setVni(Integer vni)
    {
        this._vni = vni;
    }

    public void setEncapRouter(String router)
    {
        this._router = router;
    }

    public static class Builder extends Nexthop.Builder<Builder>
    {
        private NexthopType _type;
        private TunnelType _tunnelType;
        private Ip _encapIp;
        private String _router;
        private Integer _vni;
        @Override
        protected Builder self()
        {
            return this;
        }

        public Ip getEncapIp()
        {
            return _encapIp;
        }

        public String getEncapRouter()
        {
            return _router;
        }

        public Builder setType(NexthopType type)
        {
            this._type = type;
            return this;
        }

        public Builder setEncapRouter(String router)
        {
            this._router = router;
            return this;
        }

        public Builder setTunnelType(TunnelType tunnelType)
        {
            this._tunnelType = tunnelType;
            return this;
        }

        public Builder setEncapIp(Ip encapIp)
        {
            this._encapIp = encapIp;
            return this;
        }

        public Builder setVni(Integer vni)
        {
            this._vni = vni;
            return this;
        }

        @Override
        public NexthopTunnel build()
        {
            return new NexthopTunnel(_type, _tunnelType, _encapIp, _router, _vni);
        }
    }

    @Override
    public Builder toBuilder()
    {
        return new Builder()
                .setType(_type)
                .setTunnelType(_tunnelType)
                .setEncapIp(_encapIp)
                .setEncapRouter(_router)
                .setVni(_vni);
    }
}
