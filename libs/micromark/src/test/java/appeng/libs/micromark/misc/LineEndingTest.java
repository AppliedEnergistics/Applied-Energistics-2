package appeng.libs.micromark.misc;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.TestUtil;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class LineEndingTest {
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          "a^nb||<p>a^nb</p>||should support a line feed for a line ending inside a paragraph",

          "a\rb||<p>a\rb</p>||should support a carriage return for a line ending inside a paragraph",

          "a\r^nb||<p>a\r^nb</p>||should support a carriage return + line feed for a line ending inside a paragraph",

          "\ta^n\tb||<pre><code>a^nb^n</code></pre>||should support a line feed in indented code (and prefer it)",

          "\ta\r\tb||<pre><code>a\rb\r</code></pre>||should support a carriage return in indented code (and prefer it)",

          "\ta\r^n\tb||<pre><code>a\r^nb\r^n</code></pre>||should support a carriage return + line feed in indented code (and prefer it)",

          "***^n### Heading||<hr />^n<h3>Heading</h3>||should support a line feed between flow",

          "***\r### Heading||<hr />\r<h3>Heading</h3>||should support a carriage return between flow",

          "***\r^n### Heading||<hr />\r^n<h3>Heading</h3>||should support a carriage return + line feed between flow",

          "***^n^n^n### Heading^n||<hr />^n<h3>Heading</h3>^n||should support several line feeds between flow",

          "***\r\r\r### Heading\r||<hr />\r<h3>Heading</h3>\r||should support several carriage returns between flow",

          "***\r^n\r^n\r^n### Heading\r^n||<hr />\r^n<h3>Heading</h3>\r^n||should support several carriage return + line feeds between flow",

          "```x^n^n^ny^n^n^n```^n^n^n||<pre><code class=\"language-x\">^n^ny^n^n^n</code></pre>^n||should support several line feeds in fenced code",

          "```x\r\r\ry\r\r\r```\r\r\r||<pre><code class=\"language-x\">\r\ry\r\r\r</code></pre>\r||should support several carriage returns in fenced code",

          "```x\r^n\r^n\r^ny\r^n\r^n\r^n```\r^n\r^n\r^n||<pre><code class=\"language-x\">\r^n\r^ny\r^n\r^n\r^n</code></pre>\r^n||should support several carriage return + line feeds in fenced code",

          "A\r^nB\r^n-\r^nC||<h2>A\r^nB</h2>\r^n<p>C</p>||should support a carriage return + line feed in content",

  })
  public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedHtml(markdown, expectedHtml);
  }

  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          "<div^n||<div^n||should support a line feed after html",

          "<div\r||<div\r||should support a carriage return after html",

          "<div\r^n||<div\r^n||should support a carriage return + line feed after html",

          "<div>^n^nx||<div>^n<p>x</p>||should support a blank line w/ line feeds after html",

          "<div>\r\rx||<div>\r<p>x</p>||should support a blank line w/ carriage returns after html",

          "<div>\r^n\r^nx||<div>\r^n<p>x</p>||should support a blank line w/ carriage return + line feeds after html",

          "<div>^nx||<div>^nx||should support a non-blank line w/ line feed in html",

          "<div>\rx||<div>\rx||should support a non-blank line w/ carriage return in html",

          "<div>\r^nx||<div>\r^nx||should support a non-blank line w/ carriage return + line feed in html",

  })
  public void testGeneratedHtmlUnsafe(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
  }



  }
