package org.batfish.MEV;


import java.util.Objects;

public class RouterVrfPair {
    private String _router;
    private String _vrf;

    // 无参构造函数
    public RouterVrfPair() {
    }

    // 带参数的构造函数
    public RouterVrfPair(String router, String vrf) {
        this._router = router;
        this._vrf = vrf;
    }

    // Getter 和 Setter 方法
    public String getRouter() {
        return _router;
    }

    public void setRouter(String router) {
        this._router = router;
    }

    public String getVrf() {
        return _vrf;
    }

    public void setVrf(String vrf) {
        this._vrf = vrf;
    }

    // 重写 equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouterVrfPair that = (RouterVrfPair) o;
        return Objects.equals(_router, that._router) &&
                Objects.equals(_vrf, that._vrf);
    }

    // 重写 hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(_router, _vrf);
    }

    // 重写 toString 方法
    @Override
    public String toString() {
        return "RouterVrfPair{" +
                "_router='" + _router + '\'' +
                ", _vrf='" + _vrf + '\'' +
                '}';
    }
}
