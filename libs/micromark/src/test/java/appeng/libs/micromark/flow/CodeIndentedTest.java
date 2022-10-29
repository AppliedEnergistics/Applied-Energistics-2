package appeng.libs.micromark.flow;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.TestUtil;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeIndentedTest {
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {

          "    a simple^n      indented code block||<pre><code>a simple^n  indented code block^n</code></pre>||should support indented code",
          "  - foo^n^n    bar||<ul>^n<li>^n<p>foo</p>^n<p>bar</p>^n</li>^n</ul>||should prefer list item content over indented code (1)",
          "1.  foo^n^n    - bar||<ol>^n<li>^n<p>foo</p>^n<ul>^n<li>bar</li>^n</ul>^n</li>^n</ol>||should prefer list item content over indented code (2)",
          "    <a/>^n    *hi*^n^n    - one||<pre><code>&lt;a/&gt;^n*hi*^n^n- one^n</code></pre>||should support blank lines in indented code (1)",
          "    chunk1^n^n    chunk2^n  ^n ^n ^n    chunk3||<pre><code>chunk1^n^nchunk2^n^n^n^nchunk3^n</code></pre>||should support blank lines in indented code (2)",
          "    chunk1^n      ^n      chunk2||<pre><code>chunk1^n  ^n  chunk2^n</code></pre>||should support blank lines in indented code (3)",
          "Foo^n    bar||<p>Foo^nbar</p>||should not support interrupting paragraphs",
          "    foo^nbar||<pre><code>foo^n</code></pre>^n<p>bar</p>||should support paragraphs directly after indented code",
          "# Heading^n    foo^nHeading^n------^n    foo^n----||<h1>Heading</h1>^n<pre><code>foo^n</code></pre>^n<h2>Heading</h2>^n<pre><code>foo^n</code></pre>^n<hr />||should mix w/ other content",
          "        foo^n    bar||<pre><code>    foo^nbar^n</code></pre>||should support extra whitespace on the first line",
          "^n    ^n    foo^n    ||<pre><code>foo^n</code></pre>||should not support initial blank lines",
          "    foo  ||<pre><code>foo  ^n</code></pre>||should support trailing whitespace",
          ">     a^nb||<blockquote>^n<pre><code>a^n</code></pre>^n</blockquote>^n<p>b</p>||should not support lazyness (1)",
          "> a^n    b||<blockquote>^n<p>a^nb</p>^n</blockquote>||should not support lazyness (2)",
          "> a^n     b||<blockquote>^n<p>a^nb</p>^n</blockquote>||should not support lazyness (3)",
          "> a^n      b||<blockquote>^n<p>a^nb</p>^n</blockquote>||should not support lazyness (4)",
          ">     a^n    b||<blockquote>^n<pre><code>a^n</code></pre>^n</blockquote>^n<pre><code>b^n</code></pre>||should not support lazyness (5)",
          ">     a^n     b||<blockquote>^n<pre><code>a^n</code></pre>^n</blockquote>^n<pre><code> b^n</code></pre>||should not support lazyness (6)",
          ">     a^n      b||<blockquote>^n<pre><code>a^n</code></pre>^n</blockquote>^n<pre><code>  b^n</code></pre>||should not support lazyness (7)",
  })
  public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedHtml(markdown, expectedHtml);
  }

  @Test
          public void testDisabled() {

    TestUtil.assertGeneratedHtmlWithDisabled("   a", "<p>a</p>", "should support turning off code (indented, 1)", "codeIndented");

    TestUtil.assertGeneratedHtmlWithDisabled("> a^n    b", "<blockquote>^n<p>a^nb</p>^n</blockquote>", "should support turning off code (indented, 2)", "codeIndented");

    TestUtil.assertGeneratedHtmlWithDisabled("- a^n    b", "<ul>^n<li>a^nb</li>^n</ul>", "should support turning off code (indented, 3)", "codeIndented");

    TestUtil.assertGeneratedHtmlWithDisabled("- a^n    - b", "<ul>^n<li>a^n<ul>^n<li>b</li>^n</ul>^n</li>^n</ul>", "should support turning off code (indented, 4)", "codeIndented");

    TestUtil.assertGeneratedHtmlWithDisabled("- a^n    - b", "<ul>^n<li>a^n<ul>^n<li>b</li>^n</ul>^n</li>^n</ul>", "should support turning off code (indented, 5)", "codeIndented");

    TestUtil.assertGeneratedHtmlWithDisabled("```^na^n    ```", "<pre><code>a^n</code></pre>", "should support turning off code (indented, 6)", "codeIndented");

    var opts = new ParseOptions();
    opts.withExtension(new Extension() {
      {nullDisable.add("codeIndented");}
    });
    TestUtil.assertGeneratedHtml(
            "a <?^n    ?>",
            "<p>a <?^n?></p>",
            "should support turning off code (indented, 7)",
            opts,
            new CompileOptions().allowDangerousHtml()
    );

    TestUtil.assertGeneratedHtmlWithDisabled("- Foo^n---", "<ul>^n<li>Foo</li>^n</ul>^n<hr />", "should support turning off code (indented, 8)", "codeIndented");

    TestUtil.assertGeneratedHtmlWithDisabled("- Foo^n     ---", "<ul>^n<li>^n<h2>Foo</h2>^n</li>^n</ul>", "should support turning off code (indented, 9)", "codeIndented");
  }

  }
