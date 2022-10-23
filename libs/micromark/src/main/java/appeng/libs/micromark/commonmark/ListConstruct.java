package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

public final class ListConstruct {
    private ListConstruct() {
    }

    public static final Construct list;
    public static final Construct listItemPrefixWhitespaceConstruct;
    public static final Construct indentConstruct;
    static {
        list = new Construct();
        list.name = "list";
        list.tokenize = (context, effects, ok, nok) -> new StartStateMachine(context, effects, ok, nok)::start;
        list.continuation = new Construct();
        list.continuation.tokenize = (context, effects, ok, nok) -> new ContinuationStateMachine(context, effects, ok, nok).start;
        list.exit = (context, effects) -> effects.exit((String) context.getContainerState().get("type"));

        listItemPrefixWhitespaceConstruct = new Construct();
        listItemPrefixWhitespaceConstruct.tokenize = (context, effects, ok, nok) -> new ItemPrefixWhitespaceStateMachine(context, effects, ok, nok).start;
        listItemPrefixWhitespaceConstruct.partial = true;

        indentConstruct = new Construct();
        indentConstruct.tokenize = (context, effects, ok, nok) -> new IndentStateMachine(context, effects, ok, nok).start;
        indentConstruct.partial = true;
    }

    private static class StartStateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        private int initialSize;

        private int size;

        public StartStateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;

