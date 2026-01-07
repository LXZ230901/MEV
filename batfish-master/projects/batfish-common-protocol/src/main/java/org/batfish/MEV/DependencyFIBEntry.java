package org.batfish.MEV;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Prefix;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyFIBEntry {

    private Prefix _prefix;
    private BDD _symbolicPrefix;
    private ForwardingNexthop _nexthop;
    private Integer _vni;
    private NestedDependency _Dep;

    // Constructor
    public DependencyFIBEntry(Prefix prefix, List<Dependency> RDep, List<Dependency> FDep, ForwardingNexthop nexthop, Integer vni) {
        this._prefix = prefix;
        this._nexthop = nexthop;
        this._vni = vni;
        this._Dep = new NestedDependency();
        Set<Dependency> addDep = new HashSet<>();
        addDep.addAll(RDep);
        addDep.addAll(FDep);
        this._Dep.orDependency(addDep);
    }

    public DependencyFIBEntry(Prefix prefix, List<Dependency> FDep, ForwardingNexthop nexthop, Integer vni) {
        this._prefix = prefix;
        this._nexthop = nexthop;
        this._vni = vni;
        this._Dep = new NestedDependency();
        Set<Dependency> addDep = new HashSet<>();
        addDep.addAll(FDep);
        this._Dep.orDependency(addDep);
    }

    // Getters and Setters
    public Prefix getPrefix() {
        return _prefix;
    }

    public void setPrefix(Prefix prefix) {
        this._prefix = prefix;
    }

    public NestedDependency getDep()
    {
        return this._Dep;
    }

    public ForwardingNexthop getNexthop() {
        return _nexthop;
    }

    public void setNexthop(ForwardingNexthop nexthop) {
        this._nexthop = nexthop;
    }

    public Integer getVni() {
        return _vni;
    }

    public void setVni(Integer vni) {
        this._vni = vni;
    }

    // Utility Methods

    /**
     * Checks if the entry has a specific nexthop.
     */
    public boolean hasNexthop(ForwardingNexthop nexthop) {
        return this._nexthop.equals(nexthop);
    }

    public void convertSymbolicPrefix(BDDFactory bddFactory)
    {
        // unimplemented
    }

    public void mergeDependencyFibEntry(DependencyFIBEntry dependencyFIBEntry)
    {
        if (this._nexthop.equals(dependencyFIBEntry.getNexthop()))
        {
            this._Dep.orDependencies(dependencyFIBEntry.getDep().getNestedDependency());
        }
    }

    /**
     * Displays the DependencyFIBEntry details as a string.
     */
    @Override
    public String toString() {
        return "DependencyFIBEntry{" +
                "_prefix=" + _prefix +
                ", _Dep=" + _Dep +
                ", _nexthop=" + _nexthop +
                ", _vni=" + _vni +
                '}';
    }
}

