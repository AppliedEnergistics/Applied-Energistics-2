package appeng.libs.micromark.text;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CodeTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {

            "`foo`||<p><code>foo</code></p>||should support code",
            "`` foo ` bar ``||<p><code>foo ` bar</code></p>||should support code w/ more accents",
            "` `` `||<p><code>``</code></p>||should support code w/ fences inside, and padding",
            "`  ``  `||<p><code> `` </code></p>||should support code w/ extra padding",
            "`` ` ``||<p><code>`</code></p>||should support code w/ padding and one character",
            "` a`||<p><code> a</code></p>||should support code w/ unbalanced padding",
            "` b `||<p><code> b </code></p>||should support code w/ non-padding whitespace",
            "` `^n`  `||<p><code> </code>^n<code>  </code></p>||should support code w/o data",
            "``^nfoo^nbar  ^nbaz^n``||<p><code>foo bar   baz</code></p>||should support code w/o line endings (1)",
            "``^nfoo ^n``||<p><code>foo </code></p>||should support code w/o line endings (2)",
            "`foo   bar ^nbaz`||<p><code>foo   bar  baz</code></p>||should not support whitespace collapsing",
            "`foo\\`bar`||<p><code>foo\\</code>bar`</p>||should not support character escapes",
            "``foo`bar``||<p><code>foo`bar</code></p>||should support more accents",
            "` foo `` bar `||<p><code>foo `` bar</code></p>||should support less accents",
            "*foo`*`||<p>*foo<code>*</code></p>||should precede over emphasis",
            "[not a `link](/foo`)||<p>[not a <code>link](/foo</code>)</p>||should precede over links",
            "`<a href=\"`\">`||<p><code>&lt;a href=&quot;</code>&quot;&gt;`</p>||should have same precedence as HTML (1)",
            "`<http://foo.bar.`baz>`||<p><code>&lt;http://foo.bar.</code>baz&gt;`</p>||should have same precedence as autolinks (1)",
            "<http://foo.bar.`baz>`||<p><a href=\"http://foo.bar.%60baz\">http://foo.bar.`baz</a>`</p>||should have same precedence as autolinks (2)",
            "```foo``||<p>```foo``</p>||should not support more accents before a fence",
            "`foo||<p>`foo</p>||should not support no closing fence (1)",
            "`foo``bar``||<p>`foo<code>bar</code></p>||should not support no closing fence (2)",
            // Extra:
            "`foo\t\tbar`||<p><code>foo\t\tbar</code></p>||should support tabs in code",
            "\\``x`||<p>`<code>x</code></p>||should support an escaped initial grave accent",
    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "<a href=\"`\">`||<p><a href=\"`\">`</p>||should have same precedence as HTML (2)",
    })
    public void testGeneratedHtmlUnsafe(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("`a`", "<p>`a`</p>", "should support turning off code (text)", "codeText");
    }
}
