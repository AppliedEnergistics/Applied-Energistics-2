package appeng.libs.mdast;

import appeng.libs.mdast.model.MdAstBlockquote;
import appeng.libs.mdast.model.MdAstBreak;
import appeng.libs.mdast.model.MdAstCode;
import appeng.libs.mdast.model.MdAstDefinition;
import appeng.libs.mdast.model.MdAstEmphasis;
import appeng.libs.mdast.model.MdAstHTML;
import appeng.libs.mdast.model.MdAstHeading;
import appeng.libs.mdast.model.MdAstImage;
import appeng.libs.mdast.model.MdAstImageReference;
import appeng.libs.mdast.model.MdAstInlineCode;
import appeng.libs.mdast.model.MdAstLink;
import appeng.libs.mdast.model.MdAstLinkReference;
import appeng.libs.mdast.model.MdAstList;
import appeng.libs.mdast.model.MdAstListItem;
import appeng.libs.mdast.model.MdAstLiteral;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.mdast.model.MdAstParagraph;
import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstPhrasingContent;
import appeng.libs.mdast.model.MdAstPosition;
import appeng.libs.mdast.model.MdAstReferenceType;
import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.mdast.model.MdAstStrong;
import appeng.libs.mdast.model.MdAstText;
import appeng.libs.mdast.model.MdAstThematicBreak;
import appeng.libs.micromark.Assert;
import appeng.libs.micromark.DecodeString;
import appeng.libs.micromark.ListUtils;
import appeng.libs.micromark.NamedCharacterEntities;
import appeng.libs.micromark.NormalizeIdentifier;
import appeng.libs.micromark.Point;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenProperty;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.html.HtmlContextProperty;
import appeng.libs.micromark.html.NumericCharacterReference;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;
import appeng.libs.unist.UnistPoint;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

final class MdastCompiler implements MdastContext {

    private static final TokenProperty<Boolean> SPREAD = new TokenProperty<>();

    private final MdastExtension extension;

    boolean expectingFirstListItemValue;
    boolean flowCodeInside;
    boolean setextHeadingSlurpLineEnding;
    boolean atHardBreak;
    MdAstReferenceType referenceType;
    boolean inReference;
    CharacterReferenceType characterReferenceType;

    private final Map<MdastContextProperty<?>, Object> extensionData = new IdentityHashMap<>();

    private List<MdAstNode> stack;
    private List<TokenStackEntry> tokenStack;
    @Nullable
    private TokenizeContext currentTokenContext;
    private final StringBuilder stringBuffer = new StringBuilder();

