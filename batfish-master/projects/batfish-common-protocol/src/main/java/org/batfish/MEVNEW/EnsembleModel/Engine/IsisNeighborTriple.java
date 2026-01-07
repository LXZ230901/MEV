package org.batfish.MEVNEW.EnsembleModel.Engine;


import org.batfish.MEVNEW.EnsembleModel.Node.ISIS;
import org.batfish.datamodel.isis.IsisLevel;

public class IsisNeighborTriple extends NeighborTriple{
    private ISIS _isisNeighborNode;
    private IsisLevel _isisLevel;
    private Long _isisCost;
    public IsisNeighborTriple(String router, String vrf, ISIS isisNode, IsisLevel isisLevel, Long isisCost)
    {
        super(router, vrf);
        this._isisNeighborNode = isisNode;
        this._isisLevel = isisLevel;
        this._isisCost = isisCost;
    }
    public ISIS getIsisNeighborNode()
    {
        return this._isisNeighborNode;
    }
    public IsisLevel getIsisLevel()
    {
        return this._isisLevel;
    }
    public Long getIsisCost()
    {
        return this._isisCost;
    }
    public void setIsisNeighborNode(ISIS isisNode)
    {
        this._isisNeighborNode = isisNode;
    }
    public void setIsisLevel(IsisLevel isisLevel)
    {
        this._isisLevel = isisLevel;
    }
    public void setIsisCost(Long isisCost)
    {
        this._isisCost = isisCost;
    }
}
