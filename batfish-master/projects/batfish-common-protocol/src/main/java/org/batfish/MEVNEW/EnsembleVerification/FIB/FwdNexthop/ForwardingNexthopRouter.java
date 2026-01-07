package org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop;

public class ForwardingNexthopRouter extends ForwardingNexthop {

    private String _nexthopRouter;

    // Constructor
    public ForwardingNexthopRouter(ForwardingNexthopType nexthopType, String nexthopRouter) {
        super(nexthopType);
        this._nexthopRouter = nexthopRouter;
    }

    public String getNexthopRouter() {
        return _nexthopRouter;
    }

    public void setNexthopRouter(String nexthopRouter) {
        this._nexthopRouter = nexthopRouter;
    }
}

