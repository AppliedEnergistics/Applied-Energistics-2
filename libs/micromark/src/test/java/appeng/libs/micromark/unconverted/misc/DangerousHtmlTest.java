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

public class DangerousHtmlTest {
  t.equal(micromark("<x>"), "&lt;x&gt;", "should be safe by default for flow")

  t.equal(
    micromark("a<b>"),
    "<p>a&lt;b&gt;</p>",
    "should be safe by default for text"
  )

  t.equal(
    micromark("<x>", {allowDangerousHtml: true}),
    "<x>",
    "should be unsafe w/ `allowDangerousHtml`"
  )

  }