    MdastCompiler(MdastOptions options) {
        var extensionBuilder = MdastExtension.builder()
                .canContainEol(
                        "emphasis",
                        "fragment",
                        "heading",
                        "paragraph",
                        "strong"
                )
                .enter("autolink", opener(this::link))
                .enter("autolinkProtocol", this::onenterdata)
                .enter("autolinkEmail", this::onenterdata)
                .enter("atxHeading", opener(this::heading))
                .enter("blockQuote", opener(this::blockQuote))
                .enter("characterEscape", this::onenterdata)
                .enter("characterReference", this::onenterdata)
                .enter("codeFenced", opener(this::codeFlow))
                .enter("codeFencedFenceInfo", this::buffer)
                .enter("codeFencedFenceMeta", this::buffer)
                .enter("codeIndented", opener(this::codeFlow, this::buffer))
                .enter("codeText", opener(this::codeText, this::buffer))
                .enter("codeTextData", this::onenterdata)
                .enter("data", this::onenterdata)
                .enter("codeFlowValue", this::onenterdata)
                .enter("definition", opener(this::definition))
                .enter("definitionDestinationString", this::buffer)
                .enter("definitionLabelString", this::buffer)
                .enter("definitionTitleString", this::buffer)
                .enter("emphasis", opener(this::emphasis))
                .enter("hardBreakEscape", opener(this::hardBreak))
                .enter("hardBreakTrailing", opener(this::hardBreak))
                .enter("htmlFlow", opener(this::html, this::buffer))
                .enter("htmlFlowData", this::onenterdata)
                .enter("htmlText", opener(this::html, this::buffer))
                .enter("htmlTextData", this::onenterdata)
                .enter("image", opener(this::image))
                .enter("label", this::buffer)
                .enter("link", opener(this::link))
                .enter("listItem", opener(this::listItem))
                .enter("listItemValue", this::onenterlistitemvalue)
                .enter("listOrdered", opener(this::list, this::onenterlistordered))
                .enter("listUnordered", opener(this::list))
                .enter("paragraph", opener(this::paragraph))
                .enter("reference", this::onenterreference)
                .enter("referenceString", this::buffer)
                .enter("resourceDestinationString", this::buffer)
                .enter("resourceTitleString", this::buffer)
                .enter("setextHeading", opener(this::heading))
                .enter("strong", opener(this::strong))
                .enter("thematicBreak", opener(this::thematicBreak))
                .exit("atxHeading", closer())
                .exit("atxHeadingSequence", this::onexitatxheadingsequence)
                .exit("autolink", closer())
                .exit("autolinkEmail", this::onexitautolinkemail)
                .exit("autolinkProtocol", this::onexitautolinkprotocol)
                .exit("blockQuote", closer())
                .exit("characterEscapeValue", this::onexitdata)
                .exit("characterReferenceMarkerHexadecimal", this::onexitcharacterreferencemarker)
                .exit("characterReferenceMarkerNumeric", this::onexitcharacterreferencemarker)
                .exit("characterReferenceValue", this::onexitcharacterreferencevalue)
                .exit("codeFenced", closer(this::onexitcodefenced))
                .exit("codeFencedFence", this::onexitcodefencedfence)
                .exit("codeFencedFenceInfo", this::onexitcodefencedfenceinfo)
                .exit("codeFencedFenceMeta", this::onexitcodefencedfencemeta)
                .exit("codeFlowValue", this::onexitdata)
                .exit("codeIndented", closer(this::onexitcodeindented))
                .exit("codeText", closer(this::onexitcodetext))
                .exit("codeTextData", this::onexitdata)
                .exit("data", this::onexitdata)
                .exit("definition", closer())
                .exit("definitionDestinationString", this::onexitdefinitiondestinationstring)
                .exit("definitionLabelString", this::onexitdefinitionlabelstring)
                .exit("definitionTitleString", this::onexitdefinitiontitlestring)
                .exit("emphasis", closer())
                .exit("hardBreakEscape", closer(this::onexithardbreak))
                .exit("hardBreakTrailing", closer(this::onexithardbreak))
                .exit("htmlFlow", closer(this::onexithtmlflow))
                .exit("htmlFlowData", this::onexitdata)
                .exit("htmlText", closer(this::onexithtmltext))
                .exit("htmlTextData", this::onexitdata)
                .exit("image", closer(this::onexitimage))
                .exit("label", this::onexitlabel)
                .exit("labelText", this::onexitlabeltext)
                .exit("lineEnding", this::onexitlineending)
                .exit("link", closer(this::onexitlink))
                .exit("listItem", closer())
                .exit("listOrdered", closer())
                .exit("listUnordered", closer())
                .exit("paragraph", closer())
                .exit("referenceString", this::onexitreferencestring)
                .exit("resourceDestinationString", this::onexitresourcedestinationstring)
                .exit("resourceTitleString", this::onexitresourcetitlestring)
                .exit("resource", this::onexitresource)
                .exit("setextHeading", closer(this::onexitsetextheading))
                .exit("setextHeadingLineSequence", this::onexitsetextheadinglinesequence)
                .exit("setextHeadingText", this::onexitsetextheadingtext)
                .exit("strong", closer())
                .exit("thematicBreak", closer());

        for (var mdastExtension : options.mdastExtensions) {
            extensionBuilder.addAll(mdastExtension);
        }

        extension = extensionBuilder.build();
    }

    enum CharacterReferenceType {
        characterReferenceMarkerHexadecimal,
        characterReferenceMarkerNumeric
    }

