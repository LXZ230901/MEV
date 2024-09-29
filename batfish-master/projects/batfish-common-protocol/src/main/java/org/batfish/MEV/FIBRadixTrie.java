package org.batfish.MEV;



public class FIBRadixTrie {


    private FIBRadixNode _root;

    public FIBRadixTrie()
    {
        this._root = new FIBRadixNode();
    }


    public FIBRadixNode getMatchForwardingResult(String Ip)
    {
        String prefixSlice = Ip.toLowerCase();
        FIBRadixNode currentNode = _root;
        while (!prefixSlice.isEmpty())
        {
            String longestPrefix = "";
            FIBRadixNode nextNode = null;
            Character bit = prefixSlice.charAt(0);
            if (bit == '0')
            {
                nextNode = currentNode.getLeftChildNode();
                if (nextNode == null)
                {
                    return currentNode;
                } else {
                    int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                    if (commonLength == nextNode._prefixSlice.length())
                    {
                        currentNode = nextNode;
                        prefixSlice = prefixSlice.substring(commonLength);
                    } else {
                        return currentNode;
                    }
                }
            } else {
                nextNode = currentNode.getRightChildNode();
                if (nextNode == null)
                {
                    return currentNode;
                } else {
                    int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                    if (commonLength == nextNode._prefixSlice.length())
                    {
                        currentNode = nextNode;
                        prefixSlice = prefixSlice.substring(commonLength);
                    } else {
                        return currentNode;
                    }
                }
            }
        }
        return currentNode;
    }



    public void addNodes(String addPrefix, Integer prefixLength, DependencyFIBEntry fibEntry)
    {
        String prefixSlice = addPrefix.toLowerCase();
        FIBRadixNode currentNode = _root;
        Integer prefixParentNode = 0;
        while (!prefixSlice.isEmpty()) {
            String longestPrefix = "";
            FIBRadixNode nextNode = null;
            Character bit = prefixSlice.charAt(0);
            if (bit == '0')
            {
                nextNode = currentNode.getLeftChildNode();
                if (nextNode == null)
                {
                    FIBRadixNode addPrefixNode = new FIBRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setDependencyFIBEntry(fibEntry);;
                    currentNode.setLeftChildNode(addPrefixNode);
                    // 需要在邻接表中创建节点
                    return ;
                }
                int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                if (commonLength == nextNode.getPrefixSlice().length() && commonLength == prefixSlice.length())
                {
                    if (nextNode.getDependencyFIBEntryList().isEmpty())
                    {
                        nextNode.setDependencyFIBEntry(fibEntry);
                        return ;
                        //需要创建节点
                    } else {
                        //节点已经存在
                        nextNode.mergeDependencyEntry(fibEntry);
                        return ;
                    }
                } else if (commonLength == nextNode.getPrefixSlice().length() && commonLength < prefixSlice.length())
                {
                    currentNode = nextNode;
                    prefixSlice = prefixSlice.substring(commonLength);
                } else if (commonLength == prefixSlice.length() && commonLength < nextNode._prefixSlice.length())
                {
                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));
                    FIBRadixNode addPrefixNode = new FIBRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setDependencyFIBEntry(fibEntry);
                    addPrefixNode.addFIBRadixNode(nextNode);
                    currentNode.setLeftChildNode(addPrefixNode);
                    //保存父节点以及对应子节点的验证关系,
                    //需要在邻接表中创建节点
                    return ;
                } else {
                    String commonPrefix = prefixSlice.substring(0, commonLength);
                    int commonFullPrefixLength = prefixLength - (prefixSlice.length() - commonLength);
                    String commonFullPrefix = addPrefix.substring(0, commonFullPrefixLength);

                    FIBRadixNode tempPrefixNode = new FIBRadixNode();
                    tempPrefixNode.setPrefixSlice(commonPrefix);
                    tempPrefixNode.setFullPrefix(commonFullPrefix);
                    tempPrefixNode.setPrefixLength(commonFullPrefixLength);


                    String addPrefixSlice = prefixSlice.substring(commonLength);
                    FIBRadixNode addPrefixNode = new FIBRadixNode();
                    addPrefixNode.setPrefixSlice(addPrefixSlice);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setDependencyFIBEntry(fibEntry);
                    // 临界表中创建节点


                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));

                    tempPrefixNode.addFIBRadixNode(addPrefixNode);
                    tempPrefixNode.addFIBRadixNode(nextNode);

                    currentNode.addFIBRadixNode(tempPrefixNode);

                    return ;
                }
            } else {
                nextNode = currentNode.getRightChildNode();
                if (nextNode == null)
                {
                    FIBRadixNode addPrefixNode = new FIBRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setDependencyFIBEntry(fibEntry);
                    currentNode.setRightChildNode(addPrefixNode);
                    return ;
                }
                int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                if (commonLength == nextNode.getPrefixSlice().length() && commonLength == prefixSlice.length())
                {
                    if (nextNode.getDependencyFIBEntryList().isEmpty())
                    {
                        nextNode.setDependencyFIBEntry(fibEntry);
                        return ;
                        //需要创建节点
                    } else {
                        //节点已经存在了
                        nextNode.mergeDependencyEntry(fibEntry);
                        return ;
                    }
                } else if (commonLength == nextNode.getPrefixSlice().length() && commonLength < prefixSlice.length())
                {
                    currentNode = nextNode;
                    prefixSlice = prefixSlice.substring(commonLength);
                } else if (commonLength == prefixSlice.length() && commonLength < nextNode._prefixSlice.length())
                {
                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));
                    FIBRadixNode addPrefixNode = new FIBRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.addFIBRadixNode(nextNode);
                    addPrefixNode.setDependencyFIBEntry(fibEntry);
                    currentNode.setRightChildNode(addPrefixNode);
                    //需要在邻接表中创建节点
                    return ;
                } else {
                    String commonPrefix = prefixSlice.substring(0, commonLength);
                    int commonFullPrefixLength = prefixLength - (prefixSlice.length() - commonLength);
                    String commonFullPrefix = addPrefix.substring(0, commonFullPrefixLength);

                    FIBRadixNode tempPrefixNode = new FIBRadixNode();
                    tempPrefixNode.setPrefixSlice(commonPrefix);
                    tempPrefixNode.setFullPrefix(commonFullPrefix);
                    tempPrefixNode.setPrefixLength(commonFullPrefixLength);


                    String addPrefixSlice = prefixSlice.substring(commonLength);
                    FIBRadixNode addPrefixNode = new FIBRadixNode();
                    addPrefixNode.setPrefixSlice(addPrefixSlice);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setDependencyFIBEntry(fibEntry);
                    // 临界表中创建节点

                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));

                    tempPrefixNode.addFIBRadixNode(addPrefixNode);
                    tempPrefixNode.addFIBRadixNode(nextNode);

                    currentNode.addFIBRadixNode(tempPrefixNode);
                    return ;
                }
            }
        }
        return ;
    }

    private int commonPrefixLength(String str1, String str2) {
        int minLength = Math.min(str1.length(), str2.length());
        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                return i;
            }
        }
        return minLength;
    }
}
