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

public class TextTest {
  t.equal(
    micromark(\"hello $.;"there\"),
    \"<p>hello $.;"there</p>\",
    "should support ascii text"
  )

  t.equal(
    micromark("Foo χρῆν"),
    "<p>Foo χρῆν</p>",
    "should support unicode text"
  )

  t.equal(
    micromark("Multiple     spaces"),
    "<p>Multiple     spaces</p>",
    "should preserve internal spaces verbatim"
  )

  }
