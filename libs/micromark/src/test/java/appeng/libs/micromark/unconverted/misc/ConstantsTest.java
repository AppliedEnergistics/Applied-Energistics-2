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


public class ConstantsTest {
  t.equal(
    constants.characterReferenceDecimalSizeMax,
    (0x10_ff_ff).toString(10).length,
    "`characterReferenceDecimalSizeMax`"
  )

  t.equal(
    constants.characterReferenceHexadecimalSizeMax,
    (0x10_ff_ff).toString(16).length,
    "`characterReferenceHexadecimalSizeMax`"
  )

  t.equal(
    constants.characterReferenceNamedSizeMax,
    longest(Object.keys(characterEntities)).length,
    "`characterReferenceNamedSizeMax`"
  )

  t.equal(
    constants.htmlRawSizeMax,
    longest(htmlRawNames).length,
    "`htmlRawSizeMax`"
  )

  }

/**
 * @param {Array<string>} list
 * @returns {string}
 */
function longest(list) {
  let index = -1
  /** @type {string} */
  let result = ""

  while (++index < list.length) {
    if (!result || list[index].length > result.length) {
      result = list[index]
    }
  }

  return result
}
