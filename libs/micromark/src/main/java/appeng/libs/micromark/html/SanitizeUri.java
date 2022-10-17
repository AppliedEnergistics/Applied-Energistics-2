package appeng.libs.micromark.html;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.symbol.Codes;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class SanitizeUri {
    private SanitizeUri() {
    }

    /**
     * Make a value safe for injection as a URL.
     * <p>
     * This encodes unsafe characters with percent-encoding and skips already
     * encoded sequences (see `normalizeUri` below).
     * Further unsafe characters are encoded as character references (see
     * `micromark-util-encode`).
     * <p>
     * Then, a regex of allowed protocols can be given, in which case the URL is
     * sanitized.
     * For example, `/^(https?|ircs?|mailto|xmpp)$/i` can be used for `a[href]`,
     * or `/^https?$/i` for `img[src]`.
     * If the URL includes an unknown protocol (one not matched by `protocol`, such
     * as a dangerous example, `javascript:`), the value is ignored.
     *
     * @param {string|undefined} url
     * @param {RegExp}           [protocol]
     * @returns {string}
     */
    public static String sanitizeUri(String url, @Nullable Pattern protocol) {
        var value = HtmlEncode.encode(normalizeUri(Objects.requireNonNullElse(url, "")));

        if (protocol == null) {
            return value;
        }

        var colon = value.indexOf(':');
        var questionMark = value.indexOf('?');
        var numberSign = value.indexOf('#');
        var slash = value.indexOf('/');

        if (
            // If there is no protocol, it’s relative.
                colon < 0 ||
                        // If the first colon is after a `?`, `#`, or `/`, it’s not a protocol.
                        (slash > -1 && colon > slash) ||
                        (questionMark > -1 && colon > questionMark) ||
                        (numberSign > -1 && colon > numberSign) ||
                        // It is a protocol, it should be allowed.
                        protocol.matcher(value.substring(0, colon)).matches()
        ) {
            return value;
        }

        return "";
    }

    private static final Predicate<String> ASCII_PATTERN = Pattern.compile("[!#$&-;=?-Z_a-z~]").asMatchPredicate();

    /**
     * Normalize a URL (such as used in definitions).
     * <p/>
     * Encode unsafe characters with percent-encoding, skipping already encoded
     * sequences.
     */
    public static String normalizeUri(String value) {
        var result = new StringBuilder();
        var index = -1;
        var start = 0;
        var skip = 0;

        while (++index < value.length()) {
            var code = value.charAt(index);
            String replace = null;

            // A correct percent encoded value.
            if (
                    code == Codes.percentSign &&
                            CharUtil.asciiAlphanumeric(value.charAt(index + 1)) &&
                            CharUtil.asciiAlphanumeric(value.charAt(index + 2))
            ) {
                skip = 2;
            }
            // ASCII.
            else if (code < 128) {
                if (!ASCII_PATTERN.test(String.valueOf(code))) {
                    replace = String.valueOf(code);
                }
            }
            // Astral.
            else if (code > 55295 && code < 57344) {
                var next = value.charAt(index + 1);

                // A correct surrogate pair.
                if (code < 56320 && next > 56319 && next < 57344) {
                    replace = String.valueOf(new char[]{code, next});
                    skip = 1;
                }
                // Lone surrogate.
                else {
                    replace = String.valueOf(Codes.replacementCharacter);
                }
            }
            // Unicode.
            else {
                replace = String.valueOf(code);
            }

            if (replace != null) {
                result.append(value, start, index);
                result.append(URLEncoder.encode(replace, StandardCharsets.UTF_8));
                start = index + skip + 1;
                replace = null;
            }

            if (skip > 0) {
                index += skip;
                skip = 0;
            }
        }

        result.append(value, start, value.length());
        return result.toString();
    }

}
