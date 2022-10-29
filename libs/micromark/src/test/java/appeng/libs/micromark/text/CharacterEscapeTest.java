package appeng.libs.micromark.text;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

public class CharacterEscapeTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
        "\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\`\\{\\|\\}\\~||<p>!&quot;#$%&amp;'()*+,-./:;&lt;=&gt;?@[\\]^_`{|}~</p>||should support escaped ascii punctuation",
        "\\→\\A\\a\\ \\3\\φ\\«||<p>\\→\\A\\a\\ \\3\\φ\\«</p>||should not support other characters after a backslash",
        "foo\\^nbar||<p>foo<br />^nbar</p>||should escape a line break",
        "`` \\[\\` ``||<p><code>\\[\\`</code></p>||should not escape in text code",
        "    \\[\\]||<pre><code>\\[\\]^n</code></pre>||should not escape in indented code",
        "<http://example.com?find=\\*>||<p><a href=\"http://example.com?find=%5C*\">http://example.com?find=\\*</a></p>||should not escape in autolink",
        "[foo](/bar\\* \"ti\\*tle\")||<p><a href=\"/bar*\" title=\"ti*tle\">foo</a></p>||should escape in resource and title",
        "[foo]: /bar\\* \"ti\\*tle\"^n^n[foo]||<p><a href=\"/bar*\" title=\"ti*tle\">foo</a></p>||should escape in definition resource and title",
        "``` foo\\+bar^nfoo^n```||<pre><code class=\"language-foo+bar\">foo^n</code></pre>||should escape in fenced code info",
    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testEscapedConstructs() {

        TestUtil.assertGeneratedHtmlLines(
                List.of(
                        "\\*not emphasized*",
                        "\\<br/> not a tag",
                        "\\[not a link](/foo)",
                        "\\`not code`",
                        "1\\. not a list",
                        "\\* not a list",
                        "\\# not a heading",
                        "\\[foo]: /url \"not a reference\"",
                        "\\&ouml; not a character entity"
                )
                ,
                List.of(
                        "<p>*not emphasized*",
                        "&lt;br/&gt; not a tag",
                        "[not a link](/foo)",
                        "`not code`",
                        "1. not a list",
                        "* not a list",
                        "# not a heading",
                        "[foo]: /url &quot;not a reference&quot;",
                        "&amp;ouml; not a character entity</p>"
                )
        );
    }

    @Test
    public void testShouldNotEscapeInFlowHtml() {
        TestUtil.assertGeneratedDangerousHtml(
                "<a href=\"/bar\\/)\">",
                "<a href=\"/bar\\/)\">"

        );
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("\\> a", "<p>\\&gt; a</p>", "should support turning off character escapes", "characterEscape");
    }
}
