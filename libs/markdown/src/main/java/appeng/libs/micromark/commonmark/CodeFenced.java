package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.ContentType;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

public final class CodeFenced {
    private CodeFenced() {
    }

    public static final Construct codeFenced;

    static {
        codeFenced = new Construct();
        codeFenced.name = "codeFenced";
        codeFenced.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        codeFenced.concrete = true;
    }

    private static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        Construct closingFenceConstruct = new Construct();

        {
            closingFenceConstruct.tokenize = (context1, effects1, ok1, nok1) -> new ClosingFenceStateMachine(context1, effects1, ok1, nok1).start;
            closingFenceConstruct.partial = true;
        }

        Construct nonLazyLine = new Construct();

        {
            nonLazyLine.tokenize = (context1, effects1, ok1, nok1) -> new NonLazyLineStateMachine(context1, effects1, ok1, nok1)::start;
            nonLazyLine.partial = true;
        }

        int initialPrefix;
        int sizeOpen;
        int marker;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;

            var tail = context.getLastEvent();
            initialPrefix = tail != null && tail.token().type.equals(Types.linePrefix)
                    ? tail.context().sliceSerialize(tail.token(), true).length()
                    : 0;
        }

        private State start(int code) {
            Assert.check(
                    code == Codes.graveAccent || code == Codes.tilde,
                    "expected `` ` `` or `~`"
            );
            effects.enter(Types.codeFenced);
            effects.enter(Types.codeFencedFence);
            effects.enter(Types.codeFencedFenceSequence);
            marker = code;
            return sequenceOpen(code);
        }


        private State sequenceOpen(int code) {
            if (code == marker) {
                effects.consume(code);
                sizeOpen++;
                return this::sequenceOpen;
            }

            effects.exit(Types.codeFencedFenceSequence);
            return sizeOpen < Constants.codeFencedSequenceSizeMin
                    ? nok.step(code)
                    : FactorySpace.create(effects, this::infoOpen, Types.whitespace).step(code);
        }


        private State infoOpen(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                return openAfter(code);
            }

            effects.enter(Types.codeFencedFenceInfo);
            var tokenFields = new Token();
            tokenFields.contentType = ContentType.STRING;
            effects.enter(Types.chunkString, tokenFields);
            return info(code);
        }


        private State info(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEndingOrSpace(code)) {
                effects.exit(Types.chunkString);
                effects.exit(Types.codeFencedFenceInfo);
                return FactorySpace.create(effects, this::infoAfter, Types.whitespace).step(code);
            }

            if (code == Codes.graveAccent && code == marker) return nok.step(code);
            effects.consume(code);
            return this::info;
        }


        private State infoAfter(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                return openAfter(code);
            }

            effects.enter(Types.codeFencedFenceMeta);
            var tokenFields = new Token();
            tokenFields.contentType = ContentType.STRING;
            effects.enter(Types.chunkString, tokenFields);
            return meta(code);
        }

        private State meta(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.chunkString);
                effects.exit(Types.codeFencedFenceMeta);
                return openAfter(code);
            }

            if (code == Codes.graveAccent && code == marker) return nok.step(code);
            effects.consume(code);
            return this::meta;
        }

        private State openAfter(int code) {
            effects.exit(Types.codeFencedFence);
            return context.isInterrupt() ? ok.step(code) : contentStart(code);
        }

        private State contentStart(int code) {
            if (code == Codes.eof) {
                return after(code);
            }

            if (CharUtil.markdownLineEnding(code)) {
                return effects.attempt.hook(
                        nonLazyLine,
                        effects.attempt.hook(
                                closingFenceConstruct,
                                this::after,
                                initialPrefix != 0
                                        ? FactorySpace.create(
                                        effects,
                                        this::contentStart,
                                        Types.linePrefix,
                                        initialPrefix + 1
                                ) : this::contentStart
                        ),
                        this::after
                ).step(code);
            }

            effects.enter(Types.codeFlowValue);
            return contentContinue(code);
        }


        private State contentContinue(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.codeFlowValue);
                return contentStart(code);
            }

            effects.consume(code);
            return this::contentContinue;
        }


        private State after(int code) {
            effects.exit(Types.codeFenced);
            return ok.step(code);
        }

        class NonLazyLineStateMachine {

            private final TokenizeContext context;
            private final Tokenizer.Effects effects;
            private final State ok;
            private final State nok;

            public NonLazyLineStateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
                this.context = context;
                this.effects = effects;
                this.ok = ok;
                this.nok = nok;
            }

            private State start(int code) {
                Assert.check(CharUtil.markdownLineEnding(code), "expected eol");
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return this::lineStart;
            }

            private State lineStart(int code) {
                return context.isOnLazyLine() ? nok.step(code) : ok.step(code);
            }
        }

        class ClosingFenceStateMachine {
            private final TokenizeContext context;
            private final Tokenizer.Effects effects;
            private final State ok;
            private final State nok;
            private int size;
            public final State start;

            public ClosingFenceStateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
                this.context = context;
                this.effects = effects;
                this.ok = ok;
                this.nok = nok;
                this.start = FactorySpace.create(
                        effects,
                        this::closingSequenceStart,
                        Types.linePrefix,
                        context.getParser().constructs.nullDisable.contains("codeIndented") ? Integer.MAX_VALUE : Constants.tabSize
                );
            }

            private State closingSequenceStart(int code) {
                effects.enter(Types.codeFencedFence);
                effects.enter(Types.codeFencedFenceSequence);
                return closingSequence(code);
            }

            private State closingSequence(int code) {
                if (code == marker) {
                    effects.consume(code);
                    size++;
                    return this::closingSequence;
                }

                if (size < sizeOpen) return nok.step(code);
                effects.exit(Types.codeFencedFenceSequence);
                return FactorySpace.create(effects, this::closingSequenceEnd, Types.whitespace).step(code);
            }

            private State closingSequenceEnd(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    effects.exit(Types.codeFencedFence);
                    return ok.step(code);
                }

                return nok.step(code);
            }
        }
    }
}
