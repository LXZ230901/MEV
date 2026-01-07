package org.batfish.MEVNEW.EnsembleVerification.ForwardingTopology;

import org.batfish.MEVNEW.EnsembleVerification.FIB.DependencyFIB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForwardingNode {

    private String _node;
    private List<String> _vrf;
    private HashMap<Integer, DependencyFIB> _dependencyFIB;
    private HashMap<String, Integer> _vrfToVni;
    private HashMap<Integer, String> _vniToVrf;

    // Constructor
    public ForwardingNode(String node) {
        this._node = node;
        this._vrf = new ArrayList<>();
        this._dependencyFIB = new HashMap<>();
        this._vrfToVni = new HashMap<>();
        this._vniToVrf = new HashMap<>();
    }

    // Getters and Setters
    public String getNode() {
        return _node;
    }

    public List<String> getVrf() {
        return _vrf;
    }

    public HashMap<Integer, DependencyFIB> getDependencyFIB() {
        return _dependencyFIB;
    }

    public HashMap<String, Integer> getVrfToVni()
    {
        return this._vrfToVni;
    }

    public HashMap<Integer, String> getVniToVrf()
    {
        return this._vniToVrf;
    }

    public DependencyFIB getDependencyFIB(int vni) {
        return this._dependencyFIB.get(vni);
    }

    public void setVrf(List<String> vrf) {
        this._vrf = vrf;
    }

    public void setNode(String node) {
        this._node = node;
    }

    public void setDependencyFIB(HashMap<Integer, DependencyFIB> dependencyFIB) {
        this._dependencyFIB = dependencyFIB;
    }

    public void addVrf(String vrf) {
        if (!_vrf.contains(vrf)) {
            this._vrf.add(vrf);
        }
    }

    public void putVrfAndVni(String vrf, Integer vni)
    {
        this._vniToVrf.put(vni, vrf);
        this._vrfToVni.put(vrf, vni);
    }

    public void addDependencyFIBEntry(int vni, DependencyFIB dependencyFIB) {
        this._dependencyFIB.put(vni, dependencyFIB);
    }

    public void removeVrf(String vrf) {
        this._vrf.remove(vrf);
    }

    public void removeDependencyFIBEntry(int vni) {
        this._dependencyFIB.remove(vni);
    }

    @Override
    public String toString() {
        return "ForwardingNode{" +
                "_node='" + _node + '\'' +
                ", _vrf=" + _vrf +
                ", _dependencyFIB=" + _dependencyFIB +
                '}';
    }
}
