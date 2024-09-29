package org.batfish.MEV;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PDG {

    public List<PDGNode> _graph;
    public HashMap<String, PrefixRadixTrie> _hashRadixIndex;


    public List<PDGNode> _toBeVerifiedNodes;
    public HashMap<Integer, List<PDGNode>> _graphParentNodes;
    public HashMap<Integer, List<PDGNode>> _graphChildNodes;

    public Integer _avoidVerificationNum = 0;
    public Integer _needVerificationNum = 0;
    public List<PDGNode> _toBeVerifiedSubnetNodes;
    public List<PDGNode> _toBeVerifiedIpNodes;
    public HashMap<String, List<PDGNode>> _vrfRelatedPDGNode;
    public PDG()
    {
        this._vrfRelatedPDGNode = new HashMap<>();
        this._graph = new ArrayList<>();
        this._hashRadixIndex = new HashMap<>();
        this._toBeVerifiedNodes = new ArrayList<>();
        this._graphParentNodes = new HashMap<>();
        this._graphChildNodes = new HashMap<>();
        this._avoidVerificationNum = 0;
        this._needVerificationNum = 0;
        this._toBeVerifiedSubnetNodes = new ArrayList<>();
        this._toBeVerifiedIpNodes = new ArrayList<>();
    }

    public void addDependentProperties(List<PDGNodeDependencyPair> pdgNodePropertyPairs)
    {
        pdgNodePropertyPairs.sort(Comparator.comparing(p -> p.getProperty().getDstPrefix().getPrefixLength()));
        for (PDGNodeDependencyPair pdgNodePropertyPair : pdgNodePropertyPairs)
        {
            List<Integer> parentPDGNode = pdgNodePropertyPair.getParentPDGNode();
            Property property = pdgNodePropertyPair.getProperty();
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
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        this._avoidVerificationNum += 1;
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                    } else {
                        this._needVerificationNum += 1;
                        this._toBeVerifiedNodes.add(pdgNode);
                        if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(pdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(pdgNode);
                        }
                    }
                    this._graph.add(pdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    for (Integer nodeIndex : parentPDGNode)
                    {
                        this._graphChildNodes.get(nodeIndex).add(pdgNode);
                        this._graphParentNodes.get(graphIndex).add(this._graph.get(nodeIndex));
                    }
                } else {
                    this._avoidVerificationNum += 1;
                    for (Integer nodeIndex : parentPDGNode)
                    {
                        this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(nodeIndex));
                        this._graphChildNodes.get(nodeIndex).add(this._graph.get(updateResult.getGraphIndex()));
                    }
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        this._avoidVerificationNum += 1;
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                    } else {
                        this._needVerificationNum += 1;
                        this._toBeVerifiedNodes.add(pdgNode);
                        if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(pdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(pdgNode);
                        }
                    }
                    this._graph.add(pdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    for (Integer nodeIndex : parentPDGNode)
                    {
                        this._graphChildNodes.get(nodeIndex).add(pdgNode);
                        this._graphParentNodes.get(graphIndex).add(this._graph.get(nodeIndex));
                    }
                } else {
                    this._avoidVerificationNum += 1;
                    for (Integer nodeIndex : parentPDGNode)
                    {
                        this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(nodeIndex));
                        this._graphChildNodes.get(nodeIndex).add(this._graph.get(updateResult.getGraphIndex()));
                    }
                }
            }
        }
    }

    public List<Integer> addSubPrefixProperties(PDGNodeSubPrefixPair pdgNodePropertyPair)
    {
        Integer parentPDGNode = pdgNodePropertyPair.getParentPDGNode();
        List<Property> properties = pdgNodePropertyPair.getProperty();
        List<Integer> addGraphIndex = new ArrayList<>();
        Integer A = 1;
        String B = "6";
        NestedDependency dep = pdgNodePropertyPair.getDep();
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
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setVerifiedResult(1);
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    pdgNode.addDep(dep);
                    this._graph.add(pdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.get(parentPDGNode).add(pdgNode);
                    this._graphParentNodes.get(graphIndex).add(this._graph.get(parentPDGNode));
                } else {
                    graphIndex = updateResult.getGraphIndex();
                    this._graph.get(graphIndex).addDep(dep);
                    this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(parentPDGNode));
                    this._graphChildNodes.get(parentPDGNode).add(this._graph.get(updateResult.getGraphIndex()));
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    pdgNode.setVerifiedResult(1);
                    pdgNode.addDep(dep);
                    this._graph.add(pdgNode);
                    this._graphParentNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.put(graphIndex, new ArrayList<>());
                    this._graphChildNodes.get(parentPDGNode).add(pdgNode);
                    this._graphParentNodes.get(graphIndex).add(this._graph.get(parentPDGNode));
                } else {
                    graphIndex = updateResult.getGraphIndex();
                    this._graph.get(graphIndex).addDep(dep);
                    this._graphParentNodes.get(updateResult.getGraphIndex()).add(this._graph.get(parentPDGNode));
                    this._graphChildNodes.get(parentPDGNode).add(this._graph.get(updateResult.getGraphIndex()));
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
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(pdgNode);
                        if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(pdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(pdgNode);
                        }
                    }
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(pdgNode);
                        if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(pdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(pdgNode);
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
            if (updateResult.getAddPDGNodeTag())
            {
                PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                if (updateResult.getPrefixParentNode() != 0)
                {
                    if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                    {
                        pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                    }
                    this._graph.add(pdgNode);
                } else {
                    this._graph.add(pdgNode);
                    this._toBeVerifiedNodes.add(pdgNode);
                    if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                    {
                        this._toBeVerifiedIpNodes.add(pdgNode);
                    } else {
                        this._toBeVerifiedSubnetNodes.add(pdgNode);
                    }
                }
            }
            this._hashRadixIndex.put(hashName, prefixRadixTrie);
        } else {
            ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
            TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
            if (updateResult.getAddPDGNodeTag())
            {
                PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                if (updateResult.getPrefixParentNode() != 0)
                {
                    if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                    {
                        pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                    }
                    this._graph.add(pdgNode);
                } else {
                    this._graph.add(pdgNode);
                    this._toBeVerifiedNodes.add(pdgNode);
                    if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                    {
                        this._toBeVerifiedIpNodes.add(pdgNode);
                    } else {
                        this._toBeVerifiedSubnetNodes.add(pdgNode);
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
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(pdgNode);
                        if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(pdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(pdgNode);
                        }
                    }
                }
                this._hashRadixIndex.put(hashName, prefixRadixTrie);
            } else {
                ParserResult parserResult = parsePrefixToBinary(property.getDstPrefix().toString());
                TrieUpdateResult updateResult = prefixRadixTrie.addNode(parserResult.getPrefixString(), parserResult.getLength(), _graph.size());
                if (updateResult.getAddPDGNodeTag())
                {
                    PDGNode pdgNode = new PDGNode(property.getSrcRouter(), property.getSrcVrf(), property.getDstRouter(), property.getDstVrf(), property.getDstPrefix(), this._graph.size());
                    pdgNode.setPrefixRadixNode(updateResult.getPrefixRadixTrieNode());
                    if (updateResult.getPrefixParentNode() != 0)
                    {
                        if (this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult() != -1)
                        {
                            pdgNode.setVerifiedResult(this._graph.get(updateResult.getPrefixParentNode()).getVerifiedResult());
                        }
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                    } else {
                        this._graph.add(pdgNode);
                        this._graphParentNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._graphChildNodes.put(pdgNode.getGraphIndex(), new ArrayList<>());
                        this._toBeVerifiedNodes.add(pdgNode);
                        if (pdgNode.getDstPrefix().getPrefixLength() == 32)
                        {
                            this._toBeVerifiedIpNodes.add(pdgNode);
                        } else {
                            this._toBeVerifiedSubnetNodes.add(pdgNode);
                        }
                    }
                } else {
                    Integer nodeIndex = updateResult.getGraphIndex();
                    PDGNode upNode = this._graph.get(nodeIndex);
                    List<PDGNode> childNode = this._graphChildNodes.get(nodeIndex);
                    for (PDGNode node : childNode)
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






    public void PDGClear()
    {
        this._toBeVerifiedNodes.clear();
        this._toBeVerifiedSubnetNodes.clear();
        this._toBeVerifiedIpNodes.clear();
    }

    public List<PDGNode> getGraph()
    {
        return this._graph;
    }

    public List<PDGNode> getToBeVerifiedNodes()
    {
        return this._toBeVerifiedNodes;
    }











    public List<PDGNode> getToBeVerifiedSubnetNodes()
    {
        return this._toBeVerifiedSubnetNodes;
    }

    public List<PDGNode> getToBeVerifiedIpNodes()
    {
        return this._toBeVerifiedIpNodes;
    }


    public void addRelatedVrf(Integer graphIndex, Set<String> relatedVrf)
    {
        for (String vrf : relatedVrf)
        {
            if (!this._vrfRelatedPDGNode.containsKey(vrf))
            {
                this._vrfRelatedPDGNode.put(vrf, new ArrayList<>());
            }
            this._vrfRelatedPDGNode.get(vrf).add(this._graph.get(graphIndex));
            this._graph.get(graphIndex).setRelatedVrf(relatedVrf);
        }
    }

    public void incrementVrf(Set<RouterVrfPair> incrementVrf)
    {
        Set<PDGNode> incrementPDGNode = new HashSet<>();
        for (RouterVrfPair vrf : incrementVrf)
        {
            String vrfName = vrf.getRouter() + "-" + vrf.getVrf();
            if (this._vrfRelatedPDGNode.containsKey(vrfName))
            {
                incrementPDGNode.addAll(this._vrfRelatedPDGNode.get(vrfName));
                this._vrfRelatedPDGNode.get(vrfName).clear();
            }
        }
        System.out.println("incrementPDGNode-number:"+incrementPDGNode.size());
        Set<PDGNode> childPDGNode = new HashSet<>();
        long startTime5 = System.currentTimeMillis();
        for (PDGNode pdgNode : incrementPDGNode)
        {
            Integer graphIndex = pdgNode.getGraphIndex();
            childPDGNode.addAll(this._graphChildNodes.get(graphIndex));
//            for (PDGNode downNode : this._graphChildNodes.get(graphIndex))
//            {
//                this._graphParentNodes.get(downNode.getGraphIndex()).remove(downNode);
//            }

            this._graphChildNodes.get(graphIndex).clear();
            pdgNode.getRelatedVrf().clear();
            if (pdgNode.getDstPrefix().getPrefixLength() == 32)
            {
                this._toBeVerifiedIpNodes.add(pdgNode);
            } else {
                this._toBeVerifiedSubnetNodes.add(pdgNode);
            }
        }
        childPDGNode
//                .parallelStream()
                .forEach(node -> {
                    this._graphParentNodes.get(node.getGraphIndex()).removeAll(incrementPDGNode);
                });
        long endTime5 = System.currentTimeMillis();
        System.out.println("child-node-number:" + childPDGNode.size());
        System.out.println("PDGUpdateTime:" + (endTime5 - startTime5));
    }
}
