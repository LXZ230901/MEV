package org.batfish.MEV;

public class TrieUpdateResult {
    private Integer _prefixParentNode;
    private boolean _toVerify;
    private boolean _addPDGNode;
    private PrefixRadixNode _prefixRadixTrieNode;
    public Integer _graphIndex;
    public TrieUpdateResult() {}
    public TrieUpdateResult(Integer prefixParentNode, boolean addPDGNode, boolean toVerify, PrefixRadixNode prefixRadixTrieNode)
    {
        this._prefixParentNode = prefixParentNode;
        this._toVerify = toVerify;
        this._addPDGNode = addPDGNode;
        this._prefixRadixTrieNode = prefixRadixTrieNode;
        this._graphIndex = -1;
    }

    public void setPrefixParentNode(Integer prefixParentNode)
    {
        this._prefixParentNode = prefixParentNode;
    }

    public void setToVerify(boolean toVerify)
    {
        this._toVerify = toVerify;
    }

    public void setAddPDGNodeTag(boolean addPDGNode)
    {
        this._addPDGNode = addPDGNode;
    }

    public void setPrefixRadixTrieNode(PrefixRadixNode prefixRadixTrieNode)
    {
        this._prefixRadixTrieNode = prefixRadixTrieNode;
    }

    public void setGraphIndex(Integer graphIndex)
    {
        this._graphIndex = graphIndex;
    }

    public Integer getPrefixParentNode()
    {
        return this._prefixParentNode;
    }

    public boolean getToVerifyTag()
    {
        return this._toVerify;
    }

    public boolean getAddPDGNodeTag()
    {
        return this._addPDGNode;
    }

    public PrefixRadixNode getPrefixRadixTrieNode()
    {
        return this._prefixRadixTrieNode;
    }

    public Integer getGraphIndex()
    {
        return this._graphIndex;
    }
}
