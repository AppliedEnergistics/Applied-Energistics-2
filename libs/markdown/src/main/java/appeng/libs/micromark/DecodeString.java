package appeng.libs.micromark;

import appeng.libs.micromark.commonmark.CharacterReference;
import appeng.libs.micromark.html.NumericCharacterReference;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class DecodeString {

    private static final Pattern characterEscapeOrReference =
            Pattern.compile(
            "\\\\([!-/:-@\\[-`{-~])|&(#(?:\\d{1,7}|x[\\da-f]{1,6})|[\\da-z]{1,31});", Pattern.CASE_INSENSITIVE
            );

        /**
         * Utility to decode markdown strings (which occur in places such as fenced
         * code info strings, destinations, labels, and titles).
         * The “string” content type allows character escapes and -references.
         * This decodes those.
         */
    public static String decodeString(String text) {
        return characterEscapeOrReference.matcher(text).replaceAll(DecodeString::decode);
    }

        private static String decode(MatchResult result) {
            var escape = result.group(1);
        
            if (escape != null) {
                // Escape.
                return escape;
            }

            var charRef = result.group(2);

            // Reference.
            var head = charRef.charAt(0);

            if (head == Codes.numberSign) {
                head = charRef.charAt(1);
                var hex = head == Codes.lowercaseX || head == Codes.uppercaseX;
                return NumericCharacterReference.decodeNumericCharacterReference(
                        charRef.substring(hex ? 2 : 1),
                        hex ? Constants.numericBaseHexadecimal : Constants.numericBaseDecimal
                );
            }

            return Objects.requireNonNullElse(
                    NamedCharacterEntities.decodeNamedCharacterReference(charRef),
                    result.group()
            );
        }
}
