package org.batfish.MEVNEW.MainProcedure;


import org.batfish.MEVNEW.EnsembleModel.Engine.EnsembleControlPlaneModelConstructEngine;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.AddCustomer;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.AddVPC;
import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.UpdateRouterVrfPrefixPair;
import org.batfish.MEVNEW.EnsembleVerification.DestinationPair;
import org.batfish.MEVNEW.EnsembleVerification.ForwardingTopology.ForwardingTopologyConstructorEngine;
import org.batfish.MEVNEW.EnsembleVerification.Property;
import org.batfish.MEVNEW.EnsembleVerification.PropertyVerificationEngine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.isis.IsisTopology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VerificationEngine {
    private EnsembleControlPlaneModelConstructEngine _modelConstructEngine;
    private ForwardingTopologyConstructorEngine _forwardingTopologyConstructorEngine;
    private PropertyVerificationEngine _propertyVerificationEngine;
    private Map<String, List<Property>> _propertyUnderVerification;
    private Map<String, Configuration> _configurations;
    private IsisTopology _isisTopology;
    private Topology _L3Topology;
    private List<DestinationPair> _destinationPairList;
    private Map<Prefix, DestinationPair> _prefixToDestinationPair;

    public VerificationEngine(Map<String, Configuration> configurations, IsisTopology isisTopology, Topology L3Topology)
    {
        this._configurations = configurations;
        this._isisTopology = isisTopology;
        this._L3Topology = L3Topology;
        this._destinationPairList = new ArrayList<>();
        this._prefixToDestinationPair = new HashMap<>();
    }

    public EnsembleControlPlaneModelConstructEngine getModelConstructEngine()
    {
        return this._modelConstructEngine;
    }

    public ForwardingTopologyConstructorEngine getForwardingTopologyConstructorEngine()
    {
        return this._forwardingTopologyConstructorEngine;
    }

    public PropertyVerificationEngine getPropertyVerificationEngine()
    {
        return this._propertyVerificationEngine;
    }

    public void initializeReachabilityProperty()
    {
        List<DestinationPair> destinationPairListNew = new ArrayList<>();
        HashMap<Prefix, DestinationPair> prefixToDestinationPair = new HashMap<>();
        for (String router : _configurations.keySet())
        {
            Configuration config = _configurations.get(router);
            for (String vrfName : config.getVrfs().keySet())
            {
                Vrf vrf = config.getVrfs().get(vrfName);
                if (vrf.getBgpProcess() != null)
                {
                    List<Prefix> prefixList = vrf.getBgpProcess().getNetworkPrefixs().get(AddressFamily.Type.IPV4_UNICAST);
                    if (prefixList != null)
                    {
                        DestinationPair destinationPair = new DestinationPair(router, vrfName, prefixList);
                        destinationPairListNew.add(destinationPair);
                        for (Prefix prefix : prefixList)
                        {
                            prefixToDestinationPair.put(prefix, destinationPair);
                        }
                    }
                }
            }
        }
        _destinationPairList = destinationPairListNew;
        _prefixToDestinationPair = prefixToDestinationPair;
        _propertyUnderVerification = _configurations.keySet().parallelStream()
                .flatMap(srcRouter -> {
                    Configuration config = _configurations.get(srcRouter);
                    return config.getVrfs().keySet().stream()  // 不需要并行流
                            .filter(srcVrfName -> !srcVrfName.equals("management"))
                            .flatMap(srcVrfName -> destinationPairListNew.parallelStream()  // 继续对 destinationPairListNew 使用并行流
                                    .filter(dstPair -> !srcRouter.equals(dstPair.getDstRouter()) && srcVrfName.equals(dstPair.getDstVrf()))
                                    .flatMap(dstPair -> dstPair.getDstPrefix().stream()
                                            .map(dstPrefix -> new Property(srcRouter, srcVrfName, dstPair.getDstRouter(), dstPair.getDstVrf(), dstPrefix))
                                    )
                            );
                })
                .collect(Collectors.groupingBy(
                        Property::getSrcRouter
                ));
    }

    public void computeControlPlane()
    {
        _modelConstructEngine = new EnsembleControlPlaneModelConstructEngine();
        _modelConstructEngine.controlPlaneConstructor(_configurations, _isisTopology, _L3Topology);
        _modelConstructEngine.splitModel();
        _modelConstructEngine.controlPlaneReasoning();
    }

    public void verifySpecificReachability(String srcRouter, String srcVrf, String dstRouter, String dstVrf, String dstPrefix)
    {
        _propertyUnderVerification = new HashMap<>();
        _propertyUnderVerification.put(srcRouter, new ArrayList<>());
        _propertyUnderVerification.get(srcRouter).add(new Property(srcRouter.toLowerCase(), srcVrf.toLowerCase(), dstRouter.toLowerCase(), dstVrf.toLowerCase(), Prefix.parse(dstPrefix)));
        if (_propertyVerificationEngine == null)
        {
            _forwardingTopologyConstructorEngine = new ForwardingTopologyConstructorEngine();
            _forwardingTopologyConstructorEngine.constructForwardingTopology(_configurations, _modelConstructEngine.getEnsembleModel(), _modelConstructEngine.getConnectMessage(), _modelConstructEngine.getStaticMessage());
            _propertyVerificationEngine = new PropertyVerificationEngine(_configurations, _forwardingTopologyConstructorEngine.getForwardingTopology());
        }
        _propertyVerificationEngine.propertyVerifierWithoutSymbolicWithSplitPDG(_propertyUnderVerification);
    }

    public void verifyAllReachability()
    {
        initializeReachabilityProperty();
        _forwardingTopologyConstructorEngine = new ForwardingTopologyConstructorEngine();
        _forwardingTopologyConstructorEngine.constructForwardingTopology(_configurations, _modelConstructEngine.getEnsembleModel(), _modelConstructEngine.getConnectMessage(), _modelConstructEngine.getStaticMessage());

        _propertyVerificationEngine = new PropertyVerificationEngine(_configurations, _forwardingTopologyConstructorEngine.getForwardingTopology());
        _propertyVerificationEngine.propertyVerifierWithoutSymbolicWithSplitPDG(_propertyUnderVerification);
    }

    public void verify()
    {
        _modelConstructEngine = new EnsembleControlPlaneModelConstructEngine();
        _modelConstructEngine.controlPlaneConstructor(_configurations, _isisTopology, _L3Topology);
        _modelConstructEngine.splitModel();
        _modelConstructEngine.controlPlaneReasoning();

        _forwardingTopologyConstructorEngine = new ForwardingTopologyConstructorEngine();
        _forwardingTopologyConstructorEngine.constructForwardingTopology(_configurations, _modelConstructEngine.getEnsembleModel(), _modelConstructEngine.getConnectMessage(), _modelConstructEngine.getStaticMessage());

        _propertyVerificationEngine = new PropertyVerificationEngine(_configurations, _forwardingTopologyConstructorEngine.getForwardingTopology());
        _propertyVerificationEngine.propertyVerifierWithoutSymbolicWithSplitPDG(_propertyUnderVerification);
    }

    public void incrementalVerify(List<AddCustomer> addCustomerList)
    {
        _modelConstructEngine.incrementalEnsembleModelUnderVPCAdd(new AddVPC(addCustomerList));
        _modelConstructEngine.incrementalControlPlaneReasoning();
        Map<String, Map<String, UpdateRouterVrfPrefixPair>> updateRouterVrfPrefixPair = _modelConstructEngine.computeUpdateRouterVrfPrefixPair();
        _forwardingTopologyConstructorEngine.updateForwardingTopology(addCustomerList, _modelConstructEngine.getModel(), updateRouterVrfPrefixPair);

        Map<String, List<Property>> incrementalProperty = computeIncrementalProperty(updateRouterVrfPrefixPair, addCustomerList, _destinationPairList, _prefixToDestinationPair);
        _propertyVerificationEngine.propertyVerifierWithoutSymbolicWithSplitPDG(incrementalProperty);
    }

    public Map<String, List<Property>> computeIncrementalProperty(Map<String, Map<String, UpdateRouterVrfPrefixPair>> updateRouterVrfPrefixPair, List<AddCustomer> addCustomerList, List<DestinationPair> destinationPairListNew, Map<Prefix, DestinationPair> prefixToDestinationPair)
    {
        for (AddCustomer customerAdd : addCustomerList)
        {
            String router = customerAdd.getAddRouter();
            String vrf = customerAdd.getAddVrf();
            Prefix prefix = customerAdd.getAddPrefix();

            List<Prefix> addPrefix = new ArrayList<>();
            addPrefix.add(prefix);

            DestinationPair destinationPair = new DestinationPair(router, vrf, addPrefix);
            destinationPairListNew.add(destinationPair);
            prefixToDestinationPair.put(prefix, destinationPair);
        }

        Map<String, List<Property>> incrementalPropertyList = new HashMap<>();
        for (String router : updateRouterVrfPrefixPair.keySet())
        {
            for (String vrf : updateRouterVrfPrefixPair.get(router).keySet())
            {
                UpdateRouterVrfPrefixPair routerVrfPair = updateRouterVrfPrefixPair.get(router).get(vrf);
                List<Property> incrementProperty;
                if (incrementalPropertyList.containsKey(router))
                {
                    incrementProperty = incrementalPropertyList.get(router);
                } else {
                    incrementProperty = new ArrayList<>();
                    incrementalPropertyList.put(router, incrementProperty);
                }
                for (Prefix dstPrefix : routerVrfPair.getUpdatePrefixes())
                {
                    if (!prefixToDestinationPair.containsKey(dstPrefix))
                    {
                        System.out.println("Increment property error: do not exist in incrementPrefix!");
                    }
                    DestinationPair destinationPair = prefixToDestinationPair.get(dstPrefix);
                    if (router.equals(destinationPair.getDstRouter()))
                    {
                        continue;
                    }
                    Property incrementPropertyTemp = new Property(router, vrf, destinationPair.getDstRouter(), destinationPair.getDstVrf(), dstPrefix);
                    incrementProperty.add(incrementPropertyTemp);
                }
            }
        }
        return incrementalPropertyList;
    }


    public void computeViolationTree()
    {
        _propertyVerificationEngine.computeFinalVerificationResult();
        _propertyVerificationEngine.computeViolationTree();
        _propertyVerificationEngine.computeAllViolatedTopLevelPropertyNode();
    }






    public void printTopLevelViolatedPropertyTable()
    {
        _propertyVerificationEngine.printTopViolationTable();
    }

    public void getAndPrintSpecificViolationTree(String srcRouter, String srcVrf, String dstRouter, String dstVrf, String dstPrefix)
    {
        _propertyVerificationEngine.printViolationTree(srcRouter, srcVrf, dstRouter, dstVrf, dstPrefix);
    }
}