package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.HtmlTagName;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.List;
import java.util.Objects;

public final class HtmlFlow {
    private HtmlFlow() {
    }

    public static final Construct htmlFlow;
    public static final Construct nextBlankConstruct;
    static {
        htmlFlow = new Construct();
        htmlFlow.name = "htmlFlow";
        htmlFlow.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        htmlFlow.resolve = HtmlFlow::resolveToHtmlFlow;
        htmlFlow.concrete = true;

        nextBlankConstruct = new Construct();
        nextBlankConstruct.tokenize = (context, effects, ok, nok) -> new NextBlankStateMachine(context, effects, ok, nok)::start;
        nextBlankConstruct.partial = true;
    }

    private static List<Tokenizer.Event> resolveToHtmlFlow(List<Tokenizer.Event> events, TokenizeContext context) {
        var index = events.size();

        while (index-- > 0) {
            if (
                    events.get(index).isEnter() &&
                            events.get(index).token().type == Types.htmlFlow
            ) {
                break;
            }
        }

        if (index > 1 && events.get(index - 2).token().type == Types.linePrefix) {
            // Add the prefix start to the HTML token.
            events.get(index).token().start = events.get(index - 2).token().start;
            // Add the prefix start to the HTML line token.
            events.get(index + 1).token().start = events.get(index - 2).token().start;
            // Remove the line prefix.
            events.subList(index - 2, index).clear();
        }

        return events;
    }

    private static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        private int kind;
        boolean startTag;
        String buffer;
        private int index;
        Integer marker;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        
        private State start(int code) {
            Assert.check(code == Codes.lessThan, "expected `<`");
            effects.enter(Types.htmlFlow);
            effects.enter(Types.htmlFlowData);
            effects.consume(code);
            return this::open;
        }

        
        private State open(int code) {
            if (code == Codes.exclamationMark) {
                effects.consume(code);
                return this::declarationStart;
            }

            if (code == Codes.slash) {
                effects.consume(code);
                return this::tagCloseStart;
            }

            if (code == Codes.questionMark) {
                effects.consume(code);
                kind = Constants.htmlInstruction;
                // While we’re in an instruction instead of a declaration, we’re on a `?`
                // right now, so we do need to search for `>`, similar to declarations.
                return context.isInterrupt() ? ok : this::continuationDeclarationInside;
            }

            if (CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                buffer = String.valueOf((char) code);
                startTag = true;
                return this::tagName;
            }

            return nok.step(code);
        }

        
        private State declarationStart(int code) {
            if (code == Codes.dash) {
                effects.consume(code);
                kind = Constants.htmlComment;
                return this::commentOpenInside;
            }

            if (code == Codes.leftSquareBracket) {
                effects.consume(code);
                kind = Constants.htmlCdata;
                buffer = Constants.cdataOpeningString;
                index = 0;
                return this::cdataOpenInside;
            }

            if (CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                kind = Constants.htmlDeclaration;
                return context.isInterrupt() ? ok : this::continuationDeclarationInside;
            }

            return nok.step(code);
        }

        
        private State commentOpenInside(int code) {
            if (code == Codes.dash) {
                effects.consume(code);
                return context.isInterrupt() ? ok : this::continuationDeclarationInside;
            }

            return nok.step(code);
        }

        
        private State cdataOpenInside(int code) {
            if (code == buffer.charAt(index++)) {
                effects.consume(code);
                return index == buffer.length()
                        ? context.isInterrupt()
                        ? ok
                        : this::continuation
                        : this::cdataOpenInside;
            }

            return nok.step(code);
        }

        
        private State tagCloseStart(int code) {
            if (CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                buffer = String.valueOf((char) code);
                return this::tagName;
            }

            return nok.step(code);
        }

        
        private State tagName(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.slash ||
                            code == Codes.greaterThan ||
                            CharUtil.markdownLineEndingOrSpace(code)
            ) {
                if (
                        code != Codes.slash &&
                                startTag &&
                                HtmlTagName.htmlRawNames.contains(buffer.toLowerCase())
                ) {
                    kind = Constants.htmlRaw;
                    return context.isInterrupt() ? ok.step(code) : continuation(code);
                }

                if (HtmlTagName.htmlBlockNames.contains(buffer.toLowerCase())) {
                    kind = Constants.htmlBasic;

                    if (code == Codes.slash) {
                        effects.consume(code);
                        return this::basicSelfClosing;
                    }

                    return context.isInterrupt() ? ok.step(code) : continuation(code);
                }

                kind = Constants.htmlComplete;
                // Do not support complete HTML when interrupting
                return context.isInterrupt() && !context.isOnLazyLine()
                        ? nok.step(code)
                        : startTag
                        ? completeAttributeNameBefore(code)
                        : completeClosingTagAfter(code);
            }

