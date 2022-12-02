package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

public final class BlockQuote {
    private BlockQuote() {
    }

    public static final Construct blockQuote;

    static {
        blockQuote = new Construct();
        blockQuote.name = "blockQuote";
        blockQuote.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        blockQuote.continuation = new Construct();
        blockQuote.continuation.tokenize = (context, effects, ok, nok) -> {
            return FactorySpace.create(
                    effects,
                    effects.attempt.hook(blockQuote, ok, nok),
                    Types.linePrefix,
                    context.getParser().constructs.nullDisable.contains("codeIndented")
                            ? Integer.MAX_VALUE
                            : Constants.tabSize
            );
        };
        blockQuote.exit = BlockQuote::exit;
    }

    static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        State start(int code) {
            if (code == Codes.greaterThan) {
                var state = context.getContainerState();

                Assert.check(state != null, "expected `containerState` to be defined in container");

                if (!state.containsKey("open")) {
                    var token = new Token();
                    token._container = true;
                    effects.enter(Types.blockQuote, token);
                    state.put("open", true);
                }

                effects.enter(Types.blockQuotePrefix);
                effects.enter(Types.blockQuoteMarker);
                effects.consume(code);
                effects.exit(Types.blockQuoteMarker);
                return this::after;
            }

            return nok.step(code);
        }

        State after(int code) {
            if (CharUtil.markdownSpace(code)) {
                effects.enter(Types.blockQuotePrefixWhitespace);
                effects.consume(code);
                effects.exit(Types.blockQuotePrefixWhitespace);
                effects.exit(Types.blockQuotePrefix);
                return ok;
            }

            effects.exit(Types.blockQuotePrefix);
            return ok.step(code);
        }
    }

    private static void exit(TokenizeContext context, Tokenizer.Effects effects) {
        effects.exit(Types.blockQuote);
    }

}
