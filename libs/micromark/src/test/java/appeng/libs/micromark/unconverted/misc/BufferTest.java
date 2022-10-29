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


public class BufferTest {
  t.equal(micromark(Buffer.from("")), "", "should support empty buffers")

  t.equal(
    micromark(Buffer.from("<admin@example.com>")),
    "<p><a href=\"mailto:admin@example.com\">admin@example.com</a></p>",
    "should support buffers"
  )

  t.equal(
    micromark(Buffer.from([0x62, 0x72, 0xc3, 0xa1, 0x76, 0x6f]), "ascii"),
    "<p>brC!vo</p>",
    "should support encoding"
  )

  }
