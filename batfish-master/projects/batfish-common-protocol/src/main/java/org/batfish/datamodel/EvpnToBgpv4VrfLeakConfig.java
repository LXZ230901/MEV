package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/** VRF leaking config that leaks routes from a BGPv4 RIB into an EVPN RIB */
public final class EvpnToBgpv4VrfLeakConfig implements Serializable {

  /** Name of the source VRF from which to copy routes. The source VRF must have an EVPN RIB. */
  @JsonProperty(PROP_IMPORT_FROM_VRF)
  public @Nonnull String getImportFromVrf() {
    return _importFromVrf;
  }

  /**
   * Name of the import policy to apply to imported routes when leaking. If {@code null} no policy
   * is applied, all routes are allowed.
   */
  @JsonProperty(PROP_IMPORT_POLICY)
  public @Nullable String getImportPolicy() {
    return _importPolicy;
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvpnToBgpv4VrfLeakConfig)) {
      return false;
    }
    EvpnToBgpv4VrfLeakConfig that = (EvpnToBgpv4VrfLeakConfig) o;
    return _importFromVrf.equals(that._importFromVrf)
        && Objects.equals(_importPolicy, that._importPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_importFromVrf, _importPolicy);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(EvpnToBgpv4VrfLeakConfig.class)
        .omitNullValues()
        .add(PROP_IMPORT_FROM_VRF, _importFromVrf)
        .add(PROP_IMPORT_POLICY, _importPolicy)
        .toString();
  }

  private EvpnToBgpv4VrfLeakConfig(String importFromVrf, @Nullable String importPolicy, Set<ExtendedCommunity> attachRouteTargets) {
    _importFromVrf = importFromVrf;
    _importPolicy = importPolicy;
    _attachRouteTargets = attachRouteTargets;
  }

  public Set<ExtendedCommunity> getAttachRouteTargets()
  {
    return _attachRouteTargets;
  }

  public void setAttachRouteTargets(Set<ExtendedCommunity> attachRouteTargets)
  {
    _attachRouteTargets = new HashSet<>();
    _attachRouteTargets.addAll(attachRouteTargets);
  }

  @JsonCreator
  private static EvpnToBgpv4VrfLeakConfig create(
      @JsonProperty(PROP_IMPORT_FROM_VRF) @Nullable String importFromVrf,
      @JsonProperty(PROP_IMPORT_POLICY) @Nullable String importPolicy) {
    return builder().setImportFromVrf(importFromVrf).setImportPolicy(importPolicy).build();
  }

  private static final String PROP_IMPORT_FROM_VRF = "importFromVrf";
  private static final String PROP_IMPORT_POLICY = "importPolicy";

  private final @Nonnull String _importFromVrf;
  private final @Nullable String _importPolicy;

  public Set<ExtendedCommunity> _attachRouteTargets;

  public static final class Builder {
    public @Nonnull EvpnToBgpv4VrfLeakConfig build() {
      checkArgument(_importFromVrf != null, "Missing %s", PROP_IMPORT_FROM_VRF);
      return new EvpnToBgpv4VrfLeakConfig(_importFromVrf, _importPolicy, _attachRouteTargets);
    }

    public Builder setImportPolicy(@Nullable String importPolicy) {
      _importPolicy = importPolicy;
      return this;
    }

    public @Nonnull Builder setImportFromVrf(@Nullable String importFromVrf) {
      _importFromVrf = importFromVrf;
      return this;
    }

    public @Nonnull Builder setAttachRouteTargets(@Nullable Set<ExtendedCommunity> attachRouteTargets) {
      _attachRouteTargets = attachRouteTargets;
      return this;
    }

    private @Nullable String _importPolicy;
    private @Nullable String _importFromVrf;
    public Set<ExtendedCommunity> _attachRouteTargets;

    private Builder() {}
  }
}
