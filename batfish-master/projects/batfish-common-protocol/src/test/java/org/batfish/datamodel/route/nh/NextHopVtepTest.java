package org.batfish.datamodel.route.nh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link NextHopVtep}. */
public final class NextHopVtepTest {

  @Test
  public void testJavaSerialiation() {
    NextHopVtep obj = NextHopVtep.of(100, Ip.parse("1.1.1.1"));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    NextHopVtep obj = NextHopVtep.of(100, Ip.parse("1.1.1.1"));
    assertThat(BatfishObjectMapper.clone(obj, NextHop.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    NextHopVtep obj = NextHopVtep.of(100, Ip.parse("1.1.1.1"));
    new EqualsTester()
        .addEqualityGroup(obj, NextHopVtep.of(100, Ip.parse("1.1.1.1")))
        .addEqualityGroup(NextHopVtep.of(200, Ip.parse("1.1.1.1")))
        .addEqualityGroup(NextHopVtep.of(100, Ip.parse("2.2.2.2")))
        .testEquals();
  }
}
