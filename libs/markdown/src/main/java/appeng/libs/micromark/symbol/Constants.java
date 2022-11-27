package appeng.libs.micromark.symbol;

/**
 * This module is compiled away!
 * <p>
 * Parsing markdown comes with a couple of constants, such as minimum or maximum
 * sizes of certain sequences.
 * Additionally, there are a couple symbols used inside micromark.
 * These are all defined here, but compiled away by scripts.
 */
public final class Constants {
    private Constants() {
    }

    public static final int attentionSideBefore = 1; // Symbol to mark an attention sequence as before content: `*a`
    public static final int attentionSideAfter = 2; // Symbol to mark an attention sequence as after content: `a*`
    public static final int atxHeadingOpeningFenceSizeMax = 6; // 6 number signs is fine, 7 isn’t.
    public static final int autolinkDomainSizeMax = 63; // 63 characters is fine, 64 is too many.
    public static final int autolinkSchemeSizeMax = 32; // 32 characters is fine, 33 is too many.
    public static final String cdataOpeningString = "CDATA["; // And preceded by `<![`.
    public static final int characterGroupWhitespace = 1; // Symbol used to indicate a character is whitespace
    public static final int characterGroupPunctuation = 2; // Symbol used to indicate a character is punctuation
    public static final int characterReferenceDecimalSizeMax = 7; // `&#9999999;`.
    public static final int characterReferenceHexadecimalSizeMax = 6; // `&#xff9999;`.
    public static final int characterReferenceNamedSizeMax = 31; // `&CounterClockwiseContourIntegral;`.
    public static final int codeFencedSequenceSizeMin = 3; // At least 3 ticks or tildes are needed.

    public static final int hardBreakPrefixSizeMin = 2; // At least 2 trailing spaces are needed.
    public static final int htmlRaw = 1; // Symbol for `<script>`
    public static final int htmlComment = 2; // Symbol for `<!---->`
    public static final int htmlInstruction = 3; // Symbol for `<?php?>`
    public static final int htmlDeclaration = 4; // Symbol for `<!doctype>`
    public static final int htmlCdata = 5; // Symbol for `<![CDATA[]]>`
    public static final int htmlBasic = 6; // Symbol for `<div`
    public static final int htmlComplete = 7; // Symbol for `<x>`
    public static final int htmlRawSizeMax = 8; // Length of `textarea`.
    public static final int linkResourceDestinationBalanceMax = 32; // See: <https://spec.commonmark.org/0.30/#link-destination>, <https://github.com/remarkjs/react-markdown/issues/658#issuecomment-984345577>
    public static final int linkReferenceSizeMax = 999; // See: <https://spec.commonmark.org/0.30/#link-label>
    public static final int listItemValueSizeMax = 10; // See: <https://spec.commonmark.org/0.30/#ordered-list-marker>
    public static final int numericBaseDecimal = 10;
    public static final int numericBaseHexadecimal = 0x10;
    public static final int tabSize = 4; // Tabs have a hard-coded size of 4, per CommonMark.
    public static final int thematicBreakMarkerCountMin = 3; // At least 3 asterisks, dashes, or underscores are needed.
}
