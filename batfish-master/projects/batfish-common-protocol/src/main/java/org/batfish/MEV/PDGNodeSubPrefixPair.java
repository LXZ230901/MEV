

package org.batfish.MEV;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PDGNodeSubPrefixPair {


    public Integer _parentNode;
    public List<Property> _subPrefixProperty;
    public NestedDependency _Dep;
    public boolean _verified;
    public Set<String> _relatedVrf;






    public PDGNodeSubPrefixPair(Integer parentNode, List<Property> subPrefixProperty, NestedDependency Dep, boolean verified, Set<String> relatedVrf)
    {
        this._parentNode = parentNode;
        this._subPrefixProperty = subPrefixProperty;
        this._Dep = Dep;
        this._verified = verified;
        this._relatedVrf = new HashSet<>();
        this._relatedVrf.addAll(relatedVrf);
    }

    public List<Property> getProperty()
    {
        return this._subPrefixProperty;
    }

    public Integer getParentPDGNode()
    {
        return this._parentNode;
    }

    public NestedDependency getDep()
    {
        return this._Dep;
    }






    public boolean getVerified()
    {
        return this._verified;
    }

    public void setVerified(boolean verified)
    {
        this._verified = verified;
    }



    public Set<String> getRelatedVrf()
    {
        return this._relatedVrf;
    }


}
