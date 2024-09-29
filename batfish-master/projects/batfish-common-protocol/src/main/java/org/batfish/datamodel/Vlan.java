package org.batfish.datamodel;

import javax.annotation.Nullable;
import java.io.Serializable;

public class Vlan implements Serializable {



    private static final long serialVersionUID = 1L;

    public Vlan(int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public @Nullable Integer getVni() {
        return _vni;
    }

    public void setVni(@Nullable Integer vni) {
        _vni = vni;
    }

    //////////////////////////////////////////
    ///// Private implementation details /////
    //////////////////////////////////////////

    private final int _id;
    private @Nullable Integer _vni;
}
