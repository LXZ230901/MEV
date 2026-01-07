package org.batfish.MEV;

import net.sf.javabdd.BDD;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Packet {
    private Prefix _prefix;
    private BDD _symbolicPrefix;
    private String _srcRouter;
    private String _srcVrf;
    private NestedDependency _Dep;
    private List<RedistributionDependency> _ReDep;
    private Integer _vni;
    private String _stateRouter;
    private String _stateVrf;
    private boolean _verified;
    private Set<String> _realatedVrf;
    public Packet(String srcRouter, String srcVrf, Prefix prefix)
    {
        this._prefix = prefix;
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._Dep = new NestedDependency();
        this._stateRouter = srcRouter;
        this._stateVrf = srcVrf;
        this._ReDep = new ArrayList<>();
        this._vni = 0;
        this._verified = false;
        this._realatedVrf = new HashSet<>();
    }
    // Constructor
    public Packet(Prefix prefix, BDD dstPrefix, String srcRouter, String srcVrf, List<Dependency> RDep, List<Dependency> FDep, Integer vni) {
        this._prefix = prefix;
        this._symbolicPrefix = dstPrefix;
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._Dep = new NestedDependency();
        Set<Dependency> addDep = new HashSet<>();
        addDep.addAll(RDep);
        addDep.addAll(FDep);
        this._Dep.orDependency(addDep);
        this._vni = vni;
        this._realatedVrf = new HashSet<>();
    }

    // Getters and Setters
    public Prefix getPrefix() {
        return _prefix;
    }

    public void setPrefix(Prefix prefix) {
        this._prefix = prefix;
    }

    public BDD getSymbolicDstPrefix() {
        return _symbolicPrefix;
    }

    public void setSymbolicDstPrefix(BDD dstPrefix) {
        this._symbolicPrefix = dstPrefix;
    }

    public String getSrcRouter() {
        return _srcRouter;
    }

    public void setSrcRouter(String srcRouter) {
        this._srcRouter = srcRouter;
    }

    public String getSrcVrf() {
        return _srcVrf;
    }

    public List<RedistributionDependency> getReDep()
    {
        return this._ReDep;
    }

    public void setSrcVrf(String srcVrf) {
        this._srcVrf = srcVrf;
    }

    public Integer getVni() {
        return _vni;
    }

    public void setVni(Integer vni) {
        this._vni = vni;
    }

    public void setReDep(List<RedistributionDependency> ReDep)
    {
        this._ReDep = ReDep;
    }

    public void addRedistributionDependency(RedistributionDependency ReDep)
    {
        this._ReDep.add(ReDep);
    }

    public void setStateRouter(String stateRouter)
    {
        this._stateRouter = stateRouter;
    }

    public String getStateRouter()
    {
        return this._stateRouter;
    }

    public void setStateVrf(String stateVrf)
    {
        this._stateVrf = stateVrf;
    }

    public String getStateVrf()
    {
        return this._stateVrf;
    }

    public boolean getVerified()
    {
        return this._verified;
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

    public NestedDependency getDep()
    {
        return this._Dep;
    }

    public void addRelatedVrf(String vrf)
    {
        this._realatedVrf.add(vrf);
    }


    public Set<String> getRelatedVrf()
    {
        return this._realatedVrf;
    }
    /**
     * Displays the packet details as a string.
     */
    @Override
    public String toString() {
        return "Packet{" +
                "_prefix=" + _prefix +
                ", _dstPrefix=" + _symbolicPrefix +
                ", _srcRouter='" + _srcRouter + '\'' +
                ", _srcVrf='" + _srcVrf + '\'' +
                ", _Dep=" + _Dep +
                ", _vni=" + _vni +
                '}';
    }
}
