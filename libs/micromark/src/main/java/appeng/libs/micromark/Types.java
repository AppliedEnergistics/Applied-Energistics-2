package appeng.libs.micromark;

/**
 * Here is the list of all types of tokens exposed by micromark, with a short
 * explanation of what they include and where they are found.
 * In picking names, generally, the rule is to be as explicit as possible
 * instead of reusing names.
 * For example, there is a `definitionDestination` and a `resourceDestination`,
 * instead of one shared name.
 */
public final class Types {
    private Types() {
    }

    // Generic type for data, such as in a title, a destination, etc.
    public static final String data = "data";

    // Generic type for syntactic whitespace (tabs, virtual spaces, spaces).
    // Such as, between a fenced code fence and an info string.
    public static final String whitespace = "whitespace";

    // Generic type for line endings (line feed, carriage return, carriage return +
    // line feed).
    public static final String lineEnding = "lineEnding";

    // A line ending, but ending a blank line.
    public static final String lineEndingBlank = "lineEndingBlank";

    // Generic type for whitespace (tabs, virtual spaces, spaces) at the start of a
    // line.
    public static final String linePrefix = "linePrefix";

    // Generic type for whitespace (tabs, virtual spaces, spaces) at the end of a
    // line.
    public static final String lineSuffix = "lineSuffix";

    // Whole ATX heading:
    //
    // ```markdown
    // #
    // ## Alpha
    // ### Bravo ###
    // ```
    //
    // Includes `atxHeadingSequence`, `whitespace`, `atxHeadingText`.
    public static final String atxHeading = "atxHeading";

    // Sequence of number signs in an ATX heading (`###`).
    public static final String atxHeadingSequence = "atxHeadingSequence";

    // Content in an ATX heading (`alpha`).
    // Includes text.
    public static final String atxHeadingText = "atxHeadingText";

    // Whole autolink (`<https://example.com>` or `<admin@example.com>`)
    // Includes `autolinkMarker` and `autolinkProtocol` or `autolinkEmail`.
    public static final String autolink = "autolink";

    // Email autolink w/o markers (`admin@example.com`)
    public static final String autolinkEmail = "autolinkEmail";

    // Marker around an `autolinkProtocol` or `autolinkEmail` (`<` or `>`).
    public static final String autolinkMarker = "autolinkMarker";

    // Protocol autolink w/o markers (`https://example.com`)
    public static final String autolinkProtocol = "autolinkProtocol";

    // A whole character escape (`\-`).
    // Includes `escapeMarker` and `characterEscapeValue`.
    public static final String characterEscape = "characterEscape";

    // The escaped character (`-`).
    public static final String characterEscapeValue = "characterEscapeValue";

    // A whole character reference (`&amp;`, `&#8800;`, or `&#x1D306;`).
    // Includes `characterReferenceMarker`, an optional
    // `characterReferenceMarkerNumeric`, in which case an optional
    // `characterReferenceMarkerHexadecimal`, and a `characterReferenceValue`.
    public static final String characterReference = "characterReference";

    // The start or end marker (`&` or `;`).
    public static final String characterReferenceMarker = "characterReferenceMarker";

    // Mark reference as numeric (`#`).
    public static final String characterReferenceMarkerNumeric = "characterReferenceMarkerNumeric";

    // Mark reference as numeric (`x` or `X`).
    public static final String characterReferenceMarkerHexadecimal = "characterReferenceMarkerHexadecimal";

    // Value of character reference w/o markers (`amp`, `8800`, or `1D306`).
    public static final String characterReferenceValue = "characterReferenceValue";

    // Whole fenced code:
    //
    // ````markdown
    // ```js
    // alert(1)
    // ```
    // ````
    public static final String codeFenced = "codeFenced";

    // A fenced code fence, including whitespace, sequence, info, and meta
    // (` ```js `).
    public static final String codeFencedFence = "codeFencedFence";

