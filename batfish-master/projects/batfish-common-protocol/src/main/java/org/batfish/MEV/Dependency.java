package org.batfish.MEV;

import org.batfish.datamodel.Ip;

import java.util.Objects;

public class Dependency {

    public String _srcRouter;
    public String _srcVrf;
    public String _dstRouter;
    public String _dstVrf;
    public Ip _dstIP;

    public Dependency(String srcRouter, String srcVrf, String dstRotuer, String dstVrf, Ip dstIP)
    {
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._dstRouter = dstRotuer;
        this._dstVrf = dstVrf;
        this._dstIP = dstIP;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(_srcRouter, that._srcRouter) &&
                Objects.equals(_srcVrf, that._srcVrf) &&
                Objects.equals(_dstRouter, that._dstRouter) &&
                Objects.equals(_dstVrf, that._dstVrf) &&
                Objects.equals(_dstIP, that._dstIP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_srcRouter, _srcVrf, _dstRouter, _dstVrf, _dstIP);
    }
}
