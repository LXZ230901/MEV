package org.batfish.MEVNEW.EnsembleVerification;

import org.batfish.MEVNEW.EnsembleModel.Dependency;
import org.batfish.MEVNEW.EnsembleVerification.FIB.NestedDependency;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Packet {
    private Prefix _prefix;
    private String _srcRouter;
    private String _srcVrf;
    private NestedDependency _Dep;
    private Integer _vni;
    private String _stateRouter;
    private String _stateVrf;
    private boolean _verified;
    private Set<String> _realatedVrf;
    private List<RouterVrfPair> _forwardingPath;
    public Packet(String srcRouter, String srcVrf, Prefix prefix)
    {
        this._prefix = prefix;
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._Dep = new NestedDependency();
        this._stateRouter = srcRouter;
        this._stateVrf = srcVrf;
        this._vni = 0;
        this._verified = false;
        this._realatedVrf = new HashSet<>();
        this._forwardingPath = new ArrayList<>();
    }
    // Constructor
    public Packet(Prefix prefix, String srcRouter, String srcVrf, List<Dependency> RDep, List<Dependency> FDep, Integer vni, List<RouterVrfPair> forwardingPath) {
        this._prefix = prefix;
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._Dep = new NestedDependency();
        Set<Dependency> addDep = new HashSet<>();
        addDep.addAll(RDep);
        addDep.addAll(FDep);
        this._Dep.orDependency(addDep);
        this._vni = vni;
        this._realatedVrf = new HashSet<>();
        this._forwardingPath = new ArrayList<>();
        this._forwardingPath.addAll(forwardingPath);
    }

    public Prefix getPrefix() {
        return _prefix;
    }

    public String getSrcRouter() {
        return _srcRouter;
    }

    public String getSrcVrf() {
        return _srcVrf;
    }

    public Integer getVni() {
        return _vni;
    }

    public String getStateRouter()
    {
        return this._stateRouter;
    }

    public String getStateVrf()
    {
        return this._stateVrf;
    }

    public boolean getVerified()
    {
        return this._verified;
    }

    public NestedDependency getDep()
    {
        return this._Dep;
    }

    public Set<String> getRelatedVrf()
    {
        return this._realatedVrf;
    }

    public List<RouterVrfPair> getForwardingPath()
    {
        return this._forwardingPath;
    }

    public void setPrefix(Prefix prefix) {
        this._prefix = prefix;
    }

    public void setSrcRouter(String srcRouter) {
        this._srcRouter = srcRouter;
    }

    public void setSrcVrf(String srcVrf) {
        this._srcVrf = srcVrf;
    }

    public void setVni(Integer vni) {
        this._vni = vni;
    }

    public void setStateRouter(String stateRouter)
    {
        this._stateRouter = stateRouter;
    }

    public void setStateVrf(String stateVrf)
    {
        this._stateVrf = stateVrf;
    }

    public void setRelateVrf(Set<String> relateVrf)
    {
        this._realatedVrf.addAll(relateVrf);
    }

    public void setVerified(boolean verified)
    {
        this._verified = verified;
    }

    public void addDep(NestedDependency nestedDependency)
    {
        this._Dep.andDependency(nestedDependency.getNestedDependency());
    }

    public Packet toNewPacket()
    {
        NestedDependency Dep = this._Dep.toNewNestedDependency();
        Packet packet = new Packet(this._srcRouter, this._srcVrf, this._prefix);
        packet.addDep(Dep);
        packet.setVni(this._vni);
        packet.setStateRouter(this._stateRouter);
        packet.setStateVrf(this._stateVrf);
        packet.setRelateVrf(this._realatedVrf);
        return packet;
    }

    public void addForwardingPath(RouterVrfPair routerVrfPair)
    {
        this._forwardingPath.add(routerVrfPair);
    }

    public void setForwardingPath(List<RouterVrfPair> forwardingPath)
    {
        this._forwardingPath = new ArrayList<>();
        this._forwardingPath.addAll(forwardingPath);
    }
    @Override
    public String toString() {
        return "Packet{" +
                "_prefix=" + _prefix +
                ", _srcRouter='" + _srcRouter + '\'' +
                ", _srcVrf='" + _srcVrf + '\'' +
                ", _Dep=" + _Dep +
                ", _vni=" + _vni +
                '}';
    }
}
