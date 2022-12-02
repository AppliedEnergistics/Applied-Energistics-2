package appeng.libs.micromark.factory;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;

public final class FactorySpace {
    private FactorySpace() {
    }

    public static State create(Tokenizer.Effects effects, State ok, String type) {
        return create(effects, ok, type, null);
    }

    public static State create(Tokenizer.Effects effects, State ok, String type, Integer max) {
        var limit = max != null ? max - 1 :  Integer.MAX_VALUE;

        return new StateMachine(effects, ok, type, limit)::start;
    }

    private static class StateMachine {
        private final Tokenizer.Effects effects;
        private final State ok;
        private final String type;
        private final int limit;
        private int size;

        public StateMachine(Tokenizer.Effects effects, State ok, String type, int limit) {
            this.effects = effects;
            this.ok = ok;
            this.type = type;
            this.limit = limit;
        }

        private State prefix(int code) {
            if (CharUtil.markdownSpace(code) && size++ < limit) {
                effects.consume(code);
                return this::prefix;
            }

            effects.exit(type);
            return ok.step(code);
        }

        public State start(int code) {
            if (CharUtil.markdownSpace(code)) {
                effects.enter(type);
                return prefix(code);
            }

            return ok.step(code);
        }

        ;
    }

}
