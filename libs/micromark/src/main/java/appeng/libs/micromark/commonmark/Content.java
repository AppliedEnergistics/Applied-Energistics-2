package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.ContentType;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.List;

public final class Content {
    private Content() {
    }

    public static final Construct content;
    public static final Construct continuationConstruct;

    static {
        content = new Construct();
        content.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        content.resolve = Content::resolveContent;

        continuationConstruct = new Construct();
        continuationConstruct.tokenize = (context, effects, ok, nok) -> new ContinuationStateMachine(context, effects, ok, nok)::startLookahead;
        continuationConstruct.partial = true;
    }

    /**
     * Content is transparent: it’s parsed right now. That way, definitions are also
     * parsed right now: before text in paragraphs (specifically, media) are parsed.
     */
    private static List<Tokenizer.Event> resolveContent(List<Tokenizer.Event> events, Tokenizer.TokenizeContext context) {
        Subtokenize.subtokenize(events);
        return events;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        Token previous;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        private State start(int code) {
            Assert.check(
                    code != Codes.eof && !CharUtil.markdownLineEnding(code),
                    "expected no eof or eol"
            );

            effects.enter(Types.content);
            var tokenFields = new Token();
            tokenFields.contentType = ContentType.CONTENT;
            previous = effects.enter(Types.chunkContent, tokenFields);
            return data(code);
        }


        private State data(int code) {
            if (code == Codes.eof) {
                return contentEnd(code);
            }

            if (CharUtil.markdownLineEnding(code)) {
                return effects.check.hook(
                        continuationConstruct,
                        this::contentContinue,
                        this::contentEnd
                ).step(code);
            }

            // Data.
            effects.consume(code);
            return this::data;
        }


        private State contentEnd(int code) {
            effects.exit(Types.chunkContent);
            effects.exit(Types.content);
            return ok.step(code);
        }


        private State contentContinue(int code) {
            Assert.check(CharUtil.markdownLineEnding(code), "expected eol");
            effects.consume(code);
            effects.exit(Types.chunkContent);

            var tokenFields = new Token();
            tokenFields.contentType = ContentType.CONTENT;
            tokenFields.previous = previous;
            previous.next = effects.enter(Types.chunkContent, tokenFields);
            previous = previous.next;
            return this::data;
        }

    }


    private static class ContinuationStateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public ContinuationStateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        private State startLookahead(int code) {
            Assert.check(CharUtil.markdownLineEnding(code), "expected a line ending");
            effects.exit(Types.chunkContent);
            effects.enter(Types.lineEnding);
            effects.consume(code);
            effects.exit(Types.lineEnding);
            return FactorySpace.create(effects, this::prefixed, Types.linePrefix);
        }


        private State prefixed(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                return nok.step(code);
            }

            var tail = context.getLastEvent();

            if (
                    !context.parser.constructs.nullDisable.contains("codeIndented") &&
                            tail != null &&
                            tail.token().type == Types.linePrefix &&
                            tail.context().sliceSerialize(tail.token(), true).length() >= Constants.tabSize
            ) {
                return ok.step(code);
            }

            return effects.interrupt.hook(context.parser.constructs.flow, nok, ok).step(code);
        }
    }
}
