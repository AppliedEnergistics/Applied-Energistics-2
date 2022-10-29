package appeng.libs.micromark.misc;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UrlTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "[](<%>)||<p><a href=\"%25\"></a></p>||should support incorrect percentage encoded values (1)",
            "[](<%%20>)||<p><a href=\"%25%20\"></a></p>||should support incorrect percentage encoded values (2)",
            "[](<%a%20>)||<p><a href=\"%25a%20\"></a></p>||should support incorrect percentage encoded values (3)",
            "[](<foo\uD800bar>)||<p><a href=\"foo%EF%BF%BDbar\"></a></p>||should support a lone high surrogate (lowest)",
            "[](<foo\uDBFFbar>)||<p><a href=\"foo%EF%BF%BDbar\"></a></p>||should support a lone high surrogate (highest)",
            "[](<\uD800bar>)||<p><a href=\"%EF%BF%BDbar\"></a></p>||should support a lone high surrogate at the start (lowest)",
            "[](<\uDBFFbar>)||<p><a href=\"%EF%BF%BDbar\"></a></p>||should support a lone high surrogate at the start (highest)",
            "[](<foo\uD800>)||<p><a href=\"foo%EF%BF%BD\"></a></p>||should support a lone high surrogate at the end (lowest)",
            "[](<foo\uDBFF>)||<p><a href=\"foo%EF%BF%BD\"></a></p>||should support a lone high surrogate at the end (highest)",
            "[](<foo\uDC00bar>)||<p><a href=\"foo%EF%BF%BDbar\"></a></p>||should support a lone low surrogate (lowest)",
            "[](<foo\uDFFFbar>)||<p><a href=\"foo%EF%BF%BDbar\"></a></p>||should support a lone low surrogate (highest)",
            "[](<\uDC00bar>)||<p><a href=\"%EF%BF%BDbar\"></a></p>||should support a lone low surrogate at the start (lowest)",
            "[](<\uDFFFbar>)||<p><a href=\"%EF%BF%BDbar\"></a></p>||should support a lone low surrogate at the start (highest)",
            "[](<foo\uDC00>)||<p><a href=\"foo%EF%BF%BD\"></a></p>||should support a lone low surrogate at the end (lowest)",
            "[](<foo\uDFFF>)||<p><a href=\"foo%EF%BF%BD\"></a></p>||should support a lone low surrogate at the end (highest)",
            "[](<🤔>)||<p><a href=\"%F0%9F%A4%94\"></a></p>||should support an emoji",
    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testAllAscii() {

        StringBuilder ascii = new StringBuilder();
        int code = -1;
        while (++code < 128) {
            // LF and CR can’t be in resources.
            if (code == 10 || code == 13) {
                continue;
            }

            // `<`, `>`, `\` need to be escaped.
            if (code == 60 || code == 62 || code == 92) {
                ascii.append('\\');
            }

            ascii.append((char) code);
        }

        TestUtil.assertGeneratedHtml(
                "[](<" + ascii + ">)",
                "<p><a href=\"%EF%BF%BD%01%02%03%04%05%06%07%08%09%0B%0C%0E%0F%10%11%12%13%14%15%16%17%18%19%1A%1B%1C%1D%1E%1F%20!%22#$%25&amp;'()*+,-./0123456789:;%3C=%3E?@ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~%7F\"></a></p>"
        );
    }
}
