package org.batfish.MEV;



import java.util.HashSet;
import java.util.Set;

public class PropertyVerificationResultPair {



    public boolean _result;
    public NestedDependency _Dep;
    public Set<PDGNodeDependencyPair> _pdgNodeDependencyPairs;
    public Set<PDGNodeSubPrefixPair> _pdgNodeSubPrefixPairs;
    public Integer _index;
    public Set<String> _relatedVrf;

    public PropertyVerificationResultPair(boolean verifiedResult, Integer index)
    {
        this._index = index;
        this._result = verifiedResult;
        this._relatedVrf = new HashSet<>();
    }


    public PropertyVerificationResultPair(boolean verifiedResult, Integer index, NestedDependency Dep, Set<PDGNodeDependencyPair> pdgNodeDependencyPairs, Set<String> relatedVrf)
    {
        this._result = verifiedResult;
        this._Dep = Dep;
        this._pdgNodeDependencyPairs = pdgNodeDependencyPairs;
        this._index = index;
        this._relatedVrf = new HashSet<>();
        this._relatedVrf.addAll(relatedVrf);
    }

    public PropertyVerificationResultPair(boolean verifiedResult, Integer index, Set<PDGNodeSubPrefixPair> pdgNodeSubPrefixPairs, Set<String> relatedVrf)
    {
        this._result = verifiedResult;
        this._pdgNodeSubPrefixPairs = pdgNodeSubPrefixPairs;
        this._index = index;
        this._relatedVrf = new HashSet<>();
        this._relatedVrf.addAll(relatedVrf);
    }

    public boolean getVerifiedResult()
    {
        return this._result;
    }

    public NestedDependency getDep()
    {
        return this._Dep;
    }


    public Set<PDGNodeDependencyPair> getPdgNodeDependencyPairs()
    {
        return this._pdgNodeDependencyPairs;
    }







    public Set<PDGNodeSubPrefixPair> getPdgNodeSubPrefixPairs()
    {
        return this._pdgNodeSubPrefixPairs;
    }


    public Integer getGraphIndex()
    {
        return this._index;
    }

    public Set<String> getRelatedVrf()
    {
        return this._relatedVrf;
    }

    public void setRelatedVrf(Set<String> relatedVrf)
    {
        this._relatedVrf.addAll(relatedVrf);
    }
}
