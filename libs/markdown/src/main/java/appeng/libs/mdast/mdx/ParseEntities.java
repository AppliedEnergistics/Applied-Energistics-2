package appeng.libs.mdast.mdx;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.NamedCharacterEntities;

import java.util.HashMap;
import java.util.Map;

/**
 * Reduced functionality port of https://github.com/wooorm/parse-entities/
 */
final class ParseEntities {
    private ParseEntities() {
    }

    /**
     * Parse HTML character references.
     */
    public static String parseEntities(String value) {
        var result = new StringBuilder();

        // Ensure the algorithm walks over the first character (inclusive).
        for (var index = 0; index < value.length(); ++index) {
            var character = value.charAt(index);

            if (character != '&' || index + 1 >= value.length()) {
                result.append(character);
                continue;
            }

            int following = value.charAt(index + 1);

            // The behavior depends on the identity of the next character.
            if (
                    following == '\t' ||
                            following == '\n' ||
                            following == '\f' ||
                            following == ' ' ||
                            following == '&' ||
                            following == '<'
            ) {
                // Not a character reference.
                // No characters are consumed, and nothing is returned.
                // This is not an error, either.
                result.append(character);
                continue;
            }

            var start = index + 1;
            var begin = start;
            var end = start;
            CharRefType type;

            if (following == '#') {
                // Numerical reference.
                end = ++begin;

                // The behavior further depends on the next character.
                following = value.charAt(end);

                if (following == 'X' || following == 'x') {
                    // ASCII hexadecimal digits.
                    type = CharRefType.hexadecimal;
                    end = ++begin;
                } else {
                    // ASCII decimal digits.
                    type = CharRefType.decimal;
                }
            } else {
                // Named reference.
                type = CharRefType.named;
            }

            end--;

            // Each type of character reference accepts different characters.
            // This test is used to detect whether a reference has ended (as the semicolon
            // is not strictly needed).
            var charBuffer = new StringBuilder();
            while (++end < value.length()) {
                following = value.charAt(end);

                if (!type.test((char) following)) {
                    break;
                }

                charBuffer.append((char) following);
            }

            var terminated = end < value.length() && value.charAt(end) == ';';

            boolean consumeRef = terminated;

            if (terminated) {
                end++;

                if (type == CharRefType.named) {
                    var namedReference = NamedCharacterEntities.decodeNamedCharacterReference(charBuffer.toString());
                    if (namedReference != null) {
                        result.append(namedReference);
                    } else {
                        consumeRef = false; // Unknown named references stay untouched
                    }
                } else {
                    // When terminated and numerical, parse as either hexadecimal or
                    // decimal.
                    var referenceCode = Integer.parseInt(
                            charBuffer,
                            0,
                            charBuffer.length(),
                            type == CharRefType.hexadecimal ? 16 : 10
                    );

                    // Emit a warning when the parsed number is prohibited, and replace with
                    // replacement character.
                    if (prohibited(referenceCode)) {
                        result.append((char) 65533 /* `�` */);
                    } else if (characterReferenceInvalid.containsKey(referenceCode)) {
                        // Emit a warning when the parsed number is disallowed, and replace by
                        // an alternative.
                        result.append(characterReferenceInvalid.get(referenceCode));
                    } else {
                        result.appendCodePoint(referenceCode);
                    }
                }
            }

            // Found it!
            // First eat the queued characters as normal text, then eat a reference.
            if (!consumeRef) {
                // If we could not find a reference, queue the checked characters (as
                // normal characters), and move the pointer to their end.
                // This is possible because we can be certain neither newlines nor
                // ampersands are included.
                result.append(value, start - 1, end);
            }
            index = end - 1;
        }

        // Return the reduced nodes.
        return result.toString();
    }

    /**
     * Check if `character` is outside the permissible unicode range.
     */
    private static boolean prohibited(int code) {
        return (code >= 0xd800 && code <= 0xdfff) || code > 0x10ffff;
    }

    enum CharRefType {
        named {
            @Override
            boolean test(char ch) {
                return CharUtil.asciiAlphanumeric(ch);
            }
        },
        decimal {
            @Override
            boolean test(char ch) {
                return CharUtil.asciiDigit(ch);
            }
        },
        hexadecimal {
            @Override
            boolean test(char ch) {
                return CharUtil.asciiHexDigit(ch);
            }
        };

        abstract boolean test(char ch);
    }

    /**
     * Map of invalid numeric character references to their replacements, according to HTML.
     */
    private static final Map<Integer, String> characterReferenceInvalid;
    static {
        var codes = new HashMap<Integer,String>();
        codes.put(0, "�");
        codes.put(128, "€");
        codes.put(130, "‚");
        codes.put(131, "ƒ");
        codes.put(132, "„");
        codes.put(133, "…");
        codes.put(134, "†");
        codes.put(135, "‡");
        codes.put(136, "ˆ");
        codes.put(137, "‰");
        codes.put(138, "Š");
        codes.put(139, "‹");
        codes.put(140, "Œ");
        codes.put(142, "Ž");
        codes.put(145, "‘");
        codes.put(146, "’");
        codes.put(147, "“");
        codes.put(148, "”");
        codes.put(149, "•");
        codes.put(150, "–");
        codes.put(151, "—");
        codes.put(152, "˜");
        codes.put(153, "™");
        codes.put(154, "š");
        codes.put(155, "›");
        codes.put(156, "œ");
        codes.put(158, "ž");
        codes.put(159, "Ÿ");
        characterReferenceInvalid = Map.copyOf(codes);
    }


}
