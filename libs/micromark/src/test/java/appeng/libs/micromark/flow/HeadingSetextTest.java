package appeng.libs.micromark.flow;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class HeadingSetextTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "Foo *bar*^n=========||<h1>Foo <em>bar</em></h1>||should support a heading w/ an equals to (rank of 1)",

            "Foo *bar*^n---------||<h2>Foo <em>bar</em></h2>||should support a heading w/ a dash (rank of 2)",

            "Foo *bar^nbaz*^n====||<h1>Foo <em>bar^nbaz</em></h1>||should support line endings in setext headings",

            "  Foo *bar^nbaz*\t^n====||<h1>Foo <em>bar^nbaz</em></h1>||should not include initial and final whitespace around content",

            "Foo^n-------------------------||<h2>Foo</h2>||should support long underlines",

            "Foo^n=||<h1>Foo</h1>||should support short underlines",

            " Foo^n  ===||<h1>Foo</h1>||should support indented content w/ 1 space",

            "  Foo^n---||<h2>Foo</h2>||should support indented content w/ 2 spaces",

            "   Foo^n---||<h2>Foo</h2>||should support indented content w/ 3 spaces",

            "    Foo^n    ---||<pre><code>Foo^n---^n</code></pre>||should not support too much indented content (1)",

            "    Foo^n---||<pre><code>Foo^n</code></pre>^n<hr />||should not support too much indented content (2)",

            "Foo^n   ----      ||<h2>Foo</h2>||should support initial and final whitespace around the underline",

            "Foo^n   =||<h1>Foo</h1>||should support whitespace before underline",

            "Foo^n    =||<p>Foo^n=</p>||should not support too much whitespace before underline (1)",

            "Foo^n\t=||<p>Foo^n=</p>||should not support too much whitespace before underline (2)",

            "Foo^n= =||<p>Foo^n= =</p>||should not support whitespace in the underline (1)",

            "Foo^n--- -||<p>Foo</p>^n<hr />||should not support whitespace in the underline (2)",

            "Foo  ^n-----||<h2>Foo</h2>||should not support a hard break w/ spaces at the end",

            "Foo\\^n-----||<h2>Foo\\</h2>||should not support a hard break w/ backslash at the end",

            "`Foo^n----^n`||<h2>`Foo</h2>^n<p>`</p>||should precede over inline constructs (1)",

            "<a title=\"a lot^n---^nof dashes\"/>||<h2>&lt;a title=&quot;a lot</h2>^n<p>of dashes&quot;/&gt;</p>||should precede over inline constructs (2)",

            "> Foo^n---||<blockquote>^n<p>Foo</p>^n</blockquote>^n<hr />||should not allow underline to be lazy (1)",

            "> foo^nbar^n===||<blockquote>^n<p>foo^nbar^n===</p>^n</blockquote>||should not allow underline to be lazy (2)",

            "- Foo^n---||<ul>^n<li>Foo</li>^n</ul>^n<hr />||should not allow underline to be lazy (3)",

            "Foo^nBar^n---||<h2>Foo^nBar</h2>||should support line endings in setext headings",

            "---^nFoo^n---^nBar^n---^nBaz||<hr />^n<h2>Foo</h2>^n<h2>Bar</h2>^n<p>Baz</p>||should support adjacent setext headings",

            "^n====||<p>====</p>||should not support empty setext headings",

            "---^n---||<hr />^n<hr />||should prefer other constructs over setext headings (1)",

            "- foo^n-----||<ul>^n<li>foo</li>^n</ul>^n<hr />||should prefer other constructs over setext headings (2)",

            "    foo^n---||<pre><code>foo^n</code></pre>^n<hr />||should prefer other constructs over setext headings (3)",

            "> foo^n-----||<blockquote>^n<p>foo</p>^n</blockquote>^n<hr />||should prefer other constructs over setext headings (4)",

            "\\> foo^n------||<h2>&gt; foo</h2>||should support starting w/ character escapes",

            "Foo^nbar^n---^nbaz||<h2>Foo^nbar</h2>^n<p>baz</p>||paragraph and heading interplay (1)",

            "Foo^n^nbar^n---^nbaz||<p>Foo</p>^n<h2>bar</h2>^n<p>baz</p>||paragraph and heading interplay (2)",

            "Foo^nbar^n^n---^n^nbaz||<p>Foo^nbar</p>^n<hr />^n<p>baz</p>||paragraph and heading interplay (3)",

            "Foo^nbar^n* * *^nbaz||<p>Foo^nbar</p>^n<hr />^n<p>baz</p>||paragraph and heading interplay (4)",

            "Foo^nbar^n\\---^nbaz||<p>Foo^nbar^n---^nbaz</p>||paragraph and heading interplay (5)",

            // Extra:
            "Foo  ^nbar^n-----||<h2>Foo<br />^nbar</h2>||should support a hard break w/ spaces in between",

            "Foo\\^nbar^n-----||<h2>Foo<br />^nbar</h2>||should support a hard break w/ backslash in between",

            "a^n-^nb||<h2>a</h2>^n<p>b</p>||should prefer a setext heading over an interrupting list",

            "> ===^na||<blockquote>^n<p>===^na</p>^n</blockquote>||should not support lazyness (1)",

            "> a^n===||<blockquote>^n<p>a^n===</p>^n</blockquote>||should not support lazyness (2)",

    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("a^n-", "<p>a^n-</p>", "should support turning off setext underlines", "setextUnderline");
    }
}
