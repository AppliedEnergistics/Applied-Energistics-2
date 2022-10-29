package appeng.libs.micromark.text;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

public class CharacterReferenceTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {

            "&#35; &#1234; &#992; &#0;||<p># Ӓ Ϡ �</p>||should support decimal character references",

            "&#X22; &#XD06; &#xcab;||<p>&quot; ആ ಫ</p>||should support hexadecimal character references",

            "&copy||<p>&amp;copy</p>||should not support character references w/o semicolon",

            "&MadeUpEntity;||<p>&amp;MadeUpEntity;</p>||should not support unknown named character references",

            "[foo](/f&ouml;&ouml; \"f&ouml;&ouml;\")||<p><a href=\"/f%C3%B6%C3%B6\" title=\"föö\">foo</a></p>||should support character references in resource URLs and titles",

            "[foo]: /f&ouml;&ouml; \"f&ouml;&ouml;\"^n^n[foo]||<p><a href=\"/f%C3%B6%C3%B6\" title=\"föö\">foo</a></p>||should support character references in definition URLs and titles",

            "``` f&ouml;&ouml;^nfoo^n```||<pre><code class=\"language-föö\">foo^n</code></pre>||should support character references in code language",

            "`f&ouml;&ouml;`||<p><code>f&amp;ouml;&amp;ouml;</code></p>||should not support character references in text code",

            "    f&ouml;f&ouml;||<pre><code>f&amp;ouml;f&amp;ouml;^n</code></pre>||should not support character references in indented code",

            "&#42;foo&#42;^n*foo*||<p>*foo*^n<em>foo</em></p>||should not support character references as construct markers (1)",

            "&#42; foo^n^n* foo||<p>* foo</p>^n<ul>^n<li>foo</li>^n</ul>||should not support character references as construct markers (2)",

            "[a](url &quot;tit&quot;)||<p>[a](url &quot;tit&quot;)</p>||should not support character references as construct markers (3)",

            "foo&#10;&#10;bar||<p>foo^n^nbar</p>||should not support character references as whitespace (1)",

            "&#9;foo||<p>\tfoo</p>||should not support character references as whitespace (2)",

            // Extra:
            "&CounterClockwiseContourIntegral;||<p>∳</p>||should support the longest possible named character reference",

            "&#xff9999;||<p>�</p>||should “support” a longest possible hexadecimal character reference",

            "&#9999999;||<p>�</p>||should “support” a longest possible decimal character reference",

            "&CounterClockwiseContourIntegrali;||<p>&amp;CounterClockwiseContourIntegrali;</p>||should not support the longest possible named character reference",

            "&#xff99999;||<p>&amp;#xff99999;</p>||should not support a longest possible hexadecimal character reference",

            "&#99999999;||<p>&amp;#99999999;</p>||should not support a longest possible decimal character reference",

            "&-;||<p>&amp;-;</p>||should not support the other characters after `&`",

            "&#-;||<p>&amp;#-;</p>||should not support the other characters after `#`",

            "&#x-;||<p>&amp;#x-;</p>||should not support the other characters after `#x`",

            "&lt-;||<p>&amp;lt-;</p>||should not support the other characters inside a name",

            "&#9-;||<p>&amp;#9-;</p>||should not support the other characters inside a demical",

            "&#x9-;||<p>&amp;#x9-;</p>||should not support the other characters inside a hexademical",

    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testNamedCharacterReferences() {
        TestUtil.assertGeneratedHtmlLines(
                List.of(
                        "&nbsp; &amp; &copy; &AElig; &Dcaron;",
                        "&frac34; &HilbertSpace; &DifferentialD;",
                        "&ClockwiseContourIntegral; &ngE;"
                ),
                List.of(
                        "<p>  &amp; © Æ Ď^n¾ ℋ ⅆ^n∲ ≧̸</p>"
                )
        );
    }

    @Test
    public void testShouldIgnoreCharacterReferencesInHtml() {
        TestUtil.assertGeneratedDangerousHtml(
                "<a href=\"&ouml;&ouml;.html\">",
                "<a href=\"&ouml;&ouml;.html\">"
        );
    }

    @Test
    public void testShouldNotSupportOtherThingsThatLookLikeCharacterReferences() {
        TestUtil.assertGeneratedHtmlLines(
                List.of(
                        "&nbsp &x; &#; &#x;",
                        "&#987654321;",
                        "&#abcdef0;",
                        "&ThisIsNotDefined; &hi?;"
                ),
                List.of(
                        "<p>&amp;nbsp &amp;x; &amp;#; &amp;#x;",
                        "&amp;#987654321;",
                        "&amp;#abcdef0;",
                        "&amp;ThisIsNotDefined; &amp;hi?;</p>"
                )
        );
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("&amp;", "<p>&amp;</p>", "should support turning off character references", "characterReferences");
    }
}