    // Sequence of grave accent or tilde characters (` ``` `) in a fence.
    public static final String codeFencedFenceSequence = "codeFencedFenceSequence";

    // Info word (`js`) in a fence.
    // Includes string.
    public static final String codeFencedFenceInfo = "codeFencedFenceInfo";

    // Meta words (`highlight="1"`) in a fence.
    // Includes string.
    public static final String codeFencedFenceMeta = "codeFencedFenceMeta";

    // A line of code.
    public static final String codeFlowValue = "codeFlowValue";

    // Whole indented code:
    //
    // ```markdown
    //     alert(1)
    // ```
    //
    // Includes `lineEnding`, `linePrefix`, and `codeFlowValue`.
    public static final String codeIndented = "codeIndented";

    // A text code (``` `alpha` ```).
    // Includes `codeTextSequence`, `codeTextData`, `lineEnding`, and can include
    // `codeTextPadding`.
    public static final String codeText = "codeText";

    public static final String codeTextData = "codeTextData";

    // A space or line ending right after or before a tick.
    public static final String codeTextPadding = "codeTextPadding";

    // A text code fence (` `` `).
    public static final String codeTextSequence = "codeTextSequence";

    // Whole content:
    //
    // ```markdown
    // [a] = b
    // c
    // =
    // d
    // ```
    //
    // Includes `paragraph` and `definition`.
    public static final String content = "content";
    // Whole definition:
    //
    // ```markdown
    // [micromark] = https://github.com/micromark/micromark
    // ```
    //
    // Includes `definitionLabel`, `definitionMarker`, `whitespace`,
    // `definitionDestination`, and optionally `lineEnding` and `definitionTitle`.
    public static final String definition = "definition";

    // Destination of a definition (`https://github.com/micromark/micromark` or
    // `<https://github.com/micromark/micromark>`).
    // Includes `definitionDestinationLiteral` or `definitionDestinationRaw`.
    public static final String definitionDestination = "definitionDestination";

    // Enclosed destination of a definition
    // (`<https://github.com/micromark/micromark>`).
    // Includes `definitionDestinationLiteralMarker` and optionally
    // `definitionDestinationString`.
    public static final String definitionDestinationLiteral = "definitionDestinationLiteral";

    // Markers of an enclosed definition destination (`<` or `>`).
    public static final String definitionDestinationLiteralMarker = "definitionDestinationLiteralMarker";

    // Unenclosed destination of a definition
    // (`https://github.com/micromark/micromark`).
    // Includes `definitionDestinationString`.
    public static final String definitionDestinationRaw = "definitionDestinationRaw";

    // Text in an destination (`https://github.com/micromark/micromark`).
    // Includes string.
    public static final String definitionDestinationString = "definitionDestinationString";

    // Label of a definition (`[micromark]`).
    // Includes `definitionLabelMarker` and `definitionLabelString`.
    public static final String definitionLabel = "definitionLabel";

    // Markers of a definition label (`[` or `]`).
    public static final String definitionLabelMarker = "definitionLabelMarker";

    // Value of a definition label (`micromark`).
    // Includes string.
    public static final String definitionLabelString = "definitionLabelString";

    // Marker between a label and a destination (`:`).
    public static final String definitionMarker = "definitionMarker";

    // Title of a definition (`"x"`, `"y"`, or `(z)`).
    // Includes `definitionTitleMarker` and optionally `definitionTitleString`.
    public static final String definitionTitle = "definitionTitle";

    // Marker around a title of a definition (`"`, `"`, `(`, or `)`).
    public static final String definitionTitleMarker = "definitionTitleMarker";

    // Data without markers in a title (`z`).
    // Includes string.
    public static final String definitionTitleString = "definitionTitleString";

    // Emphasis (`*alpha*`).
    // Includes `emphasisSequence` and `emphasisText`.
    public static final String emphasis = "emphasis";

    // Sequence of emphasis markers (`*` or `_`).
    public static final String emphasisSequence = "emphasisSequence";

