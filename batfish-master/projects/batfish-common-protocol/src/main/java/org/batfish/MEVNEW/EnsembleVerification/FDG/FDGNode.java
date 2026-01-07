package org.batfish.MEVNEW.EnsembleVerification.FDG;

import org.batfish.MEVNEW.EnsembleVerification.FIB.NestedDependency;
import org.batfish.datamodel.Prefix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FDGNode {

    private String _srcRouter;
    private String _srcVrf;
    private String _dstRouter;
    private String _dstVrf;
    private Prefix _dstPrefix;
    private Integer _graphIndex;
    private Integer _verified;
    private PrefixRadixNode _prefixRadixNode;

    private List<FDGNode> _childNodes;
    private List<FDGNode> _parentNodes;

    private NestedDependency _Dep;

    private Set<String> _relatedVrf;

    private Integer _finalVerificationResult;
    public FDGNode()
    {
        this._childNodes = new ArrayList<>();
        this._parentNodes = new ArrayList<>();
        this._relatedVrf = new HashSet<>();
    }

    public FDGNode(String srcRouter, String srcVrf, String dstRouter, String dstVrf, Prefix dstPrefix, Integer graphIndex)
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

    public String getDstRouter() {
        return _dstRouter;
    }

    public String getDstVrf() {
        return _dstVrf;
    }

    public Prefix getDstPrefix() {
        return _dstPrefix;
    }

    public Integer getGraphIndex() {
        return _graphIndex;
    }

    public Integer getVerifiedResult() {
        return _verified;
    }

    public PrefixRadixNode getPrefixRadixNode() {
        return _prefixRadixNode;
    }

    public List<FDGNode> getChildNodes() {
        return _childNodes;
    }

    public List<FDGNode> getParentNodes() {
        return _parentNodes;
    }

    public String getSrcVrf()
    {
        return _srcVrf;
    }

    public String getSrcRouter()
    {
        return _srcRouter;
    }

    public NestedDependency getDep()
    {
        return this._Dep;
    }

    public Set<String> getRelatedVrf()
    {
        return this._relatedVrf;
    }

    public Integer getFinalVerificationResult()
    {
        return this._finalVerificationResult;
    }

    public void setFinalVerificationResult(Integer finalVerificationResult)
    {
        this._finalVerificationResult = finalVerificationResult;
    }

    public void setSrcRouter(String srcRouter)
    {
        this._srcRouter = srcRouter;
    }

    public void setSrcVrf(String srcVrf)
    {
        this._srcVrf = srcVrf;
    }

    public void setDstRouter(String dstRouter) {
        this._dstRouter = dstRouter;
    }

    public void setDstVrf(String dstVrf) {
        this._dstVrf = dstVrf;
    }

    public void setDstPrefix(Prefix dstPrefix) {
        this._dstPrefix = dstPrefix;
    }

    public void setGraphIndex(Integer graphIndex) {
        this._graphIndex = graphIndex;
    }

    public void setVerifiedResult(Integer verified) {
        this._verified = verified;
    }

    public void setPrefixRadixNode(PrefixRadixNode prefixRadixNode) {
        this._prefixRadixNode = prefixRadixNode;
    }

    public void setChildNodes(List<FDGNode> childNodes) {
        this._childNodes = childNodes;
    }

    public void setParentNodes(List<FDGNode> parentNodes) {
        this._parentNodes = parentNodes;
    }

    public void addChildNode(FDGNode childNode) {
        this._childNodes.add(childNode);
        childNode.getParentNodes().add(this);
    }

    public void removeChildNode(FDGNode childNode) {
        this._childNodes.remove(childNode);
        childNode.getParentNodes().remove(this);
    }

    public void addParentNode(FDGNode parentNode) {
        this._parentNodes.add(parentNode);
        parentNode.getChildNodes().add(this);
    }

    public void removeParentNode(FDGNode parentNode) {
        this._parentNodes.remove(parentNode);
        parentNode.getChildNodes().remove(this);
    }

    public void addDep(NestedDependency Dep)
    {
        this._Dep = Dep;
    }

    public void setRelatedVrf(Set<String> relatedVrf)
    {
        this._relatedVrf.addAll(relatedVrf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FDGNode that = (FDGNode) o;
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
