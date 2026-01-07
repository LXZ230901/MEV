package org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop;

public abstract class ForwardingNexthop {

    private ForwardingNexthopType _nexthopType;

    public ForwardingNexthop(ForwardingNexthopType nexthopType) {
        this._nexthopType = nexthopType;
    }

    // Getters and Setters
    public ForwardingNexthopType getNexthopType() {
        return _nexthopType;
    }

    public void setNexthopType(ForwardingNexthopType nexthopType) {
        this._nexthopType = nexthopType;
    }

    @Override
    public String toString() {
        return "ForwardingNexthop" +
                "_nexthopType=" + _nexthopType;
    }
}