    // Emphasis text (`alpha`).
    // Includes text.
    public static final String emphasisText = "emphasisText";

    // The character escape marker (`\`).
    public static final String escapeMarker = "escapeMarker";

    // A hard break created with a backslash (`\\n`).
    // Note = does not include the line ending.
    public static final String hardBreakEscape = "hardBreakEscape";

    // A hard break created with trailing spaces (`  \n`).
    // Does not include the line ending.
    public static final String hardBreakTrailing = "hardBreakTrailing";

    // Flow HTML:
    //
    // ```markdown
    // <div
    // ```
    //
    // Inlcudes `lineEnding`, `htmlFlowData`.
    public static final String htmlFlow = "htmlFlow";

    public static final String htmlFlowData = "htmlFlowData";

    // HTML in text (the tag in `a <i> b`).
    // Includes `lineEnding`, `htmlTextData`.
    public static final String htmlText = "htmlText";

    public static final String htmlTextData = "htmlTextData";

    // Whole image (`![alpha](bravo)`, `![alpha][bravo]`, `![alpha][]`, or
    // `![alpha]`).
    // Includes `label` and an optional `resource` or `reference`.
    public static final String image = "image";

    // Whole link label (`[*alpha*]`).
    // Includes `labelLink` or `labelImage`, `labelText`, and `labelEnd`.
    public static final String label = "label";

    // Text in an label (`*alpha*`).
    // Includes text.
    public static final String labelText = "labelText";

    // Start a link label (`[`).
    // Includes a `labelMarker`.
    public static final String labelLink = "labelLink";

    // Start an image label (`![`).
    // Includes `labelImageMarker` and `labelMarker`.
    public static final String labelImage = "labelImage";

    // Marker of a label (`[` or `]`).
    public static final String labelMarker = "labelMarker";

    // Marker to start an image (`!`).
    public static final String labelImageMarker = "labelImageMarker";

    // End a label (`]`).
    // Includes `labelMarker`.
    public static final String labelEnd = "labelEnd";

    // Whole link (`[alpha](bravo)`, `[alpha][bravo]`, `[alpha][]`, or `[alpha]`).
    // Includes `label` and an optional `resource` or `reference`.
    public static final String link = "link";

    // Whole paragraph:
    //
    // ```markdown
    // alpha
    // bravo.
    // ```
    //
    // Includes text.
    public static final String paragraph = "paragraph";

    // A reference (`[alpha]` or `[]`).
    // Includes `referenceMarker` and an optional `referenceString`.
    public static final String reference = "reference";

    // A reference marker (`[` or `]`).
    public static final String referenceMarker = "referenceMarker";

    // Reference text (`alpha`).
    // Includes string.
    public static final String referenceString = "referenceString";

    // A resource (`(https://example.com "alpha")`).
    // Includes `resourceMarker`, an optional `resourceDestination` with an optional
    // `whitespace` and `resourceTitle`.
    public static final String resource = "resource";

    // A resource destination (`https://example.com`).
    // Includes `resourceDestinationLiteral` or `resourceDestinationRaw`.
    public static final String resourceDestination = "resourceDestination";

    // A literal resource destination (`<https://example.com>`).
    // Includes `resourceDestinationLiteralMarker` and optionally
    // `resourceDestinationString`.
    public static final String resourceDestinationLiteral = "resourceDestinationLiteral";

    // A resource destination marker (`<` or `>`).
    public static final String resourceDestinationLiteralMarker = "resourceDestinationLiteralMarker";

    // A raw resource destination (`https://example.com`).
    // Includes `resourceDestinationString`.
    public static final String resourceDestinationRaw = "resourceDestinationRaw";

    // Resource destination text (`https://example.com`).
    // Includes string.
    public static final String resourceDestinationString = "resourceDestinationString";

    // A resource marker (`(` or `)`).
    public static final String resourceMarker = "resourceMarker";

