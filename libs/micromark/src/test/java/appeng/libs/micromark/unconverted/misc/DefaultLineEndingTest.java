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

public class DefaultLineEndingTest {
  t.equal(
    micromark("> a"),
    "<blockquote>\n<p>a</p>\n</blockquote>",
    "should use `\\n` default"
  )

  t.equal(
    micromark("> a\n"),
    "<blockquote>\n<p>a</p>\n</blockquote>\n",
    "should infer the first line ending (1)"
  )

  t.equal(
    micromark("> a\r"),
    "<blockquote>\r<p>a</p>\r</blockquote>\r",
    "should infer the first line ending (2)"
  )

  t.equal(
    micromark("> a\r\n"),
    "<blockquote>\r\n<p>a</p>\r\n</blockquote>\r\n",
    "should infer the first line ending (3)"
  )

  t.equal(
    micromark("> a", {defaultLineEnding: "\r"}),
    "<blockquote>\r<p>a</p>\r</blockquote>",
    "should support the given line ending"
  )

  t.equal(
    micromark("> a\n", {defaultLineEnding: "\r"}),
    "<blockquote>\r<p>a</p>\r</blockquote>\n",
    "should support the given line ending, even if line endings exist"
  )

  }
