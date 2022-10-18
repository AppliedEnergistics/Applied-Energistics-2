package appeng.libs.micromark.html;

import appeng.libs.micromark.ChunkUtils;
import appeng.libs.micromark.NamedCharacterEntities;
import appeng.libs.micromark.NormalizeIdentifier;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * While micromark is a lexer/tokenizer, the common case of going from markdown
 * to html is currently built in as this module, even though the parts can be
 * used separately to build ASTs, CSTs, or many other output formats.
 * <p>
 * Having an HTML compiler built in is useful because it allows us to check for
 * compliancy to CommonMark, the de facto norm of markdown, specified in roughly
 * 600 input/output cases.
 * <p>
 * This module has an interface that accepts lists of events instead of the
 * whole at once, however, because markdown can’t be truly streaming, we buffer
 * events before processing and outputting the final result.
 */
public class HtmlCompiler {

    /**
     * These two are allowlists of safe protocols for full URLs in respectively the
     * `href` (on `<a>`) and `src` (on `<img>`) attributes.
     * They are based on what is allowed on GitHub,
     * <https://github.com/syntax-tree/hast-util-sanitize/blob/9275b21/lib/github.json#L31>
     */
    private static final Pattern protocolHref = Pattern.compile("^(https?|ircs?|mailto|xmpp)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern protocolSrc = Pattern.compile("^https?$", Pattern.CASE_INSENSITIVE);
    private final CompileOptions options;

    private static final class Media {
        boolean image;
        String labelId;
        String label;
        String referenceId;
        String destination;
        String title;
    }

    /**
     * Tags is needed because according to markdown, links and emphasis and
     * whatnot can exist in images, however, as HTML doesn’t allow content in
     * images, the tags are ignored in the `alt` attribute, but the content
     * remains.
     */
    private boolean tags = true;

    /**
     * An object to track identifiers to media (URLs and titles) defined with
     * definitions.
     */
    private final Map<String, Media> definitions = new HashMap<>();

    /**
     * A lot of the handlers need to capture some of the output data, modify it
     * somehow, and then deal with it.
     * We do that by tracking a stack of buffers, that can be opened (with
     * `buffer`) and closed (with `resume`) to access them.
     */
    private final List<List<String>> buffers = new ArrayList<>(List.of(new ArrayList<>()));

    /**
     * As we can have links in images and the other way around, where the deepest
     * ones are closed first, we need to track which one we’re in.
     */
    private final List<Media> mediaStack = new ArrayList<>();

    /**
     * Same as `mediaStack` for tightness, which is specific to lists.
     * We need to track if we’re currently in a tight or loose container.
     */
    private final List<Boolean> tightStack = new ArrayList<Boolean>();

    private final HtmlExtension defaultHandlers = HtmlExtension.builder()
            .enter("blockQuote", this::onenterblockquote)
            .enter("codeFenced", this::onentercodefenced)
            .enter("codeFencedFenceInfo", this::buffer)
            .enter("codeFencedFenceMeta", this::buffer)
            .enter("codeIndented", this::onentercodeindented)
            .enter("codeText", this::onentercodetext)
            .enter("content", this::onentercontent)
            .enter("definition", this::onenterdefinition)
            .enter("definitionDestinationString", this::onenterdefinitiondestinationstring)
            .enter("definitionLabelString", this::buffer)
            .enter("definitionTitleString", this::buffer)
            .enter("emphasis", this::onenteremphasis)
            .enter("htmlFlow", this::onenterhtmlflow)
            .enter("htmlText", this::onenterhtml)
            .enter("image", this::onenterimage)
            .enter("label", this::buffer)
            .enter("link", this::onenterlink)
            .enter("listItemMarker", this::onenterlistitemmarker)
            .enter("listItemValue", this::onenterlistitemvalue)
            .enter("listOrdered", this::onenterlistordered)
            .enter("listUnordered", this::onenterlistunordered)
            .enter("paragraph", this::onenterparagraph)
            .enter("reference", this::buffer)
            .enter("resource", this::onenterresource)
            .enter("resourceDestinationString", this::onenterresourcedestinationstring)
            .enter("resourceTitleString", this::buffer)
            .enter("setextHeading", this::onentersetextheading)
            .enter("strong", this::onenterstrong)
            .exit("atxHeading", this::onexitatxheading)
            .exit("atxHeadingSequence", this::onexitatxheadingsequence)
            .exit("autolinkEmail", this::onexitautolinkemail)
            .exit("autolinkProtocol", this::onexitautolinkprotocol)
            .exit("blockQuote", this::onexitblockquote)
            .exit("characterEscapeValue", this::onexitdata)
            .exit("characterReferenceMarkerHexadecimal", this::onexitcharacterreferencemarker)
            .exit("characterReferenceMarkerNumeric", this::onexitcharacterreferencemarker)
            .exit("characterReferenceValue", this::onexitcharacterreferencevalue)
            .exit("codeFenced", this::onexitflowcode)
            .exit("codeFencedFence", this::onexitcodefencedfence)
            .exit("codeFencedFenceInfo", this::onexitcodefencedfenceinfo)
            .exit("codeFencedFenceMeta", this::resume)
            .exit("codeFlowValue", this::onexitcodeflowvalue)
            .exit("codeIndented", this::onexitflowcode)
            .exit("codeText", this::onexitcodetext)
            .exit("codeTextData", this::onexitdata)
            .exit("data", this::onexitdata)
            .exit("definition", this::onexitdefinition)
            .exit("definitionDestinationString", this::onexitdefinitiondestinationstring)
            .exit("definitionLabelString", this::onexitdefinitionlabelstring)
            .exit("definitionTitleString", this::onexitdefinitiontitlestring)
            .exit("emphasis", this::onexitemphasis)
            .exit("hardBreakEscape", this::onexithardbreak)
            .exit("hardBreakTrailing", this::onexithardbreak)
            .exit("htmlFlow", this::onexithtml)
            .exit("htmlFlowData", this::onexitdata)
            .exit("htmlText", this::onexithtml)
            .exit("htmlTextData", this::onexitdata)
            .exit("image", this::onexitmedia)
            .exit("label", this::onexitlabel)
            .exit("labelText", this::onexitlabeltext)
            .exit("lineEnding", this::onexitlineending)
            .exit("link", this::onexitmedia)
            .exit("listOrdered", this::onexitlistordered)
            .exit("listUnordered", this::onexitlistunordered)
            .exit("paragraph", this::onexitparagraph)
            .exit("reference", this::resume)
            .exit("referenceString", this::onexitreferencestring)
            .exit("resource", this::resume)
            .exit("resourceDestinationString", this::onexitresourcedestinationstring)
            .exit("resourceTitleString", this::onexitresourcetitlestring)
            .exit("setextHeading", this::onexitsetextheading)
            .exit("setextHeadingLineSequence", this::onexitsetextheadinglinesequence)
            .exit("setextHeadingText", this::onexitsetextheadingtext)
            .exit("strong", this::onexitstrong)
            .exit("thematicBreak", this::onexitthematicbreak)
            .build();

    /**
     * Combine the HTML extensions with the default handlers.
     * An HTML extension is an object whose fields are either `enter` or `exit`
     * (reflecting whether a token is entered or exited).
     * The values at such objects are names of tokens mapping to handlers.
     * Handlers are called, respectively when a token is opener or closed, with
     * that token, and a context as `this`.
     */
    HtmlExtension handlers;

    /**
     * Handlers do often need to keep track of some state.
     * That state is provided here as a key-value store (an object).
     *
     * @type {CompileData}
     */
    CompileData data = new CompileData();

    static class CompileData {
        public boolean lastWasTag;
        public boolean expectFirstItem;
        public boolean slurpOneLineEnding;
        public boolean slurpAllLineEndings;
        public boolean fencedCodeInside;
        public Integer fencesCount;
        public boolean flowCodeSeenData;
        public boolean ignoreEncode;
        public int headingRank;
        public boolean inCodeText;
        public String characterReferenceType;
    }

    /**
     * Generally, micromark copies line endings (`"\r"`, `"\n"`, `"\r\n"`) in the
     * markdown document over to the compiled HTML.
     * In some cases, such as `> a`, CommonMark requires that extra line endings
     * are added: `<blockquote>\n<p>a</p>\n</blockquote>`.
     * This variable hold the default line ending when given (or `undefined`),
     * and in the latter case will be updated to the first found line ending if
     * there is one.
     */
    String lineEndingStyle;

    public HtmlCompiler(CompileOptions options) {
        this.options = options;

        if (options.getExtensions().isEmpty()) {
            this.handlers = defaultHandlers;
        } else {
            var builder = HtmlExtension.builder()
                    .addAll(defaultHandlers)
                    .enterDocument(defaultHandlers.enterDocument)
                    .exitDocument(defaultHandlers.exitDocument);
            for (var extension : options.getExtensions()) {
                builder.addAll(extension);
                if (extension.enterDocument != null) {
                    builder.enterDocument(extension.enterDocument);
                }
                if (extension.exitDocument != null) {
                    builder.exitDocument(extension.exitDocument);
                }
            }
            this.handlers = builder.build();
        }

        lineEndingStyle = options.getDefaultLineEnding();
    }

    /**
     * Deal w/ a slice of events.
     * Return either the empty string if there’s nothing of note to return, or the
     * result when done.
     */
    public String compile(List<Tokenizer.Event> events) {
        int index = -1;
        int start = 0;
        List<Integer> listStack = new ArrayList<>();
        // As definitions can come after references, we need to figure out the media
        // (urls and titles) defined by them before handling the references.
        // So, we do sort of what HTML does: put metadata at the start (in head), and
        // then put content after (`body`).
        List<Tokenizer.Event> head = new ArrayList<>();
        List<Tokenizer.Event> body = new ArrayList<>();
        while (++index < events.size()) {
            var event = events.get(index);
            var token = event.token();

            // Figure out the line ending style used in the document.
            if (lineEndingStyle == null && (token.type.equals("lineEnding") || token.type.equals("lineEndingBlank"))) {
                // @ts-expect-error Hush, it’s a line ending.
                lineEndingStyle = event.context().sliceSerialize(token);
            }

            // Preprocess lists to infer whether the list is loose or not.
            if (token.type.equals("listOrdered") || token.type.equals("listUnordered")) {
                if (event.isEnter()) {
                    listStack.add(index);
                } else {
                    prepareList(new ArrayList<>(events.subList(listStack.remove(listStack.size() - 1), index)));
                }
            }

            // Move definitions to the front.
            if (token.type.equals("definition")) {
                if (event.isEnter()) {
                    body = ChunkUtils.push(body, new ArrayList<>(events.subList(start, index)));
                    start = index;
                } else {
                    head = ChunkUtils.push(head, new ArrayList<>(events.subList(start, index + 1)));
                    start = index + 1;
                }
            }
        }
        head = ChunkUtils.push(head, body);
        head = ChunkUtils.push(head, new ArrayList<>(events.subList(start, events.size())));
        var result = head;

        var context = new Context();

        // Handle the start of the document, if defined.
        if (handlers.enterDocument != null) {
            handlers.enterDocument.handle(context);
        }

        // Handle all events.
        for (var event : result) {
            var token = event.token();

            var typeHandlers = event.isEnter() ? handlers.enter : handlers.exit;
            var handler = typeHandlers.get(token.type);
            if (handler != null) {
                context.event = event;
                handler.handle(context, token);
                context.event = null;
            }
        }

        // Handle the end of the document, if defined.
        if (handlers.exitDocument != null) {
            handlers.exitDocument.handle(context);
        }
        return String.join("", buffers.get(0));
    }

    class Context implements CompileContext {
        Tokenizer.Event event;

        @Override
        public CompileOptions getOptions() {
            return options;
        }

        @Override
        public void setData(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getData(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lineEndingIfNeeded() {
            HtmlCompiler.this.lineEndingIfNeeded();
        }

        @Override
        public String encode(String value) {
            return HtmlCompiler.this.encode(value);
        }

        @Override
        public void buffer() {
            HtmlCompiler.this.buffer();
        }

        @Override
        public String resume() {
            return HtmlCompiler.this.resume();
        }

        @Override
        public void raw(String value) {
            HtmlCompiler.this.raw(value);
        }

        @Override
        public void tag(String value) {
            HtmlCompiler.this.tag(value);
        }

        @Override
        public String sliceSerialize(Token token) {
            return event.context().sliceSerialize(token);
        }
    }

    /**
     * Figure out whether lists are loose or not.
     */
    private void prepareList(List<Tokenizer.Event> slice) {
        var length = slice.size();
        var index = 0; // Skip open.
        var containerBalance = 0;
        var loose = false;
        boolean atMarker = false;
        while (++index < length) {
            var event = slice.get(index);
            var token = event.token();
            if (token._container) {
                atMarker = false;
                if (event.isEnter()) {
                    containerBalance++;
                } else {
                    containerBalance--;
                }
            } else switch (token.type) {
                case "listItemPrefix": {
                    if (event.isExit()) {
                        atMarker = true;
                    }
                    break;
                }
                case "linePrefix": {
                    // Ignore

                    break;
                }
                case "lineEndingBlank": {
                    if (event.isEnter() && containerBalance == 0) {
                        if (atMarker) {
                            atMarker = false;
                        } else {
                            loose = true;
                        }
                    }
                    break;
                }
                default: {
                    atMarker = false;
                }
            }
        }
        slice.get(0).token()._loose = loose;
    }

    private void buffer() {
        buffers.add(new ArrayList<>());
    }

    private String resume() {
        var buf = buffers.remove(buffers.size() - 1);
        return String.join("", buf);
    }

    private void tag(String value) {
        if (!tags) return;
        data.lastWasTag = true;
        buffers.get(buffers.size() - 1).add(value);
    }

    private void raw(String value) {
        data.lastWasTag = false;
        buffers.get(buffers.size() - 1).add(value);
    }

    private void lineEnding() {
        raw(Objects.requireNonNullElse(lineEndingStyle, "\n"));
    }

    private void lineEndingIfNeeded() {
        var buffer = buffers.get(buffers.size() - 1);
        var slice = !buffer.isEmpty() ? buffer.get(buffer.size() - 1) : null;
        var previous = slice != null ? slice.charAt(slice.length() - 1) : null;
        if (previous == null || previous == 10 || previous == 13) {
            return;
        }
        lineEnding();
    }

    private String encode(String value) {
        return data.ignoreEncode ? value : HtmlEncode.encode(value);
    }

    //
    // Handlers.
    //

    private void onenterlistordered(CompileContext context, Token token) {
        tightStack.add(!token._loose);
        lineEndingIfNeeded();
        tag("<ol");
        data.expectFirstItem = true;
    }

    private void onenterlistunordered(CompileContext context, Token token) {
        tightStack.add(!token._loose);
        lineEndingIfNeeded();
        tag("<ul");
        data.expectFirstItem = true;
    }

    private void onenterlistitemvalue(CompileContext context, Token token) {
        if (data.expectFirstItem) {
            var value = Integer.parseInt(context.sliceSerialize(token), 10);
            if (value != 1) {
                tag(" start=\"" + encode(String.valueOf(value)) + "\"");
            }
        }
    }

    private void onenterlistitemmarker() {
        if (data.expectFirstItem) {
            tag(">");
        } else {
            onexitlistitem();
        }
        lineEndingIfNeeded();
        tag("<li>");
        data.expectFirstItem = false;
        // “Hack” to prevent a line ending from showing up if the item is empty.
        data.lastWasTag = false;
    }

    private void onexitlistordered() {
        onexitlistitem();
        tightStack.remove(tightStack.size() - 1);
        lineEnding();
        tag("</ol>");
    }

    private void onexitlistunordered() {
        onexitlistitem();
        tightStack.remove(tightStack.size() - 1);
        lineEnding();
        tag("</ul>");
    }

    private void onexitlistitem() {
        if (data.lastWasTag && !data.slurpAllLineEndings) {
            lineEndingIfNeeded();
        }
        tag("</li>");
        data.slurpAllLineEndings = false;
    }

    private void onenterblockquote() {
        tightStack.add(false);
        lineEndingIfNeeded();
        tag("<blockquote>");
    }

    private void onexitblockquote() {
        tightStack.remove(tightStack.size() - 1);
        lineEndingIfNeeded();
        tag("</blockquote>");
        data.slurpAllLineEndings = false;
    }

    private void onenterparagraph() {
        if (tightStack.isEmpty() || !tightStack.get(tightStack.size() - 1)) {
            lineEndingIfNeeded();
            tag("<p>");
        }
        data.slurpAllLineEndings = false;
    }

    private void onexitparagraph() {
        if (!tightStack.isEmpty() && tightStack.get(tightStack.size() - 1)) {
            data.slurpAllLineEndings = true;
        } else {
            tag("</p>");
        }
    }

    private void onentercodefenced() {
        lineEndingIfNeeded();
        tag("<pre><code");
        data.fencesCount = 0;
    }

    private void onexitcodefencedfenceinfo() {
        var value = resume();
        tag(" class=\"language-" + value + "\"");
    }

    private void onexitcodefencedfence() {
        int count = Objects.requireNonNullElse(data.fencesCount, 0);
        if (count == 0) {
            tag(">");
            data.slurpOneLineEnding = true;
        }
        data.fencesCount = count + 1;
    }

    private void onentercodeindented() {
        lineEndingIfNeeded();
        tag("<pre><code>");
    }

    private void onexitflowcode() {
        var count = data.fencesCount;

        // One special case is if we are inside a container, and the fenced code was
        // not closed (meaning it runs to the end).
        // In that case, the following line ending, is considered *outside* the
        // fenced code and block quote by micromark, but CM wants to treat that
        // ending as part of the code.
        if (count != null && count < 2 &&
                !tightStack.isEmpty() && !data.lastWasTag) {
            lineEnding();
        }

        // But in most cases, it’s simpler: when we’ve seen some data, emit an extra
        // line ending when needed.
        if (data.flowCodeSeenData) {
            lineEndingIfNeeded();
        }
        tag("</code></pre>");
        if (count != null && count < 2) lineEndingIfNeeded();
        data.flowCodeSeenData = false;
        data.fencesCount = null;
        data.slurpOneLineEnding = false;
    }

    private void onenterimage() {
        var media = new Media();
        media.image = true;
        mediaStack.add(media);
        tags = false; // Disallow tags.
    }

    private void onenterlink() {
        mediaStack.add(new Media());
    }

    private void onexitlabeltext(CompileContext context, Token token) {
        mediaStack.get(mediaStack.size() - 1).labelId = context.sliceSerialize(token);
    }

    private void onexitlabel() {
        mediaStack.get(mediaStack.size() - 1).label = resume();
    }

    private void onexitreferencestring(CompileContext context, Token token) {
        mediaStack.get(mediaStack.size() - 1).referenceId = context.sliceSerialize(token);
    }

    private void onenterresource() {
        buffer(); // We can have line endings in the resource, ignore them.
        mediaStack.get(mediaStack.size() - 1).destination = "";
    }

    private void onenterresourcedestinationstring() {
        buffer();
        // Ignore encoding the result, as we’ll first percent encode the url and
        // encode manually after.
        data.ignoreEncode = true;
    }

    private void onexitresourcedestinationstring() {
        mediaStack.get(mediaStack.size() - 1).destination = resume();
        data.ignoreEncode = false;
    }

    private void onexitresourcetitlestring() {
        mediaStack.get(mediaStack.size() - 1).title = resume();
    }

    private void onexitmedia() {
        var index = mediaStack.size() - 1; // Skip current.
        var media = mediaStack.get(index);
        var id = Objects.requireNonNullElse(media.referenceId, media.labelId);
        var context = media.destination == null ? definitions.get(NormalizeIdentifier.normalizeIdentifier(id)) : media;
        tags = true;
        while (index-- > 0) {
            if (mediaStack.get(index).image) {
                tags = false;
                break;
            }
        }
        if (media.image) {
            tag("<img src=\"" + SanitizeUri.sanitizeUri(context.destination, options.isAllowDangerousProtocol() ? null : protocolSrc) + "\" alt=\"");
            raw(media.label);
            tag("\"");
        } else {
            tag("<a href=\"" + SanitizeUri.sanitizeUri(context.destination, options.isAllowDangerousProtocol() ? null : protocolHref) + "\"");
        }
        tag(context.title != null ? " title=\"" + context.title + "\"" : "");
        if (media.image) {
            tag(" />");
        } else {
            tag(">");
            raw(media.label);
            tag("</a>");
        }
        mediaStack.remove(mediaStack.size() - 1);
    }

    private void onenterdefinition() {
        buffer();
        mediaStack.add(new Media());
    }

    private void onexitdefinitionlabelstring(CompileContext context, Token token) {
        // Discard label, use the source content instead.
        resume();
        mediaStack.get(mediaStack.size() - 1).labelId = context.sliceSerialize(token);
    }

    private void onenterdefinitiondestinationstring() {
        buffer();
        data.ignoreEncode = true;
    }

    private void onexitdefinitiondestinationstring() {
        mediaStack.get(mediaStack.size() - 1).destination = resume();
        data.ignoreEncode = false;
    }

    private void onexitdefinitiontitlestring() {
        mediaStack.get(mediaStack.size() - 1).title = resume();
    }

    private void onexitdefinition() {
        var media = mediaStack.get(mediaStack.size() - 1);
        var id = NormalizeIdentifier.normalizeIdentifier(media.labelId);
        resume();
        if (!definitions.containsKey(id)) {
            definitions.put(id, mediaStack.get(mediaStack.size() - 1));
        }
        mediaStack.remove(mediaStack.size() - 1);
    }

    private void onentercontent() {
        data.slurpAllLineEndings = true;
    }

    private void onexitatxheadingsequence(CompileContext context, Token token) {
        // Exit for further sequences.
        if (data.headingRank != 0) return;
        data.headingRank = context.sliceSerialize(token).length();
        lineEndingIfNeeded();
        tag("<h" + data.headingRank + ">");
    }

    private void onentersetextheading() {
        buffer();
        data.slurpAllLineEndings = false;
    }

    private void onexitsetextheadingtext() {
        data.slurpAllLineEndings = true;
    }

    private void onexitatxheading() {
        tag("</h" + data.headingRank + ">");
        data.headingRank = 0;
    }

    private void onexitsetextheadinglinesequence(CompileContext context, Token token) {
        data.headingRank = context.sliceSerialize(token).charAt(0) == 61 ? 1 : 2;
    }

    private void onexitsetextheading() {
        var value = resume();
        lineEndingIfNeeded();
        tag("<h" + data.headingRank + ">");
        raw(value);
        tag("</h" + data.headingRank + ">");
        data.slurpAllLineEndings = false;
        data.headingRank = 0;
    }

    private void onexitdata(CompileContext context, Token token) {
        raw(encode(context.sliceSerialize(token)));
    }

    private void onexitlineending(CompileContext context, Token token) {
        if (data.slurpAllLineEndings) {
            return;
        }
        if (data.slurpOneLineEnding) {
            data.slurpOneLineEnding = false;
            return;
        }
        if (data.inCodeText) {
            raw(" ");
            return;
        }
        raw(encode(context.sliceSerialize(token)));
    }

    private void onexitcodeflowvalue(CompileContext context, Token token) {
        raw(encode(context.sliceSerialize(token)));
        data.flowCodeSeenData = true;
    }

    private void onexithardbreak() {
        tag("<br />");
    }

    private void onenterhtmlflow() {
        lineEndingIfNeeded();
        onenterhtml();
    }

    private void onexithtml() {
        data.ignoreEncode = false;
    }

    private void onenterhtml() {
        if (options.isAllowDangerousHtml()) {
            data.ignoreEncode = true;
        }
    }

    private void onenteremphasis() {
        tag("<em>");
    }

    private void onenterstrong() {
        tag("<strong>");
    }

    private void onentercodetext() {
        data.inCodeText = true;
        tag("<code>");
    }

    private void onexitcodetext() {
        data.inCodeText = false;
        tag("</code>");
    }

    private void onexitemphasis() {
        tag("</em>");
    }

    private void onexitstrong() {
        tag("</strong>");
    }

    private void onexitthematicbreak() {
        lineEndingIfNeeded();
        tag("<hr />");
    }

    private void onexitcharacterreferencemarker(CompileContext context, Token token) {
        data.characterReferenceType = token.type;
    }

    private void onexitcharacterreferencevalue(CompileContext context, Token token) {
        var value = context.sliceSerialize(token);

        // @ts-expect-error `decodeNamedCharacterReference` can return false for
        // invalid named character references, but everything we’ve tokenized is
        // valid.
        value = data.characterReferenceType != null ? NumericCharacterReference.decodeNumericCharacterReference(value, data.characterReferenceType.equals("characterReferenceMarkerNumeric") ? 10 : 16) : NamedCharacterEntities.decodeNamedCharacterReference(value);
        raw(encode(value));
        data.characterReferenceType = null;
    }

    private void onexitautolinkprotocol(CompileContext context, Token token) {
        var uri = context.sliceSerialize(token);
        tag("<a href=\"" + SanitizeUri.sanitizeUri(uri, options.isAllowDangerousProtocol() ? null : protocolHref) + "\">");
        raw(encode(uri));
        tag("</a>");
    }

    private void onexitautolinkemail(CompileContext context, Token token) {
        var uri = context.sliceSerialize(token);
        tag("<a href=\"" + SanitizeUri.sanitizeUri("mailto:" + uri, null) + "\">");
        raw(encode(uri));
        tag("</a>");
    }

}
