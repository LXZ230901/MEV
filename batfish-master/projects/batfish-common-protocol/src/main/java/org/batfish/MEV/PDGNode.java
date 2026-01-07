package org.batfish.MEV;

import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PDGNode {

    private String _srcRouter;
    private String _srcVrf;
    private String _dstRouter;
    private String _dstVrf;
    private Prefix _dstPrefix;
    private Integer _graphIndex;
    private Integer _verified;
    private PrefixRadixNode _prefixRadixNode;

    private List<PDGNode> _childNodes;
    private List<PDGNode> _parentNodes;

    private NestedDependency _Dep;

    private Set<String> _relatedVrf;
    public PDGNode()
    {
        this._childNodes = new ArrayList<>();
        this._parentNodes = new ArrayList<>();
        this._relatedVrf = new HashSet<>();
    }

    public PDGNode(String srcRouter, String srcVrf, String dstRouter, String dstVrf, Prefix dstPrefix, Integer graphIndex)
    {
        this._srcRouter = srcRouter;
        this._srcVrf = srcVrf;
        this._dstRouter = dstRouter;
        this._dstVrf = dstVrf;
        this._dstPrefix = dstPrefix;
        this._graphIndex = graphIndex;
        this._verified = -1;
        this._childNodes = new ArrayList<>();
        this._parentNodes = new ArrayList<>();
        this._Dep = new NestedDependency();
        this._relatedVrf = new HashSet<>();
    }

    public void setSrcRouter(String srcRouter)
    {
        this._srcRouter = srcRouter;
    }

    public void setSrcVrf(String srcVrf)
    {
        this._srcVrf = srcVrf;
    }

    public String getDstRouter() {
        return _dstRouter;
    }

    public void setDstRouter(String dstRouter) {
        this._dstRouter = dstRouter;
    }

    public String getDstVrf() {
        return _dstVrf;
    }

    public void setDstVrf(String dstVrf) {
        this._dstVrf = dstVrf;
    }

    public Prefix getDstPrefix() {
        return _dstPrefix;
    }

    public void setDstPrefix(Prefix dstPrefix) {
        this._dstPrefix = dstPrefix;
    }

    public Integer getGraphIndex() {
        return _graphIndex;
    }

    public void setGraphIndex(Integer graphIndex) {
        this._graphIndex = graphIndex;
    }

    public Integer getVerifiedResult() {
        return _verified;
    }

    public void setVerifiedResult(Integer verified) {
        this._verified = verified;
    }

    public PrefixRadixNode getPrefixRadixNode() {
        return _prefixRadixNode;
    }

    public void setPrefixRadixNode(PrefixRadixNode prefixRadixNode) {
        this._prefixRadixNode = prefixRadixNode;
    }

    public List<PDGNode> getChildNodes() {
        return _childNodes;
    }

    public void setChildNodes(List<PDGNode> childNodes) {
        this._childNodes = childNodes;
    }

    public List<PDGNode> getParentNodes() {
        return _parentNodes;
    }

    public void setParentNodes(List<PDGNode> parentNodes) {
        this._parentNodes = parentNodes;
    }

    public String getSrcRouter()
    {
        return _srcRouter;
    }


    public String getSrcVrf()
    {
        return _srcVrf;
    }

    // Methods to add and remove child and parent nodes
    public void addChildNode(PDGNode childNode) {
        this._childNodes.add(childNode);
        childNode.getParentNodes().add(this);
    }

    public void removeChildNode(PDGNode childNode) {
        this._childNodes.remove(childNode);
        childNode.getParentNodes().remove(this);
    }

    public void addParentNode(PDGNode parentNode) {
        this._parentNodes.add(parentNode);
        parentNode.getChildNodes().add(this);
    }

    public void removeParentNode(PDGNode parentNode) {
        this._parentNodes.remove(parentNode);
        parentNode.getChildNodes().remove(this);
    }

    public void addDep(NestedDependency Dep)
    {
        this._Dep = Dep;
    }
    public NestedDependency getDep()
    {
        return this._Dep;
    }




    public void setRelatedVrf(Set<String> relatedVrf)
    {
        this._relatedVrf.addAll(relatedVrf);
    }

    public Set<String> getRelatedVrf()
    {
        return this._relatedVrf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDGNode that = (PDGNode) o;
        return Objects.equals(_srcRouter, that._srcRouter) &&
                Objects.equals(_srcVrf, that._srcVrf) &&
                Objects.equals(_dstRouter, that._dstRouter) &&
                Objects.equals(_dstVrf, that._dstVrf) &&
                Objects.equals(_dstPrefix, that._dstPrefix) &&
                Objects.equals(_graphIndex, that._graphIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_srcRouter, _srcVrf, _dstRouter, _dstVrf, _dstPrefix, _graphIndex);
    }
}
