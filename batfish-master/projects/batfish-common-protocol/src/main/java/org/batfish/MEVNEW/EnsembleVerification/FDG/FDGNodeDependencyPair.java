package org.batfish.MEVNEW.EnsembleVerification.FDG;



import org.batfish.MEVNEW.EnsembleVerification.Property;

import java.util.List;
import java.util.Objects;


public class FDGNodeDependencyPair {
    private List<Integer> _parentFDGNode;
    private Property _subProperty;

    public FDGNodeDependencyPair(List<Integer> parentFDGNode, Property property)
    {
        this._parentFDGNode = parentFDGNode;
        this._subProperty = property;
    }

    public List<Integer> getParentFDGNode()
    {
        return this._parentFDGNode;
    }

    public Property getProperty()
    {
        return this._subProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FDGNodeDependencyPair that = (FDGNodeDependencyPair) o;
        return Objects.equals(_parentFDGNode, that._parentFDGNode) &&
                Objects.equals(_subProperty, that._subProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_parentFDGNode, _subProperty);
    }
}
