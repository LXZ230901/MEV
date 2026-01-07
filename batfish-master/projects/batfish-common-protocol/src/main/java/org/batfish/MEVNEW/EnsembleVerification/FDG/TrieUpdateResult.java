package org.batfish.MEVNEW.EnsembleVerification.FDG;



public class TrieUpdateResult {
    private Integer _prefixParentNode;
    private boolean _toVerify;
    private boolean _addFDGNode;
    private PrefixRadixNode _prefixRadixTrieNode;
    public Integer _graphIndex;
    public TrieUpdateResult() {}
    public TrieUpdateResult(Integer prefixParentNode, boolean addFDGNode, boolean toVerify, PrefixRadixNode prefixRadixTrieNode)
    {
        this._prefixParentNode = prefixParentNode;
        this._toVerify = toVerify;
        this._addFDGNode = addFDGNode;
        this._prefixRadixTrieNode = prefixRadixTrieNode;
        this._graphIndex = -1;
    }

    public Integer getPrefixParentNode()
    {
        return this._prefixParentNode;
    }

    public boolean getToVerifyTag()
    {
        return this._toVerify;
    }

    public boolean getAddFDGNodeTag()
    {
        return this._addFDGNode;
    }

    public PrefixRadixNode getPrefixRadixTrieNode()
    {
        return this._prefixRadixTrieNode;
    }

    public Integer getGraphIndex()
    {
        return this._graphIndex;
    }

    public void setPrefixParentNode(Integer prefixParentNode)
    {
        this._prefixParentNode = prefixParentNode;
    }

    public void setToVerify(boolean toVerify)
    {
        this._toVerify = toVerify;
    }

    public void setAddFDGNodeTag(boolean addFDGNode)
    {
        this._addFDGNode = addFDGNode;
    }

    public void setPrefixRadixTrieNode(PrefixRadixNode prefixRadixTrieNode)
    {
        this._prefixRadixTrieNode = prefixRadixTrieNode;
    }

    public void setGraphIndex(Integer graphIndex)
    {
        this._graphIndex = graphIndex;
    }
}
