package appeng.libs.micromark.unconverted.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullByteTest {
  t.equal(
    micromark("asd\0asd"),
    "<p>asd�asd</p>",
    "should replace `\\0` w/ a replacement characters (`�`)"
  )

  t.equal(
    micromark("&#0;"),
    "<p>�</p>",
    "should replace NUL in a character reference"
  )

  // This doesn’t make sense in MD, as character escapes only work on ascii
  // punctuation, but it’s good to demonstrate the behavior.
  t.equal(
    micromark("\\0"),
    "<p>\\0</p>",
    "should not support NUL in a character escape"
  )

  }
