package org.batfish.MEV;

public class RedistributionTag {

    private String _router;
    private String _vrf;
    private String _protocolLayer;

    // Default Constructor
    public RedistributionTag() {
    }

    // Constructor with Parameters
    public RedistributionTag(String router, String vrf, String protocolLayer) {
        this._router = router;
        this._vrf = vrf;
        this._protocolLayer = protocolLayer;
    }

    // Getters and Setters
    public String getRouter() {
        return _router;
    }

    public void setRouter(String router) {
        this._router = router;
    }

    public String getVrf() {
        return _vrf;
    }

    public void setVrf(String vrf) {
        this._vrf = vrf;
    }

    public String getProtocolLayer() {
        return _protocolLayer;
    }

    public void setProtocolLayer(String protocolLayer) {
        this._protocolLayer = protocolLayer;
    }

    // Utility Methods

    /**
     * Checks if the RedistributionTag matches the given router, VRF, and protocol layer.
     */
    public boolean matches(String router, String vrf, String protocolLayer) {
        return this._router.equals(router) &&
                this._vrf.equals(vrf) &&
                this._protocolLayer.equals(protocolLayer);
    }

    /**
     * Displays the RedistributionTag details as a string.
     */
    @Override
    public String toString() {
        return "RedistributionTag{" +
                "_router='" + _router + '\'' +
                ", _vrf='" + _vrf + '\'' +
                ", _protocolLayer='" + _protocolLayer + '\'' +
                '}';
    }
}

