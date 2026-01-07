package org.batfish.MEVNEW.EnsembleModel.Engine;
import org.batfish.MEVNEW.EnsembleModel.Node.EVPN;
import org.batfish.datamodel.Ip;

import java.util.ArrayList;
import java.util.List;

public class EvpnNeighborTriple extends NeighborTriple{
    private EVPN _neighborBgpNode;
    private boolean _reflector;
    private List<Ip> _localIpList;
    public EvpnNeighborTriple(String router, String vrf, Ip ip, EVPN bgpNode)
    {
        super(router, vrf);
        this.setNeighborIp(ip);
        this._neighborBgpNode = bgpNode;
        this._reflector = false;
        this._localIpList = new ArrayList<>();
    }
    public EVPN getNeighborEvpnNode()
    {
        return this._neighborBgpNode;
    }
    public boolean getReflector()
    {
        return this._reflector;
    }
    public List<Ip> getLocalIpList()
    {
        return _localIpList;
    }
    public void setNeighborEvpnNode(EVPN neighborBgpNode)
    {
        this._neighborBgpNode = neighborBgpNode;
    }
    public void setReflector(boolean reflector)
    {
        this._reflector = reflector;
    }
    public void setLocalIpList(List<Ip> localIpList)
    {
        this._localIpList.addAll(localIpList);
    }
}