    MdAstRoot compile(List<Tokenizer.Event> events) {
        MdAstRoot tree = new MdAstRoot();
        stack = new ArrayList<>();
        stack.add(tree);
        tokenStack = new ArrayList<>();
        List<Integer> listStack = new ArrayList<>();
        int index = -1;

        while (++index < events.size()) {
            var event = events.get(index);

            // We preprocess lists to add `listItem` tokens, and to infer whether
            // items the list itself are spread out.
            if (
                    event.token().type.equals(Types.listOrdered) ||
                            event.token().type.equals(Types.listUnordered)
            ) {
                if (event.isEnter()) {
                    listStack.add(index);
                } else {
                    var tail = ListUtils.pop(listStack);
                    Assert.check(tail != null, "expected list ot be open");
                    index = prepareList(events, tail, index);
                }
            }
        }

        index = -1;

        while (++index < events.size()) {
            var event = events.get(index);
            var handlerMap = event.isEnter() ? extension.enter : extension.exit;
            var handler = handlerMap.get(event.token().type);

            if (handler != null) {
                currentTokenContext = event.context();
                try {
                    handler.handle(this, event.token());
                } finally {
                    Assert.check(currentTokenContext == event.context(), "currentTokenContext changed while calling handler!");
                    currentTokenContext = null;
                }
            }
        }

        if (!tokenStack.isEmpty()) {
            var tail = tokenStack.get(tokenStack.size() - 1);
            var handler = Objects.requireNonNullElse(tail.onError(), this::defaultOnError);
            handler.error(this, null, tail.token());
        }

        // Figure out `root` position.
        tree.position = new MdAstPosition()
                .withStart(point(
                        !events.isEmpty() ? events.get(0).token().start :
                                makePoint(1, 1, 0)
                ))
                .withEnd(point(
                        !events.isEmpty()
                                ? events.get(events.size() - 2).token().end
                                : makePoint(1, 1, 0)
                ));

        for (var transform : extension.transforms) {
            tree = transform.transform(tree);
        }

        return tree;
    }

    private static UnistPoint makePoint(int line, int column, int offset) {
        return new Point(line, column, offset, -1, -1);
    }

    private static int prepareList(List<Tokenizer.Event> events, int start, int length) {
        var index = start - 1;
        var containerBalance = -1;
        var listSpread = false;
        Token listItem = null;
        Integer lineIndex = null;
        Integer firstBlankLineIndex = null;
        boolean atMarker = false;

        while (++index <= length) {
            var event = events.get(index);
            var tokenType = event.token().type;

            if (
                    tokenType.equals(Types.listUnordered) ||
                            tokenType.equals(Types.listOrdered) ||
                            tokenType.equals(Types.blockQuote)
            ) {
                if (event.isEnter()) {
                    containerBalance++;
                } else {
                    containerBalance--;
                }

                atMarker = false;
            } else if (tokenType.equals(Types.lineEndingBlank)) {
                if (event.isEnter()) {
                    if (
                            listItem != null &&
                                    !atMarker &&
                                    containerBalance == 0 &&
                                    (firstBlankLineIndex == null || firstBlankLineIndex == 0)
                    ) {
                        firstBlankLineIndex = index;
                    }

                    atMarker = false;
                }
            } else if (
                    tokenType.equals(Types.linePrefix) ||
                            tokenType.equals(Types.listItemValue) ||
                            tokenType.equals(Types.listItemMarker) ||
                            tokenType.equals(Types.listItemPrefix) ||
                            tokenType.equals(Types.listItemPrefixWhitespace)
            ) {
                // Empty.
            } else {
                atMarker = false;
            }

            if (
                    (containerBalance == 0 &&
                            event.isEnter() &&
                            tokenType.equals(Types.listItemPrefix)) ||
                            (containerBalance == -1 &&
                                    event.isExit() &&
                                    (tokenType.equals(Types.listUnordered) ||
                                            tokenType.equals(Types.listOrdered)))
            ) {
                if (listItem != null) {
                    var tailIndex = index;
                    lineIndex = null;

                    while (tailIndex-- != 0) {
                        var tailEvent = events.get(tailIndex);
                        var tailEventTokenType = tailEvent.token().type;

                        if (
                                tailEventTokenType.equals(Types.lineEnding) ||
                                        tailEventTokenType.equals(Types.lineEndingBlank)
                        ) {
                            if (tailEvent.isExit()) continue;

                            if (lineIndex != null && lineIndex != 0) {
                                events.get(lineIndex).token().type = Types.lineEndingBlank;
                                listSpread = true;
                            }

                            tailEvent.token().type = Types.lineEnding;
                            lineIndex = tailIndex;
                        } else if (
                                tailEventTokenType.equals(Types.linePrefix) ||
                                        tailEventTokenType.equals(Types.blockQuotePrefix) ||
                                        tailEventTokenType.equals(Types.blockQuotePrefixWhitespace) ||
                                        tailEventTokenType.equals(Types.blockQuoteMarker) ||
                                        tailEventTokenType.equals(Types.listItemIndent)
                        ) {
                            // Empty
                        } else {
                            break;
                        }
                    }

                    if (
                            (firstBlankLineIndex != null && firstBlankLineIndex != 0) &&
                                    (lineIndex == null || lineIndex == 0 || firstBlankLineIndex < lineIndex)
                    ) {
                        listItem.set(SPREAD, true);
                    }

                    // Fix position.
                    listItem.end = (lineIndex != null && lineIndex != 0) ? events.get(lineIndex).token().start : event.token().end;

                    ListUtils.splice(events, Objects.requireNonNullElse(lineIndex, index), 0, List.of(Tokenizer.Event.exit(listItem, event.context())));
                    index++;
                    length++;
                }

                // Create a new list item.
                if (tokenType.equals(Types.listItemPrefix)) {
                    listItem = new Token();
                    listItem.type = "listItem";
                    listItem.set(SPREAD, false);
                    listItem.start = event.token().start;

                    // @ts-expect-error: `listItem` is most definitely defined, TS...
                    ListUtils.splice(events, index, 0, List.of(Tokenizer.Event.enter(listItem, event.context())));
                    index++;
                    length++;
                    firstBlankLineIndex = null;
                    atMarker = true;
                }
            }
        }

        events.get(start).token().set(SPREAD, listSpread);
        return length;
    }

