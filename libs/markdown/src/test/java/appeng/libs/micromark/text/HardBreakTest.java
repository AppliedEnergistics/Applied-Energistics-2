package appeng.libs.micromark.text;

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

public class HardBreakTest {
  @ParameterizedTest(name = "[{index}] {2}")
  @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
          "foo  ^nbaz||<p>foo<br />^nbaz</p>||should support two trailing spaces to form a hard break",
          "foo\\^nbaz||<p>foo<br />^nbaz</p>||should support a backslash to form a hard break",
          "foo       ^nbaz||<p>foo<br />^nbaz</p>||should support multiple trailing spaces",
          "foo  ^n     bar||<p>foo<br />^nbar</p>||should support leading spaces after a trailing hard break",
          "foo\\^n     bar||<p>foo<br />^nbar</p>||should support leading spaces after an escape hard break",
          "*foo  ^nbar*||<p><em>foo<br />^nbar</em></p>||should support trailing hard breaks in emphasis",
          "*foo\\^nbar*||<p><em>foo<br />^nbar</em></p>||should support escape hard breaks in emphasis",
          "`code  ^ntext`||<p><code>code   text</code></p>||should not support trailing hard breaks in code",
          "``code\\^ntext``||<p><code>code\\ text</code></p>||should not support escape hard breaks in code",
          "foo  ||<p>foo</p>||should not support trailing hard breaks at the end of a paragraph",
          "foo\\||<p>foo\\</p>||should not support escape hard breaks at the end of a paragraph",
          "### foo\\||<h3>foo\\</h3>||should not support escape hard breaks at the end of a heading",
          "### foo  ||<h3>foo</h3>||should not support trailing hard breaks at the end of a heading",
          "aaa  \t^nbb||<p>aaa^nbb</p>||should support a mixed line suffix (1)",
          "aaa\t  ^nbb||<p>aaa^nbb</p>||should support a mixed line suffix (2)",
          "aaa  \t  ^nbb||<p>aaa^nbb</p>||should support a mixed line suffix (3)",
          "aaa\0  ^nbb||<p>aaa�<br />^nbb</p>||should support a hard break after a replacement character",
          "aaa\0\t^nbb||<p>aaa�^nbb</p>||should support a line suffix after a replacement character",
          "*a*  ^nbb||<p><em>a</em><br />^nbb</p>||should support a hard break after a span",
          "*a*\t^nbb||<p><em>a</em>^nbb</p>||should support a line suffix after a span",
          "*a*  \t^nbb||<p><em>a</em>^nbb</p>||should support a mixed line suffix after a span (1)",
          "*a*\t  ^nbb||<p><em>a</em>^nbb</p>||should support a mixed line suffix after a span (2)",
          "*a*  \t  ^nbb||<p><em>a</em>^nbb</p>||should support a mixed line suffix after a span (3)",
  })
  public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
    TestUtil.assertGeneratedHtml(markdown, expectedHtml);
  }

  @Test
  void testDisabled() {
    TestUtil.assertGeneratedHtmlWithDisabled("a\\^nb", "<p>a\\^nb</p>", "should support turning off hard break (escape)", "hardBreakEscape");
  }

  }
