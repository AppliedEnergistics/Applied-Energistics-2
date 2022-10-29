package appeng.libs.micromark.unconverted.text;

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

public class SoftBreakTest {
  t.equal(
    micromark("foo\nbaz"),
    "<p>foo\nbaz</p>",
    "should support line endings"
  )

  t.equal(
    micromark("foo \n baz"),
    "<p>foo\nbaz</p>",
    "should trim spaces around line endings"
  )

  }
