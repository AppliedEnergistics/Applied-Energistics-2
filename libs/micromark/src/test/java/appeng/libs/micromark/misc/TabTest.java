package appeng.libs.micromark.misc;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.TestUtil;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TabTest {
  @Nested
public class FlowTest {
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          "    x||<pre><code>x^n</code></pre>||should support a 4*SP to start code",
          "\tx||<pre><code>x^n</code></pre>||should support a HT to start code",
          " \tx||<pre><code>x^n</code></pre>||should support a SP + HT to start code",
          "  \tx||<pre><code>x^n</code></pre>||should support a 2*SP + HT to start code",
          "   \tx||<pre><code>x^n</code></pre>||should support a 3*SP + HT to start code",
          "    \tx||<pre><code>\tx^n</code></pre>||should support a 4*SP to start code, and leave the next HT as code data",
          "   \t# x||<pre><code># x^n</code></pre>||should not support a 3*SP + HT to start an ATX heading",
          "   \t> x||<pre><code>&gt; x^n</code></pre>||should not support a 3*SP + HT to start a block quote",
          "   \t- x||<pre><code>- x^n</code></pre>||should not support a 3*SP + HT to start a list item",
          "   \t---||<pre><code>---^n</code></pre>||should not support a 3*SP + HT to start a thematic break",
          "   \t---||<pre><code>---^n</code></pre>||should not support a 3*SP + HT to start a thematic break",
          "   \t```||<pre><code>```^n</code></pre>||should not support a 3*SP + HT to start a fenced code",
          "   \t<div>||<pre><code>&lt;div&gt;^n</code></pre>||should not support a 3*SP + HT to start HTML",
          "#\tx\t#\t||<h1>x</h1>||should support tabs around ATX heading sequences",
          "#\t\tx\t\t#\t\t||<h1>x</h1>||should support arbitrary tabs around ATX heading sequences",
          "```\tx\ty\t^n```\t||<pre><code class=\"language-x\"></code></pre>||should support tabs around fenced code fences, info, and meta",
          "```\t\tx\t\ty\t\t^n```\t\t||<pre><code class=\"language-x\"></code></pre>||should support arbitrary tabs around fenced code fences, info, and meta",
          "```x^n\t```||<pre><code class=\"language-x\">\t```^n</code></pre>^n||should not support tabs before fenced code closing fences",
          "*\t*\t*\t||<hr />||should support tabs in thematic breaks",
          "*\t\t*\t\t*\t\t||<hr />||should support arbitrary tabs in thematic breaks",
  })
  public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedHtml(markdown, expectedHtml);
  }
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          "<x\ty\tz\t=\t\"\tx\t\">||<x\ty\tz\t=\t\"\tx\t\">||should support tabs in HTML (if whitespace is allowed)",
  })
  public void testGeneratedHtmlUnsafe(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
  }

    }

    @Nested
    public class TextTest {
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          "<http:\t>||<p>&lt;http:\t&gt;</p>||should not support a tab to start an autolink w/ protocol’s rest",
          "<http:x\t>||<p>&lt;http:x\t&gt;</p>||should not support a tab in an autolink w/ protocol’s rest",
          "<example\t@x.com>||<p>&lt;example\t@x.com&gt;</p>||should not support a tab in an email autolink’s local part",
          "<example@x\ty.com>||<p>&lt;example@x\ty.com&gt;</p>||should not support a tab in an email autolink’s label",
          "\\\tx||<p>\\\tx</p>||should not support character escaped tab",
          "&#9;||<p>\t</p>||should support character reference resolving to a tab",
          "`\tx`||<p><code>\tx</code></p>||should support a tab starting code",
          "`x\t`||<p><code>x\t</code></p>||should support a tab ending code",
          "`\tx\t`||<p><code>\tx\t</code></p>||should support tabs around code",
          "`\tx `||<p><code>\tx </code></p>||should support a tab starting, and a space ending, code",
          "` x\t`||<p><code> x\t</code></p>||should support a space starting, and a tab ending, code",
          // Note: CM does not strip it in this case.
          // However, that should be a bug there: makes more sense to remove it like
          // trailing spaces.
          "x\t^ny||<p>x^ny</p>||should support a trailing tab at a line ending in a paragraph",
          "x^n\ty||<p>x^ny</p>||should support an initial tab after a line ending in a paragraph",
          "x[\ty](z)||<p>x<a href=\"z\">\ty</a></p>||should support an initial tab in a link label",
          "x[y\t](z)||<p>x<a href=\"z\">y\t</a></p>||should support a final tab in a link label",
          "[x\ty](z)||<p><a href=\"z\">x\ty</a></p>||should support a tab in a link label",
          // Note: CM.js bug, see: <https://github.com/commonmark/commonmark.js/issues/191>
          "[x](\ty)||<p><a href=\"y\">x</a></p>||should support a tab starting a link resource",
          "[x](y\t)||<p><a href=\"y\">x</a></p>||should support a tab ending a link resource",
          "[x](y\t\"z\")||<p><a href=\"y\" title=\"z\">x</a></p>||should support a tab between a link destination and title",
  })
  public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedHtml(markdown, expectedHtml);
  }

    }

    @Nested
    public class VirtualSpacesTest {
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          "```^n\tx||<pre><code>\tx^n</code></pre>^n||should support a tab in fenced code",
          " ```^n\tx||<pre><code>   x^n</code></pre>^n||should strip 1 space from an initial tab in fenced code if the opening fence is indented as such",
          "  ```^n\tx||<pre><code>  x^n</code></pre>^n||should strip 2 spaces from an initial tab in fenced code if the opening fence is indented as such",
          "   ```^n\tx||<pre><code> x^n</code></pre>^n||should strip 3 spaces from an initial tab in fenced code if the opening fence is indented as such",
  })
  public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedHtml(markdown, expectedHtml);
  }

    }

  }
