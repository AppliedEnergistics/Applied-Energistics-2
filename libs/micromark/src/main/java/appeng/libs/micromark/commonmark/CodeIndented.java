package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

public final class CodeIndented {
    private CodeIndented() {
    }

    public static final Construct codeIndented;
    public static final Construct indentedContent;

    static {
        codeIndented = new Construct();
        codeIndented.name = "codeIndented";
        codeIndented.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;

        indentedContent = new Construct();
        indentedContent.tokenize = (context, effects, ok, nok) -> new IndentedContentStateMachine(context,  effects, ok, nok)::start;
        indentedContent.partial = true;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        
        private State start(int code) {
            effects.enter(Types.codeIndented);
            return FactorySpace.create(
                    effects,
                    this::afterStartPrefix,
                    Types.linePrefix,
                    Constants.tabSize + 1
            ).step(code);
        }

        
        private State afterStartPrefix(int code) {
    var tail = context.getLastEvent();
            return tail != null &&
                    tail.token().type.equals(Types.linePrefix) &&
                    tail.context().sliceSerialize(tail.token(), true).length() >= Constants.tabSize
                    ? afterPrefix(code)
                    : nok.step(code);
        }

        
        private State afterPrefix(int code) {
            if (code == Codes.eof) {
                return after(code);
            }

            if (CharUtil.markdownLineEnding(code)) {
                return effects.attempt.hook(indentedContent, this::afterPrefix, this::after).step(code);
            }

            effects.enter(Types.codeFlowValue);
            return content(code);
        }

        
        private State content(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.codeFlowValue);
                return afterPrefix(code);
            }

            effects.consume(code);
            return this::content;
        }

        
        private State after(int code) {
            effects.exit(Types.codeIndented);
            return ok.step(code);
        }

    }


    private static class IndentedContentStateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public IndentedContentStateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        
        private State start(int code) {
            // If this is a lazy line, it can’t be code.
            if (context.isOnLazyLine()) {
                return nok.step(code);
            }

            if (CharUtil.markdownLineEnding(code)) {
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return this::start;
            }

            return FactorySpace.create(
                    effects,
                    this::afterPrefix,
                    Types.linePrefix,
                    Constants.tabSize + 1
            ).step(code);
        }

        
        private State afterPrefix(int code) {
            var tail = context.getLastEvent();
            return tail != null &&
                    tail.token().type.equals(Types.linePrefix) &&
                    tail.context().sliceSerialize(tail.token(), true).length() >= Constants.tabSize
                    ? ok.step(code)
                    : CharUtil.markdownLineEnding(code)
                    ? start(code)
                    : nok.step(code);
        }
    }
}
