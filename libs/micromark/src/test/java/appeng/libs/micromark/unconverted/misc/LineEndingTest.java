package appeng.libs.micromark.unconverted.misc;

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
  t.equal(
    micromark("a\nb"),
    "<p>a\nb</p>",
    "should support a line feed for a line ending inside a paragraph"
  )

  t.equal(
    micromark("a\rb"),
    "<p>a\rb</p>",
    "should support a carriage return for a line ending inside a paragraph"
  )

  t.equal(
    micromark("a\r\nb"),
    "<p>a\r\nb</p>",
    "should support a carriage return + line feed for a line ending inside a paragraph"
  )

  t.equal(
    micromark("\ta\n\tb"),
    "<pre><code>a\nb\n</code></pre>",
    "should support a line feed in indented code (and prefer it)"
  )

  t.equal(
    micromark("\ta\r\tb"),
    "<pre><code>a\rb\r</code></pre>",
    "should support a carriage return in indented code (and prefer it)"
  )

  t.equal(
    micromark("\ta\r\n\tb"),
    "<pre><code>a\r\nb\r\n</code></pre>",
    "should support a carriage return + line feed in indented code (and prefer it)"
  )

  t.equal(
    micromark("***\n### Heading"),
    "<hr />\n<h3>Heading</h3>",
    "should support a line feed between flow"
  )

  t.equal(
    micromark("***\r### Heading"),
    "<hr />\r<h3>Heading</h3>",
    "should support a carriage return between flow"
  )

  t.equal(
    micromark("***\r\n### Heading"),
    "<hr />\r\n<h3>Heading</h3>",
    "should support a carriage return + line feed between flow"
  )

  t.equal(
    micromark("***\n\n\n### Heading\n"),
    "<hr />\n<h3>Heading</h3>\n",
    "should support several line feeds between flow"
  )

  t.equal(
    micromark("***\r\r\r### Heading\r"),
    "<hr />\r<h3>Heading</h3>\r",
    "should support several carriage returns between flow"
  )

  t.equal(
    micromark("***\r\n\r\n\r\n### Heading\r\n"),
    "<hr />\r\n<h3>Heading</h3>\r\n",
    "should support several carriage return + line feeds between flow"
  )

  t.equal(
    micromark("```x\n\n\ny\n\n\n```\n\n\n"),
    "<pre><code class=\"language-x\">\n\ny\n\n\n</code></pre>\n",
    "should support several line feeds in fenced code"
  )

  t.equal(
    micromark("```x\r\r\ry\r\r\r```\r\r\r"),
    "<pre><code class=\"language-x\">\r\ry\r\r\r</code></pre>\r",
    "should support several carriage returns in fenced code"
  )

  t.equal(
    micromark("```x\r\n\r\n\r\ny\r\n\r\n\r\n```\r\n\r\n\r\n"),
    "<pre><code class=\"language-x\">\r\n\r\ny\r\n\r\n\r\n</code></pre>\r\n",
    "should support several carriage return + line feeds in fenced code"
  )

  t.equal(
    micromark("A\r\nB\r\n-\r\nC"),
    "<h2>A\r\nB</h2>\r\n<p>C</p>",
    "should support a carriage return + line feed in content"
  )

  t.equal(
    micromark("<div\n", {allowDangerousHtml: true}),
    "<div\n",
    "should support a line feed after html"
  )

  t.equal(
    micromark("<div\r", {allowDangerousHtml: true}),
    "<div\r",
    "should support a carriage return after html"
  )

  t.equal(
    micromark("<div\r\n", {allowDangerousHtml: true}),
    "<div\r\n",
    "should support a carriage return + line feed after html"
  )

  t.equal(
    micromark("<div>\n\nx", {allowDangerousHtml: true}),
    "<div>\n<p>x</p>",
    "should support a blank line w/ line feeds after html"
  )

  t.equal(
    micromark("<div>\r\rx", {allowDangerousHtml: true}),
    "<div>\r<p>x</p>",
    "should support a blank line w/ carriage returns after html"
  )

  t.equal(
    micromark("<div>\r\n\r\nx", {allowDangerousHtml: true}),
    "<div>\r\n<p>x</p>",
    "should support a blank line w/ carriage return + line feeds after html"
  )

  t.equal(
    micromark("<div>\nx", {allowDangerousHtml: true}),
    "<div>\nx",
    "should support a non-blank line w/ line feed in html"
  )

  t.equal(
    micromark("<div>\rx", {allowDangerousHtml: true}),
    "<div>\rx",
    "should support a non-blank line w/ carriage return in html"
  )

  t.equal(
    micromark("<div>\r\nx", {allowDangerousHtml: true}),
    "<div>\r\nx",
    "should support a non-blank line w/ carriage return + line feed in html"
  )

  }
