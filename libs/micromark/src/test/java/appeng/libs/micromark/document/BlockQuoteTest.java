package appeng.libs.micromark.document;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.TestUtil;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockQuoteTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "> # a^n> b^n> c||<blockquote>^n<h1>a</h1>^n<p>b^nc</p>^n</blockquote>||should support block quotes",
            "># a^n>b^n> c||<blockquote>^n<h1>a</h1>^n<p>b^nc</p>^n</blockquote>||should support block quotes w/o space",
            "   > # a^n   > b^n > c||<blockquote>^n<h1>a</h1>^n<p>b^nc</p>^n</blockquote>||should support prefixing block quotes w/ spaces",
            "    > # a^n    > b^n    > c||<pre><code>&gt; # a^n&gt; b^n&gt; c^n</code></pre>||should not support block quotes w/ 4 spaces",
            "> # a^n> b^nc||<blockquote>^n<h1>a</h1>^n<p>b^nc</p>^n</blockquote>||should support lazy content lines",
            "> a^nb^n> c||<blockquote>^n<p>a^nb^nc</p>^n</blockquote>||should support lazy content lines inside block quotes",
            "> a^n> ---||<blockquote>^n<h2>a</h2>^n</blockquote>||should support setext headings underlines in block quotes",
            "> a^n---||<blockquote>^n<p>a</p>^n</blockquote>^n<hr />||should not support lazy setext headings underlines in block quotes",
            "> - a^n> - b||<blockquote>^n<ul>^n<li>a</li>^n<li>b</li>^n</ul>^n</blockquote>||should support lists in block quotes",
            "> - a^n- b||<blockquote>^n<ul>^n<li>a</li>^n</ul>^n</blockquote>^n<ul>^n<li>b</li>^n</ul>||should not support lazy lists in block quotes",
            ">     a^n    b||<blockquote>^n<pre><code>a^n</code></pre>^n</blockquote>^n<pre><code>b^n</code></pre>||should not support lazy indented code in block quotes",
            "> ```^na^n```||<blockquote>^n<pre><code></code></pre>^n</blockquote>^n<p>a</p>^n<pre><code></code></pre>^n||should not support lazy fenced code in block quotes",
            "> a^n    - b||<blockquote>^n<p>a^n- b</p>^n</blockquote>||should not support lazy indented code (or lazy list) in block quotes",
            ">||<blockquote>^n</blockquote>||should support empty block quotes (1)",
            ">^n>  ^n> ||<blockquote>^n</blockquote>||should support empty block quotes (2)",
            ">^n> a^n>  ||<blockquote>^n<p>a</p>^n</blockquote>||should support initial or final lazy empty block quote lines",
            "> a^n^n> b||<blockquote>^n<p>a</p>^n</blockquote>^n<blockquote>^n<p>b</p>^n</blockquote>||should support adjacent block quotes",
            "> a^n> b||<blockquote>^n<p>a^nb</p>^n</blockquote>||should support a paragraph in a block quote",
            "> a^n>^n> b||<blockquote>^n<p>a</p>^n<p>b</p>^n</blockquote>||should support adjacent paragraphs in block quotes",
            "a^n> b||<p>a</p>^n<blockquote>^n<p>b</p>^n</blockquote>||should support interrupting paragraphs w/ block quotes",
            "> a^n***^n> b||<blockquote>^n<p>a</p>^n</blockquote>^n<hr />^n<blockquote>^n<p>b</p>^n</blockquote>||should support interrupting block quotes w/ thematic breaks",
            "> a^nb||<blockquote>^n<p>a^nb</p>^n</blockquote>||should not support interrupting block quotes w/ paragraphs",
            "> a^n^nb||<blockquote>^n<p>a</p>^n</blockquote>^n<p>b</p>||should support interrupting block quotes w/ blank lines",
            "> a^n>^nb||<blockquote>^n<p>a</p>^n</blockquote>^n<p>b</p>||should not support interrupting a blank line in a block quotes w/ paragraphs",
            "> > > a^nb||<blockquote>^n<blockquote>^n<blockquote>^n<p>a^nb</p>^n</blockquote>^n</blockquote>^n</blockquote>||should not support interrupting many block quotes w/ paragraphs (1)",
            ">>> a^n> b^n>>c||<blockquote>^n<blockquote>^n<blockquote>^n<p>a^nb^nc</p>^n</blockquote>^n</blockquote>^n</blockquote>||should not support interrupting many block quotes w/ paragraphs (2)",
            ">     a^n^n>    b||<blockquote>^n<pre><code>a^n</code></pre>^n</blockquote>^n<blockquote>^n<p>b</p>^n</blockquote>||should support 5 spaces for indented code, not 4",
    })
    public void testBlockquote(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testDisableBlockQuotes() {
        var parseOptions = new ParseOptions();
        parseOptions.withExtension(new Extension() {
            {
                nullDisable.add("blockQuote");
            }
        });

        assertEquals(
                "<p>&gt; # a\n&gt; b\n&gt; c</p>",
                new HtmlCompiler().compile(Micromark.parseAndPostprocess("> # a\n> b\n> c", parseOptions)),
                "should support turning off block quotes"
        );
    }
}
