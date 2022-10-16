package appeng.libs.micromark;

import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;

public final class InitializeContent {

    private InitializeContent() {
    }

    public static final InitialConstruct content;

    static {
        content = new InitialConstruct();
        content.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects).contentStart;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private Token previous;

        public final State contentStart;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects) {
            this.context = context;
            this.effects = effects;

            contentStart = effects.attempt.hook(
                    context.parser.constructs.contentInitial,
                    this::afterContentStartConstruct,
                    this::paragraphInitial
            );
        }

        private State afterContentStartConstruct(int code) {
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
            return FactorySpace.create(effects, contentStart, Types.linePrefix);
        }

        private State paragraphInitial(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                throw new IllegalStateException("expected anything other than a line ending or EOF");
            }
            effects.enter(Types.paragraph);
            return lineStart(code);
        }

        private State lineStart(int code) {
            var tokenFields = new Token();
            tokenFields.contentType = ContentType.TEXT;
            tokenFields.previous = previous;

            var token = effects.enter(Types.chunkText, tokenFields);

            if (previous != null) {
                previous.next = token;
            }

            previous = token;
            return data(code);
        }

        private State data(int code) {
            if (code == Codes.eof) {
                effects.exit(Types.chunkText);
                effects.exit(Types.paragraph);
                effects.consume(code);
                return null;
            }

            if (CharUtil.markdownLineEnding(code)) {
                effects.consume(code);
                effects.exit(Types.chunkText);
                return this::lineStart;
            }

            // Data.
            effects.consume(code);
            return this::data;
        }
    }

}
