package org.batfish.MEVNEW.EnsembleModel.Message;

public class NexthopLocal extends Nexthop{
    private String _router;

    public NexthopLocal(NexthopType type, String router)
    {
        super(type, 0);
        this._router = router;
    }

    public String getNexthopRouter()
    {
        return this._router;
    }

    public void setNexthopRotuer(String router)
    {
        this._router = router;
    }

    public static class Builder extends Nexthop.Builder<Builder>
    {
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
        public NexthopLocal build()
        {
            return new NexthopLocal(_type, _router);
        }
    }

    @Override
    public Builder toBuilder()
    {
        return new Builder()
                .setType(_type)
                .setNexthopRouter(_router);
    }
}
