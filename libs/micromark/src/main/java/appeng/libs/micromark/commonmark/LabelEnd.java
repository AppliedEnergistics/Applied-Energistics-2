package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.ListUtils;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.NormalizeIdentifier;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactoryDestination;
import appeng.libs.micromark.factory.FactoryLabel;
import appeng.libs.micromark.factory.FactoryTitle;
import appeng.libs.micromark.factory.FactoryWhitespace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LabelEnd {
    private LabelEnd() {
    }

    public static final Construct labelEnd;
    public static final Construct resourceConstruct;
    public static final Construct fullReferenceConstruct;
    public static final Construct collapsedReferenceConstruct;

    static {
        labelEnd = new Construct();
        labelEnd.name = "labelEnd";
        labelEnd.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        labelEnd.resolveTo = LabelEnd::resolveToLabelEnd;
        labelEnd.resolveAll = LabelEnd::resolveAll;

        resourceConstruct = new Construct();
        resourceConstruct.tokenize = (context, effects, ok, nok) -> new ResourceStateMachine(context, effects, ok, nok)::start;

        fullReferenceConstruct = new Construct();
        fullReferenceConstruct.tokenize = (context, effects, ok, nok) -> new FullReferenceStateMachine(context, effects, ok, nok)::start;

        collapsedReferenceConstruct = new Construct();
        collapsedReferenceConstruct.tokenize = (context, effects, ok, nok) -> new CollapsedReferenceStateMachine(context, effects, ok, nok)::start;
    }

    public static List<Tokenizer.Event> resolveAll(List<Tokenizer.Event> events, Tokenizer.TokenizeContext context) {
        var index = -1;

        while (++index < events.size()) {
            var token = events.get(index).token();

            if (
                    token.type.equals(Types.labelImage) ||
                            token.type.equals(Types.labelLink) ||
                            token.type.equals(Types.labelEnd)
            ) {
                // Remove the marker.
                ListUtils.splice(events, index + 1, token.type.equals(Types.labelImage) ? 4 : 2);
                token.type = Types.data;
                index++;
            }
        }

        return events;
    }

    private static List<Tokenizer.Event> resolveToLabelEnd(List<Tokenizer.Event> events, Tokenizer.TokenizeContext context) {
        var index = events.size();
        var offset = 0;
        Token token;
        Integer open = null;
        Integer close = null;
        List<Tokenizer.Event> media;

        // Find an opening.
        while (index-- > 0) {
            token = events.get(index).token();

            if (open != null) {
                // If we see another link, or inactive link label, we’ve been here before.
                if (
                        token.type.equals(Types.link) ||
                                (token.type.equals(Types.labelLink) && token._inactive)
                ) {
                    break;
                }

                // Mark other link openings as inactive, as we can’t have links in
                // links.
                if (events.get(index).isEnter() && token.type.equals(Types.labelLink)) {
                    token._inactive = true;
                }
            } else if (close != null) {
                if (
                        events.get(index).isEnter() &&
                                (token.type.equals(Types.labelImage) || token.type.equals(Types.labelLink)) &&
                                !token._balanced
                ) {
                    open = index;

                    if (!token.type.equals(Types.labelLink)) {
                        offset = 2;
                        break;
                    }
                }
            } else if (token.type.equals(Types.labelEnd)) {
                close = index;
            }
        }

        Assert.check(open != null, "`open` is supposed to be found");
        Assert.check(close != null, "`close` is supposed to be found");

        var group = new Token();
        group.type = events.get(open).token().type.equals(Types.labelLink) ? Types.link : Types.image;
        group.start = events.get(open).token().start;
        group.end = events.get(events.size() - 1).token().end;

        var label = new Token();
        label.type = Types.label;
        label.start = events.get(open).token().start;
        label.end = events.get(close).token().end;

        var text = new Token();
        text.type = Types.labelText;
        text.start = events.get(open + offset + 2).token().end;
        text.end = events.get(close - 2).token().start;

        media = new ArrayList<>();
        media.add(Tokenizer.Event.enter(group, context));
        media.add(Tokenizer.Event.enter(label, context));

        // Opening marker.
        media = ListUtils.push(media, events.subList(open + 1, open + offset + 3));

        // Text open.
        media = ListUtils.push(media, List.of(Tokenizer.Event.enter(text, context)));

        // Between.
        media = ListUtils.push(
                media,
                Construct.resolveAll(
                        context.parser.constructs.nullInsideSpan,
                        ListUtils.slice(events, open + offset + 4, close - 3),
                        context
                )
        );

        // Text close, marker close, label close.
        media = ListUtils.push(media, List.of(
                Tokenizer.Event.exit(text, context),
                events.get(close - 2),
                events.get(close - 1),
                Tokenizer.Event.exit(label, context)
        ));

        // Reference, resource, or so.
        media = ListUtils.push(media, events.subList(close + 1, events.size()));

        // Media close.
        media = ListUtils.push(media, List.of(Tokenizer.Event.exit(group, context)));

        ListUtils.splice(events, open, events.size(), media);

        return events;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        int index;
        @Nullable
        Token labelStart;
        boolean defined = false;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
            this.index = context.events.size();

            // Find an opening.
            while (index-- > 0) {
                if (
                        (context.events.get(index).token().type.equals(Types.labelImage) ||
                                context.events.get(index).token().type.equals(Types.labelLink)) &&
                                !context.events.get(index).token()._balanced
                ) {
                    labelStart = context.events.get(index).token();
                    break;
                }
            }
        }


        /**
         * Start of label end.
         *
         * <pre>
         * > | [a](b) c
         *       ^
         * > | [a][b] c
         *       ^
         * > | [a][] b
         *       ^
         * > | [a] b
         * </pre>
         */
        private State start(int code) {
            Assert.check(code == Codes.rightSquareBracket, "expected `]`");

            if (labelStart == null) {
                return nok.step(code);
            }

            // It’s a balanced bracket, but contains a link.
            if (labelStart._inactive) return balanced(code);

            defined = context.parser.defined.contains(
                    NormalizeIdentifier.normalizeIdentifier(
                            context.sliceSerialize(labelStart.end, context.now())
                    )
            );
            effects.enter(Types.labelEnd);
            effects.enter(Types.labelMarker);
            effects.consume(code);
            effects.exit(Types.labelMarker);
            effects.exit(Types.labelEnd);
            return this::afterLabelEnd;
        }

        /**
         * After `]`.
         *
         * <pre>
         * > | [a](b) c
         *       ^
         * > | [a][b] c
         *       ^
         * > | [a][] b
         *       ^
         * > | [a] b
         *       ^
         * </pre>
         */
        private State afterLabelEnd(int code) {
            // Resource (`[asd](fgh)`)?
            if (code == Codes.leftParenthesis) {
                return effects.attempt.hook(
                        resourceConstruct,
                        ok,
                        defined ? ok : this::balanced
                ).step(code);
            }

            // Full (`[asd][fgh]`) or collapsed (`[asd][]`) reference?
            if (code == Codes.leftSquareBracket) {
                return effects.attempt.hook(
                        fullReferenceConstruct,
                        ok,
                        defined
                                ? effects.attempt.hook(collapsedReferenceConstruct, ok, this::balanced)
                                : this::balanced
                ).step(code);
            }

            // Shortcut (`[asd]`) reference?
            return defined ? ok.step(code) : balanced(code);
        }

        /**
         * Done, it’s nothing.
         * <p>
         * There was an okay opening, but we didn’t match anything.
         *
         * <pre>
         * > | [a](b c
         *        ^
         * > | [a][b c
         *        ^
         * > | [a] b
         *        ^
         * </pre>
         */
        private State balanced(int code) {
            labelStart._balanced = true;
            return nok.step(code);
        }
    }

    private static class ResourceStateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public ResourceStateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        /**
         * Before a resource, at `(`.
         *
         * <pre>
         * > | [a](b) c
         *        ^
         * </pre>
         */
        private State start(int code) {
            Assert.check(code == Codes.leftParenthesis, "expected left paren");
            effects.enter(Types.resource);
            effects.enter(Types.resourceMarker);
            effects.consume(code);
            effects.exit(Types.resourceMarker);
            return FactoryWhitespace.create(effects, this::open);
        }

        /**
         * At the start of a resource, after optional whitespace.
         *
         * <pre>
         * > | [a](b) c
         *         ^
         * </pre>
         */
        private State open(int code) {
            if (code == Codes.rightParenthesis) {
                return end(code);
            }

            return FactoryDestination.create(
                    effects,
                    this::destinationAfter,
                    nok,
                    Types.resourceDestination,
                    Types.resourceDestinationLiteral,
                    Types.resourceDestinationLiteralMarker,
                    Types.resourceDestinationRaw,
                    Types.resourceDestinationString,
                    Constants.linkResourceDestinationBalanceMax
            ).step(code);
        }

        /**
         * In a resource, after a destination, before optional whitespace.
         *
         * <pre>
         * > | [a](b) c
         *          ^
         * </pre>
         */
        private State destinationAfter(int code) {
            return CharUtil.markdownLineEndingOrSpace(code)
                    ? FactoryWhitespace.create(effects, this::between).step(code)
                    : end(code);
        }

        /**
         * In a resource, after a destination, after whitespace.
         *
         * <pre>
         * > | [a](b ) c
         *           ^
         * </pre>
         */
        private State between(int code) {
            if (
                    code == Codes.quotationMark ||
                            code == Codes.apostrophe ||
                            code == Codes.leftParenthesis
            ) {
                return FactoryTitle.create(
                        effects,
                        FactoryWhitespace.create(effects, this::end),
                        nok,
                        Types.resourceTitle,
                        Types.resourceTitleMarker,
                        Types.resourceTitleString
                ).step(code);
            }

            return end(code);
        }

        /**
         * In a resource, at the `)`.
         *
         * <pre>
         * > | [a](b) d
         *          ^
         * </pre>
         */
        private State end(int code) {
            if (code == Codes.rightParenthesis) {
                effects.enter(Types.resourceMarker);
                effects.consume(code);
                effects.exit(Types.resourceMarker);
                effects.exit(Types.resource);
                return ok;
            }

            return nok.step(code);
        }
    }

    private static class FullReferenceStateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public FullReferenceStateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        /**
         * In a reference (full), at the `[`.
         *
         * <pre>
         * > | [a][b] d
         *        ^
         * </pre>
         */
        private State start(int code) {
            Assert.check(code == Codes.leftSquareBracket, "expected left bracket");
            return FactoryLabel.create(
                    context,
                    effects,
                    this::afterLabel,
                    nok,
                    Types.reference,
                    Types.referenceMarker,
                    Types.referenceString
            ).step(code);
        }

        /**
         * In a reference (full), after `]`.
         *
         * <pre>
         * > | [a][b] d
         *          ^
         * </pre>
         */
        private State afterLabel(int code) {
            var lastTokenText = context.sliceSerialize(context.getLastEvent().token());
            return context.parser.defined.contains(
                    NormalizeIdentifier.normalizeIdentifier(
                            lastTokenText.substring(1, lastTokenText.length() - 1)
                    )
            )
                    ? ok.step(code)
                    : nok.step(code);
        }
    }

    private static class CollapsedReferenceStateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public CollapsedReferenceStateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        /**
         * In a reference (collapsed), at the `[`.
         * <p>
         * > 👉 **Note**: we only get here if the label is defined.
         *
         * <pre>
         * > | [a][] d
         *        ^
         * </pre>
         */
        private State start(int code) {
            Assert.check(code == Codes.leftSquareBracket, "expected left bracket");
            effects.enter(Types.reference);
            effects.enter(Types.referenceMarker);
            effects.consume(code);
            effects.exit(Types.referenceMarker);
            return this::open;
        }

        /**
         * In a reference (collapsed), at the `]`.
         * <p>
         * > 👉 **Note**: we only get here if the label is defined.
         *
         * <pre>
         * > | [a][] d
         *         ^
         * </pre>
         */
        private State open(int code) {
            if (code == Codes.rightSquareBracket) {
                effects.enter(Types.referenceMarker);
                effects.consume(code);
                effects.exit(Types.referenceMarker);
                effects.exit(Types.reference);
                return ok;
            }

            return nok.step(code);
        }
    }
}
