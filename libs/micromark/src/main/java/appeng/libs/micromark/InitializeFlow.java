package appeng.libs.micromark;

import appeng.libs.micromark.commonmark.BlankLine;
import appeng.libs.micromark.commonmark.Content;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;

public final class InitializeFlow {

    private InitializeFlow() {
    }

    public static final InitialConstruct flow;

    static {
        flow = new InitialConstruct();
        flow.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects).initial;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        public final State initial;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects) {
            this.context = context;
            this.effects = effects;

            this.initial = effects.attempt.hook(
                    // Try to parse a blank line.
                    BlankLine.blankLine,
                    this::atBlankEnding,
                    // Try to parse initial flow (essentially, only code).
                    effects.attempt.hook(
                            context.parser.constructs.flowInitial,
                            this::afterConstruct,
                            FactorySpace.create(
                                    effects,
                                    effects.attempt.hook(
                                            context.parser.constructs.flow,
                                            this::afterConstruct,
                                            effects.attempt.hook(Content.content, this::afterConstruct, null)
                                    ),
                                    Types.linePrefix
                            )
                    )
            );
        }

        private State atBlankEnding(int code) {
            if (code != Codes.eof && !CharUtil.markdownLineEnding(code)) {
                throw new IllegalStateException("expected eol or eof");
            }

            if (code == Codes.eof) {
                effects.consume(code);
                return null;
            }

            effects.enter(Types.lineEndingBlank);
            effects.consume(code);
            effects.exit(Types.lineEndingBlank);
            context.currentConstruct = null;
            return initial;
        }

        private State afterConstruct(int code) {
            if (code != Codes.eof && !CharUtil.markdownLineEnding(code)) {
                throw new IllegalStateException("expected eol or eof");
            }

            if (code == Codes.eof) {
                effects.consume(code);
                return null;
            }

            effects.enter(Types.lineEnding);
            effects.consume(code);
            effects.exit(Types.lineEnding);
            context.currentConstruct = null;
            return initial;
        }
    }
}
