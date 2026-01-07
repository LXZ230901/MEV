package org.batfish.MEVNEW.EnsembleVerification.FIB;

import org.batfish.MEVNEW.EnsembleModel.Dependency;
import org.batfish.MEVNEW.EnsembleVerification.FIB.FwdNexthop.ForwardingNexthop;
import org.batfish.datamodel.Prefix;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyFIBEntry {

    private Prefix _prefix;
    private ForwardingNexthop _nexthop;
    private Integer _vni;
    private NestedDependency _dep;


    public DependencyFIBEntry(Prefix prefix, List<Dependency> RDep, List<Dependency> FDep, ForwardingNexthop nexthop, Integer vni) {
        this._prefix = prefix;
        this._nexthop = nexthop;
        this._vni = vni;
        this._dep = new NestedDependency();
        Set<Dependency> addDep = new HashSet<>();
        addDep.addAll(RDep);
        addDep.addAll(FDep);
        this._dep.orDependency(addDep);
    }

    public DependencyFIBEntry(Prefix prefix, List<Dependency> FDep, ForwardingNexthop nexthop, Integer vni) {
        this._prefix = prefix;
        this._nexthop = nexthop;
        this._vni = vni;
        this._dep = new NestedDependency();
        Set<Dependency> addDep = new HashSet<>();
        addDep.addAll(FDep);
        this._dep.orDependency(addDep);
    }

    // Getters and Setters
    public Prefix getPrefix() {
        return _prefix;
    }

    public NestedDependency getDep()
    {
        return this._dep;
    }

    public ForwardingNexthop getNexthop() {
        return _nexthop;
    }

    public Integer getVni() {
        return _vni;
    }

    public void setPrefix(Prefix prefix) {
        this._prefix = prefix;
    }

    public void setNexthop(ForwardingNexthop nexthop) {
        this._nexthop = nexthop;
    }

    public void setVni(Integer vni) {
        this._vni = vni;
    }

    public boolean hasNexthop(ForwardingNexthop nexthop) {
        return this._nexthop.equals(nexthop);
    }

    public void mergeDependencyFibEntry(DependencyFIBEntry dependencyFIBEntry)
    {
        if (this._nexthop.equals(dependencyFIBEntry.getNexthop()))
        {
            this._dep.orDependencies(dependencyFIBEntry.getDep().getNestedDependency());
        }
    }

    @Override
    public String toString() {
        return "DependencyFIBEntry{" +
                "_prefix=" + _prefix +
                ", _dep=" + _dep +
                ", _nexthop=" + _nexthop +
                ", _vni=" + _vni +
                '}';
    }
}

