package org.batfish.MEV;


import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.List;

public class DestinationPair {
    public String _dstRouter;
    public String _dstVrf;
    public List<Prefix> _dstPrefix;
    public DestinationPair(String dstRouter, String dstVrf, List<Prefix> dstPrefix)
    {
        this._dstRouter = dstRouter;
        this._dstVrf = dstVrf;
        this._dstPrefix = new ArrayList<>();
        this._dstPrefix.addAll(dstPrefix);
    }

    public String getDstRouter()
    {
        return this._dstRouter;
    }

    public String getDstVrf()
    {
        return this._dstVrf;
    }

    public List<Prefix> getDstPrefix()
    {
        return this._dstPrefix;
    }
}
