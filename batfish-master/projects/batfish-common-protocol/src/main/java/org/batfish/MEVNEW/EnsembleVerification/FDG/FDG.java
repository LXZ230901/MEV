package org.batfish.MEVNEW.EnsembleVerification.FDG;


import org.batfish.MEVNEW.EnsembleVerification.FIB.NestedDependency;
import org.batfish.MEVNEW.EnsembleVerification.ForwardingTopology.PrefixParser;
import org.batfish.MEVNEW.EnsembleVerification.Property;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FDG {

    public List<FDGNode> _graph;
    public HashMap<String, PrefixRadixTrie> _hashRadixIndex;


    public List<FDGNode> _toBeVerifiedNodes;
    public HashMap<Integer, List<FDGNode>> _graphParentNodes;
    public HashMap<Integer, List<FDGNode>> _graphChildNodes;

    public Integer _avoidVerificationNum = 0;
    public Integer _needVerificationNum = 0;
    public List<FDGNode> _toBeVerifiedSubnetNodes;
    public List<FDGNode> _toBeVerifiedIpNodes;
    public HashMap<String, List<FDGNode>> _vrfRelatedFDGNode;

    public Set<FDGNode> _rootNodes;
    public Set<FDGNode> _violatedRootNodes;
    public FDG()
    {
        this._vrfRelatedFDGNode = new HashMap<>();
        this._graph = new ArrayList<>();
        this._hashRadixIndex = new HashMap<>();
        this._toBeVerifiedNodes = new ArrayList<>();
        this._graphParentNodes = new HashMap<>();
        this._graphChildNodes = new HashMap<>();
        this._avoidVerificationNum = 0;
        this._needVerificationNum = 0;
        this._toBeVerifiedSubnetNodes = new ArrayList<>();
        this._toBeVerifiedIpNodes = new ArrayList<>();
        this._rootNodes = new HashSet<>();
        this._violatedRootNodes = new HashSet<>();
    }

    public void addDependentProperties(List<FDGNodeDependencyPair> fdgNodePropertyPairs)
    {
        fdgNodePropertyPairs.sort(Comparator.comparing(p -> p.getProperty().getDstPrefix().getPrefixLength()));
        for (FDGNodeDependencyPair fdgNodePropertyPair : fdgNodePropertyPairs)
        {
            List<Integer> parentFDGNode = fdgNodePropertyPair.getParentFDGNode();
            Property property = fdgNodePropertyPair.getProperty();
            String hashName = property.getSrcRouter() + " " + property.getSrcVrf() + " " + property.getDstRouter() + " " + property.getDstVrf();
            // 首先，来一个属性节点我们需要在Trie中查询是否有对应的节点，过程中需要记录遍历了哪一些节点，然后如果节点已经验证完成的话，那么就直接用其结果；如果没有的话，就需要加进入
            // 还有,需要记录的就是他们节点之间的更新关系
            // 还有应该记录他们在图中的位置,以及他们之间的更新关系,在最后更新的时候再更新验证关系也可以
            PrefixRadixTrie prefixRadixTrie = this._hashRadixIndex.get(hashName);
            Integer graphIndex = this._graph.size();
            if (prefixRadixTrie == null)
            {
                prefixRadixTrie = new PrefixRadixTrie();
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(PrefixParser.convertToBinaryIP(property.getDstPrefix().toString()), PrefixParser.extractPrefixLength(property.getDstPrefix().toString()), graphIndex);
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        this._avoidVerificationNum += 1;
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            fdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                    } else {
                        this._needVerificationNum += 1;
                        this._toBeVerifiedNodes.add(fdgNode);
                        if (fdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(fdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(fdgNode);
                        }
                    }
                    this._graph.add(fdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    for (Integer nodeIndex : parentFDGNode)
                    {
                        this._graphChildNodes.get(nodeIndex).add(fdgNode);
                        this._graphParentNodes.get(graphIndex).add(this._graph.get(nodeIndex));
                    }
                } else {
                    this._avoidVerificationNum += 1;
                    for (Integer nodeIndex : parentFDGNode)
                    {
                        this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(nodeIndex));
                        this._graphChildNodes.get(nodeIndex).add(this._graph.get(updateResult.getGraphIndex()));
                    }
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        this._avoidVerificationNum += 1;
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            fdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                    } else {
                        this._needVerificationNum += 1;
                        this._toBeVerifiedNodes.add(fdgNode);
                        if (fdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(fdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(fdgNode);
                        }
                    }
                    this._graph.add(fdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    for (Integer nodeIndex : parentFDGNode)
                    {
                        this._graphChildNodes.get(nodeIndex).add(fdgNode);
                        this._graphParentNodes.get(graphIndex).add(this._graph.get(nodeIndex));
                    }
                } else {
                    this._avoidVerificationNum += 1;
                    for (Integer nodeIndex : parentFDGNode)
                    {
                        this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(nodeIndex));
                        this._graphChildNodes.get(nodeIndex).add(this._graph.get(updateResult.getGraphIndex()));
                    }
                }
            }
        }
    }

    public List<Integer> addSubPrefixProperties(FDGNodeSubPrefixPair fdgNodePropertyPair)
    {
        Integer parentFDGNode = fdgNodePropertyPair.getParentFDGNode();
        List<Property> properties = fdgNodePropertyPair.getProperty();
        List<Integer> addGraphIndex = new ArrayList<>();
        Integer A = 1;
        String B = "6";
        NestedDependency dep = fdgNodePropertyPair.getDep();
        for (Property property : properties)
        {
            String hashName = property.getSrcRouter() + " " + property.getSrcVrf() + " " + property.getDstRouter() + " " + property.getDstVrf();
            // 首先，来一个属性节点我们需要在Trie中查询是否有对应的节点，过程中需要记录遍历了哪一些节点，然后如果节点已经验证完成的话，那么就直接用其结果；如果没有的话，就需要加进入
            // 还有,需要记录的就是他们节点之间的更新关系
            // 还有应该记录他们在图中的位置,以及他们之间的更新关系,在最后更新的时候再更新验证关系也可以
            PrefixRadixTrie prefixRadixTrie = this._hashRadixIndex.get(hashName);
            Integer graphIndex = this._graph.size();
            if (prefixRadixTrie == null)
            {
                prefixRadixTrie = new PrefixRadixTrie();
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), graphIndex);
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    fdgNode.setVerifiedResult(1);
                    fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    fdgNode.addDep(dep);
                    this._graph.add(fdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.get(parentFDGNode).add(fdgNode);
                    this._graphParentNodes.get(graphIndex).add(this._graph.get(parentFDGNode));
                } else {
                    graphIndex = updateResult.getGraphIndex();
                    this._graph.get(graphIndex).addDep(dep);
                    this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(parentFDGNode));
                    this._graphChildNodes.get(parentFDGNode).add(this._graph.get(updateResult.getGraphIndex()));
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    fdgNode.setVerifiedResult(1);
                    fdgNode.addDep(dep);
                    this._graph.add(fdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.get(parentFDGNode).add(fdgNode);
                    this._graphParentNodes.get(graphIndex).add(this._graph.get(parentFDGNode));
                } else {
                    graphIndex = updateResult.getGraphIndex();
                    this._graph.get(graphIndex).addDep(dep);
                    this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(parentFDGNode));
                    this._graphChildNodes.get(parentFDGNode).add(this._graph.get(updateResult.getGraphIndex()));
                }
            }
            addGraphIndex.add(graphIndex);
        }
        return addGraphIndex;
    }

    public void addPropertyNodes(List<Property> propertys)
    {
        for (Property property : propertys)
        {
            String hashName = property.getSrcRouter() + " " + property.getSrcVrf() + " " + property.getDstRouter() + " " + property.getDstVrf();
            // 首先，来一个属性节点我们需要在Trie中查询是否有对应的节点，过程中需要记录遍历了哪一些节点，然后如果节点已经验证完成的话，那么就直接用其结果；如果没有的话，就需要加进入
            // 还有,需要记录的就是他们节点之间的更新关系
            // 还有应该记录他们在图中的位置,以及他们之间的更新关系,在最后更新的时候再更新验证关系也可以
            PrefixRadixTrie prefixRadixTrie = this._hashRadixIndex.get(hashName);
            if (prefixRadixTrie == null)
            {
                prefixRadixTrie = new PrefixRadixTrie();
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            fdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(fdgNode);
                        this._graphParentNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(fdgNode);
                        this._graphParentNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(fdgNode);
                        if (fdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(fdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(fdgNode);
                        }
                    }
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode FDGNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    FDGNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            FDGNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(FDGNode);
                        this._graphParentNodes.put(FDGNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(FDGNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(FDGNode);
                        this._graphParentNodes.put(FDGNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(FDGNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(FDGNode);
                        if (FDGNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(FDGNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(FDGNode);
                        }
                    }
                }
            }
        }
    }

    public void addPropertyNode(Property property)
    {
        String hashName = property.getSrcRouter() + " " + property.getSrcVrf() + " " + property.getDstRouter() + " " + property.getDstVrf();
        // 首先，来一个属性节点我们需要在Trie中查询是否有对应的节点，过程中需要记录遍历了哪一些节点，然后如果节点已经验证完成的话，那么就直接用其结果；如果没有的话，就需要加进入
        // 还有,需要记录的就是他们节点之间的更新关系
        // 还有应该记录他们在图中的位置,以及他们之间的更新关系,在最后更新的时候再更新验证关系也可以
        PrefixRadixTrie prefixRadixTrie = this._hashRadixIndex.get(hashName);
        if (prefixRadixTrie == null)
        {
            prefixRadixTrie = new PrefixRadixTrie();
            ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
            TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
            if (updateResult.getAddFDGNodeTag())
            {
                FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                if (updateResult.getPrefixParentNode() != 0)
                {
                    if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                    {
                        fdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                    }
                    this._graph.add(fdgNode);
                } else {
                    this._graph.add(fdgNode);
                    this._toBeVerifiedNodes.add(fdgNode);
                    if (fdgNode.getDstPrefix().getPrefixLength() == 32)
                    {
                        this._toBeVerifiedIpNodes.add(fdgNode);
                    } else {
                        this._toBeVerifiedSubnetNodes.add(fdgNode);
                    }
                }
            }
            this._hashRadixIndex.put(hashName, prefixRadixTrie);
        } else {
            ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
            TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
            if (updateResult.getAddFDGNodeTag())
            {
                FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                if (updateResult.getPrefixParentNode() != 0)
                {
                    if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                    {
                        fdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                    }
                    this._graph.add(fdgNode);
                } else {
                    this._graph.add(fdgNode);
                    this._toBeVerifiedNodes.add(fdgNode);
                    if (fdgNode.getDstPrefix().getPrefixLength() == 32)
                    {
                        this._toBeVerifiedIpNodes.add(fdgNode);
                    } else {
                        this._toBeVerifiedSubnetNodes.add(fdgNode);
                    }
                }
            }
        }
    }

    public void incrementPropertyNodes(List<Property> propertys)
    {
        for (Property property : propertys)
        {
            String hashName = property.getSrcRouter() + " " + property.getSrcVrf() + " " + property.getDstRouter() + " " + property.getDstVrf();
            // 首先，来一个属性节点我们需要在Trie中查询是否有对应的节点，过程中需要记录遍历了哪一些节点，然后如果节点已经验证完成的话，那么就直接用其结果；如果没有的话，就需要加进入
            // 还有,需要记录的就是他们节点之间的更新关系
            // 还有应该记录他们在图中的位置,以及他们之间的更新关系,在最后更新的时候再更新验证关系也可以
            PrefixRadixTrie prefixRadixTrie = this._hashRadixIndex.get(hashName);
            if (prefixRadixTrie == null)
            {
                prefixRadixTrie = new PrefixRadixTrie();
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            fdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(fdgNode);
                        this._graphParentNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(fdgNode);
                        this._graphParentNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(fdgNode);
                        if (fdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(fdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(fdgNode);
                        }
                    }
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddFDGNodeTag())
                {
                    FDGNode fdgNode = new FDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    fdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            fdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(fdgNode);
                        this._graphParentNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(fdgNode);
                        this._graphParentNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(fdgNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(fdgNode);
                        if (fdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(fdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(fdgNode);
                        }
                    }
                } else {
                    Integer nodeIndex = updateResult.getGraphIndex();
                    FDGNode upNode = this._graph.get(nodeIndex);
                    List<FDGNode> childNode = this._graphChildNodes.get(nodeIndex);
                    for (FDGNode node : childNode)
                    {
                        Integer downNodeIndex = node.getGraphIndex();
                        this._graphParentNodes.get(downNodeIndex).remove(upNode);
                    }
                    this._graphChildNodes.get(nodeIndex).clear();
                    if (upNode.getDstPrefix().getPrefixLength() == 32)
                    {
                        this._toBeVerifiedSubnetNodes.add(upNode);
                    } else {
                        this._toBeVerifiedIpNodes.add(upNode);
                    }
                }
            }
        }
    }

    public static class ParserResult {
        public String _binaryString;
        public int _length;

        public ParserResult(String binaryString, int length) {
            this._binaryString = binaryString;
            this._length = length;
        }

        public String getPrefixString()
        {
            return this._binaryString;
        }

        public Integer getLength()
        {
            return this._length;
        }
    }

    public static ParserResult parsePrefixToBinary(String prefix) {
        String[] parts = prefix.split("/");
        String ipPart = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        String[] octets = ipPart.split("\\.");
        StringBuilder binaryStringBuilder = new StringBuilder();

        for (String octet : octets) {
            int octetValue = Integer.parseInt(octet);
            String binaryString = String.format("%8s", Integer.toBinaryString(octetValue)).replace(' ', '0');
            binaryStringBuilder.append(binaryString);
        }

        String binaryString = binaryStringBuilder.toString().substring(0, prefixLength);

        return new ParserResult(binaryString, prefixLength);
    }

    public void FDGClear()
    {
        this._toBeVerifiedNodes.clear();
        this._toBeVerifiedSubnetNodes.clear();
        this._toBeVerifiedIpNodes.clear();
    }

    public List<FDGNode> getGraph()
    {
        return this._graph;
    }

    public List<FDGNode> getToBeVerifiedNodes()
    {
        return this._toBeVerifiedNodes;
    }

    public List<FDGNode> getToBeVerifiedSubnetNodes()
    {
        return this._toBeVerifiedSubnetNodes;
    }

    public List<FDGNode> getToBeVerifiedIpNodes()
    {
        return this._toBeVerifiedIpNodes;
    }

    public void addRelatedVrf(Integer graphIndex, Set<String> relatedVrf)
    {
        for (String vrf : relatedVrf)
        {
            if (!this._vrfRelatedFDGNode.containsKey(vrf))
            {
                this._vrfRelatedFDGNode.put(vrf, new ArrayList<>());
            }
            this._vrfRelatedFDGNode.get(vrf).add(this._graph.get(graphIndex));
            this._graph.get(graphIndex).setRelatedVrf(relatedVrf);
        }
    }

    public void computeFinalVerificationResult()
    {
        for (FDGNode node : this._rootNodes)
        {
            computeFinalVerificationResultForEachRoot(node);
        }
    }
    public int computeFinalVerificationResultForEachRoot(FDGNode root) {
        if (root == null) return 1;

        int result = root.getVerifiedResult();  // 自己的初始值

        // 子节点 DFS
        for (FDGNode child : _graphChildNodes.get(root.getGraphIndex())) {
            result *= computeFinalVerificationResultForEachRoot(child);
        }

        root.setFinalVerificationResult(result);


        return result;
    }

    public void findRootNodes()
    {
        for (FDGNode node : this._graph)
        {
            if (_graphParentNodes.get(node.getGraphIndex()).isEmpty())
            {
                this._rootNodes.add(node);
            }
        }
    }


    public void findViolatedRoot()
    {
        for (FDGNode node : this._rootNodes)
        {
            if (node.getFinalVerificationResult() == 0)
            {
                this._violatedRootNodes.add(node);
            }
        }
    }
    public HashMap<String, String> getViolationTree()
    {
        HashMap<String, String> violationTree = new HashMap<>();
        for (FDGNode node : this._violatedRootNodes)
        {
            String treeName = node.getSrcRouter() + "--" + node.getSrcVrf() + "--" + node.getDstRouter() + "--" + node.getDstVrf() + "--" + node.getDstPrefix().toString();
            violationTree.put(treeName, buildViolationGraph(node));
        }
        return violationTree;
    }

    public String buildViolationGraph(FDGNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Tree {\n");
        sb.append("    node [shape=box, style=filled, fontname=\"Arial\"];\n");

        buildDot(root, sb);

        sb.append("}\n");
        return sb.toString();
    }

    private void buildDot(FDGNode node, StringBuilder sb) {
        if (node == null) return;

        // final = 1 的节点不显示，且不递归继续它的子树
        if (node.getFinalVerificationResult() == 1) {
            return;
        }

        // final = 0 → 显示该节点
        String color = "#ffdddd"; // final=0 红色

        sb.append(String.format(
                "    \"%s\" [label=\"graphIndex=%s\\nsrc=%s(%s)\\ndst=%s(%s)\\nprefix=%s\\nfinal=%d\", fillcolor=\"%s\"];\n",
                node.getGraphIndex(),
                node.getGraphIndex(),
                node.getSrcRouter(),
                node.getSrcVrf(),
                node.getDstRouter(),
                node.getDstVrf(),
                node.getDstPrefix(),
                node.getFinalVerificationResult(),
                color
        ));

        // 递归子节点，仅输出 final=0 的节点
        for (FDGNode child : _graphChildNodes.get(node.getGraphIndex())) {
            if (child.getFinalVerificationResult() == 0) {
                sb.append(String.format("    \"%s\" -> \"%s\";\n",
                        node.getGraphIndex(),
                        child.getGraphIndex()
                ));
            }
            buildDot(child, sb);  // 继续深入，但只会显示 final=0 的节点
        }
    }

    public Set<FDGNode> getViolatedRootNode()
    {
        return _violatedRootNodes;
    }

//    public void incrementVrf(Set<RouterVrfPair> incrementVrf)
//    {
//        Set<FDGNode> incrementFDGNode = new HashSet<>();
//        for (RouterVrfPair vrf : incrementVrf)
//        {
//            String vrfName = vrf.getRouter() + "-" + vrf.getVrf();
//            if (this._vrfRelatedFDGNode.containsKey(vrfName))
//            {
//                incrementFDGNode.addAll(this._vrfRelatedFDGNode.get(vrfName));
//                this._vrfRelatedFDGNode.get(vrfName).clear();
//            }
//        }
//        System.out.println("incrementFDGNode-number:"+incrementFDGNode.size());
//        Set<FDGNode> childFDGNode = new HashSet<>();
//        long startTime5 = System.currentTimeMillis();
//        for (FDGNode FDGNode : incrementFDGNode)
//        {
//            Integer graphIndex = FDGNode.getGraphIndex();
//            childFDGNode.addAll(this._graphChildNodes.get(graphIndex));
////            for (FDGNode downNode : this._graphChildNodes.get(graphIndex))
////            {
////                this._graphParentNodes.get(downNode.getGraphIndex()).remove(downNode);
////            }
//
//            this._graphChildNodes.get(graphIndex).clear();
//            FDGNode.getRelatedVrf().clear();
//            if (FDGNode.getDstPrefix().getPrefixLength() == 32)
//            {
//                this._toBeVerifiedIpNodes.add(FDGNode);
//            } else {
//                this._toBeVerifiedSubnetNodes.add(FDGNode);
//            }
//        }
//        childFDGNode
////                .parallelStream()
//                .forEach(node -> {
//                    this._graphParentNodes.get(node.getGraphIndex()).removeAll(incrementFDGNode);
//                });
//        long endTime5 = System.currentTimeMillis();
//        System.out.println("child-node-number:" + childFDGNode.size());
//        System.out.println("FDGUpdateTime:" + (endTime5 - startTime5));
//    }
}
