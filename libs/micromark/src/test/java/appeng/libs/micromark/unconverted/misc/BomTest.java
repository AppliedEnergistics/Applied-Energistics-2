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


public class ByteOrderMarkTest {
  t.equal(micromark("\uFEFF"), "", "should ignore just a bom")

  t.equal(
    micromark("\uFEFF# hea\uFEFFding"),
    "<h1>hea\uFEFFding</h1>",
    "should ignore a bom"
  )

  t.equal(
    micromark(Buffer.from("\uFEFF")),
    "",
    "should ignore just a bom (buffer)"
  )

  t.equal(
    micromark(Buffer.from("\uFEFF# hea\uFEFFding")),
    "<h1>hea\uFEFFding</h1>",
    "should ignore a bom (buffer)"
  )
public class should ignore a bom (stream)Test {
    t.plan(1)

    slowStream("\uFEFF# hea\uFEFFding")
      .pipe(stream())
      .pipe(
        concat((result) => {
          t.equal(result, "<h1>hea\uFEFFding</h1>", "pass")
        })
      )
  })
public class should ignore just a bom (stream)Test {
    t.plan(1)

    slowStream("\uFEFF")
      .pipe(stream())
      .pipe(
        concat((result) => {
          t.equal(result, "", "pass")
        })
      )
  })

  }