    UnistPoint point(UnistPoint d) {
        return d;
    }

    MdastExtension.Handler opener(Supplier<MdAstNode> create) {
        return (ctx, token) -> {
            enter(create.get(), token);
        };
    }

    MdastExtension.Handler opener(Function<Token, MdAstNode> create) {
        return (ctx, token) -> {
            enter(create.apply(token), token);
        };
    }

    MdastExtension.Handler opener(Supplier<MdAstNode> create, MdastExtension.Handler and) {
        return opener(t -> create.get(), and);
    }

    MdastExtension.Handler opener(Supplier<MdAstNode> create, Runnable and) {
        return opener(t -> create.get(), (context, token) -> and.run());
    }

    MdastExtension.Handler opener(Function<Token, MdAstNode> create, Runnable and) {
        return opener(create, (context, token) -> and.run());
    }

    MdastExtension.Handler opener(Function<Token, MdAstNode> create, MdastExtension.Handler and) {
        return (ctx, token) -> {
            enter(create.apply(token), token);
            if (and != null) {
                and.handle(this, token);
            }
        };
    }

    @Override
    public List<MdAstNode> getStack() {
        return stack;
    }

    @Override
    public List<TokenStackEntry> getTokenStack() {
        return tokenStack;
    }

    public void buffer() {
        this.stack.add(new Fragment());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T get(MdastContextProperty<T> property) {
        return (T) extensionData.get(property);
    }

    @Override
    public <T> void set(MdastContextProperty<T> property, T value) {
        extensionData.put(property, value);
    }

    @Override
    public void remove(MdastContextProperty<?> property) {
        extensionData.remove(property);
    }

    @Override
    public <N extends MdAstNode> N enter(N node, Token token, OnEnterError errorHandler) {
        var parent = (MdAstParent<?>) this.stack.get(this.stack.size() - 1);
        Assert.check(parent != null, "expected `parent`");
        parent.addChild(node);
        this.stack.add(node);
        this.tokenStack.add(new TokenStackEntry(token, errorHandler));
        node.position = new MdAstPosition();
        node.position.start = token.start;
        return node;
    }

    private MdastExtension.Handler closer() {
        return (context, token) -> {
            exit(token);
        };
    }

    private MdastExtension.Handler closer(Runnable and) {
        return (context, token) -> {
            and.run();
            exit(token);
        };
    }

    private MdastExtension.Handler closer(@Nullable MdastExtension.Handler and) {
        return (context, token) -> {
            if (and != null) {
                and.handle(this, token);
            }
            exit(token);
        };
    }

    @Override
    public MdAstNode exit(Token token, OnExitError onExitError) {
        var node = ListUtils.pop(this.stack);
        Assert.check(node != null, "expected `node`");
        var open = ListUtils.pop(this.tokenStack);

        if (open == null) {
            throw new RuntimeException(
                    "Cannot close `" +
                            token.type +
                            "` (" +
                            MdAstPosition.stringify(token.start, token.end) +
                            "): it’s not open"
            );
        } else if (!open.token().type.equals(token.type)) {
            if (onExitError != null) {
                onExitError.error(this, token, open.token());
            } else {
                var handler = Objects.requireNonNullElse(open.onError(), this::defaultOnError);
                handler.error(this, token, open.token());
            }
        }

        Assert.check(!node.type().equals("fragment"), "unexpected fragment `exit`ed");
        Assert.check(node.position != null, "expected `position` to be defined");
        node.position.end = token.end;
        return node;
    }

    @Override
    public String sliceSerialize(Token token) {
        Assert.check(currentTokenContext != null, "missing current token context");
        return currentTokenContext.sliceSerialize(token);
    }

    @Override
    public MdastExtension getExtension() {
        return extension;
    }

    public String resume() {
        stringBuffer.setLength(0);
        ListUtils.pop(this.stack).toText(stringBuffer);
        return stringBuffer.toString();
    }

    //
    // Handlers.
    //

    private void onenterlistordered() {
        expectingFirstListItemValue = true;
    }

    private void onenterlistitemvalue(MdastContext context, Token token) {
        if (expectingFirstListItemValue) {
            var ancestor = (MdAstList) (stack.get(stack.size() - 2));
            ancestor.start = Integer.parseInt(
                    this.sliceSerialize(token),
                    Constants.numericBaseDecimal
            );
            expectingFirstListItemValue = false;
        }
    }

    private void onexitcodefencedfenceinfo() {
        var data = this.resume();
        var node = (MdAstCode) (stack.get(stack.size() - 1));
        node.lang = data;
    }

    private void onexitcodefencedfencemeta() {
        var data = this.resume();
        var node = (MdAstCode) (stack.get(stack.size() - 1));
        node.meta = data;
    }

    private void onexitcodefencedfence() {
        // Exit if this is the closing fence.
        if (flowCodeInside) return;
        this.buffer();
        flowCodeInside = true;
    }

    private static final Pattern START_END_NEWLINE = Pattern.compile("^(\r?\n|\r)|(\r?\n|\r)\\z");

    private void onexitcodefenced() {
        var data = this.resume();
        var node = (MdAstCode) (stack.get(stack.size() - 1));

        // Removes the first and last newline in the string
        node.value = START_END_NEWLINE.matcher(data).replaceAll("");

        flowCodeInside = false;
    }

    private void onexitcodeindented() {
        var data = this.resume();
        var node = (MdAstCode) (stack.get(stack.size() - 1));

        node.value = data.replaceAll("(\\r?\\n|\\r)$", "");
    }

    private void onexitdefinitionlabelstring(MdastContext context, Token token) {
        // Discard label, use the source content instead.
        var label = this.resume();
        var node = (MdAstDefinition) (stack.get(stack.size() - 1));
        node.label = label;
        node.identifier = NormalizeIdentifier.normalizeIdentifier(
                this.sliceSerialize(token)
        ).toLowerCase();
    }

    private void onexitdefinitiontitlestring() {
        var data = this.resume();
        var node = (MdAstDefinition) (stack.get(stack.size() - 1));
        node.title = data;
    }

    private void onexitdefinitiondestinationstring() {
        var data = this.resume();
        var node = (MdAstDefinition) (stack.get(stack.size() - 1));
        node.url = data;
    }

    private void onexitatxheadingsequence(MdastContext context, Token token) {
        var node = (MdAstHeading) (stack.get(stack.size() - 1));
        if (node.depth == 0) {
            var depth = this.sliceSerialize(token).length();

            Assert.check(
                    depth == 1 ||
                            depth == 2 ||
                            depth == 3 ||
                            depth == 4 ||
                            depth == 5 ||
                            depth == 6,
                    "expected `depth` between `1` and `6`"
            );

            node.depth = depth;
        }
    }

    private void onexitsetextheadingtext() {
        setextHeadingSlurpLineEnding = true;
    }

    private void onexitsetextheadinglinesequence(MdastContext context, Token token) {
        var node = (MdAstHeading) (stack.get(stack.size() - 1));

        node.depth =
                this.sliceSerialize(token).charAt(0) == Codes.equalsTo ? 1 : 2;
    }

    private void onexitsetextheading() {
        setextHeadingSlurpLineEnding = false;
    }

    private void onenterdata(MdastContext context, Token token) {

        var parent = (MdAstParent<?>) this.stack.get(stack.size() - 1);

        MdAstNode tail = null;
        if (!parent.children().isEmpty()) {
            tail = (MdAstNode) parent.children().get(parent.children().size() - 1);
        }

        if (tail == null || !tail.type().equals("text")) {
            // Add a new text node.
            tail = text();
            // @ts-expect-error: we’ll add `end` later.
            tail.position = new MdAstPosition().withStart(token.start);
            // @ts-expect-error: Assume `parent` accepts `text`.
            parent.addChild(tail);
        }

        this.stack.add(tail);
    }

    private void onexitdata(MdastContext context, Token token) {
        var tail = ListUtils.pop(stack);
        Assert.check(tail != null, "expected a `node` to be on the stack");
        Assert.check(tail.position != null, "expected `node` to have an open position");
        if (!(tail instanceof MdAstLiteral literal)) {
            throw new IllegalStateException("expected a `literal` to be on the stack");
        }
        literal.value += this.sliceSerialize(token);
        literal.position.end = point(token.end);
    }

    private void onexitlineending(MdastContext ignored, Token token) {
        var context = stack.get(stack.size() - 1);
        Assert.check(context != null, "expected `node`");

        // If we’re at a hard break, include the line ending in there.
        if (atHardBreak) {
            if (!(context instanceof MdAstParent<?> parent)) {
                throw new IllegalStateException("expected `parent`");
            }
            var tail = (MdAstNode) parent.children().get(parent.children().size() - 1);
            Assert.check(tail.position != null, "expected tail to have a starting position");
            tail.position.end = point(token.end);
            atHardBreak = false;
            return;
        }

        if (
                !setextHeadingSlurpLineEnding &&
                        extension.canContainEols.contains(context.type())
        ) {
            onenterdata(this, token);
            onexitdata(this, token);
        }
    }

    private void onexithardbreak() {
        atHardBreak = true;
    }

    private void onexithtmlflow() {
        var data = this.resume();
        var node = (MdAstHTML) (stack.get(stack.size() - 1));
        node.value = data;
    }

    private void onexithtmltext() {
        var data = this.resume();
        var node = (MdAstHTML) (stack.get(stack.size() - 1));
        node.value = data;
    }

    private void onexitcodetext() {
        var data = this.resume();
        var node = (MdAstInlineCode) (stack.get(stack.size() - 1));
        node.value = data;
    }

    private void onexitlink() {
        if (!(stack.get(stack.size() - 1) instanceof LinkOrLinkReference context)) {
            // This indicates unbalanced tags and will crash later
            return;
        }

        MdAstParent<?> replacement;
        if (inReference) {
            var ref = new MdAstLinkReference();
            ref.referenceType = Objects.requireNonNullElse(referenceType, MdAstReferenceType.SHORTCUT);
            ref.identifier = context.identifier;
            ref.label = context.label;
            replacement = ref;

        } else {
            var link = new MdAstLink();
            link.url = context.url;
            link.title = context.title;
            replacement = link;
        }
        replacement.position = context.position;
        replacement.data = context.data;
        for (var child : context.children()) {
            replacement.addChild((MdAstNode) child);
        }
        ((MdAstParent<?>) stack.get(stack.size() - 2)).replaceChild(context, replacement);
        stack.set(stack.size() - 1, replacement);

        referenceType = null;
    }


    private void onexitimage() {
        var context = stack.get(stack.size() - 1);
        if (!(context instanceof ImageOrImageReference closedImageOrRef)) {
            return;
        }

        MdAstNode replacement;
        if (inReference) {
            var imgRef = new MdAstImageReference();
            imgRef.referenceType = Objects.requireNonNullElse(referenceType, MdAstReferenceType.SHORTCUT);
            imgRef.identifier = closedImageOrRef.identifier;
            imgRef.label = closedImageOrRef.label;
            imgRef.alt = closedImageOrRef.alt;
            replacement = imgRef;
        } else {
            var img = new MdAstImage();
            img.url = closedImageOrRef.url;
            img.title = closedImageOrRef.title;
            img.alt = closedImageOrRef.alt;
            replacement = img;
        }
        replacement.position = context.position;
        replacement.data = context.data;

        ((MdAstParent<?>) stack.get(stack.size() - 2)).replaceChild(context, replacement);
        // TODO: Needs replacement in parent too
        stack.set(stack.size() - 1, replacement);

        referenceType = null;
    }

    private void onexitlabeltext(MdastContext context, Token token) {
        // Search up through the ancestors to find the reference
        // Fixes issues where unclosed tags/constructs are reported as an error here
        // instead of where the tag is then really closed.
        var string = this.sliceSerialize(token);
        for (int i = stack.size() - 2; i >= 0; i--) {
            var ancestor = stack.get(i);

            if (ancestor instanceof LinkOrLinkReference link) {
                link.label = DecodeString.decodeString(string);
                link.identifier = NormalizeIdentifier.normalizeIdentifier(string).toLowerCase();
                return;
            } else if (ancestor instanceof ImageOrImageReference image) {
                image.label = DecodeString.decodeString(string);
                image.identifier = NormalizeIdentifier.normalizeIdentifier(string).toLowerCase();
                return;
            }
        }

        throw new IllegalStateException("Couldn't find reference on the stack to close");
    }

    // While it's undecided whether an image ends up being a reference or not
    public static class ImageOrImageReference extends MdAstImage {
        public String label;
        public String identifier;
    }

    // While it's undecided whether a link ends up being a reference or not
    public static class LinkOrLinkReference extends MdAstLink {
        public String label;
        public String identifier;
    }

    private void onexitlabel() {
        var fragment = stack.get(stack.size() - 1);
        var value = this.resume();
        var node = stack.get(stack.size() - 1);

        // Assume a reference.
        inReference = true;

        if (node instanceof MdAstLink link && fragment instanceof MdAstParent<?> container) {
            for (var child : container.children()) {
                link.addChild((MdAstNode) child);
            }
        } else if (node instanceof MdAstImage image) {
            image.alt = value;
        }
        // The else case will crash later
    }

    private void onexitresourcedestinationstring() {
        var data = this.resume();
        var node = stack.get(stack.size() - 1);
        if (node instanceof MdAstLink link) {
            link.url = data;
        } else if (node instanceof MdAstImage image) {
            image.url = data;
        }

    }

    private void onexitresourcetitlestring() {
        var data = this.resume();
        var node = (stack.get(stack.size() - 1));

        if (node instanceof MdAstLink link) {
            link.title = data;
        } else if (node instanceof MdAstImage image) {
            image.title = data;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void onexitresource() {
        inReference = false;
    }

    private void onenterreference() {
        referenceType = MdAstReferenceType.COLLAPSED;
    }

    private void onexitreferencestring(MdastContext context, Token token) {
        var label = this.resume();
        var node = stack.get(stack.size() - 1);

        if (node instanceof LinkOrLinkReference ref) {
            ref.label = label;
            ref.identifier = NormalizeIdentifier.normalizeIdentifier(
                    this.sliceSerialize(token)
            ).toLowerCase();
        } else if (node instanceof ImageOrImageReference ref) {
            ref.label = label;
            ref.identifier = NormalizeIdentifier.normalizeIdentifier(
                    this.sliceSerialize(token)
            ).toLowerCase();
        } else {
            throw new IllegalStateException("Expected a link or image reference, but found: " + node);
        }
        referenceType = MdAstReferenceType.FULL;
    }

    private void onexitcharacterreferencemarker(MdastContext context, Token token) {
        characterReferenceType = switch (token.type) {
            case "characterReferenceMarkerHexadecimal" -> CharacterReferenceType.characterReferenceMarkerHexadecimal;
            case "characterReferenceMarkerNumeric" -> CharacterReferenceType.characterReferenceMarkerNumeric;
            default -> throw new IllegalStateException();
        };
    }

    private void onexitcharacterreferencevalue(MdastContext context, Token token) {
        var data = this.sliceSerialize(token);
        var type = characterReferenceType;
        String value;

        if (type != null) {
            value = NumericCharacterReference.decodeNumericCharacterReference(
                    data,
                    type == CharacterReferenceType.characterReferenceMarkerNumeric
                            ? Constants.numericBaseDecimal
                            : Constants.numericBaseHexadecimal
            );
            characterReferenceType = null;
        } else {
            // @ts-expect-error `decodeNamedCharacterReference` can return false for
            // invalid named character references, but everything we’ve tokenized is
            // valid.
            value = NamedCharacterEntities.decodeNamedCharacterReference(data);
        }

        var tail = ListUtils.pop(stack);
        Assert.check(tail != null, "expected `node`");
        Assert.check(tail.position != null, "expected `node.position`");
        if (tail instanceof MdAstLiteral literal) {
            literal.value += value;
            literal.position.end = point(token.end);
        } else {
            throw new IllegalStateException("expected `node.value`");
        }

    }

    private void onexitautolinkprotocol(MdastContext context, Token token) {
        onexitdata(this, token);
        var node = (MdAstLink) (stack.get(stack.size() - 1));
        node.url = this.sliceSerialize(token);
    }

    private void onexitautolinkemail(MdastContext context, Token token) {
        onexitdata(this, token);
        var node = (MdAstLink) (stack.get(stack.size() - 1));
        node.url = "mailto:" + this.sliceSerialize(token);
    }

    //
    // Creaters.
    //

    MdAstBlockquote blockQuote() {
        return new MdAstBlockquote();
    }

    MdAstCode codeFlow() {
        return new MdAstCode();
    }

    MdAstInlineCode codeText() {
        return new MdAstInlineCode();
    }

    MdAstDefinition definition() {
        return new MdAstDefinition();
    }

    MdAstEmphasis emphasis() {
        return new MdAstEmphasis();
    }

    MdAstHeading heading() {
        return new MdAstHeading();
    }

    MdAstBreak hardBreak() {
        return new MdAstBreak();
    }

    MdAstHTML html() {
        return new MdAstHTML();
    }

    MdAstImage image() {
        return new ImageOrImageReference();
    }

    MdAstLink link() {
        return new LinkOrLinkReference();
    }

    MdAstList list(Token token) {
        var list = new MdAstList();
        list.ordered = token.type.equals("listOrdered");
        list.spread = Boolean.TRUE.equals(token.get(SPREAD));
        return list;
    }

    MdAstListItem listItem(Token token) {
        var item = new MdAstListItem();
        item.spread = Boolean.TRUE.equals(token.get(SPREAD));
        return item;
    }

    MdAstParagraph paragraph() {
        return new MdAstParagraph();
    }

    MdAstStrong strong() {
        return new MdAstStrong();
    }

    MdAstText text() {
        return new MdAstText();
    }

    MdAstThematicBreak thematicBreak() {
        return new MdAstThematicBreak();
    }

    private void defaultOnError(MdastContext context, @Nullable Token left, Token right) {
        if (left != null) {
            throw new RuntimeException(
                    "Cannot close `" +
                            left.type +
                            "` (" +
                            MdAstPosition.stringify(left.start, left.end) +
                            "): a different token (`" +
                            right.type +
                            "`, " +
                            MdAstPosition.stringify(right.start, right.end) +
                            ") is open"
            );
        } else {
            throw new RuntimeException(
                    "Cannot close document, a token (`" +
                            right.type +
                            "`, " +
                            MdAstPosition.stringify(right.start, right.end) +
                            ") is still open"
            );
        }
    }

    static class Fragment extends MdAstParent<MdAstPhrasingContent> {
        public Fragment() {
            super("fragment");
        }

        @Override
        protected Class<MdAstPhrasingContent> childClass() {
            return MdAstPhrasingContent.class;
        }
    }

}
