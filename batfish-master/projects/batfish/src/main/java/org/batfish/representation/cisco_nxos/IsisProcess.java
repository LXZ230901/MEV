package org.batfish.representation.cisco_nxos;

import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.isis.IsisLevel;

import java.io.Serializable;


public class IsisProcess implements Serializable {

  private IsisLevel _level;

  private IsoAddress _netAddress;

//  private Map<RoutingProtocolInstance, IsisRedistributionPolicy> _redistributionPolicies;

  public IsisProcess() {
//    _redistributionPolicies = new HashMap<>();
  }

  public IsisLevel getLevel() {
    return _level;
  }

  public IsoAddress getNetAddress() {
    return _netAddress;
  }

//  public Map<RoutingProtocolInstance, IsisRedistributionPolicy> getRedistributionPolicies() {
//    return _redistributionPolicies;
//  }

  public void setLevel(IsisLevel level) {
    _level = level;
  }

  public void setNetAddress(IsoAddress netAddress) {
    _netAddress = netAddress;
  }
}
