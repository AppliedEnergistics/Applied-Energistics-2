package appeng.libs.micromark.text;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class HtmlTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "<a><bab><c2c>||<p><a><bab><c2c></p>||should support opening tags",

            "<a/><b2/>||<p><a/><b2/></p>||should support self-closing tags",

            "<a  /><b2^ndata=\"foo\" >||<p><a  /><b2^ndata=\"foo\" ></p>||should support whitespace in tags",

            "<a foo=\"bar\" bam = \"baz <em>\"</em>\"^n_boolean zoop:33=zoop:33 />||<p><a foo=\"bar\" bam = \"baz <em>\"</em>\"^n_boolean zoop:33=zoop:33 /></p>||should support attributes on tags",

            "Foo <responsive-image src=\"foo.jpg\" />||<p>Foo <responsive-image src=\"foo.jpg\" /></p>||should support non-html tags",

            "<33> <__>||<p>&lt;33&gt; &lt;__&gt;</p>||should not support nonconforming tag names",

            "<a h*#ref=\"hi\">||<p>&lt;a h*#ref=&quot;hi&quot;&gt;</p>||should not support nonconforming attribute names",

            "<a href=\\\"hi\"> <a href=hi\">||<p>&lt;a href=&quot;hi\"&gt; &lt;a href=hi\"&gt;</p>||should not support nonconforming attribute values",

            "< a><^nfoo><bar/ >^n<foo bar=baz^nbim!bop />||<p>&lt; a&gt;&lt;^nfoo&gt;&lt;bar/ &gt;^n&lt;foo bar=baz^nbim!bop /&gt;</p>||should not support nonconforming whitespace",

            "<a href=\"bar\"title=title>||<p>&lt;a href=\"bar\"title=title&gt;</p>||should not support missing whitespace",

            "</a></foo >||<p></a></foo ></p>||should support closing tags",

            "</a href=\"foo\">||<p>&lt;/a href=&quot;foo&quot;&gt;</p>||should not support closing tags w/ attributes",

            "foo <!-- this is a^ncomment - with hyphen -->||<p>foo <!-- this is a^ncomment - with hyphen --></p>||should support comments",

            "foo <!-- not a comment -- two hyphens -->||<p>foo &lt;!-- not a comment -- two hyphens --&gt;</p>||should not support comments w/ two dashes inside",

            "foo <!--> foo -->||<p>foo &lt;!--&gt; foo --&gt;</p>||should not support nonconforming comments (1)",

            "foo <!-- foo--->||<p>foo &lt;!-- foo---&gt;</p>||should not support nonconforming comments (2)",

            "foo <?php echo $a; ?>||<p>foo <?php echo $a; ?></p>||should support instructions",

            "foo <!ELEMENT br EMPTY>||<p>foo <!ELEMENT br EMPTY></p>||should support declarations",

            "foo <![CDATA[>&<]]>||<p>foo <![CDATA[>&<]]></p>||should support cdata",

            "foo <a href=\"&ouml;\">||<p>foo <a href=\"&ouml;\"></p>||should support (ignore) character references",

            "foo <a href=\"\\*\">||<p>foo <a href=\"\\*\"></p>||should not support character escapes (1)",

            "<a href=\"\\\"\">||<p>&lt;a href=&quot;&quot;&quot;&gt;</p>||should not support character escapes (2)",

            // Extra:
            "foo <!1>||<p>foo &lt;!1&gt;</p>||should not support non-comment, non-cdata, and non-named declaration",

            "foo <!-not enough!-->||<p>foo &lt;!-not enough!--&gt;</p>||should not support comments w/ not enough dashes",

            "foo <!---ok-->||<p>foo <!---ok--></p>||should support comments that start w/ a dash, if it’s not followed by a greater than",

            "foo <!--->||<p>foo &lt;!---&gt;</p>||should not support comments that start w/ `->`",

            "foo <!-- -> -->||<p>foo <!-- -> --></p>||should support `->` in a comment",

            "foo <!--||<p>foo &lt;!--</p>||should not support eof in a comment (1)",

            "foo <!--a||<p>foo &lt;!--a</p>||should not support eof in a comment (2)",

            "foo <!--a-||<p>foo &lt;!--a-</p>||should not support eof in a comment (3)",

            "foo <!--a--||<p>foo &lt;!--a--</p>||should not support eof in a comment (4)",

            // Note: cmjs parses this differently.
            // See: <https://github.com/commonmark/commonmark.js/issues/193>
            "foo <![cdata[]]>||<p>foo &lt;![cdata[]]&gt;</p>||should not support lowercase “cdata”",

            "foo <![CDATA||<p>foo &lt;![CDATA</p>||should not support eof in a CDATA (1)",

            "foo <![CDATA[||<p>foo &lt;![CDATA[</p>||should not support eof in a CDATA (2)",

            "foo <![CDATA[]||<p>foo &lt;![CDATA[]</p>||should not support eof in a CDATA (3)",

            "foo <![CDATA[]]||<p>foo &lt;![CDATA[]]</p>||should not support eof in a CDATA (4)",

            "foo <![CDATA[asd||<p>foo &lt;![CDATA[asd</p>||should not support eof in a CDATA (5)",

            "foo <![CDATA[]]]]>||<p>foo <![CDATA[]]]]></p>||should support end-like constructs in CDATA",

            "foo <!doctype||<p>foo &lt;!doctype</p>||should not support eof in declarations",

            "foo <?php||<p>foo &lt;?php</p>||should not support eof in instructions (1)",

            "foo <?php?||<p>foo &lt;?php?</p>||should not support eof in instructions (2)",

            "foo <???>||<p>foo <???></p>||should support question marks in instructions",

            "foo </3>||<p>foo &lt;/3&gt;</p>||should not support closing tags that don’t start w/ alphas",

            "foo </a->||<p>foo </a-></p>||should support dashes in closing tags",

            "foo </a   >||<p>foo </a   ></p>||should support whitespace after closing tag names",

            "foo </a!>||<p>foo &lt;/a!&gt;</p>||should not support other characters after closing tag names",

            "foo <a->||<p>foo <a-></p>||should support dashes in opening tags",

            "foo <a   >||<p>foo <a   ></p>||should support whitespace after opening tag names",

            "foo <a!>||<p>foo &lt;a!&gt;</p>||should not support other characters after opening tag names",

            "foo <a !>||<p>foo &lt;a !&gt;</p>||should not support other characters in opening tags (1)",

            "foo <a b!>||<p>foo &lt;a b!&gt;</p>||should not support other characters in opening tags (2)",

            "foo <a b/>||<p>foo <a b/></p>||should support a self-closing slash after an attribute name",

            "foo <a b>||<p>foo <a b></p>||should support a greater than after an attribute name",

            "foo <a b=<>||<p>foo &lt;a b=&lt;&gt;</p>||should not support less than to start an unquoted attribute value",

            "foo <a b=>>||<p>foo &lt;a b=&gt;&gt;</p>||should not support greater than to start an unquoted attribute value",

            "foo <a b==>||<p>foo &lt;a b==&gt;</p>||should not support equals to to start an unquoted attribute value",

            "foo <a b=`>||<p>foo &lt;a b=`&gt;</p>||should not support grave accent to start an unquoted attribute value",

            "foo <a b=\"asd||<p>foo &lt;a b=&quot;asd</p>||should not support eof in double quoted attribute value",

            "foo <a b=\"asd\"||<p>foo &lt;a b=\"asd</p>||should not support eof in single quoted attribute value",

            "foo <a b=asd||<p>foo &lt;a b=asd</p>||should not support eof in unquoted attribute value",

            "foo <a b=^nasd>||<p>foo <a b=^nasd></p>||should support an eol before an attribute value",

            "<x> a||<p><x> a</p>||should support starting a line w/ a tag if followed by anything other than an eol (after optional space/tabs)",

            "<span foo=||<p>&lt;span foo=</p>||should support an EOF before an attribute value",

            "a <!b^nc>||<p>a <!b^nc></p>||should support an EOL in a declaration",
            "a <![CDATA[^n]]>||<p>a <![CDATA[^n]]></p>||should support an EOL in cdata",

            // Note: cmjs parses this differently.
            // See: <https://github.com/commonmark/commonmark.js/issues/196>
            "a <?^n?>||<p>a <?^n?></p>||should support an EOL in an instruction",

    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("a <x>", "<p>a &lt;x&gt;</p>", "should support turning off html (text)", "htmlText");
    }

}
