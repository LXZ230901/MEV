package org.batfish.MEVNEW.EnsembleModel.Engine;





import org.batfish.MEVNEW.EnsembleModel.Node.BGP;
import org.batfish.datamodel.Ip;

import java.util.ArrayList;
import java.util.List;

public class BgpNeighborTriple extends NeighborTriple{
    private BGP _neighborBgpNode;
    private boolean _reflector;
    private List<Ip> _localIpList;
    private Integer _vni;
    public BgpNeighborTriple(String router, String vrf, Ip ip, BGP bgpNode)
    {
        super(router, vrf);
        this.setNeighborIp(ip);
        this._neighborBgpNode = bgpNode;
        this._reflector = false;
        this._localIpList = new ArrayList<>();
        this._vni = 0;
    }
    public BGP getNeighborBgpNode()
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
    public Integer getVni()
    {
        return this._vni;
    }
    public void setNeighborBgpNode(BGP neighborBgpNode)
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
    public void setVni(Integer vni)
    {
        this._vni = vni;
    }
}