            if (code == Codes.dash || CharUtil.asciiAlphanumeric(code)) {
                effects.consume(code);
                buffer += String.valueOf((char) code);
                return this::tagName;
            }

            return nok.step(code);
        }

        
        private State basicSelfClosing(int code) {
            if (code == Codes.greaterThan) {
                effects.consume(code);
                return context.isInterrupt() ? ok : this::continuation;
            }

            return nok.step(code);
        }

        
        private State completeClosingTagAfter(int code) {
            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::completeClosingTagAfter;
            }

            return completeEnd(code);
        }

        
        private State completeAttributeNameBefore(int code) {
            if (code == Codes.slash) {
                effects.consume(code);
                return this::completeEnd;
            }

            if (code == Codes.colon || code == Codes.underscore || CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                return this::completeAttributeName;
            }

            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::completeAttributeNameBefore;
            }

            return completeEnd(code);
        }

        
        private State completeAttributeName(int code) {
            if (
                    code == Codes.dash ||
                            code == Codes.dot ||
                            code == Codes.colon ||
                            code == Codes.underscore ||
                            CharUtil.asciiAlphanumeric(code)
            ) {
                effects.consume(code);
                return this::completeAttributeName;
            }

            return completeAttributeNameAfter(code);
        }

        
        private State completeAttributeNameAfter(int code) {
            if (code == Codes.equalsTo) {
                effects.consume(code);
                return this::completeAttributeValueBefore;
            }

            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::completeAttributeNameAfter;
            }

            return completeAttributeNameBefore(code);
        }

        
        private State completeAttributeValueBefore(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.lessThan ||
                            code == Codes.equalsTo ||
                            code == Codes.greaterThan ||
                            code == Codes.graveAccent
            ) {
                return nok.step(code);
            }

            if (code == Codes.quotationMark || code == Codes.apostrophe) {
                effects.consume(code);
                marker = code;
                return this::completeAttributeValueQuoted;
            }

            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::completeAttributeValueBefore;
            }

            marker = null;
            return completeAttributeValueUnquoted(code);
        }

        
        private State completeAttributeValueQuoted(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                return nok.step(code);
            }

            if (Objects.equals(code, marker)) {
                effects.consume(code);
                return this::completeAttributeValueQuotedAfter;
            }

            effects.consume(code);
            return this::completeAttributeValueQuoted;
        }

        
        private State completeAttributeValueUnquoted(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.quotationMark ||
                            code == Codes.apostrophe ||
                            code == Codes.lessThan ||
                            code == Codes.equalsTo ||
                            code == Codes.greaterThan ||
                            code == Codes.graveAccent ||
                            CharUtil.markdownLineEndingOrSpace(code)
            ) {
                return completeAttributeNameAfter(code);
            }

            effects.consume(code);
            return this::completeAttributeValueUnquoted;
        }

        
        private State completeAttributeValueQuotedAfter(int code) {
            if (
                    code == Codes.slash ||
                            code == Codes.greaterThan ||
                            CharUtil.markdownSpace(code)
            ) {
                return completeAttributeNameBefore(code);
            }

            return nok.step(code);
        }

        
        private State completeEnd(int code) {
            if (code == Codes.greaterThan) {
                effects.consume(code);
                return this::completeAfter;
            }

            return nok.step(code);
        }

        
        private State completeAfter(int code) {
            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::completeAfter;
            }

            return code == Codes.eof || CharUtil.markdownLineEnding(code)
                    ? continuation(code)
                    : nok.step(code);
        }

        
        private State continuation(int code) {
            if (code == Codes.dash && kind == Constants.htmlComment) {
                effects.consume(code);
                return this::continuationCommentInside;
            }

            if (code == Codes.lessThan && kind == Constants.htmlRaw) {
                effects.consume(code);
                return this::continuationRawTagOpen;
            }

            if (code == Codes.greaterThan && kind == Constants.htmlDeclaration) {
                effects.consume(code);
                return this::continuationClose;
            }

            if (code == Codes.questionMark && kind == Constants.htmlInstruction) {
                effects.consume(code);
                return this::continuationDeclarationInside;
            }

            if (code == Codes.rightSquareBracket && kind == Constants.htmlCdata) {
                effects.consume(code);
                return this::continuationCharacterDataInside;
            }

            if (
                    CharUtil.markdownLineEnding(code) &&
                            (kind == Constants.htmlBasic || kind == Constants.htmlComplete)
            ) {
                return effects.check.hook(
                        nextBlankConstruct,
                        this::continuationClose,
                        this::continuationAtLineEnding
                ).step(code);
            }

            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                return continuationAtLineEnding(code);
            }

            effects.consume(code);
            return this::continuation;
        }

        
        private State continuationAtLineEnding(int code) {
            effects.exit(Types.htmlFlowData);
            return htmlContinueStart(code);
        }

        
        private State htmlContinueStart(int code) {
            if (code == Codes.eof) {
                return done(code);
            }

            if (CharUtil.markdownLineEnding(code)) {
                var tempConstruct = new Construct();
                tempConstruct.tokenize = this::htmlLineEnd;
                tempConstruct.partial = true;

                return effects.attempt.hook(tempConstruct,
                this::htmlContinueStart,
                        this::done
      ).step(code);
            }

            effects.enter(Types.htmlFlowData);
            return continuation(code);
        }

        private State htmlLineEnd(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
            State lineStart= (int code) -> {
                return context.isOnLazyLine() ? nok.step(code) : ok.step(code);
            };

            return (int code) -> {
                Assert.check(CharUtil.markdownLineEnding(code), "expected eol");
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return lineStart;
            };
        }

        private State continuationCommentInside(int code) {
            if (code == Codes.dash) {
                effects.consume(code);
                return this::continuationDeclarationInside;
            }

            return continuation(code);
        }

        
        private State continuationRawTagOpen(int code) {
            if (code == Codes.slash) {
                effects.consume(code);
                buffer = "";
                return this::continuationRawEndTag;
            }

            return continuation(code);
        }

        
        private State continuationRawEndTag(int code) {
            if (
                    code == Codes.greaterThan &&
                            HtmlTagName.htmlRawNames.contains(buffer.toLowerCase())
            ) {
                effects.consume(code);
                return this::continuationClose;
            }

            if (CharUtil.asciiAlpha(code) && buffer.length() < Constants.htmlRawSizeMax) {
                effects.consume(code);
                buffer += String.valueOf((char) code);
                return this::continuationRawEndTag;
            }

            return continuation(code);
        }

        
        private State continuationCharacterDataInside(int code) {
            if (code == Codes.rightSquareBracket) {
                effects.consume(code);
                return this::continuationDeclarationInside;
            }

            return continuation(code);
        }

        
        private State continuationDeclarationInside(int code) {
            if (code == Codes.greaterThan) {
                effects.consume(code);
                return this::continuationClose;
            }

            // More dashes.
            if (code == Codes.dash && kind == Constants.htmlComment) {
                effects.consume(code);
                return this::continuationDeclarationInside;
            }

            return continuation(code);
        }

        
        private State continuationClose(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.htmlFlowData);
                return done(code);
            }

            effects.consume(code);
            return this::continuationClose;
        }

        
        private State done(int code) {
            effects.exit(Types.htmlFlow);
            return ok.step(code);
        }
    }

    private static class NextBlankStateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public NextBlankStateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        
        private State start(int code) {
            Assert.check(CharUtil.markdownLineEnding(code), "expected a line ending");
            effects.exit(Types.htmlFlowData);
            effects.enter(Types.lineEndingBlank);
            effects.consume(code);
            effects.exit(Types.lineEndingBlank);
            return effects.attempt.hook(BlankLine.blankLine, ok, nok);
        }
    }
}
