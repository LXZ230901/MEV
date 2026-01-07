package org.batfish.MEV;

public class ForwardingNexthop {

    private ForwardingNexthopType _nexthopType;
    private String _nexthopRouter;
    private String _nexthopLayer;

    public ForwardingNexthop() {}

    // Constructor
    public ForwardingNexthop(ForwardingNexthopType nexthopType, String nexthopRouter, String nexthopLayer) {
        this._nexthopType = nexthopType;
        this._nexthopRouter = nexthopRouter;
        this._nexthopLayer = nexthopLayer;
    }

    // Getters and Setters
    public ForwardingNexthopType getNexthopType() {
        return _nexthopType;
    }

    public void setNexthopType(ForwardingNexthopType nexthopType) {
        this._nexthopType = nexthopType;
    }

    public String getNexthopRouter() {
        return _nexthopRouter;
    }

    public void setNexthopRouter(String nexthopRouter) {
        this._nexthopRouter = nexthopRouter;
    }

    public String getNexthopLayer() {
        return _nexthopLayer;
    }

    public void setNexthopLayer(String nexthopLayer) {
        this._nexthopLayer = nexthopLayer;
    }

    // Utility Methods

    /**
     * Checks if the nexthop is of a specific type.
     */
    public boolean isType(ForwardingNexthopType type) {
        return this._nexthopType.equals(type);
    }

    /**
     * Displays the ForwardingNexthop details as a string.
     */
    @Override
    public String toString() {
        return "ForwardingNexthop{" +
                "_nexthopType=" + _nexthopType +
                ", _nexthopRouter='" + _nexthopRouter + '\'' +
                ", _nexthopLayer='" + _nexthopLayer + '\'' +
                '}';
    }
}

