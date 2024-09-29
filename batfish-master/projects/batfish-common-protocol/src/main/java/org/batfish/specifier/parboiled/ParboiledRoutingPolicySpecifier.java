package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.NameRegexRoutingPolicySpecifier;
import org.batfish.specifier.NameRoutingPolicySpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** An {@link RoutingPolicySpecifier} that resolves based on the AST generated by {@link Parser}. */
@ParametersAreNonnullByDefault
public final class ParboiledRoutingPolicySpecifier implements RoutingPolicySpecifier {

  @ParametersAreNonnullByDefault
  private final class RoutingPolicyAstNodeToRoutingPolicys
      implements RoutingPolicyAstNodeVisitor<Set<RoutingPolicy>> {
    private final SpecifierContext _ctxt;
    private final String _node;

    RoutingPolicyAstNodeToRoutingPolicys(String node, SpecifierContext ctxt) {
      _node = node;
      _ctxt = ctxt;
    }

    @Override
    public @Nonnull Set<RoutingPolicy> visitDifferenceRoutingPolicyAstNode(
        DifferenceRoutingPolicyAstNode differenceRoutingPolicyAstNode) {
      return Sets.difference(
          differenceRoutingPolicyAstNode.getLeft().accept(this),
          differenceRoutingPolicyAstNode.getRight().accept(this));
    }

    @Override
    public @Nonnull Set<RoutingPolicy> visitIntersectionRoutingPolicyAstNode(
        IntersectionRoutingPolicyAstNode intersectionRoutingPolicyAstNode) {
      return Sets.intersection(
          intersectionRoutingPolicyAstNode.getLeft().accept(this),
          intersectionRoutingPolicyAstNode.getRight().accept(this));
    }

    @Override
    public @Nonnull Set<RoutingPolicy> visitNameRoutingPolicyAstNode(
        NameRoutingPolicyAstNode nameRoutingPolicyAstNode) {
      return new NameRoutingPolicySpecifier(nameRoutingPolicyAstNode.getName())
          .resolve(_node, _ctxt);
    }

    @Override
    public @Nonnull Set<RoutingPolicy> visitNameRegexRoutingPolicyAstNode(
        NameRegexRoutingPolicyAstNode nameRegexRoutingPolicyAstNode) {
      return new NameRegexRoutingPolicySpecifier(nameRegexRoutingPolicyAstNode.getPattern())
          .resolve(_node, _ctxt);
    }

    @Override
    public @Nonnull Set<RoutingPolicy> visitUnionRoutingPolicyAstNode(
        UnionRoutingPolicyAstNode unionRoutingPolicyAstNode) {
      return Sets.union(
          unionRoutingPolicyAstNode.getLeft().accept(this),
          unionRoutingPolicyAstNode.getRight().accept(this));
    }
  }

  private final RoutingPolicyAstNode _ast;

  ParboiledRoutingPolicySpecifier(RoutingPolicyAstNode ast) {
    _ast = ast;
  }

  /**
   * Returns an {@link RoutingPolicySpecifier} based on {@code input} which is parsed as {@link
   * Grammar#ROUTING_POLICY_SPECIFIER}.
   *
   * @throws IllegalArgumentException if the parsing fails or does not produce the expected AST
   */
  public static ParboiledRoutingPolicySpecifier parse(String input) {
    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.ROUTING_POLICY_SPECIFIER))
            .run(input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              input,
              Grammar.ROUTING_POLICY_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(
        ast instanceof RoutingPolicyAstNode,
        "ParboiledRoutingPolicySpecifier requires an RoutingPolicySpecifier input");

    return new ParboiledRoutingPolicySpecifier((RoutingPolicyAstNode) ast);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParboiledRoutingPolicySpecifier)) {
      return false;
    }
    return Objects.equals(_ast, ((ParboiledRoutingPolicySpecifier) o)._ast);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ast);
  }

  @Override
  public Set<RoutingPolicy> resolve(String node, SpecifierContext ctxt) {
    return _ast.accept(new RoutingPolicyAstNodeToRoutingPolicys(node, ctxt));
  }
}
