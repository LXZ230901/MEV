package org.batfish.MEV;


import java.util.Map;

public class DijkstraIsisResult {
    private final Map<ISIS, ISIS> nexthop;
    private final Map<ISIS, Long> dist;

    public DijkstraIsisResult(Map<ISIS, ISIS> nexthop, Map<ISIS, Long> dist) {
        this.nexthop = nexthop;
        this.dist = dist;
    }

    public Map<ISIS, ISIS> getNexthop() {
        return nexthop;
    }

    public Map<ISIS, Long> getDist() {
        return dist;
    }
}

