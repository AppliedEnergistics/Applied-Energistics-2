package appeng.libs.micromark;

import appeng.libs.micromark.commonmark.Attention;
import appeng.libs.micromark.symbol.Codes;

import java.util.List;
import java.util.Map;

public final class DefaultExtension {
    private DefaultExtension() {
    }

    public static Extension create() {
        var extension = new Extension();

        extension.document = Map.of(
                Codes.asterisk, List.of(list),
                Codes.plusSign, List.of(list),
                Codes.dash, List.of(list),
                Codes.digit0, List.of(list),
                Codes.digit1, List.of(list),
                Codes.digit2, List.of(list),
                Codes.digit3, List.of(list),
                Codes.digit4, List.of(list),
                Codes.digit5, List.of(list),
                Codes.digit6, List.of(list),
                Codes.digit7, List.of(list),
                Codes.digit8, List.of(list),
                Codes.digit9, List.of(list),
                Codes.greaterThan, List.of(blockQuote)
        );

        extension.contentInitial = Map.of(
                Codes.leftSquareBracket, List.of(definition)
        );

        extension.flowInitial = Map.of(
                Codes.horizontalTab, List.of(codeIndented),
                Codes.virtualSpace, List.of(codeIndented),
                Codes.space, List.of(codeIndented)
        );

        extension.flow = Map.of(
                Codes.numberSign, List.of(headingAtx),
                Codes.asterisk, List.of(thematicBreak),
                Codes.dash, List.of(setextUnderline, thematicBreak),
                Codes.lessThan, List.of(htmlFlow),
                Codes.equalsTo, List.of(setextUnderline),
                Codes.underscore, List.of(thematicBreak),
                Codes.graveAccent, List.of(codeFenced),
                Codes.tilde, List.of(codeFenced)
        );

        extension.string = Map.of(
                Codes.ampersand, List.of(characterReference),
                Codes.backslash, List.of(characterEscape)
        );

        extension.text = Map.of(
                Codes.carriageReturn, List.of(lineEnding),
                Codes.lineFeed, List.of(lineEnding),
                Codes.carriageReturnLineFeed, List.of(lineEnding),
                Codes.exclamationMark, List.of(labelStartImage),
                Codes.ampersand, List.of(characterReference),
                Codes.asterisk, List.of(Attention.attention),
                Codes.lessThan, List.of(autolink, htmlText),
                Codes.leftSquareBracket, List.of(labelStartLink),
                Codes.backslash, List.of(hardBreakEscape, characterEscape),
                Codes.rightSquareBracket, List.of(labelEnd),
                Codes.underscore, List.of(Attention.attention),
                Codes.graveAccent, List.of(codeText)
        );

        extension.nullInsideSpan = List.of(attention, InitializeText.resolver);

        extension.nullAttentionMarkers = List.of(Codes.asterisk, Codes.underscore);

        extension.nullDisable = List.of();

        return extension;
    }

}
