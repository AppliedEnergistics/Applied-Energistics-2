package appeng.libs.micromark;

import appeng.libs.micromark.symbol.Codes;

public final class CharUtil {
    private CharUtil() {
    }

    /**
     * Check whether the character code represents an ASCII alpha (`a` through `z`,
     * case insensitive).
     * <p>
     * An **ASCII alpha** is an ASCII upper alpha or ASCII lower alpha.
     * <p>
     * An **ASCII upper alpha** is a character in the inclusive range U+0041 (`A`)
     * to U+005A (`Z`).
     * <p>
     * An **ASCII lower alpha** is a character in the inclusive range U+0061 (`a`)
     * to U+007A (`z`).
     */
    public static boolean asciiAlpha(int code) {
        return code >= 'a' && code <= 'z' || code >= 'A' && code <= 'Z';
    }

    /**
     * Check whether the character code represents an ASCII digit (`0` through `9`).
     * <p>
     * An **ASCII digit** is a character in the inclusive range U+0030 (`0`) to
     * U+0039 (`9`).
     */
    public static boolean asciiDigit(int code) {
        return code >= '0' && code <= '9';
    }

    /**
     * Check whether the character code represents an ASCII hex digit (`a` through
     * `f`, case insensitive, or `0` through `9`).
     * <p>
     * An **ASCII hex digit** is an ASCII digit (see `asciiDigit`), ASCII upper hex
     * digit, or an ASCII lower hex digit.
     * <p>
     * An **ASCII upper hex digit** is a character in the inclusive range U+0041
     * (`A`) to U+0046 (`F`).
     * <p>
     * An **ASCII lower hex digit** is a character in the inclusive range U+0061
     * (`a`) to U+0066 (`f`).
     */
    public static boolean asciiHexDigit(int code) {
        return asciiDigit(code) || code >= 'a' && code <= 'f' || code >= 'A' && code <= 'F';
    }

    /**
     * Check whether the character code represents an ASCII alphanumeric (`a`
     * through `z`, case insensitive, or `0` through `9`).
     * <p>
     * An **ASCII alphanumeric** is an ASCII digit (see `asciiDigit`) or ASCII alpha
     * (see `asciiAlpha`).
     */
    public static boolean asciiAlphanumeric(int code) {
        return asciiDigit(code) || asciiAlpha(code);
    }

    /**
     * Check whether the character code represents ASCII punctuation.
     * <p>
     * An **ASCII punctuation** is a character in the inclusive ranges U+0021
     * EXCLAMATION MARK (`!`) to U+002F SLASH (`/`), U+003A COLON (`:`) to U+0040 AT
     * SIGN (`@`), U+005B LEFT SQUARE BRACKET (`[`) to U+0060 GRAVE ACCENT
     * (`` ` ``), or U+007B LEFT CURLY BRACE (`{`) to U+007E TILDE (`~`).
     */
    public static boolean asciiPunctuation(int code) {
        return code >= '!' && code <= '/'
                || code >= ':' && code <= '@'
                || code >= '[' && code <= '`'
                || code >= '{' && code <= '~';
    }

    /**
     * Check whether the character code represents an ASCII atext.
     * <p>
     * atext is an ASCII alphanumeric (see `asciiAlphanumeric`), or a character in
     * the inclusive ranges U+0023 NUMBER SIGN (`#`) to U+0027 APOSTROPHE (`'`),
     * U+002A ASTERISK (`*`), U+002B PLUS SIGN (`+`), U+002D DASH (`-`), U+002F
     * SLASH (`/`), U+003D EQUALS TO (`=`), U+003F QUESTION MARK (`?`), U+005E
     * CARET (`^`) to U+0060 GRAVE ACCENT (`` ` ``), or U+007B LEFT CURLY BRACE
     * (`{`) to U+007E TILDE (`~`).
     * <p>
     * See:
     * **\[RFC5322]**:
     * [Internet Message Format](https://tools.ietf.org/html/rfc5322).
     * P. Resnick.
     * IETF.
     */
    public static boolean asciiAtext(int code) {
        return asciiAlphanumeric(code)
                || code >= '#' && code <= '\''
                || code == '*' || code == '+' || code == '-'
                || code == '/' || code == '=' || code == '?'
                || code >= '^' && code <= '~';
    }

