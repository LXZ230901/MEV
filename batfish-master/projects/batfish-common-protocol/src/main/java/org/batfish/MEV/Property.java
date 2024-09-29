package org.batfish.MEV;

import org.batfish.datamodel.Prefix;

import java.util.Objects;

public class Property {
    public String _srcRouter;
    public String _srcVrf;
    public String _dstRouter;
    public String _dstVrf;
    public Prefix _dstPrefix;

    public Property(String srcRouter, String srcVrf, String dstRouter, String dstVrf, Prefix dstPrefix) {
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._dstRouter = dstRouter;
        this._dstVrf = dstVrf;
        this._dstPrefix = dstPrefix;
    }

    // Getter 方法
    public String getSrcRouter() {
        return _srcRouter;
    }

    public String getSrcVrf() {
        return _srcVrf;
    }

    public String getDstRouter() {
        return _dstRouter;
    }

    public String getDstVrf() {
        return _dstVrf;
    }

    public Prefix getDstPrefix() {
        return _dstPrefix;
    }

    // Setter 方法
    public void setSrcRouter(String srcRouter) {
        this._srcRouter = srcRouter;
    }

    public void setSrcVrf(String srcVrf) {
        this._srcVrf = srcVrf;
    }

    public void setDstRouter(String dstRouter) {
        this._dstRouter = dstRouter;
    }

    public void setDstVrf(String dstVrf) {
        this._dstVrf = dstVrf;
    }

    public void setDstPrefix(Prefix dstPrefix) {
        this._dstPrefix = dstPrefix;
    }

    // toString 方法
    @Override
    public String toString() {
        return "Property{" +
                "srcRouter='" + _srcRouter + '\'' +
                ", srcVrf='" + _srcVrf + '\'' +
                ", dstRouter='" + _dstRouter + '\'' +
                ", dstVrf='" + _dstVrf + '\'' +
                ", dstPrefix=" + _dstPrefix +
                '}';
    }



    // equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return _srcRouter.equals(property._srcRouter) &&
                _srcVrf.equals(property._srcVrf) &&
                _dstRouter.equals(property._dstRouter) &&
                _dstVrf.equals(property._dstVrf) &&
                _dstPrefix.equals(property._dstPrefix);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(_srcRouter, _srcVrf, _dstRouter, _dstVrf, _dstPrefix);
    }
}
