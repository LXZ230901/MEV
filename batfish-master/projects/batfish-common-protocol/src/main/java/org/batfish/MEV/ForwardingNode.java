package org.batfish.MEV;



import net.sf.javabdd.BDDFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForwardingNode {

    private String _node;
    private List<String> _vrf;
    private HashMap<String, Integer> _vrfToVni;
    private HashMap<Integer, DependencyFIB> _dependencyFIB;
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

    public void setNode(String node) {
        this._node = node;
    }

    public List<String> getVrf() {
        return _vrf;
    }

    public void setVrf(List<String> vrf) {
        this._vrf = vrf;
    }


    public HashMap<Integer, DependencyFIB> getDependencyFIB() {
        return _dependencyFIB;
    }

    public void setDependencyFIB(HashMap<Integer, DependencyFIB> dependencyFIB) {
        this._dependencyFIB = dependencyFIB;
    }

    // Utility Methods

    /**
     * Adds a VRF to the ForwardingNode.
     */
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

    public HashMap<String, Integer> getVrfToVni()
    {
        return this._vrfToVni;
    }

    public HashMap<Integer, String> getVniToVrf()
    {
        return this._vniToVrf;
    }

    /**
     * Adds a DependencyFIB entry associated with a specific VNI.
     */
    public void addDependencyFIBEntry(int vni, DependencyFIB dependencyFIB) {
        this._dependencyFIB.put(vni, dependencyFIB);
    }

    /**
     * Retrieves a DependencyFIB entry associated with a specific VNI.
     */
    public DependencyFIB getDependencyFIB(int vni) {
        return this._dependencyFIB.get(vni);
    }

    /**
     * Removes a VRF from the ForwardingNode.
     */
    public void removeVrf(String vrf) {
        this._vrf.remove(vrf);
    }

    /**
     * Removes a DependencyFIB entry associated with a specific VNI.
     */
    public void removeDependencyFIBEntry(int vni) {
        this._dependencyFIB.remove(vni);
    }


    /**
     * Displays the ForwardingNode details as a string.
     */
    @Override
    public String toString() {
        return "ForwardingNode{" +
                "_node='" + _node + '\'' +
                ", _vrf=" + _vrf +
                ", _dependencyFIB=" + _dependencyFIB +
                '}';
    }

    public void convertSymbolicFIB(BDDFactory factory)
    {
        this._dependencyFIB.values().forEach(
                dependencyFIB -> {
                    dependencyFIB.convertSymbolicFIB(factory);
                }
        );
    }
}
