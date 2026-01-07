package org.batfish.MEV;

import java.util.List;
import java.util.Objects;


public class PDGNodeDependencyPair {
    private List<Integer> _parentPDGNode;
    private Property _subProperty;

    public PDGNodeDependencyPair(List<Integer> parentPDGNode, Property property)
    {
        this._parentPDGNode = parentPDGNode;
        this._subProperty = property;
    }

    public List<Integer> getParentPDGNode()
    {
        return this._parentPDGNode;
    }

    public Property getProperty()
    {
        return this._subProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDGNodeDependencyPair that = (PDGNodeDependencyPair) o;
        return Objects.equals(_parentPDGNode, that._parentPDGNode) &&
                Objects.equals(_subProperty, that._subProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_parentPDGNode, _subProperty);
    }
}
