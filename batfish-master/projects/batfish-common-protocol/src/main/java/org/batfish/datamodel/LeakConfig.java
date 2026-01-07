package org.batfish.datamodel;

import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

import java.util.HashSet;
import java.util.Set;

public class LeakConfig {
    public Set<ExtendedCommunity> _exportRouteTargets;
    public Set<ExtendedCommunity> _importRouteTargets;
    public RouteDistinguisher _exportRouteDistinguisher;

    public LeakConfig()
    {
        this._exportRouteTargets = new HashSet<>();
        this._importRouteTargets = new HashSet<>();
    }

    public void setExportRouteTargets(Set<ExtendedCommunity> exportRouteTargets)
    {
        this._exportRouteTargets.addAll(exportRouteTargets);
    }

    public void setExportRouteDistinguisher(RouteDistinguisher routeDistinguisher)
    {
        this._exportRouteDistinguisher = routeDistinguisher;
    }

    public void setImportRouteTargets(Set<ExtendedCommunity> importRouteTargets)
    {
        this._importRouteTargets.addAll(importRouteTargets);
    }

    public Set<ExtendedCommunity> getExportRouteTargets()
    {
        return this._exportRouteTargets;
    }

    public RouteDistinguisher getExportRouteDistinguisher()
    {
        return this._exportRouteDistinguisher;
    }

    public Set<ExtendedCommunity> getImportRouteTargets()
    {
        return this._importRouteTargets;
    }
}
