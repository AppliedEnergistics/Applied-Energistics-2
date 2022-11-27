package appeng.libs.micromark.misc;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class DangerousProtocolsTest {
    @Nested
    public class AutolinkTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<javascript:alert(1)>||<p><a href=\"\">javascript:alert(1)</a></p>||should be safe by default",
                "<http://a>||<p><a href=\"http://a\">http://a</a></p>||should allow `http:`",
                "<https://a>||<p><a href=\"https://a\">https://a</a></p>||should allow `https:`",
                "<irc:///help>||<p><a href=\"irc:///help\">irc:///help</a></p>||should allow `irc:`",
                "<mailto:a>||<p><a href=\"mailto:a\">mailto:a</a></p>||should allow `mailto:`",
        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedHtml(markdown, expectedHtml);
        }
    }

    @Nested
    public class ImageTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "![](javascript:alert(1))||<p><img src=\"\" alt=\"\" /></p>||should be safe by default",
                "![](http://a)||<p><img src=\"http://a\" alt=\"\" /></p>||should allow `http:`",
                "![](https://a)||<p><img src=\"https://a\" alt=\"\" /></p>||should allow `https:`",
                "![](irc:///help)||<p><img src=\"\" alt=\"\" /></p>||should not allow `irc:`",
                "![](mailto:a)||<p><img src=\"\" alt=\"\" /></p>||should not allow `mailto:`",
                "![](#a)||<p><img src=\"#a\" alt=\"\" /></p>||should allow a hash",
                "![](?a)||<p><img src=\"?a\" alt=\"\" /></p>||should allow a search",
                "![](/a)||<p><img src=\"/a\" alt=\"\" /></p>||should allow an absolute",
                "![](./a)||<p><img src=\"./a\" alt=\"\" /></p>||should allow an relative",
                "![](../a)||<p><img src=\"../a\" alt=\"\" /></p>||should allow an upwards relative",
                "![](a#b:c)||<p><img src=\"a#b:c\" alt=\"\" /></p>||should allow a colon in a hash",
                "![](a?b:c)||<p><img src=\"a?b:c\" alt=\"\" /></p>||should allow a colon in a search",
                "![](a/b:c)||<p><img src=\"a/b:c\" alt=\"\" /></p>||should allow a colon in a path",
        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedHtml(markdown, expectedHtml);
        }
    }

    @Nested
    public class LinkTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "[](javascript:alert(1))||<p><a href=\"\"></a></p>||should be safe by default",
                "[](http://a)||<p><a href=\"http://a\"></a></p>||should allow `http:`",
                "[](https://a)||<p><a href=\"https://a\"></a></p>||should allow `https:`",
                "[](irc:///help)||<p><a href=\"irc:///help\"></a></p>||should allow `irc:`",
                "[](mailto:a)||<p><a href=\"mailto:a\"></a></p>||should allow `mailto:`",
                "[](#a)||<p><a href=\"#a\"></a></p>||should allow a hash",
                "[](?a)||<p><a href=\"?a\"></a></p>||should allow a search",
                "[](/a)||<p><a href=\"/a\"></a></p>||should allow an absolute",
                "[](./a)||<p><a href=\"./a\"></a></p>||should allow an relative",
                "[](../a)||<p><a href=\"../a\"></a></p>||should allow an upwards relative",
                "[](a#b:c)||<p><a href=\"a#b:c\"></a></p>||should allow a colon in a hash",
                "[](a?b:c)||<p><a href=\"a?b:c\"></a></p>||should allow a colon in a search",
                "[](a/b:c)||<p><a href=\"a/b:c\"></a></p>||should allow a colon in a path",
        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedHtml(markdown, expectedHtml);
        }
    }
}
