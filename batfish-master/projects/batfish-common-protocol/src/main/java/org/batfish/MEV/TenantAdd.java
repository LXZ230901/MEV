package org.batfish.MEV;




//


import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

import java.util.List;
import java.util.Set;

public class TenantAdd {
    public List<VRFAdd> _addVrfList;
    public TenantAdd() {}
    public void addUser(String router, String vrf, Set<ExtendedCommunity> rt, RouteDistinguisher rd, Integer vni, Set<Prefix> subNet)
    {
        VRFAdd vrfAdd = new VRFAdd(router, vrf, rt, rd, vni, subNet);
        this._addVrfList.add(vrfAdd);
    }

    public List<VRFAdd> getAddUser()
    {
        return this._addVrfList;
    }

}
