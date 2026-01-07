package org.batfish.MEVNEW.EnsembleVerification;





import org.batfish.MEVNEW.EnsembleVerification.FDG.FDGNodeDependencyPair;
import org.batfish.MEVNEW.EnsembleVerification.FDG.FDGNodeSubPrefixPair;
import org.batfish.MEVNEW.EnsembleVerification.FIB.NestedDependency;

import java.util.HashSet;
import java.util.Set;

public class PropertyVerificationResultPair {



    public boolean _result;
    public NestedDependency _Dep;
    public Set<FDGNodeDependencyPair> _fdgNodeDependencyPairs;
    public Set<FDGNodeSubPrefixPair> _fdgNodeSubPrefixPairs;
    public Integer _index;
    public Set<String> _relatedVrf;

    public PropertyVerificationResultPair(boolean verifiedResult, Integer index)
    {
        this._index = index;
        this._result = verifiedResult;
        this._relatedVrf = new HashSet<>();
    }


    public PropertyVerificationResultPair(boolean verifiedResult, Integer index, NestedDependency Dep, Set<FDGNodeDependencyPair> fdgNodeDependencyPairs, Set<String> relatedVrf)
    {
        this._result = verifiedResult;
        this._Dep = Dep;
        this._fdgNodeDependencyPairs = fdgNodeDependencyPairs;
        this._index = index;
        this._relatedVrf = new HashSet<>();
        this._relatedVrf.addAll(relatedVrf);
    }

    public PropertyVerificationResultPair(boolean verifiedResult, Integer index, Set<FDGNodeSubPrefixPair> fdgNodeSubPrefixPairs, Set<String> relatedVrf)
    {
        this._result = verifiedResult;
        this._fdgNodeSubPrefixPairs = fdgNodeSubPrefixPairs;
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

    public Set<FDGNodeDependencyPair> getFdgNodeDependencyPairs()
    {
        return this._fdgNodeDependencyPairs;
    }

    public Set<FDGNodeSubPrefixPair> getFdgNodeSubPrefixPairs()
    {
        return this._fdgNodeSubPrefixPairs;
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
