package appeng.libs.micromark.factory;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;

public final class FactoryWhitespace {
    private FactoryWhitespace() {
    }

    public static State create(Tokenizer.Effects effects, State ok) {
        return new StateMachine(effects, ok)::start;
    }

    private static class StateMachine {
        private final Tokenizer.Effects effects;
        private final State ok;
        private boolean seen;

        public StateMachine(Tokenizer.Effects effects, State ok) {
            this.effects = effects;
            this.ok = ok;
        }

        public State start(int code) {
            if (CharUtil.markdownLineEnding(code)) {
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                seen = true;
                return this::start;
            }

            if (CharUtil.markdownSpace(code)) {
                return FactorySpace.create(
                        effects,
                        this::start,
                        seen ? Types.linePrefix : Types.lineSuffix
                ).step(code);
            }

            return ok.step(code);
        }
    }

}
