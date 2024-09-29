package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PostInInterfacePostNat extends InterfaceStateExpr {
  public PostInInterfacePostNat(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPostInInterfacePostNat(this);
  }
}
