package org.batfish.MEVNEW.EnsembleVerification.ForwardingTopology;

import org.batfish.MEVNEW.EnsembleModel.IncrementalOperation.AddCustomer;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForwardingTopology {
    public HashMap<String, ForwardingNode> _topology;
    public Map<String, Map<String, Integer>> _vrfToVniList;
    public ForwardingTopology()
    {
        this._topology = new HashMap<>();
        this._vrfToVniList = new HashMap<>();
    }





    public void initForwardingTopology(Map<String, Configuration> configurations)
    {
        for (String node : configurations.keySet())
        {
            ForwardingNode forwardingNode = new ForwardingNode(node);
            for (String vrf : configurations.get(node).getVrfs().keySet())
            {
                forwardingNode.getVrf().add(vrf);
            }
            this._topology.put(node, forwardingNode);
        }

        for (String router : configurations.keySet())
        {
            if (!this._vrfToVniList.containsKey(router))
            {
                this._vrfToVniList.put(router, new HashMap<>());
            }
            for (Vrf vrf : configurations.get(router).getVrfs().values())
            {
                String vrfName = vrf.getName();
                if (!vrf.getLayer3Vnis().isEmpty())
                {
                    for (Integer vni : vrf.getLayer3Vnis().keySet())
                    {
                        this._vrfToVniList.get(router).put(vrfName, vni);
                    }
                } else if (vrfName.equals("default"))
                {
                    this._vrfToVniList.get(router).put(vrfName, 0);
                }
            }
        }
    }

    public HashMap<String, ForwardingNode> getForwardingTopology()
    {
        return this._topology;
    }

    public Map<String, Map<String, Integer>> getVrfToVniList()
    {
        return this._vrfToVniList;
    }

    public void setVrfToVniList(Map<String, Map<String, Integer>> vrfToVniList)
    {
        this._vrfToVniList.putAll(vrfToVniList);
    }

    public void updateVrfToVniList(List<AddCustomer> addCustomerList)
    {
        for (AddCustomer addCustomer : addCustomerList)
        {
            String router = addCustomer.getAddRouter();
            String vrf = addCustomer.getAddVrf();
            Integer vni = addCustomer.getAddVni();
            if (!_vrfToVniList.containsKey(router))
            {
                _vrfToVniList.put(router, new HashMap<>());
            }
            _vrfToVniList.get(router).put(vrf, vni);
        }
    }
}
