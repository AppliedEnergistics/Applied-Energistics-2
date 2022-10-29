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

const unsafe = {allowDangerousHtml: true}
public class HtmlTest {
  t.equal(
    micromark("<a><bab><c2c>", unsafe),
    "<p><a><bab><c2c></p>",
    "should support opening tags"
  )

  t.equal(
    micromark("<a/><b2/>", unsafe),
    "<p><a/><b2/></p>",
    "should support self-closing tags"
  )

  t.equal(
    micromark("<a  /><b2\ndata=\"foo\" >", unsafe),
    "<p><a  /><b2\ndata=\"foo\" ></p>",
    "should support whitespace in tags"
  )

  t.equal(
    micromark(
      "<a foo=\"bar\" bam = "baz <em>\"</em>"\n_boolean zoop:33=zoop:33 />",
      unsafe
    ),
    "<p><a foo=\"bar\" bam = "baz <em>\"</em>"\n_boolean zoop:33=zoop:33 /></p>",
    "should support attributes on tags"
  )

  t.equal(
    micromark("Foo <responsive-image src=\"foo.jpg\" />", unsafe),
    "<p>Foo <responsive-image src=\"foo.jpg\" /></p>",
    "should support non-html tags"
  )

  t.equal(
    micromark("<33> <__>", unsafe),
    "<p>&lt;33&gt; &lt;__&gt;</p>",
    "should not support nonconforming tag names"
  )

  t.equal(
    micromark("<a h*#ref=\"hi\">", unsafe),
    "<p>&lt;a h*#ref=&quot;hi&quot;&gt;</p>",
    "should not support nonconforming attribute names"
  )

