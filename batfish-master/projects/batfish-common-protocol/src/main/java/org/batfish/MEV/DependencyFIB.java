package org.batfish.MEV;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyFIB {

    private HashMap<Prefix, DependencyFIBEntry> _fib;

    private FIBRadixTrie _fibRadixTrie;

    private HashMap<BDD, DependencyFIBEntry> _symbolicFib;


    // Constructor
    public DependencyFIB() {
        this._fib = new HashMap<>();
        this._symbolicFib = new HashMap<>();
        this._fibRadixTrie = new FIBRadixTrie();
    }

    // Getter and Setter
    public HashMap<Prefix, DependencyFIBEntry> getFib() {
        return _fib;
    }

    public void setFib(HashMap<Prefix, DependencyFIBEntry> fib) {
        this._fib = fib;
    }

    // Utility Methods

    /**
     * Adds an entry to the FIB for a given prefix.
     */
    public void addEntry(Prefix prefix, DependencyFIBEntry entry) {
        this._fib.put(prefix, entry);
    }


    /**
     * Retrieves the list of DependencyFIBEntry objects for a given prefix.
     */
    public DependencyFIBEntry getEntry(Prefix prefix) {
        return this._fib.get(prefix);
    }

    /**
     * Checks if the FIB contains a given prefix.
     */
    public boolean containsPrefix(Prefix prefix) {
        return this._fib.containsKey(prefix);
    }

    /**
     * Returns the size of the FIB.
     */
    public int size() {
        return this._fib.size();
    }

    /**
     * Clears all entries in the FIB.
     */
    public void clear() {
        this._fib.clear();
    }

    public HashMap<BDD, DependencyFIBEntry> getSymbolicFib()
    {
        return this._symbolicFib;
    }

    /**
     * Displays the DependencyFIB details as a string.
     */
    @Override
    public String toString() {
        return "DependencyFIB{" +
                "_fib=" + _fib +
                '}';
    }

    public FIBRadixTrie getFIBRadixTrie()
    {
        return this._fibRadixTrie;
    }

    public void convertSymbolicFIB(BDDFactory factory)
    {
        this._symbolicFib.clear();
        for (Prefix prefix : this._fib.keySet())
        {
            //这里没有考虑子前缀的问题，后续需要考虑一下。
            BDD symbolicPrefix = PrefixToBDDConverter.convertPrefixToBDD(factory, prefix.toString());
            addSymbolicEntry(symbolicPrefix, this._fib.get(prefix));
        }
    }

//    public void addSymbolicEntry(BDD newBDD, DependencyFIBEntry entry) {
//        BDD modifiedBDD = newBDD;
//
//        // Create a copy of the existing keys to avoid ConcurrentModificationException
//        for (Map.Entry<BDD, DependencyFIBEntry> existingEntry : _symbolicFib.entrySet()) {
//            BDD existingBDD = existingEntry.getKey();
//
//            // Check if existingBDD contains newBDD
//            if (existingBDD.and(modifiedBDD).equals(existingBDD))
//            {
//                modifiedBDD = modifiedBDD.and(existingBDD.not());
//            } else if (existingBDD.and(modifiedBDD).equals(modifiedBDD))
//            {
//                BDD updatedExistingBDD = existingBDD.and(modifiedBDD.not());
//                _symbolicFib.put(updatedExistingBDD, existingEntry.getValue());
//                _symbolicFib.remove(existingBDD);
//            }
//        }
//
//        // Insert or update the modified new BDD entry
//        if (!modifiedBDD.isZero())
//        {
//            _symbolicFib.put(modifiedBDD, entry);
//        }
//    }

    public void addSymbolicEntry(BDD newBDD, DependencyFIBEntry entry) {
        BDD modifiedBDD = newBDD;

        // Temporary map to hold updated entries after the iteration
        Map<BDD, DependencyFIBEntry> tempUpdates = new HashMap<>();
        List<BDD> toRemove = new ArrayList<>();

        // Iterate over a copy of the entry set to avoid ConcurrentModificationException
        for (Map.Entry<BDD, DependencyFIBEntry> existingEntry : new HashMap<>(_symbolicFib).entrySet()) {
            BDD existingBDD = existingEntry.getKey();

            // Check if existingBDD contains newBDD
            if (existingBDD.and(modifiedBDD).equals(existingBDD)) {
                modifiedBDD = modifiedBDD.and(existingBDD.not());
            } else if (existingBDD.and(modifiedBDD).equals(modifiedBDD)) {
                BDD updatedExistingBDD = existingBDD.and(modifiedBDD.not());
                tempUpdates.put(updatedExistingBDD, existingEntry.getValue());
                toRemove.add(existingBDD); // Mark for removal after iteration
            }
        }

        // Remove marked entries from the original map
        for (BDD bdd : toRemove) {
            _symbolicFib.remove(bdd);
        }

        // Apply updates to the original map
        _symbolicFib.putAll(tempUpdates);

        // Insert or update the modified new BDD entry
        if (!modifiedBDD.isZero()) {
            _symbolicFib.put(modifiedBDD, entry);
        }
    }

}
