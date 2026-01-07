package org.batfish.MEV;

import java.util.List;

import java.util.ArrayList;

public class RedistributionDependency {

    private Property _property;
    private List<Dependency> _RDep;
    private List<Dependency> _FDep;
    private String _layer;

    // Default Constructor
    public RedistributionDependency() {
        this._RDep = new ArrayList<>();
        this._FDep = new ArrayList<>();
    }

    // Constructor with Parameters
    public RedistributionDependency(Property property, List<Dependency> RDep, List<Dependency> FDep, String layer) {
        this._property = property;
        this._RDep = RDep != null ? RDep : new ArrayList<>();
        this._FDep = FDep != null ? FDep : new ArrayList<>();
        this._layer = layer;
    }

    // Getters and Setters
    public Property getProperty() {
        return _property;
    }

    public void setProperty(Property property) {
        this._property = property;
    }

    public List<Dependency> getRDep() {
        return _RDep;
    }

    public void setRDep(List<Dependency> RDep) {
        this._RDep = RDep;
    }

    public List<Dependency> getFDep() {
        return _FDep;
    }

    public void setFDep(List<Dependency> FDep) {
        this._FDep = FDep;
    }

    public String getLayer() {
        return _layer;
    }

    public void setLayer(String layer) {
        this._layer = layer;
    }

    // Utility Methods

    /**
     * Adds a Dependency to the RDep list.
     */
    public void addRDep(Dependency dependency) {
        this._RDep.add(dependency);
    }

    /**
     * Adds a Dependency to the FDep list.
     */
    public void addFDep(Dependency dependency) {
        this._FDep.add(dependency);
    }

    /**
     * Removes a Dependency from the RDep list.
     */
    public void removeRDep(Dependency dependency) {
        this._RDep.remove(dependency);
    }

    /**
     * Removes a Dependency from the FDep list.
     */
    public void removeFDep(Dependency dependency) {
        this._FDep.remove(dependency);
    }

    /**
     * Displays the RedistributionDependency details as a string.
     */
    @Override
    public String toString() {
        return "RedistributionDependency{" +
                "_property=" + _property +
                ", _RDep=" + _RDep +
                ", _FDep=" + _FDep +
                ", _layer='" + _layer + '\'' +
                '}';
    }
}
