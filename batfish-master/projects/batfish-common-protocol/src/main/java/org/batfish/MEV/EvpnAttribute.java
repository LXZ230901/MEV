package org.batfish.MEV;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EvpnAttribute extends Attribute{

    public Set<StandardCommunity> _community;
    public Set<ExtendedCommunity> _RTs;
    public AsPath _asPath;
    public Long _localPreference;
    public Integer _MED;
    public Integer _vni;
    public MessageType _type;
    public boolean _reflector;
    public List<Long> _routerIdList;
    public EvpnAttribute (int AD, Set<StandardCommunity> community, AsPath asPath, Long localPreference, Integer MED, Set<ExtendedCommunity> RTs, Integer vni)
    {
        super(AD);
        this._community = new HashSet<>(community);
        this._asPath = AsPath.of(asPath.getAsSets());
        this._localPreference = localPreference;
        this._MED = MED;
        this._RTs = new HashSet<>(RTs);
        this._vni = vni;
        this._reflector = false;
        this._routerIdList = new ArrayList<>();
    }

    public EvpnAttribute (int AD, Set<StandardCommunity> community, AsPath asPath, Long localPreference, Integer MED, Set<ExtendedCommunity> RTs, Integer vni, boolean reflector, List<Long> routerIdList)
    {
        super(AD);
        this._community = new HashSet<>(community);
        this._asPath = AsPath.of(asPath.getAsSets());
        this._localPreference = localPreference;
        this._MED = MED;
        this._RTs = new HashSet<>(RTs);
        this._vni = vni;
        this._reflector = reflector;
        this._routerIdList = new ArrayList<>();
        this._routerIdList.addAll(routerIdList);
    }

    public Set<StandardCommunity> getCommunity() {
        return _community;
    }

    public Set<ExtendedCommunity> getRTs() {
        return _RTs;
    }

    public AsPath getAsPath() {
        return _asPath;
    }

    public Long getLocalPreference() {
        return _localPreference;
    }

    public Integer getMED() {
        return _MED;
    }

    public Integer getVni() {
        return _vni;
    }

    public void setCommunity(Set<StandardCommunity> community)
    {
        this._community.clear();
        this._community.addAll(community);
    }

    public void addCommunity(StandardCommunity community)
    {
        this._community.add(community);
    }

    public void setAsPath(AsPath asPath)
    {
        this._asPath = AsPath.of(asPath.getAsSets());
    }

    public void addAsPath(AsSet as)
    {
        this._asPath = AsPath.of(
                ImmutableList.<AsSet>builder()
                        .addAll(this._asPath.getAsSets())
                        .add(as)
                        .build());
    }

    public void addRouterIdList(long routeId)
    {
        this._routerIdList.add(routeId);
    }

    public void setLocalPreference(Long localPreference)
    {
        this._localPreference = localPreference;
    }

    public void setMED(Integer MED)
    {
        this._MED = MED;
    }

    public void setRTs(Set<ExtendedCommunity> RTs)
    {
        this._RTs.addAll(RTs);
    }

    public void addRT(ExtendedCommunity RT)
    {
        this._RTs.add(RT);
    }

    public void setVni(Integer vni)
    {
        this._vni = vni;
    }

    public void setReflector(boolean reflector)
    {
        this._reflector = reflector;
    }

    public void setRouterIdList(Long routerId)
    {
        this._routerIdList.add(routerId);
    }

    public boolean getReflector()
    {
        return this._reflector;
    }

    public List<Long> getRouterIdList()
    {
        return this._routerIdList;
    }


    @Override
    public int comparePriority(Attribute attribute) {
        if (!attribute.getClass().equals(getClass())) {
            return Integer.compare(attribute.getAD(), getAD());
        }
        EvpnAttribute attr = (EvpnAttribute) attribute;

        // Compare local preferences
        if (!this._localPreference.equals(attr.getLocalPreference())) {
            return Long.compare(this._localPreference, attr.getLocalPreference());
        }

        // Compare AS path lengths
        if (this._asPath.size() != attr.getAsPath().size()) {
            return Integer.compare(attr.getAsPath().size(), this._asPath.size());
        }

//        if (!this._type.equals(attr._type))
//        {
//            if (this._type == MessageType.eBGP && attr.getType() == MessageType.iBGP)
//            {
//                return 1;
//            } else {
//                return -1;
//            }
//        }

        if (this._routerIdList.size() != attr.getRouterIdList().size())
        {
            return Integer.compare(attr.getRouterIdList().size(), this._routerIdList.size());
        }

        // Compare MED values
        return Integer.compare(attr.getMED(), this._MED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EvpnAttribute that = (EvpnAttribute) o;
        return Objects.equals(_community, that.getCommunity()) &&
                Objects.equals(_RTs, that.getRTs()) &&
                Objects.equals(_asPath, that.getAsPath()) &&
                Objects.equals(_localPreference, that.getLocalPreference()) &&
                Objects.equals(_MED, that.getMED()) &&
                Objects.equals(_vni, that.getVni()) &&
                Objects.equals(getAD(), that.getAD());
    }

    @Override
    public int hashCode() {
        int result = getAD();
        result = result * 31 + _community.hashCode();
        result = result * 31 + _RTs.hashCode();
        result = result * 31 + _asPath.hashCode();
        result = result * 31 + _localPreference.hashCode();
        result = result * 31 + _MED.hashCode();
        result = result * 31 + _vni.hashCode();
        return result;
    }

    public static class Builder extends Attribute.Builder<Builder>{
        public Integer _AD;
        public Set<StandardCommunity> _community = new HashSet<>();
        public Set<ExtendedCommunity> _RTs = new HashSet<>();
        public AsPath _asPath;
        public Long _localPreference;
        public Integer _MED;
        public Integer _vni;

        public boolean _reflector;
        public List<Long> _routerIdList;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder(Integer AD)
        {
         this._AD = AD;
        }

        public Set<StandardCommunity> getCommunity() {
            return _community;
        }

        public Set<ExtendedCommunity> getRTs() {
            return _RTs;
        }

        public AsPath getAsPath() {
            return _asPath;
        }

        public Long getLocalPreference() {
            return _localPreference;
        }

        public Integer getMED() {
            return _MED;
        }

        public Integer getVni() {
            return _vni;
        }

        public Builder setCommunity(Set<StandardCommunity> community)
        {
            this._community.addAll(community);
            return this;
        }

        public Builder addCommunity(StandardCommunity community) {
            this._community.add(community);
            return this;
        }

        public Builder setAsPath(AsPath asPath) {
            this._asPath = AsPath.of(asPath.getAsSets());
            return this;
        }

        public Builder addAsPath(AsSet as) {
            if (this._asPath == null) {
                this._asPath = AsPath.of(new ArrayList<>());
            }
            this._asPath.getAsSets().add(as);
            return this;
        }

        public Builder setLocalPreference(Long localPreference) {
            this._localPreference = localPreference;
            return this;
        }

        public Builder setMED(Integer MED) {
            this._MED = MED;
            return this;
        }

        public Builder setRTs(Set<ExtendedCommunity> RTs) {
            this._RTs = RTs;
            return this;
        }

        public Builder addRT(ExtendedCommunity RT) {
            this._RTs.add(RT);
            return this;
        }

        public Builder setVni(Integer vni) {
            this._vni = vni;
            return this;
        }

        public Builder setReflector(boolean reflector)
        {
            this._reflector = reflector;
            return this;
        }

        public Builder setRouterIdList(List<Long> routerIdList)
        {
            this._routerIdList = new ArrayList<>();
            this._routerIdList.addAll(routerIdList);
            return this;
        }

        public EvpnAttribute build() {
            return new EvpnAttribute(_AD, _community, _asPath, _localPreference, _MED, _RTs, _vni, _reflector, _routerIdList);
        }
    }

    @Override
    public Builder toBuilder()
    {
        return new Builder(this._AD)
                .setCommunity(this._community)
                .setMED(this._MED)
                .setRTs(this._RTs)
                .setVni(this._vni)
                .setAsPath(this._asPath)
                .setLocalPreference(this._localPreference)
                .setReflector(this._reflector)
                .setRouterIdList(this._routerIdList);
    }

    public void setType(MessageType type)
    {
        this._type = type;
    }

    public MessageType getType()
    {
        return this._type;
    }
}
