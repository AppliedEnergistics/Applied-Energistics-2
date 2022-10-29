package appeng.libs.micromark.flow;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ThematicBreakTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "***^n---^n___||<hr />^n<hr />^n<hr />||should support thematic breaks w/ asterisks, dashes, and underscores",

            "+++||<p>+++</p>||should not support thematic breaks w/ plusses",

            "===||<p>===</p>||should not support thematic breaks w/ equals",

            "--||<p>--</p>||should not support thematic breaks w/ two dashes",

            "**||<p>**</p>||should not support thematic breaks w/ two asterisks",

            "__||<p>__</p>||should not support thematic breaks w/ two underscores",

            " ***||<hr />||should support thematic breaks w/ 1 space",

            "  ***||<hr />||should support thematic breaks w/ 2 spaces",

            "   ***||<hr />||should support thematic breaks w/ 3 spaces",

            "    ***||<pre><code>***^n</code></pre>||should not support thematic breaks w/ 4 spaces",

            "Foo^n    ***||<p>Foo^n***</p>||should not support thematic breaks w/ 4 spaces as paragraph continuation",

            "_____________________________________||<hr />||should support thematic breaks w/ many markers",

            " - - -||<hr />||should support thematic breaks w/ spaces (1)",

            " **  * ** * ** * **||<hr />||should support thematic breaks w/ spaces (2)",

            "-     -      -      -||<hr />||should support thematic breaks w/ spaces (3)",

            "- - - -    ||<hr />||should support thematic breaks w/ trailing spaces",

            "_ _ _ _ a||<p>_ _ _ _ a</p>||should not support thematic breaks w/ other characters (1)",

            "a------||<p>a------</p>||should not support thematic breaks w/ other characters (2)",

            "---a---||<p>---a---</p>||should not support thematic breaks w/ other characters (3)",

            " *-*||<p><em>-</em></p>||should not support thematic breaks w/ mixed markers",

            "- foo^n***^n- bar||<ul>^n<li>foo</li>^n</ul>^n<hr />^n<ul>^n<li>bar</li>^n</ul>||should support thematic breaks mixed w/ lists (1)",

            "* Foo^n* * *^n* Bar||<ul>^n<li>Foo</li>^n</ul>^n<hr />^n<ul>^n<li>Bar</li>^n</ul>||should support thematic breaks mixed w/ lists (2)",

            "Foo^n***^nbar||<p>Foo</p>^n<hr />^n<p>bar</p>||should support thematic breaks interrupting paragraphs",

            "Foo^n---^nbar||<h2>Foo</h2>^n<p>bar</p>||should not support thematic breaks w/ dashes interrupting paragraphs (setext heading)",

            "- Foo^n- * * *||<ul>^n<li>Foo</li>^n<li>^n<hr />^n</li>^n</ul>||should support thematic breaks in lists",

            "> ---^na||<blockquote>^n<hr />^n</blockquote>^n<p>a</p>||should not support lazyness (1)",

            "> a^n---||<blockquote>^n<p>a</p>^n</blockquote>^n<hr />||should not support lazyness (2)",

    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("***", "<p>***</p>", "should support turning off thematic breaks", "thematicBreak");
    }
}
