package appeng.libs.micromark.text;

import appeng.libs.micromark.TestUtil;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ImageTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {

            "![foo](/url \"title\")||<p><img src=\"/url\" alt=\"foo\" title=\"title\" /></p>||should support image w/ resource",

            "[foo *bar*]: train.jpg \"train & tracks\"^n^n![foo *bar*]||<p><img src=\"train.jpg\" alt=\"foo bar\" title=\"train &amp; tracks\" /></p>||should support image as shortcut reference",

            "![foo ![bar](/url)](/url2)||<p><img src=\"/url2\" alt=\"foo bar\" /></p>||should “support” images in images",

            "![foo [bar](/url)](/url2)||<p><img src=\"/url2\" alt=\"foo bar\" /></p>||should “support” links in images",

            "[foo *bar*]: train.jpg \"train & tracks\"^n^n![foo *bar*][]||<p><img src=\"train.jpg\" alt=\"foo bar\" title=\"train &amp; tracks\" /></p>||should support “content” in images",

            "[FOOBAR]: train.jpg \"train & tracks\"^n^n![foo *bar*][foobar]||<p><img src=\"train.jpg\" alt=\"foo bar\" title=\"train &amp; tracks\" /></p>||should support “content” in images",

            "![foo](train.jpg)||<p><img src=\"train.jpg\" alt=\"foo\" /></p>||should support images w/o title",

            "My ![foo bar](/path/to/train.jpg  \"title\"   )||<p>My <img src=\"/path/to/train.jpg\" alt=\"foo bar\" title=\"title\" /></p>||should support images w/ lots of whitespace",

            "![foo](<url>)||<p><img src=\"url\" alt=\"foo\" /></p>||should support images w/ enclosed destinations",

            "![](/url)||<p><img src=\"/url\" alt=\"\" /></p>||should support images w/ empty labels",

            "[bar]: /url^n^n![foo][bar]||<p><img src=\"/url\" alt=\"foo\" /></p>||should support full references (1)",

            "[BAR]: /url^n^n![foo][bar]||<p><img src=\"/url\" alt=\"foo\" /></p>||should support full references (2)",

            "[foo]: /url \"title\"^n^n![foo][]||<p><img src=\"/url\" alt=\"foo\" title=\"title\" /></p>||should support collapsed references (1)",

            "[*foo* bar]: /url \"title\"^n^n![*foo* bar][]||<p><img src=\"/url\" alt=\"foo bar\" title=\"title\" /></p>||should support collapsed references (2)",

            "[foo]: /url \"title\"^n^n![Foo][]||<p><img src=\"/url\" alt=\"Foo\" title=\"title\" /></p>||should support case-insensitive labels",

            "[foo]: /url \"title\"^n^n![foo] ^n[]||<p><img src=\"/url\" alt=\"foo\" title=\"title\" />^n[]</p>||should not support whitespace between sets of brackets",

            "[foo]: /url \"title\"^n^n![foo]||<p><img src=\"/url\" alt=\"foo\" title=\"title\" /></p>||should support shortcut references (1)",

            "[*foo* bar]: /url \"title\"^n^n![*foo* bar]||<p><img src=\"/url\" alt=\"foo bar\" title=\"title\" /></p>||should support shortcut references (2)",

            "[[foo]]: /url \"title\"^n^n![[foo]]||<p>[[foo]]: /url &quot;title&quot;</p>^n<p>![[foo]]</p>||should not support link labels w/ unescaped brackets",

            "[foo]: /url \"title\"^n^n![Foo]||<p><img src=\"/url\" alt=\"Foo\" title=\"title\" /></p>||should support case-insensitive label matching",

            "[foo]: /url \"title\"^n^n!\\[foo]||<p>![foo]</p>||should “support” an escaped bracket instead of an image",

            "[foo]: /url \"title\"^n^n\\![foo]||<p>!<a href=\"/url\" title=\"title\">foo</a></p>||should support an escaped bang instead of an image, but still have a link",

            // Extra
            "![foo]()||<p><img src=\"\" alt=\"foo\" /></p>||should support images w/o destination",

            "![foo](<>)||<p><img src=\"\" alt=\"foo\" /></p>||should support images w/ explicit empty destination",

            "![](example.png)||<p><img src=\"example.png\" alt=\"\" /></p>||should support images w/o alt",

            "![alpha](bravo.png \"\")||<p><img src=\"bravo.png\" alt=\"alpha\" /></p>||should support images w/ empty title (1)",

            "![alpha](bravo.png \"\")||<p><img src=\"bravo.png\" alt=\"alpha\" /></p>||should support images w/ empty title (2)",

            "![alpha](bravo.png ())||<p><img src=\"bravo.png\" alt=\"alpha\" /></p>||should support images w/ empty title (3)",

            "![&amp;&copy;&](example.com/&amp;&copy;& \"&amp;&copy;&\")||<p><img src=\"example.com/&amp;%C2%A9&amp;\" alt=\"&amp;©&amp;\" title=\"&amp;©&amp;\" /></p>||should support character references in images",

            // Extra
            // See: <https://github.com/commonmark/commonmark.js/issues/192>
            "![](<> \"\")||<p><img src=\"\" alt=\"\" /></p>||should ignore an empty title",

    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("![x]()", "<p>!<a href=\"\">x</a></p>", "should support turning off label start (image)", "labelStartImage");
    }

    @Test
    public void testDangerousProtocol() {
        TestUtil.assertGeneratedHtml(
                "![](javascript:alert(1))",
                "<p><img src=\"\" alt=\"\" /></p>",
                "should ignore non-http protocols by default"
        );

        var options = new CompileOptions().allowDangerousProtocol();
        TestUtil.assertGeneratedHtml("![](javascript:alert(1))",
                "<p><img src=\"javascript:alert(1)\" alt=\"\" /></p>",
                "should allow non-http protocols w/ `allowDangerousProtocol`",
                new ParseOptions(),
                options
        );
    }

}