    // A resource title (`"alpha"`, `"alpha"`, or `(alpha)`).
    // Includes `resourceTitleMarker` and optionally `resourceTitleString`.
    public static final String resourceTitle = "resourceTitle";

    // A resource title marker (`"`, `"`, `(`, or `)`).
    public static final String resourceTitleMarker = "resourceTitleMarker";

    // Resource destination title (`alpha`).
    // Includes string.
    public static final String resourceTitleString = "resourceTitleString";

    // Whole setext heading:
    //
    // ```markdown
    // alpha
    // bravo
    // =====
    // ```
    //
    // Includes `setextHeadingText`, `lineEnding`, `linePrefix`, and
    // `setextHeadingLine`.
    public static final String setextHeading = "setextHeading";

    // Content in a setext heading (`alpha\nbravo`).
    // Includes text.
    public static final String setextHeadingText = "setextHeadingText";

    // Underline in a setext heading, including whitespace suffix (`==`).
    // Includes `setextHeadingLineSequence`.
    public static final String setextHeadingLine = "setextHeadingLine";

    // Sequence of equals or dash characters in underline in a setext heading (`-`).
    public static final String setextHeadingLineSequence = "setextHeadingLineSequence";

    // Strong (`**alpha**`).
    // Includes `strongSequence` and `strongText`.
    public static final String strong = "strong";

    // Sequence of strong markers (`**` or `__`).
    public static final String strongSequence = "strongSequence";

    // Strong text (`alpha`).
    // Includes text.
    public static final String strongText = "strongText";

    // Whole thematic break:
    //
    // ```markdown
    // * * *
    // ```
    //
    // Includes `thematicBreakSequence` and `whitespace`.
    public static final String thematicBreak = "thematicBreak";

    // A sequence of one or more thematic break markers (`***`).
    public static final String thematicBreakSequence = "thematicBreakSequence";

    // Whole block quote:
    //
    // ```markdown
    // > a
    // >
    // > b
    // ```
    //
    // Includes `blockQuotePrefix` and flow.
    public static final String blockQuote = "blockQuote";
    // The `>` or `> ` of a block quote.
    public static final String blockQuotePrefix = "blockQuotePrefix";
    // The `>` of a block quote prefix.
    public static final String blockQuoteMarker = "blockQuoteMarker";
    // The optional ` ` of a block quote prefix.
    public static final String blockQuotePrefixWhitespace = "blockQuotePrefixWhitespace";

    // Whole unordered list:
    //
    // ```markdown
    // - a
    //   b
    // ```
    //
    // Includes `listItemPrefix`, flow, and optionally  `listItemIndent` on further
    // lines.
    public static final String listOrdered = "listOrdered";

    // Whole ordered list:
    //
    // ```markdown
    // 1. a
    //    b
    // ```
    //
    // Includes `listItemPrefix`, flow, and optionally  `listItemIndent` on further
    // lines.
    public static final String listUnordered = "listUnordered";

    // The indent of further list item lines.
    public static final String listItemIndent = "listItemIndent";

    // A marker, as in, `*`, `+`, `-`, `.`, or `)`.
    public static final String listItemMarker = "listItemMarker";

    // The thing that starts a list item, such as `1. `.
    // Includes `listItemValue` if ordered, `listItemMarker`, and
    // `listItemPrefixWhitespace` (unless followed by a line ending).
    public static final String listItemPrefix = "listItemPrefix";

    // The whitespace after a marker.
    public static final String listItemPrefixWhitespace = "listItemPrefixWhitespace";

    // The numerical value of an ordered item.
    public static final String listItemValue = "listItemValue";

    // Internal types used for subtokenizers, compiled away
    public static final String chunkDocument = "chunkDocument";
    public static final String chunkContent = "chunkContent";
    public static final String chunkFlow = "chunkFlow";
    public static final String chunkText = "chunkText";
    public static final String chunkString = "chunkString";

}
