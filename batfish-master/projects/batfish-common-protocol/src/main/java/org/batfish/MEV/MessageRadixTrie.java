package org.batfish.MEV;

import java.util.List;

public class MessageRadixTrie {


    private MessageRadixNode _root;

    public MessageRadixTrie ()
    {
        this._root = new MessageRadixNode();
    }


    public List<Message> getMatchMessage(String Ip)
    {
        String prefixSlice = Ip.toLowerCase();
        MessageRadixNode currentNode = _root;
        while (!prefixSlice.isEmpty())
        {
            String longestPrefix = "";
            MessageRadixNode nextNode = null;
            Character bit = prefixSlice.charAt(0);
            if (bit == '0')
            {
                nextNode = currentNode.getLeftChildNode();
                if (nextNode == null)
                {
                    return currentNode.getMessageList();
                } else {
                    int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                    if (commonLength == nextNode._prefixSlice.length())
                    {
                        currentNode = nextNode;
                        prefixSlice = prefixSlice.substring(commonLength);
                    } else {
                        return currentNode.getMessageList();
                    }
                }
            } else {
                nextNode = currentNode.getRightChildNode();
                if (nextNode == null)
                {
                    return currentNode.getMessageList();
                } else {
                    int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                    if (commonLength == nextNode._prefixSlice.length())
                    {
                        currentNode = nextNode;
                        prefixSlice = prefixSlice.substring(commonLength);
                    } else {
                        return currentNode.getMessageList();
                    }
                }
            }
        }
        return currentNode.getMessageList();
    }

    public void addNodes(String addPrefix, Integer prefixLength, List<Message> messageList)
    {
        String prefixSlice = addPrefix.toLowerCase();
        MessageRadixNode currentNode = _root;
        Integer prefixParentNode = 0;
        while (!prefixSlice.isEmpty()) {
            String longestPrefix = "";
            MessageRadixNode nextNode = null;
            Character bit = prefixSlice.charAt(0);
            if (bit == '0')
            {
                nextNode = currentNode.getLeftChildNode();
                if (nextNode == null)
                {
                    MessageRadixNode addPrefixNode = new MessageRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setMessageList(messageList);;
                    currentNode.setLeftChildNode(addPrefixNode);
                    // 需要在邻接表中创建节点
                    return ;
                }
                int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                if (commonLength == nextNode.getPrefixSlice().length() && commonLength == prefixSlice.length())
                {
                    if (nextNode.getMessageList().isEmpty())
                    {
                        nextNode.setMessageList(messageList);
                        return ;
                        //需要创建节点
                    } else {
                        //节点已经存在
                        return ;
                    }
                } else if (commonLength == nextNode.getPrefixSlice().length() && commonLength < prefixSlice.length())
                {
                    currentNode = nextNode;
                    prefixSlice = prefixSlice.substring(commonLength);
                } else if (commonLength == prefixSlice.length() && commonLength < nextNode._prefixSlice.length())
                {
                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));
                    MessageRadixNode addPrefixNode = new MessageRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setMessageList(messageList);
                    addPrefixNode.addMessageRadixNode(nextNode);
                    currentNode.setLeftChildNode(addPrefixNode);
                    //保存父节点以及对应子节点的验证关系,
                    //需要在邻接表中创建节点
                    return ;
                } else {
                    String commonPrefix = prefixSlice.substring(0, commonLength);
                    int commonFullPrefixLength = prefixLength - (prefixSlice.length() - commonLength);
                    String commonFullPrefix = addPrefix.substring(0, commonFullPrefixLength);

                    MessageRadixNode tempPrefixNode = new MessageRadixNode();
                    tempPrefixNode.setPrefixSlice(commonPrefix);
                    tempPrefixNode.setFullPrefix(commonFullPrefix);
                    tempPrefixNode.setPrefixLength(commonFullPrefixLength);


                    String addPrefixSlice = prefixSlice.substring(commonLength);
                    MessageRadixNode addPrefixNode = new MessageRadixNode();
                    addPrefixNode.setPrefixSlice(addPrefixSlice);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setMessageList(messageList);
                    // 临界表中创建节点


                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));

                    tempPrefixNode.addMessageRadixNode(addPrefixNode);
                    tempPrefixNode.addMessageRadixNode(nextNode);

                    currentNode.addMessageRadixNode(tempPrefixNode);

                    return ;
                }
            } else {
                nextNode = currentNode.getRightChildNode();
                if (nextNode == null)
                {
                    MessageRadixNode addPrefixNode = new MessageRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setMessageList(messageList);
                    currentNode.setRightChildNode(addPrefixNode);
                    return ;
                }
                int commonLength = commonPrefixLength(nextNode.getPrefixSlice(), prefixSlice);
                if (commonLength == nextNode.getPrefixSlice().length() && commonLength == prefixSlice.length())
                {
                    if (nextNode.getMessageList().isEmpty())
                    {
                        nextNode.setMessageList(messageList);
                        return ;
                        //需要创建节点
                    } else {
                        //节点已经存在了
                        return ;
                    }
                } else if (commonLength == nextNode.getPrefixSlice().length() && commonLength < prefixSlice.length())
                {
                    currentNode = nextNode;
                    prefixSlice = prefixSlice.substring(commonLength);
                } else if (commonLength == prefixSlice.length() && commonLength < nextNode._prefixSlice.length())
                {
                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));
                    MessageRadixNode addPrefixNode = new MessageRadixNode();
                    addPrefixNode.setPrefixSlice(prefixSlice);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.addMessageRadixNode(nextNode);
                    addPrefixNode.setMessageList(messageList);
                    currentNode.setRightChildNode(addPrefixNode);
                    //需要在邻接表中创建节点
                    return ;
                } else {
                    String commonPrefix = prefixSlice.substring(0, commonLength);
                    int commonFullPrefixLength = prefixLength - (prefixSlice.length() - commonLength);
                    String commonFullPrefix = addPrefix.substring(0, commonFullPrefixLength);

                    MessageRadixNode tempPrefixNode = new MessageRadixNode();
                    tempPrefixNode.setPrefixSlice(commonPrefix);
                    tempPrefixNode.setFullPrefix(commonFullPrefix);
                    tempPrefixNode.setPrefixLength(commonFullPrefixLength);


                    String addPrefixSlice = prefixSlice.substring(commonLength);
                    MessageRadixNode addPrefixNode = new MessageRadixNode();
                    addPrefixNode.setPrefixSlice(addPrefixSlice);
                    addPrefixNode.setPrefixLength(prefixLength);
                    addPrefixNode.setFullPrefix(addPrefix);
                    addPrefixNode.setMessageList(messageList);
                    // 临界表中创建节点

                    nextNode.setPrefixSlice(nextNode.getPrefixSlice().substring(commonLength));

                    tempPrefixNode.addMessageRadixNode(addPrefixNode);
                    tempPrefixNode.addMessageRadixNode(nextNode);

                    currentNode.addMessageRadixNode(tempPrefixNode);
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
