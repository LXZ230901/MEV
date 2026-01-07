package org.batfish.MEVNEW.EnsembleVerification.FIB;

import org.batfish.datamodel.Prefix;

import java.util.HashMap;

public class DependencyFIB {

    private HashMap<Prefix, DependencyFIBEntry> _fib;

    private FIBRadixTrie _fibRadixTrie;

    public DependencyFIB() {
        this._fib = new HashMap<>();
        this._fibRadixTrie = new FIBRadixTrie();
    }

    public HashMap<Prefix, DependencyFIBEntry> getFib() {
        return _fib;
    }

    public FIBRadixTrie getFIBRadixTrie()
    {
        return this._fibRadixTrie;
    }

    public void setFib(HashMap<Prefix, DependencyFIBEntry> fib) {
        this._fib = fib;
    }

    public void addEntry(Prefix prefix, DependencyFIBEntry entry) {
        this._fib.put(prefix, entry);
    }

    public DependencyFIBEntry getEntry(Prefix prefix) {
        return this._fib.get(prefix);
    }

    public boolean containsPrefix(Prefix prefix) {
        return this._fib.containsKey(prefix);
    }

    public int size() {
        return this._fib.size();
    }

    public void clear() {
        this._fib.clear();
    }

    @Override
    public String toString() {
        return "DependencyFIB{" +
                "_fib=" + _fib +
                '}';
    }
}
