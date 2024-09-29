package org.batfish.MEV;

public class PrefixRadixTrie {

    private PrefixRadixNode _root;

//    PrefixRadixTrie构建的算法
    public PrefixRadixTrie()
    {
        _root = new PrefixRadixNode();
    }

    public TrieUpdateResult addNode(String addPrefix, Integer prefixLength, Integer graphIndex)
    {
        String prefixSlice = addPrefix.toLowerCase();
        PrefixRadixNode currentNode = _root;
        Integer prefixParentNode = 0;
        while (!prefixSlice.isEmpty()) {
            String longestPrefix = "";
            PrefixRadixNode nextNode = null;
            Character bit = prefixSlice.charAt(0);
            if (bit == '0')
            {
                nextNode = currentNode.getLeftPrefixChildNode();
                if (nextNode == null)
                {
                    PrefixRadixNode addPrefixNode = new PrefixRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setGraphIndex(graphIndex);
                    currentNode.setLeftPrefixChildNode(addPrefixNode);
                    // 需要在邻接表中创建节点
                    return new TrieUpdateResult(prefixParentNode, true, true, addPrefixNode);
                }
                int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                if (commonLength == nextNode.getPrefixSlice().length() && commonLength == prefixSlice.length())
                {
                    if (nextNode.getGraphIndex() == 0)
                    {
                        nextNode.setGraphIndex(graphIndex);
                        return new TrieUpdateResult(prefixParentNode, true, true, nextNode);
                        //需要创建节点
                    } else {
                        //节点已经存在了
                        TrieUpdateResult trieUpdateResult = new TrieUpdateResult(prefixParentNode, false, false, null);
                        trieUpdateResult.setGraphIndex(nextNode.getGraphIndex());
                        return trieUpdateResult;
                    }
                } else if (commonLength == nextNode.getPrefixSlice().length() && commonLength < prefixSlice.length())
                {
                    currentNode = nextNode;
                    if (currentNode.getGraphIndex() != 0)
                    {
                        prefixParentNode = currentNode.getGraphIndex();
                    }
                    prefixSlice = prefixSlice.substring(commonLength);
                } else if (commonLength == prefixSlice.length() && commonLength < nextNode._prefixSlice.length())
                {
                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));
                    PrefixRadixNode addPrefixNode = new PrefixRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.addPrefixNode(nextNode);
                    addPrefixNode.setGraphIndex(graphIndex);
                    currentNode.setLeftPrefixChildNode(addPrefixNode);
                    //保存父节点以及对应子节点的验证关系,
                    //需要在邻接表中创建节点
                    return new TrieUpdateResult(prefixParentNode, true, true, addPrefixNode);
                } else {
                    String commonPrefix = prefixSlice.substring(0, commonLength);
                    int commonFullPrefixLength = prefixLength - (prefixSlice.length() - commonLength);
                    String commonFullPrefix = addPrefix.substring(0, commonFullPrefixLength);

                    PrefixRadixNode tempPrefixNode = new PrefixRadixNode();
                    tempPrefixNode.setPrefixSlice(commonPrefix);
                    tempPrefixNode.setFullPrefix(commonFullPrefix);
                    tempPrefixNode.setPrefixLength(commonFullPrefixLength);


                    String addPrefixSlice = prefixSlice.substring(commonLength);
                    PrefixRadixNode addPrefixNode = new PrefixRadixNode();
                    addPrefixNode.setPrefixSlice(addPrefixSlice);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setGraphIndex(graphIndex);
                    // 临界表中创建节点


                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));

                    tempPrefixNode.addPrefixNode(addPrefixNode);
                    tempPrefixNode.addPrefixNode(nextNode);

                    currentNode.addPrefixNode(tempPrefixNode);

                    return new TrieUpdateResult(prefixParentNode, true, true, addPrefixNode);
                }
            } else {
                nextNode = currentNode.getRightPrefixChildNode();
                if (nextNode == null)
                {
                    PrefixRadixNode addPrefixNode = new PrefixRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setGraphIndex(graphIndex);
                    currentNode.setRightPrefixChildNode(addPrefixNode);
                    return new TrieUpdateResult(prefixParentNode, true, true, addPrefixNode);
                }
                int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                if (commonLength == nextNode.getPrefixSlice().length() && commonLength == prefixSlice.length())
                {
                    if (nextNode.getGraphIndex() == 0)
                    {
                        nextNode.setGraphIndex(graphIndex);
                        return new TrieUpdateResult(prefixParentNode, true, true, nextNode);
                        //需要创建节点
                    } else {
                        //节点已经存在了
                        TrieUpdateResult trieUpdateResult = new TrieUpdateResult(prefixParentNode, false, false, null);
                        trieUpdateResult.setGraphIndex(nextNode.getGraphIndex());
                        return trieUpdateResult;
                    }
                } else if (commonLength == nextNode.getPrefixSlice().length() && commonLength < prefixSlice.length())
                {
                    currentNode = nextNode;
                    if (currentNode.getGraphIndex() != 0)
                    {
                        prefixParentNode = currentNode.getGraphIndex();
                    }
                    prefixSlice = prefixSlice.substring(commonLength);
                } else if (commonLength == prefixSlice.length() && commonLength < nextNode._prefixSlice.length())
                {
                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));
                    PrefixRadixNode addPrefixNode = new PrefixRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.addPrefixNode(nextNode);
                    addPrefixNode.setGraphIndex(graphIndex);
                    currentNode.setRightPrefixChildNode(addPrefixNode);
                    //需要在邻接表中创建节点
                    return new TrieUpdateResult(prefixParentNode, true, true, addPrefixNode);
                } else {
                    String commonPrefix = prefixSlice.substring(0, commonLength);
                    int commonFullPrefixLength = prefixLength - (prefixSlice.length() - commonLength);
                    String commonFullPrefix = addPrefix.substring(0, commonFullPrefixLength);

                    PrefixRadixNode tempPrefixNode = new PrefixRadixNode();
                    tempPrefixNode.setPrefixSlice(commonPrefix);
                    tempPrefixNode.setFullPrefix(commonFullPrefix);
                    tempPrefixNode.setPrefixLength(commonFullPrefixLength);


                    String addPrefixSlice = prefixSlice.substring(commonLength);
                    PrefixRadixNode addPrefixNode = new PrefixRadixNode();
                    addPrefixNode.setPrefixSlice(addPrefixSlice);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setGraphIndex(graphIndex);
                    // 临界表中创建节点

                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));

                    tempPrefixNode.addPrefixNode(addPrefixNode);
                    tempPrefixNode.addPrefixNode(nextNode);

                    currentNode.addPrefixNode(tempPrefixNode);
                    return new TrieUpdateResult(prefixParentNode, true, true, addPrefixNode);
                }
            }
        }
        return new TrieUpdateResult();
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
