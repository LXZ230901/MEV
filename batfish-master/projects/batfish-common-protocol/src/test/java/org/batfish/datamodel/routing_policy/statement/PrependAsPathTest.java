package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.AsPath.ofSingletonAsSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.junit.Test;

/** Test of {@link PrependAsPath}. */
@ParametersAreNonnullByDefault
public final class PrependAsPathTest {

  private static Environment newTestEnvironment(Bgpv4Route.Builder outputRoute) {
    Configuration c = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    return Environment.builder(c).setOutputRoute(outputRoute).build();
  }

  @Test
  public void testJavaSerialization() {
    Statement obj = new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L))));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    Statement obj = new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L))));
    assertThat(BatfishObjectMapper.clone(obj, Statement.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    Statement obj = new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L))));
    new EqualsTester()
        .addEqualityGroup(
            obj, new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L)))))
        .addEqualityGroup(
            new PrependAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(2L)))))
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    List<AsExpr> prepend = Lists.newArrayList(new ExplicitAs(1L), new ExplicitAs(2L));
    PrependAsPath operation = new PrependAsPath(new LiteralAsList(prepend));
    Bgpv4Route.Builder builder = Bgpv4Route.testBuilder();
    builder.setAsPath(ofSingletonAsSets(3L, 4L));
    Environment env = newTestEnvironment(builder);

    operation.execute(env);
    assertThat(builder.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 3L, 4L)));
  }

  @Test
  public void testEvaluateWriteIntermediateAttributes() {
    List<AsExpr> prepend = Lists.newArrayList(new ExplicitAs(1L), new ExplicitAs(2L));
    PrependAsPath operation = new PrependAsPath(new LiteralAsList(prepend));
    Bgpv4Route.Builder outputRoute = Bgpv4Route.testBuilder();
    outputRoute.setAsPath(ofSingletonAsSets(3L, 4L));

    Bgpv4Route.Builder intermediateAttributes = Bgpv4Route.testBuilder();
    intermediateAttributes.setAsPath(ofSingletonAsSets(5L, 6L));
    Environment env = newTestEnvironment(outputRoute);
    env.setIntermediateBgpAttributes(intermediateAttributes);
    env.setWriteToIntermediateBgpAttributes(true);

    operation.execute(env);
    assertThat(outputRoute.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 3L, 4L)));
    assertThat(intermediateAttributes.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 3L, 4L)));
  }

  @Test
  public void testEvaluateReadAndWriteIntermediateAttributes() {
    List<AsExpr> toPrepend = ImmutableList.of(new ExplicitAs(1L), new ExplicitAs(2L));
    PrependAsPath operation = new PrependAsPath(new LiteralAsList(toPrepend));
    Bgpv4Route.Builder outputRoute = Bgpv4Route.testBuilder();
    outputRoute.setAsPath(ofSingletonAsSets(3L, 4L));

    Bgpv4Route.Builder intermediateAttributes = Bgpv4Route.testBuilder();
    intermediateAttributes.setAsPath(ofSingletonAsSets(5L, 6L));
    Environment env = newTestEnvironment(outputRoute);
    env.setIntermediateBgpAttributes(intermediateAttributes);
    env.setReadFromIntermediateBgpAttributes(true);
    env.setWriteToIntermediateBgpAttributes(true);

    operation.execute(env);
    assertThat(outputRoute.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 5L, 6L)));
    assertThat(intermediateAttributes.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 5L, 6L)));
  }
}