            var tail = context.getLastEvent();
            initialSize = tail != null && tail.token().type.equals(Types.linePrefix)
                            ? tail.context().sliceSerialize(tail.token(), true).length()
                            : 0;
        }

        
        private State start(int code) {
            var kind = (String)
                    context.getContainerState().getOrDefault("type",
                            (code == Codes.asterisk || code == Codes.plusSign || code == Codes.dash
                                    ? Types.listUnordered
                                    : Types.listOrdered));

            if (
                    kind.equals(Types.listUnordered)
                            ? !context.getContainerState().containsKey("marker") || code == (int) context.getContainerState().get("marker")
                            : CharUtil.asciiDigit(code)
            ) {
                if (!context.getContainerState().containsKey("type")) {
                    context.getContainerState().put("type", kind);
                            var tokenFields = new Token();
                            tokenFields._container = true;
                    effects.enter(kind, tokenFields);
                }

                if (kind.equals(Types.listUnordered)) {
                    effects.enter(Types.listItemPrefix);
                    return code == Codes.asterisk || code == Codes.dash
                            ? effects.check.hook(ThematicBreak.thematicBreak, nok, this::atMarker).step(code)
          : atMarker(code);
                }

                if (!context.isInterrupt() || code == Codes.digit1) {
                    effects.enter(Types.listItemPrefix);
                    effects.enter(Types.listItemValue);
                    return inside(code);
                }
            }

            return nok.step(code);
        }

        
        private State inside(int code) {
            if (CharUtil.asciiDigit(code) && ++size < Constants.listItemValueSizeMax) {
                effects.consume(code);
                return this::inside;
            }

            if (
                    (!context.isInterrupt() || size < 2) &&
                            (context.getContainerState().containsKey("marker")
                                    ? code == (int) context.getContainerState().get("marker")
                                    : code == Codes.rightParenthesis || code == Codes.dot)
            ) {
                effects.exit(Types.listItemValue);
                return atMarker(code);
            }

            return nok.step(code);
        }

        /**
         
         **/
        private State atMarker(int code) {
            Assert.check(code != Codes.eof, "eof (`null`) is not a marker");
            effects.enter(Types.listItemMarker);
            effects.consume(code);
            effects.exit(Types.listItemMarker);
            context.getContainerState().putIfAbsent("marker", code);
            return effects.check.hook(
                    BlankLine.blankLine,
                    // Can’t be empty when interrupting.
                    context.isInterrupt() ? nok : this::onBlank,
                    effects.attempt.hook(
                            listItemPrefixWhitespaceConstruct,
                            this::endOfPrefix,
                            this::otherPrefix
                    )
            );
        }

        
        private State onBlank(int code) {
            context.getContainerState().put("initialBlankLine", true);
            initialSize++;
            return endOfPrefix(code);
        }

        
        private State otherPrefix(int code) {
            if (CharUtil.markdownSpace(code)) {
                effects.enter(Types.listItemPrefixWhitespace);
                effects.consume(code);
                effects.exit(Types.listItemPrefixWhitespace);
                return this::endOfPrefix;
            }

            return nok.step(code);
        }

        
        private State endOfPrefix(int code) {
            context.getContainerState().put("size",
                    initialSize +
                            context.sliceSerialize(effects.exit(Types.listItemPrefix), true).length()
            );
            return ok.step(code);
        }
    }

    private static class ContinuationStateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public final State start;

        public ContinuationStateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;

            context.getContainerState().remove("_closeFlow");
            start = effects.check.hook(BlankLine.blankLine, this::onBlank, this::notBlank);
        }

        private State onBlank(int code) {
            context.getContainerState().putIfAbsent("furtherBlankLines", context.getContainerState().getOrDefault("initialBlankLine", false));

            // We have a blank line.
            // Still, try to consume at most the items size.
            return FactorySpace.create(
                    effects,
                    ok,
                    Types.listItemIndent,
                    (int) context.getContainerState().getOrDefault("size", 0) + 1
            ).step(code);
        }

        
        private State notBlank(int code) {
            if ((boolean) context.getContainerState().getOrDefault("furtherBlankLines", false) || !CharUtil.markdownSpace(code)) {
                context.getContainerState().remove("furtherBlankLines");
                context.getContainerState().remove("initialBlankLine");
                return notInCurrentItem(code);
            }

            context.getContainerState().remove("furtherBlankLines");
            context.getContainerState().remove("initialBlankLine");
            return effects.attempt.hook(indentConstruct, ok, this::notInCurrentItem).step(code);
        }

        
        private State notInCurrentItem(int code) {
            // While we do continue, we signal that the flow should be closed.
            context.getContainerState().put("_closeFlow", true);
            // As we’re closing flow, we’re no longer interrupting.
            context.setInterrupt(false);
            return FactorySpace.create(
                    effects,
                    effects.attempt.hook(list, ok, nok),
                    Types.linePrefix,
                    context.getParser().constructs.nullDisable.contains("codeIndented")
                    ? null
                    : Constants.tabSize
    ).step(code);
        }

    }

    private static class IndentStateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        public final State start;

        public IndentStateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;

            this.start = FactorySpace.create(
                    effects,
                    this::afterPrefix,
                    Types.listItemIndent,
                    (int) context.getContainerState().getOrDefault("size", 0) + 1
            );
        }


        
        private State afterPrefix(int code) {
    var tail = context.getLastEvent();
            return tail != null &&
                    tail.token().type.equals(Types.listItemIndent) &&
                    tail.context().sliceSerialize(tail.token(), true).length() == (int) context.getContainerState().getOrDefault("size", 0)
                    ? ok.step(code)
                    : nok.step(code);
        }

    }

    private static class ItemPrefixWhitespaceStateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        public final State start;

        public ItemPrefixWhitespaceStateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
            this.start = FactorySpace.create(
                    effects,
                    this::afterPrefix,
                    Types.listItemPrefixWhitespace,
                    context.getParser().constructs.nullDisable.contains("codeIndented")
                    ? null
                    : Constants.tabSize + 1
            );
        }


        
        private State afterPrefix(int code) {
    var tail = context.getLastEvent();

            return !CharUtil.markdownSpace(code) &&
                    tail  != null &&
                    tail.token().type.equals(Types.listItemPrefixWhitespace)
                    ? ok.step(code)
                    : nok.step(code);
        }
    }


}
