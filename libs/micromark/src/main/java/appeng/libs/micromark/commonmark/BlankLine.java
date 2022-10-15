package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;

public final class BlankLine {
    private BlankLine() {
    }

    public static final Construct blankLine;

    static {
        blankLine = new Construct();
        blankLine.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok).initial;
        blankLine.partial = true;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        public final State initial;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
            this.initial = FactorySpace.factorySpace(effects, this::afterWhitespace, Types.linePrefix);
        }

        /**
         * After zero or more spaces or tabs, before a line ending or EOF.
         * <p>
         * <pre>
         * > | ␠␠␊
         *       ^
         * > | ␊
         *     ^
         * </pre>
         */
        private State afterWhitespace(int code) {
            return code == Codes.eof || CharUtil.markdownLineEnding(code) ? ok.step(code) : nok.step(code);
        }
    }
}
