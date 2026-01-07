package org.batfish.MEVNEW.EnsembleModel.IncrementalOperation;

import org.batfish.MEVNEW.EnsembleModel.Message.TunnelType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public class AddCustomer {
    public RouteDistinguisher _rd;
    public Integer _as;
    public String _routerName;
    public String _vrfName;
    public Prefix _prefix;
    public ExtendedCommunity _rt;
    public Integer _vni;
    public TunnelType _tunnelEndPointType;
    public Ip _tunnelIp;

    public AddCustomer(String routerName, String vrfName, Prefix prefix, ExtendedCommunity rt, Integer vni, RouteDistinguisher rd, Integer as) {
        _routerName = routerName;
        _vrfName = vrfName;
        _prefix = prefix;
        _rt = rt;
        _vni = vni;
        _as = as;
        _rd = rd;
    }

    public TunnelType getTunnelEndPointType() {
        return _tunnelEndPointType;
    }

    public Integer getAs()
    {
        return _as;
    }

    public RouteDistinguisher getRd()
    {
        return _rd;
    }

    public Ip getTunnelIp() {
        return _tunnelIp;
    }

    public String getAddRouter()
    {
        return _routerName;
    }

    public String getAddVrf()
    {
        return _vrfName;
    }

    public Prefix getAddPrefix()
    {
        return _prefix;
    }

    public ExtendedCommunity getAddRT()
    {
        return _rt;
    }

    public Integer getAddVni()
    {
        return _vni;
    }

    public void setTunnelIp(Ip tunnelIp) {
        _tunnelIp = tunnelIp;
    }

    public void setTunnelEndPointType(TunnelType tunnelEndPointType) {
        _tunnelEndPointType = tunnelEndPointType;
    }
}