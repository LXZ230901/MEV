package org.batfish.grammar.cisco_nxos.parsing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/**
 * Cisco NX-OS lexer base class providing additional functionality on top of {@link BatfishLexer}.
 */
@ParametersAreNonnullByDefault
public abstract class CiscoNxosBaseLexer extends BatfishLexer {

  private @Nullable Integer _bannerDelimiter;
  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  protected boolean _enableAclNum = false;
  protected boolean _enableDec = true;
  protected boolean _enableIpv6Address = true;
  protected boolean _enableCommunityListNum = false;

  protected boolean _enableRegex = false;

  protected boolean _inAccessList = false;

  public CiscoNxosBaseLexer(CharStream input) {
    super(input);
  }

  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _secondToLastTokenType = _lastTokenType;
      _lastTokenType = token.getType();
    }
  }

  protected final int lastTokenType() {
    return _lastTokenType;
  }

  protected final void setBannerDelimiter() {
    _bannerDelimiter = getText().codePointAt(0);
  }

  protected final boolean bannerDelimiterFollows() {
    return getInputStream().LA(1) == _bannerDelimiter;
  }

  protected final void unsetBannerDelimiter() {
    _bannerDelimiter = null;
  }

  protected final int secondToLastTokenType() {
    return _secondToLastTokenType;
  }

  @Override
  public @Nonnull String printStateVariables() {
    StringBuilder sb = new StringBuilder();
    sb.append("_enableAclNum: " + _enableAclNum + "\n");
    sb.append("_enableCommunityListNum: " + _enableCommunityListNum + "\n");
    sb.append("_enableDec: " + _enableDec + "\n");
    sb.append("_enableIpv6Address: " + _enableIpv6Address + "\n");
    sb.append("_enableRegex: " + _enableRegex + "\n");
    sb.append("_inAccessList: " + _inAccessList + "\n");
    return sb.toString();
  }
}
