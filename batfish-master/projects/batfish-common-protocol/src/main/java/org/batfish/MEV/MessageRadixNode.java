package org.batfish.MEV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageRadixNode {
    public String _prefixSlice;
    public String _fullPrefix;
    public List<Message> _messageList;
    public MessageRadixNode _leftChildNode;
    public MessageRadixNode _rightChildNode;
    public HashMap<Character, MessageRadixNode> _childNodes;
    public Integer _prefixLength;

    public MessageRadixNode()
    {
        this._prefixSlice = null;
        this._childNodes = new HashMap<>();
        this._fullPrefix = null;
        this._leftChildNode = null;
        this._rightChildNode = null;
        this._prefixLength = 0;
        this._messageList = new ArrayList<>();
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


    public void setLeftChildNode(MessageRadixNode leftChildNode)
    {
        this._leftChildNode = leftChildNode;
    }

    public void setRightChildNode(MessageRadixNode rightChildNode)
    {
        this._rightChildNode = rightChildNode;
    }

    public void addMessageRadixNode(MessageRadixNode messageRadixNode)
    {
        if (messageRadixNode.getPrefixSlice().charAt(0) == '0')
        {
            this._leftChildNode = messageRadixNode;
        } else {
            this._rightChildNode = messageRadixNode;
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


    public HashMap<Character, MessageRadixNode> getChildNodes()
    {
        return this._childNodes;
    }

    public MessageRadixNode getLeftChildNode()
    {
        return this._leftChildNode;
    }

    public MessageRadixNode getRightChildNode()
    {
        return this._rightChildNode;
    }

    public Integer getPrefixLength()
    {
        return this._prefixLength;
    }

    public List<Message> getMessageList()
    {
        return this._messageList;
    }

    public void setMessageList(List<Message> messageList)
    {
        this._messageList.addAll(messageList);
    }
}
