package org.batfish.MEVNEW.EnsembleModel.IncrementalOperation;


import org.batfish.datamodel.Prefix;

import java.util.HashSet;
import java.util.Set;

public class UpdateRouterVrfPrefixPair {
    public String _router;
    public String _vrf;
    public Set<Prefix> _prefix;

    public UpdateRouterVrfPrefixPair(String router, String vrf)
    {
        _router = router;
        _vrf = vrf;
        _prefix = new HashSet<>();
    }
    public Set<Prefix> getUpdatePrefixes()
    {
        return _prefix;
    }

    public String getUpdateRouter()
    {
        return _router;
    }

    public String getUpdateVrf()
    {
        return _vrf;
    }

    public void addUpdatePrefixes(Set<Prefix> updatePrefixes)
    {
        _prefix.addAll(updatePrefixes);
    }

    public void addUpdatePrefix(Prefix updatePrefix)
    {
        _prefix.add(updatePrefix);
    }
}
