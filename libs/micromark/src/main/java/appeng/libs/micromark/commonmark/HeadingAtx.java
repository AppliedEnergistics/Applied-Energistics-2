package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.ChunkUtils;
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

public final class HeadingAtx {
    private HeadingAtx() {
    }

    public static final Construct headingAtx;
    static {
        headingAtx = new Construct();
        headingAtx.name = "headingAtx";
        headingAtx.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        headingAtx.resolve = HeadingAtx::resolveHeadingAtx;
    }

    private static List<Tokenizer.Event> resolveHeadingAtx(List<Tokenizer.Event> events, Tokenizer.TokenizeContext context) {
        var contentEnd = events.size() - 2;
        var contentStart = 3;

        // Prefix whitespace, part of the opening.
        if (events.get(contentStart).token().type.equals(Types.whitespace)) {
            contentStart += 2;
        }

        // Suffix whitespace, part of the closing.
        if (
                contentEnd - 2 > contentStart &&
                        events.get(contentEnd).token().type.equals(Types.whitespace)
        ) {
            contentEnd -= 2;
        }

        if (
                events.get(contentEnd).token().type.equals(Types.atxHeadingSequence) &&
                        (contentStart == contentEnd - 1 ||
                                (contentEnd - 4 > contentStart &&
                                        events.get(contentEnd - 2).token().type.equals(Types.whitespace)))
        ) {
            contentEnd -= contentStart + 1 == contentEnd ? 2 : 4;
        }

        if (contentEnd > contentStart) {
            var content = new Token();
                    content.type = Types.atxHeadingText;
                    content.start = events.get(contentStart).token().start;
                    content.end = events.get(contentEnd).token().end;

            var text = new Token();
                    text.type = Types.chunkText;
                    text.start = events.get(contentStart).token().start;
                    text.end = events.get(contentEnd).token().end;
                    text.contentType = ContentType.TEXT;

            ChunkUtils.splice(events, contentStart, contentEnd - contentStart + 1, List.of(
              Tokenizer.Event.enter(content, context),
              Tokenizer.Event.enter(text, context),
              Tokenizer.Event.exit(text, context),
              Tokenizer.Event.exit(content, context)
            ));
        }

        return events;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        private int size;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        
        private State start(int code) {
            Assert.check(code == Codes.numberSign, "expected `#`");
            effects.enter(Types.atxHeading);
            effects.enter(Types.atxHeadingSequence);
            return fenceOpenInside(code);
        }

        
        private State fenceOpenInside(int code) {
            if (
                    code == Codes.numberSign &&
                            size++ < Constants.atxHeadingOpeningFenceSizeMax
            ) {
                effects.consume(code);
                return this::fenceOpenInside;
            }

            if (code == Codes.eof || CharUtil.markdownLineEndingOrSpace(code)) {
                effects.exit(Types.atxHeadingSequence);
                return context.interrupt ? ok.step(code) : headingBreak(code);
            }

            return nok.step(code);
        }

        
        private State headingBreak(int code) {
            if (code == Codes.numberSign) {
                effects.enter(Types.atxHeadingSequence);
                return sequence(code);
            }

            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.atxHeading);
                return ok.step(code);
            }

            if (CharUtil.markdownSpace(code)) {
                return FactorySpace.create(effects, this::headingBreak, Types.whitespace).step(code);
            }

            effects.enter(Types.atxHeadingText);
            return data(code);
        }

        
        private State sequence(int code) {
            if (code == Codes.numberSign) {
                effects.consume(code);
                return this::sequence;
            }

            effects.exit(Types.atxHeadingSequence);
            return headingBreak(code);
        }

        
        private State data(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.numberSign ||
                            CharUtil.markdownLineEndingOrSpace(code)
            ) {
                effects.exit(Types.atxHeadingText);
                return headingBreak(code);
            }

            effects.consume(code);
            return this::data;
        }
    }
}
