package org.batfish.MEV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FIBRadixNode {
    public String _prefixSlice;
    public String _fullPrefix;
    public List<DependencyFIBEntry> _fibEntryList;
    public ForwardingNexthop _nexthop;
    public Integer _vni;
    public FIBRadixNode _leftChildNode;
    public FIBRadixNode _rightChildNode;
    public HashMap<Character, FIBRadixNode> _childNodes;
    public Integer _prefixLength;
    public NestedDependency _Dep;

    public FIBRadixNode()
    {
        this._prefixSlice = null;
        this._childNodes = new HashMap<>();
        this._fullPrefix = null;
        this._leftChildNode = null;
        this._rightChildNode = null;
        this._prefixLength = 0;
        this._fibEntryList = new ArrayList<>();
        this._vni = 0;
        this._Dep = new NestedDependency();
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


    public void setLeftChildNode(FIBRadixNode leftChildNode)
    {
        this._leftChildNode = leftChildNode;
    }

    public void setRightChildNode(FIBRadixNode rightChildNode)
    {
        this._rightChildNode = rightChildNode;
    }

    public void addFIBRadixNode(FIBRadixNode fibRadixNode)
    {
        if (fibRadixNode.getPrefixSlice().charAt(0) == '0')
        {
            this._leftChildNode = fibRadixNode;
        } else {
            this._rightChildNode = fibRadixNode;
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


    public HashMap<Character, FIBRadixNode> getChildNodes()
    {
        return this._childNodes;
    }

    public FIBRadixNode getLeftChildNode()
    {
        return this._leftChildNode;
    }

    public FIBRadixNode getRightChildNode()
    {
        return this._rightChildNode;
    }

    public Integer getPrefixLength()
    {
        return this._prefixLength;
    }

    public List<DependencyFIBEntry> getDependencyFIBEntryList()
    {
        return this._fibEntryList;
    }

    public void setDependencyFIBEntry(DependencyFIBEntry dependencyFIBEntry)
    {
        this._fibEntryList.add(dependencyFIBEntry);
        this._nexthop = dependencyFIBEntry.getNexthop();
        this._vni = dependencyFIBEntry.getVni();
        this._Dep = dependencyFIBEntry.getDep();
    }

    public void setNexthop(ForwardingNexthop nexthop)
    {
        this._nexthop = nexthop;
    }

    public ForwardingNexthop getNexthop()
    {
        return this._nexthop;
    }

    public void mergeDependencyEntry(DependencyFIBEntry dependencyFIBEntry)
    {
        // 这一部分需要干的内容是，如果有融合的话，把他们的依赖融合起来
        // unimplemented
        this._Dep.orDependencies(dependencyFIBEntry.getDep().getNestedDependency());
    }


    public Integer getForwardingVni()
    {
        return this._vni;
    }



    public NestedDependency getDep()
    {
        return this._Dep;
    }
}
