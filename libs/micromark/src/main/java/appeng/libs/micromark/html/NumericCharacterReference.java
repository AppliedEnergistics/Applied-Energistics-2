package appeng.libs.micromark.html;

import appeng.libs.micromark.symbol.Codes;

public final class NumericCharacterReference {
    private NumericCharacterReference() {
    }

    /**
     * Turn the number (in string form as either hexa- or plain decimal) coming from
     * a numeric character reference into a character.
     *
     * @param value
     *   Value to decode.
     * @param base
     *   Numeric base.
     */
    public static String decodeNumericCharacterReference(String value, int base) {
        var code = Integer.parseInt(value, base);

        if (
            // C0 except for HT, LF, FF, CR, space
                code < Codes.ht ||
                        code == Codes.vt ||
                        (code > Codes.cr && code < Codes.space) ||
                        // Control character (DEL) of the basic block and C1 controls.
                        (code > Codes.tilde && code < 160) ||
                        // Lone high surrogates and low surrogates.
                        (code > 55295 && code < 57344) ||
                        // Noncharacters.
                        (code > 64975 && code < 65008) ||
                        /* eslint-disable no-bitwise */
                        (code & 65535) == 65535 ||
                        (code & 65535) == 65534 ||
                        /* eslint-enable no-bitwise */
                        // Out of range
                        code > 1114111
        ) {
            return String.valueOf((char) Codes.replacementCharacter);
        }

        return String.valueOf((char) code);
    }

}
