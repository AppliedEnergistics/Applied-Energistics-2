package appeng.libs.micromark;

import java.util.regex.Pattern;

public class NormalizeIdentifier {
    private static final Pattern MARKDOWN_WHITESPACE = Pattern.compile("[\\t\\n\\r ]+");

    private NormalizeIdentifier() {
    }

    /**
     * Normalize an identifier (such as used in definitions).
     */
    public static String normalizeIdentifier(String value) {
        // Collapse Markdown whitespace.
        value = MARKDOWN_WHITESPACE
                .matcher(value)
                .replaceAll(" ");

        // Trim.
        value = value.trim();

        // Some characters are considered “uppercase”, but if their lowercase
        // counterpart is uppercased will result in a different uppercase
        // character.
        // Hence, to get that form, we perform both lower- and uppercase.
        // Upper case makes sure keys will not interact with default prototypal
        // methods: no method is uppercase.
        value = value.toLowerCase().toUpperCase();

        return value;
    }

}
