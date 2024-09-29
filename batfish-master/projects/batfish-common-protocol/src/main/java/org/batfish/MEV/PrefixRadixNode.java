package org.batfish.MEV;

import java.util.HashMap;

public class PrefixRadixNode {
    public String _prefixSlice;
    public String _fullPrefix;
    public Integer _graphIndex;
    public PrefixRadixNode _leftPrefixChildNode;
    public PrefixRadixNode _rightPrefixChildNode;
    public HashMap<Character, PrefixRadixNode> _prefixChildNodes;
    public Integer _prefixLength;

    public PrefixRadixNode()
    {
        this._prefixSlice = null;
        this._prefixChildNodes = new HashMap<>();
        this._fullPrefix = null;
        this._graphIndex = 0;
        this._leftPrefixChildNode = null;
        this._rightPrefixChildNode = null;
        this._prefixLength = 0;
    }

    public void setPrefixSlice(String prefixSlice)
    {
        this._prefixSlice = prefixSlice;
    }

    public void setPrefixLength(Integer prefixLength)
    {
        this._prefixLength = prefixLength;
    }

    public void setFullPrefix(String fullPrefix)
    {
        this._fullPrefix = fullPrefix;
    }

    public void setGraphIndex(Integer graphIndex)
    {
        this._graphIndex = graphIndex;
    }

    public void setLeftPrefixChildNode(PrefixRadixNode leftPrefixChildNode)
    {
        this._leftPrefixChildNode = leftPrefixChildNode;
    }

    public void setRightPrefixChildNode(PrefixRadixNode rightPrefixChildNode)
    {
        this._rightPrefixChildNode = rightPrefixChildNode;
    }

    public void addPrefixNode(PrefixRadixNode prefixRadixNode)
    {
        if (prefixRadixNode.getPrefixSlice().charAt(0) == '0')
        {
            this._leftPrefixChildNode = prefixRadixNode;
        } else {
            this._rightPrefixChildNode = prefixRadixNode;
        }
    }

    public String getPrefixSlice()
    {
        return this._prefixSlice;
    }

    public String getFullPrefix()
    {
        return this._fullPrefix;
    }

    public Integer getGraphIndex()
    {
        return this._graphIndex;
    }

    public HashMap<Character,PrefixRadixNode> getPrefixChildNodes()
    {
        return this._prefixChildNodes;
    }

    public PrefixRadixNode getLeftPrefixChildNode()
    {
        return this._leftPrefixChildNode;
    }

    public PrefixRadixNode getRightPrefixChildNode()
    {
        return this._rightPrefixChildNode;
    }

    public Integer getPrefixLength()
    {
        return this._prefixLength;
    }
}