    /**
     * Check whether a character code is an ASCII control character.
     * <p>
     * An **ASCII control** is a character in the inclusive range U+0000 NULL (NUL)
     * to U+001F (US), or U+007F (DEL).
     */
    public static boolean asciiControl(int code) {
        return (
                // Special whitespace codes (which have negative values), C0 and Control
                // character DEL
                code != Codes.eof && (code < Codes.space || code == Codes.del)
        );
    }

    /**
     * Check whether a character code is a markdown line ending (see
     * `markdownLineEnding`) or markdown space (see `markdownSpace`).
     */
    public static boolean markdownLineEndingOrSpace(int code) {
        return code != Codes.eof && (code < Codes.nul || code == Codes.space);
    }

    /**
     * Check whether a character code is a markdown line ending.
     * <p>
     * A **markdown line ending** is the virtual characters M-0003 CARRIAGE RETURN
     * LINE FEED (CRLF), M-0004 LINE FEED (LF) and M-0005 CARRIAGE RETURN (CR).
     * <p>
     * In micromark, the actual character U+000A LINE FEED (LF) and U+000D CARRIAGE
     * RETURN (CR) are replaced by these virtual characters depending on whether
     * they occurred together.
     */
    public static boolean markdownLineEnding(int code) {
        return code != Codes.eof && code < Codes.horizontalTab;
    }

    /**
     * Check whether a character code is a markdown space.
     * <p>
     * A **markdown space** is the concrete character U+0020 SPACE (SP) and the
     * virtual characters M-0001 VIRTUAL SPACE (VS) and M-0002 HORIZONTAL TAB (HT).
     * <p>
     * In micromark, the actual character U+0009 CHARACTER TABULATION (HT) is
     * replaced by one M-0002 HORIZONTAL TAB (HT) and between 0 and 3 M-0001 VIRTUAL
     * SPACE (VS) characters, depending on the column at which the tab occurred.
     */
    public static boolean markdownSpace(int code) {
        return (
                code == Codes.horizontalTab ||
                        code == Codes.virtualSpace ||
                        code == Codes.space
        );
    }

    /**
     * Check whether the character code represents Unicode whitespace.
     * <p>
     * Note that this does handle micromark specific markdown whitespace characters.
     * See `markdownLineEndingOrSpace` to check that.
     * <p>
     * A **Unicode whitespace** is a character in the Unicode `Zs` (Separator,
     * Space) category, or U+0009 CHARACTER TABULATION (HT), U+000A LINE FEED (LF),
     * U+000C (FF), or U+000D CARRIAGE RETURN (CR) (**\[UNICODE]**).
     * <p>
     * See:
     * **\[UNICODE]**:
     * [The Unicode Standard](https://www.unicode.org/versions/).
     * Unicode Consortium.
     */
    public static boolean unicodeWhitespace(int code) {
        return Character.isSpaceChar((char) code);
    }

    /**
     * Check whether the character code represents Unicode punctuation.
     * <p>
     * A **Unicode punctuation** is a character in the Unicode `Pc` (Punctuation,
     * Connector), `Pd` (Punctuation, Dash), `Pe` (Punctuation, Close), `Pf`
     * (Punctuation, Final quote), `Pi` (Punctuation, Initial quote), `Po`
     * (Punctuation, Other), or `Ps` (Punctuation, Open) categories, or an ASCII
     * punctuation (see `asciiPunctuation`).
     * <p>
     * See:
     * **\[UNICODE]**:
     * [The Unicode Standard](https://www.unicode.org/versions/).
     * Unicode Consortium.
     */
    public static boolean unicodePunctuation(int code) {
        if (asciiPunctuation(code)) {
            return true;
        }

        var type = Character.getType((char) code);
        return type == Character.CONNECTOR_PUNCTUATION
                || type == Character.DASH_PUNCTUATION
                || type == Character.END_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION
                || type == Character.START_PUNCTUATION;
    }

}
