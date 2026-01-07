package org.batfish.MEV;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.bgp.community.StandardCommunity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BgpAttribute extends Attribute{
    public Set<StandardCommunity> _community;
    public AsPath _asPath;
    public Long _localPreference;
    public Integer _MED;
    public MessageType _type;
    public boolean _reflector;
    public List<Long> _routerIdList;
    public BgpAttribute (int AD, Set<StandardCommunity> community, AsPath asPath, Long localPreference, Integer MED)
    {
        super(AD);
        this._community = new HashSet<>(community);
        this._asPath = AsPath.of(asPath.getAsSets());
        this._localPreference = localPreference;
        this._MED = MED;
        this._reflector = false;
        this._routerIdList = new ArrayList<>();
    }

    public BgpAttribute (int AD, Set<StandardCommunity> community, AsPath asPath, Long localPreference, Integer MED, boolean reflector, List<Long> routerIdList)
    {
        super(AD);
        this._community = new HashSet<>(community);
        this._asPath = AsPath.of(asPath.getAsSets());
        this._localPreference = localPreference;
        this._MED = MED;
        this._reflector = reflector;
        this._routerIdList = new ArrayList<>();
        this._routerIdList.addAll(routerIdList);
    }

    public Set<StandardCommunity> getCommunity() {
        return _community;
    }

    public AsPath getAsPath() {
        return _asPath;
    }

    public long getLocalPreference() {
        return _localPreference;
    }

    public Integer getMED() {
        return _MED;
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

    public void setLocalPreference(Long localPreference)
    {
        this._localPreference = localPreference;
    }

    public void setMED(Integer MED)
    {
        this._MED = MED;
    }

    public void setReflector(boolean reflector)
    {
        this._reflector = reflector;
    }

    public boolean getReflector()
    {
        return this._reflector;
    }

    public void addRouterIdList(Long routerId)
    {
        this._routerIdList.add(routerId);
    }

    public List<Long> getRouterList()
    {
        return this._routerIdList;
    }

    @Override
    public int comparePriority(Attribute attribute) {
        if (!attribute.getClass().equals(getClass())) {
            return Integer.compare(attribute.getAD(), getAD());
        }
        BgpAttribute attr = (BgpAttribute) attribute;

        // Compare local preferences
        if (this._localPreference != attr.getLocalPreference()) {
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

        if (this._routerIdList.size() != attr.getRouterList().size())
        {
            return Integer.compare(attr.getRouterList().size(), this._routerIdList.size());
        }

        // Compare MED values
        return Integer.compare(attr.getMED(), this._MED);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        BgpAttribute bgpAttribute = (BgpAttribute) o;
        return Objects.equals(_community, bgpAttribute.getCommunity()) &&
                Objects.equals(_asPath, bgpAttribute.getAsPath()) &&
                Objects.equals(_MED, bgpAttribute.getMED()) &&
                Objects.equals(_AD, bgpAttribute.getAD()) &&
                Objects.equals(_localPreference, bgpAttribute.getLocalPreference());
    }

    @Override
    public int hashCode()
    {
        int result = _AD;
        result = result * 31 + _asPath.hashCode();
        result = result * 31 + _community.hashCode();
        result = result * 31 + _localPreference.hashCode();
        result = result * 31 + _MED.hashCode();
        return result;
    }

    public static class Builder extends Attribute.Builder<Builder>{
        public Integer _AD;
        public Set<StandardCommunity> _community = new HashSet<>();
        public AsPath _asPath;
        public Long _localPreference;
        public Integer _MED;
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


        public AsPath getAsPath() {
            return _asPath;
        }

        public Long getLocalPreference() {
            return _localPreference;
        }

        public Integer getMED() {
            return _MED;
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

        @Override
        public BgpAttribute build() {
            return new BgpAttribute(_AD, _community, _asPath, _localPreference, _MED, _reflector, _routerIdList);
        }
    }

    @Override
    public Builder toBuilder()
    {
        return new Builder(this._AD)
                .setCommunity(this._community)
                .setMED(this._MED)
                .setAsPath(this._asPath)
                .setLocalPreference(this._localPreference)
                .setReflector(_reflector)
                .setRouterIdList(_routerIdList);

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