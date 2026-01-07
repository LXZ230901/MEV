package org.batfish.MEVNEW.EnsembleModel.Message;



public class NexthopRouter extends Nexthop{
    private String _router;

    public NexthopRouter(NexthopType type, String router, Integer vni)
    {
        super(type, vni);
        this._router = router;
    }

    public String getNexthopRouter()
    {
        return this._router;
    }

    public void setNexthopRouter(String router)
    {
        this._router = router;
    }

    public static class Builder extends Nexthop.Builder<Builder>
    {
        private Integer _vni;
        private NexthopType _type;
        private String _router;

        @Override
        protected Builder self()
        {
            return this;
        }

        public String getNexthopRouter()
        {
            return _router;
        }

        public Builder setVni(Integer vni)
        {
            this._vni = vni;
            return this;
        }

        public Builder setType(NexthopType type)
        {
            this._type = type;
            return this;
        }

        public Builder setNexthopRouter(String router)
        {
            this._router = router;
            return this;
        }

        @Override
        public NexthopRouter build()
        {
            return new NexthopRouter(_type, _router, _vni);
        }
    }

    @Override
    public NexthopRouter.Builder toBuilder()
    {
        return new NexthopRouter.Builder()
                .setType(_type)
                .setNexthopRouter(_router)
                .setVni(_vni);
    }
}
