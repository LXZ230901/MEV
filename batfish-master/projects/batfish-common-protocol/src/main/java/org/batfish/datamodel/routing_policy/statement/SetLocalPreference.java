package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.ParametersAreNonnullByDefault;

import org.batfish.MEV.BgpAttribute;
import org.batfish.MEV.EvpnAttribute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

@ParametersAreNonnullByDefault
public final class SetLocalPreference extends Statement {
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";

  private LongExpr _localPreference;

  @JsonCreator
  private SetLocalPreference() {}

  public SetLocalPreference(LongExpr localPreference) {
    _localPreference = localPreference;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetLocalPreference(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SetLocalPreference)) {
      return false;
    }
    return _localPreference.equals(((SetLocalPreference) obj)._localPreference);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    long localPreference = _localPreference.evaluate(environment);
    if (environment.getOutputMessage().getAttribute() instanceof BgpAttribute)
    {
      BgpAttribute attribute = (BgpAttribute) environment.getOutputMessage().getAttribute();
      attribute.setLocalPreference(localPreference);
    } else if (environment.getOutputMessage().getAttribute() instanceof EvpnAttribute)
    {
      EvpnAttribute attribute = (EvpnAttribute) environment.getOutputMessage().getAttribute();
      attribute.setLocalPreference(localPreference);
    }
    return result;
  }

//  @Override
//  public Result execute(Environment environment) {
//    if (!(environment.getOutputRoute() instanceof HasWritableLocalPreference)) {
//      return new Result();
//    }
//    Result result = new Result();
//    HasWritableLocalPreference<?, ?> outputRoute =
//        (HasWritableLocalPreference<?, ?>) environment.getOutputRoute();
//    long localPreference = _localPreference.evaluate(environment);
//    outputRoute.setLocalPreference(localPreference);
//    if (environment.getWriteToIntermediateBgpAttributes()) {
//      environment.getIntermediateBgpAttributes().setLocalPreference(localPreference);
//    }
//    return result;
//  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public LongExpr getLocalPreference() {
    return _localPreference;
  }

  @Override
  public int hashCode() {
    return _localPreference.hashCode();
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public void setLocalPreference(LongExpr localPreference) {
    _localPreference = localPreference;
  }
}
