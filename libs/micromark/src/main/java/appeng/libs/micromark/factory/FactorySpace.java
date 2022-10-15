package appeng.libs.micromark.factory;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;

public final class FactorySpace {
    private FactorySpace() {
    }

    public static State factorySpace(Tokenizer.Effects effects, State ok, String type) {
        return factorySpace(effects, ok, type, Integer.MAX_VALUE);
    }

    public static State factorySpace(Tokenizer.Effects effects, State ok, String type, int max) {
        return new StateMachine(effects, ok, type, max - 1)::start;
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
