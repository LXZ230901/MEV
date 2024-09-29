package org.batfish.MEV;


import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

import java.util.HashSet;
import java.util.Set;

public class VRFAdd {
    String _router;
    String _vrf;
    public Set<ExtendedCommunity> _attachRTs;
    public RouteDistinguisher _attachRD;
    public Integer _permitVnis;
    public Set<Prefix> _subNet;


    public VRFAdd(String router, String vrf, Set<ExtendedCommunity> attachRT, RouteDistinguisher attachRD, Integer attatchVni, Set<Prefix> subNet)
    {
        this._router = router;
        this._vrf = vrf;
        this._attachRTs = new HashSet<>();
        this._attachRTs.addAll(attachRT);
        this._attachRD = attachRD;
        this._permitVnis = attatchVni;
        this._subNet = new HashSet<>();
        this._subNet.addAll(subNet);
    }

    public String getRouter()
    {
        return this._router;
    }

    public String getVrf()
    {
        return this._vrf;
    }

    public Set<ExtendedCommunity> getRT()
    {
        return this._attachRTs;
    }












    public RouteDistinguisher getRD()
    {
        return this._attachRD;
    }

    public Integer getVni()
    {
        return this._permitVnis;
    }

    public Set<Prefix> getSubNet()
    {
        return this._subNet;
    }
}