  t.equal(
    micromark(\"<a href=\\"hi"> <a href=hi">\", unsafe),
    \"<p>&lt;a href=&quot;hi"&gt; &lt;a href=hi"&gt;</p>\",
    "should not support nonconforming attribute values"
  )

  t.equal(
    micromark("< a><\nfoo><bar/ >\n<foo bar=baz\nbim!bop />", unsafe),
    "<p>&lt; a&gt;&lt;\nfoo&gt;&lt;bar/ &gt;\n&lt;foo bar=baz\nbim!bop /&gt;</p>",
    "should not support nonconforming whitespace"
  )

  t.equal(
    micromark(\"<a href="bar"title=title>\", unsafe),
    \"<p>&lt;a href="bar"title=title&gt;</p>\",
    "should not support missing whitespace"
  )

  t.equal(
    micromark("</a></foo >", unsafe),
    "<p></a></foo ></p>",
    "should support closing tags"
  )

  t.equal(
    micromark("</a href=\"foo\">", unsafe),
    "<p>&lt;/a href=&quot;foo&quot;&gt;</p>",
    "should not support closing tags w/ attributes"
  )

  t.equal(
    micromark("foo <!-- this is a\ncomment - with hyphen -->", unsafe),
    "<p>foo <!-- this is a\ncomment - with hyphen --></p>",
    "should support comments"
  )

  t.equal(
    micromark("foo <!-- not a comment -- two hyphens -->", unsafe),
    "<p>foo &lt;!-- not a comment -- two hyphens --&gt;</p>",
    "should not support comments w/ two dashes inside"
  )

  t.equal(
    micromark("foo <!--> foo -->", unsafe),
    "<p>foo &lt;!--&gt; foo --&gt;</p>",
    "should not support nonconforming comments (1)"
  )

  t.equal(
    micromark("foo <!-- foo--->", unsafe),
    "<p>foo &lt;!-- foo---&gt;</p>",
    "should not support nonconforming comments (2)"
  )

  t.equal(
    micromark("foo <?php echo $a; ?>", unsafe),
    "<p>foo <?php echo $a; ?></p>",
    "should support instructions"
  )

  t.equal(
    micromark("foo <!ELEMENT br EMPTY>", unsafe),
    "<p>foo <!ELEMENT br EMPTY></p>",
    "should support declarations"
  )

  t.equal(
    micromark("foo <![CDATA[>&<]]>", unsafe),
    "<p>foo <![CDATA[>&<]]></p>",
    "should support cdata"
  )

  t.equal(
    micromark("foo <a href=\"&ouml;\">", unsafe),
    "<p>foo <a href=\"&ouml;\"></p>",
    "should support (ignore) character references"
  )

  t.equal(
    micromark("foo <a href=\"\\*\">", unsafe),
    "<p>foo <a href=\"\\*\"></p>",
    "should not support character escapes (1)"
  )

  t.equal(
    micromark("<a href=\"\\\"\">", unsafe),
    "<p>&lt;a href=&quot;&quot;&quot;&gt;</p>",
    "should not support character escapes (2)"
  )

  // Extra:
  t.equal(
    micromark("foo <!1>", unsafe),
    "<p>foo &lt;!1&gt;</p>",
    "should not support non-comment, non-cdata, and non-named declaration"
  )

  t.equal(
    micromark("foo <!-not enough!-->", unsafe),
    "<p>foo &lt;!-not enough!--&gt;</p>",
    "should not support comments w/ not enough dashes"
  )

  t.equal(
    micromark("foo <!---ok-->", unsafe),
    "<p>foo <!---ok--></p>",
    "should support comments that start w/ a dash, if it’s not followed by a greater than"
  )

  t.equal(
    micromark("foo <!--->", unsafe),
    "<p>foo &lt;!---&gt;</p>",
    "should not support comments that start w/ `->`"
  )

  t.equal(
    micromark("foo <!-- -> -->", unsafe),
    "<p>foo <!-- -> --></p>",
    "should support `->` in a comment"
  )

  t.equal(
    micromark("foo <!--", unsafe),
    "<p>foo &lt;!--</p>",
    "should not support eof in a comment (1)"
  )

  t.equal(
    micromark("foo <!--a", unsafe),
    "<p>foo &lt;!--a</p>",
    "should not support eof in a comment (2)"
  )

  t.equal(
    micromark("foo <!--a-", unsafe),
    "<p>foo &lt;!--a-</p>",
    "should not support eof in a comment (3)"
  )

  t.equal(
    micromark("foo <!--a--", unsafe),
    "<p>foo &lt;!--a--</p>",
    "should not support eof in a comment (4)"
  )

  // Note: cmjs parses this differently.
  // See: <https://github.com/commonmark/commonmark.js/issues/193>
  t.equal(
    micromark("foo <![cdata[]]>", unsafe),
    "<p>foo &lt;![cdata[]]&gt;</p>",
    "should not support lowercase “cdata”"
  )

  t.equal(
    micromark("foo <![CDATA", unsafe),
    "<p>foo &lt;![CDATA</p>",
    "should not support eof in a CDATA (1)"
  )

  t.equal(
    micromark("foo <![CDATA[", unsafe),
    "<p>foo &lt;![CDATA[</p>",
    "should not support eof in a CDATA (2)"
  )

  t.equal(
    micromark("foo <![CDATA[]", unsafe),
    "<p>foo &lt;![CDATA[]</p>",
    "should not support eof in a CDATA (3)"
  )

  t.equal(
    micromark("foo <![CDATA[]]", unsafe),
    "<p>foo &lt;![CDATA[]]</p>",
    "should not support eof in a CDATA (4)"
  )

  t.equal(
    micromark("foo <![CDATA[asd", unsafe),
    "<p>foo &lt;![CDATA[asd</p>",
    "should not support eof in a CDATA (5)"
  )

  t.equal(
    micromark("foo <![CDATA[]]]]>", unsafe),
    "<p>foo <![CDATA[]]]]></p>",
    "should support end-like constructs in CDATA"
  )

  t.equal(
    micromark("foo <!doctype", unsafe),
    "<p>foo &lt;!doctype</p>",
    "should not support eof in declarations"
  )

  t.equal(
    micromark("foo <?php", unsafe),
    "<p>foo &lt;?php</p>",
    "should not support eof in instructions (1)"
  )

  t.equal(
    micromark("foo <?php?", unsafe),
    "<p>foo &lt;?php?</p>",
    "should not support eof in instructions (2)"
  )

  t.equal(
    micromark("foo <???>", unsafe),
    "<p>foo <???></p>",
    "should support question marks in instructions"
  )

  t.equal(
    micromark("foo </3>", unsafe),
    "<p>foo &lt;/3&gt;</p>",
    "should not support closing tags that don’t start w/ alphas"
  )

  t.equal(
    micromark("foo </a->", unsafe),
    "<p>foo </a-></p>",
    "should support dashes in closing tags"
  )

  t.equal(
    micromark("foo </a   >", unsafe),
    "<p>foo </a   ></p>",
    "should support whitespace after closing tag names"
  )

  t.equal(
    micromark("foo </a!>", unsafe),
    "<p>foo &lt;/a!&gt;</p>",
    "should not support other characters after closing tag names"
  )

  t.equal(
    micromark("foo <a->", unsafe),
    "<p>foo <a-></p>",
    "should support dashes in opening tags"
  )

  t.equal(
    micromark("foo <a   >", unsafe),
    "<p>foo <a   ></p>",
    "should support whitespace after opening tag names"
  )

  t.equal(
    micromark("foo <a!>", unsafe),
    "<p>foo &lt;a!&gt;</p>",
    "should not support other characters after opening tag names"
  )

  t.equal(
    micromark("foo <a !>", unsafe),
    "<p>foo &lt;a !&gt;</p>",
    "should not support other characters in opening tags (1)"
  )

  t.equal(
    micromark("foo <a b!>", unsafe),
    "<p>foo &lt;a b!&gt;</p>",
    "should not support other characters in opening tags (2)"
  )

  t.equal(
    micromark("foo <a b/>", unsafe),
    "<p>foo <a b/></p>",
    "should support a self-closing slash after an attribute name"
  )

  t.equal(
    micromark("foo <a b>", unsafe),
    "<p>foo <a b></p>",
    "should support a greater than after an attribute name"
  )

  t.equal(
    micromark("foo <a b=<>", unsafe),
    "<p>foo &lt;a b=&lt;&gt;</p>",
    "should not support less than to start an unquoted attribute value"
  )

  t.equal(
    micromark("foo <a b=>>", unsafe),
    "<p>foo &lt;a b=&gt;&gt;</p>",
    "should not support greater than to start an unquoted attribute value"
  )

  t.equal(
    micromark("foo <a b==>", unsafe),
    "<p>foo &lt;a b==&gt;</p>",
    "should not support equals to to start an unquoted attribute value"
  )

  t.equal(
    micromark("foo <a b=`>"),
    "<p>foo &lt;a b=`&gt;</p>",
    "should not support grave accent to start an unquoted attribute value"
  )

  t.equal(
    micromark("foo <a b=\"asd", unsafe),
    "<p>foo &lt;a b=&quot;asd</p>",
    "should not support eof in double quoted attribute value"
  )

  t.equal(
    micromark(\"foo <a b="asd\", unsafe),
    \"<p>foo &lt;a b="asd</p>\",
    "should not support eof in single quoted attribute value"
  )

  t.equal(
    micromark("foo <a b=asd", unsafe),
    "<p>foo &lt;a b=asd</p>",
    "should not support eof in unquoted attribute value"
  )

  t.equal(
    micromark("foo <a b=\nasd>", unsafe),
    "<p>foo <a b=\nasd></p>",
    "should support an eol before an attribute value"
  )

  t.equal(
    micromark("<x> a", unsafe),
    "<p><x> a</p>",
    "should support starting a line w/ a tag if followed by anything other than an eol (after optional space/tabs)"
  )

  t.equal(
    micromark("<span foo=", unsafe),
    "<p>&lt;span foo=</p>",
    "should support an EOF before an attribute value"
  )

  t.equal(
    micromark("a <!b\nc>", unsafe),
    "<p>a <!b\nc></p>",
    "should support an EOL in a declaration"
  )
  t.equal(
    micromark("a <![CDATA[\n]]>", unsafe),
    "<p>a <![CDATA[\n]]></p>",
    "should support an EOL in cdata"
  )

  // Note: cmjs parses this differently.
  // See: <https://github.com/commonmark/commonmark.js/issues/196>
  t.equal(
    micromark("a <?\n?>", unsafe),
    "<p>a <?\n?></p>",
    "should support an EOL in an instruction"
  )

  t.equal(
    micromark("a <x>", {extensions: [{disable: {null: ["htmlText"]}}]}),
    "<p>a &lt;x&gt;</p>",
    "should support turning off html (text)"
  )

  }
