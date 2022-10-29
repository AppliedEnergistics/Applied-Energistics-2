package appeng.libs.micromark.flow;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

public class HtmlTest {

    @Nested
    public class RawTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<style^n  type=\"text/css\">^n^nfoo||<style^n  type=\"text/css\">^n^nfoo||should support raw tags w/o ending",

                "<style>p{color:red;}</style>^n*foo*||<style>p{color:red;}</style>^n<p><em>foo</em></p>||should support raw tags w/ start and end on a single line",

                "<script>^nfoo^n</script>1. *bar*||<script>^nfoo^n</script>1. *bar*||should support raw tags w/ more data on ending line",

                "<script||<script||should support an eof directly after a raw tag name",

                "</script^nmore||<p>&lt;/script^nmore</p>||should not support a raw closing tag",

                "<script/||<p>&lt;script/</p>||should not support an eof after a self-closing slash",

                "<script/^n*asd*||<p>&lt;script/^n<em>asd</em></p>||should not support a line ending after a self-closing slash",

                "<script/>||<script/>||should support an eof after a self-closing tag",

                "<script/>^na||<script/>^na||should support a line ending after a self-closing tag",

                "<script/>a||<p><script/>a</p>||should not support other characters after a self-closing tag",

                "<script>a||<script>a||should support other characters after a raw opening tag",

                // Extra.
                "Foo^n<script||<p>Foo</p>^n<script||should support interrupting paragraphs w/ raw tags",

                "<script>^n  ^n  ^n</script>||<script>^n  ^n  ^n</script>||should support blank lines in raw",

                "> <script>^na||<blockquote>^n<script>^n</blockquote>^n<p>a</p>||should not support lazyness (1)",

                "> a^n<script>||<blockquote>^n<p>a</p>^n</blockquote>^n<script>||should not support lazyness (2)",

        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
        }

        @Test
        public void testShouldSupportRawPreTags() {
            TestUtil.assertGeneratedHtmlLinesUnsafe(
                    List.of(
                            "<pre language=\"haskell\"><code>",
                            "import Text.HTML.TagSoup",
                            "",
                            "main :: IO ()",
                            "main = print $ parseTags tags",
                            "</code></pre>",
                            "okay"
                    ),
                    List.of(
                            "<pre language=\"haskell\"><code>",
                            "import Text.HTML.TagSoup",
                            "",
                            "main :: IO ()",
                            "main = print $ parseTags tags",
                            "</code></pre>",
                            "<p>okay</p>"
                    )
            );
        }
        @Test
        public void testShouldSupportRawScriptTags() {
            TestUtil.assertGeneratedHtmlLinesUnsafe(
                    List.of(
                            "<script type=\"text/javascript\">",
                            "// JavaScript example",
                            "",
                            "document.getElementById(\"demo\").innerHTML = \"Hello JavaScript!\";",
                            "</script>",
                            "okay"
                    ),
                    List.of(
                            "<script type=\"text/javascript\">",
                            "// JavaScript example",
                            "",
                            "document.getElementById(\"demo\").innerHTML = \"Hello JavaScript!\";",
                            "</script>",
                            "<p>okay</p>"
                    )
            );
        }
        @Test
        public void testShouldSupportRawStyleTags() {
            TestUtil.assertGeneratedHtmlLinesUnsafe(
                    List.of(
                            "<style",
                            "  type=\"text/css\">",
                            "h1 {color:red;}",
                            "",
                            "p {color:blue;}",
                            "</style>",
                            "okay"
                    ),
                    List.of(
                            "<style",
                            "  type=\"text/css\">",
                            "h1 {color:red;}",
                            "",
                            "p {color:blue;}",
                            "</style>",
                            "<p>okay</p>"
                    )
            );
        }
    }

    @Nested
    public class CommentTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<!-- Foo^n^nbar^n   baz -->^nokay||<!-- Foo^n^nbar^n   baz -->^n<p>okay</p>||should support comments (type 2)",
                "<!-- foo -->*bar*^n*baz*||<!-- foo -->*bar*^n<p><em>baz</em></p>||should support comments w/ start and end on a single line",
                "<!-asd-->||<p>&lt;!-asd--&gt;</p>||should not support a single dash to start comments",
                "<!-->||<!-->||should support comments where the start dashes are the end dashes (1)",
                "<!--->||<!--->||should support comments where the start dashes are the end dashes (2)",
                "<!---->||<!---->||should support empty comments",
                // If the `\"` is encoded, we’re in text. If it remains, we’re in HTML.
                "<!--^n->^n\"||<!--^n->^n\"||should not end a comment at one dash (`->`)",
                "<!--^n-->^n\"||<!--^n-->^n<p>&quot;</p>||should end a comment at two dashes (`-->`)",
                "<!--^n--->^n\"||<!--^n--->^n<p>&quot;</p>||should end a comment at three dashes (`--->`)",
                "<!--^n---->^n\"||<!--^n---->^n<p>&quot;</p>||should end a comment at four dashes (`---->`)",
                "  <!-- foo -->||  <!-- foo -->||should support comments w/ indent",
                "    <!-- foo -->||<pre><code>&lt;!-- foo --&gt;^n</code></pre>||should not support comments w/ a 4 character indent",
                // Extra.
                "Foo^n<!--||<p>Foo</p>^n<!--||should support interrupting paragraphs w/ comments",
                "<!--^n  ^n  ^n-->||<!--^n  ^n  ^n-->||should support blank lines in comments",
                "> <!--^na||<blockquote>^n<!--^n</blockquote>^n<p>a</p>||should not support lazyness (1)",
                "> a^n<!--||<blockquote>^n<p>a</p>^n</blockquote>^n<!--||should not support lazyness (2)",
        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
        }
    }

    @Nested
    public class InstructionTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<?php^n^n  echo '>';^n^n?>^nokay||<?php^n^n  echo '>';^n^n?>^n<p>okay</p>||should support instructions (type 3)",

                "<?>||<?>||should support empty instructions where the `?` is part of both the start and the end",

                "<??>||<??>||should support empty instructions",

                // Extra.
                "Foo^n<?||<p>Foo</p>^n<?||should support interrupting paragraphs w/ instructions",

                "<?^n  ^n  ^n?>||<?^n  ^n  ^n?>||should support blank lines in instructions",

                "> <?^na||<blockquote>^n<?^n</blockquote>^n<p>a</p>||should not support lazyness (1)",

                "> a^n<?||<blockquote>^n<p>a</p>^n</blockquote>^n<?||should not support lazyness (2)",

        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
        }
    }

    @Nested
    public class DeclarationTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<!DOCTYPE html>||<!DOCTYPE html>||should support declarations (type 4)",

                "<!123>||<p>&lt;!123&gt;</p>||should not support declarations that start w/o an alpha",

                "<!>||<p>&lt;!&gt;</p>||should not support declarations w/o an identifier",

                "<!a>||<!a>||should support declarations w/o a single alpha as identifier",

                // Extra.
                "Foo^n<!d||<p>Foo</p>^n<!d||should support interrupting paragraphs w/ declarations",

                // Note about the lower letter:
                // <https://github.com/commonmark/commonmark-spec/pull/621>
                "<!a^n  ^n  ^n>||<!a^n  ^n  ^n>||should support blank lines in declarations",

                "> <!a^nb||<blockquote>^n<!a^n</blockquote>^n<p>b</p>||should not support lazyness (1)",

                "> a^n<!b||<blockquote>^n<p>a</p>^n</blockquote>^n<!b||should not support lazyness (2)",
        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
        }
    }

    @Nested
    public class CdataTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<![CDATA[^nfunction matchwo(a,b)^n{^n  if (a < b && a < 0) then {^n    return 1;^n^n  } else {^n^n    return 0;^n  }^n}^n]]>^nokay||<![CDATA[^nfunction matchwo(a,b)^n{^n  if (a < b && a < 0) then {^n    return 1;^n^n  } else {^n^n    return 0;^n  }^n}^n]]>^n<p>okay</p>||should support cdata (type 5)",

                "<![CDATA[]]>||<![CDATA[]]>||should support empty cdata",

                "<![CDATA]]>||<p>&lt;![CDATA]]&gt;</p>||should not support cdata w/ a missing `[`",

                "<![CDATA[]]]>||<![CDATA[]]]>||should support cdata w/ a single `]` as content",

                // Extra.
                "Foo^n<![CDATA[||<p>Foo</p>^n<![CDATA[||should support interrupting paragraphs w/ cdata",

                // Note: cmjs parses this differently.
                // See: <https://github.com/commonmark/commonmark.js/issues/193>
                "<![cdata[]]>||<p>&lt;![cdata[]]&gt;</p>||should not support lowercase cdata",

                "<![CDATA[^n  ^n  ^n]]>||<![CDATA[^n  ^n  ^n]]>||should support blank lines in cdata",

                "> <![CDATA[^na||<blockquote>^n<![CDATA[^n</blockquote>^n<p>a</p>||should not support lazyness (1)",

                "> a^n<![CDATA[||<blockquote>^n<p>a</p>^n</blockquote>^n<![CDATA[||should not support lazyness (2)",
        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
        }

    }

    @Nested
    public class BasicTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<table><tr><td>^n<pre>^n**Hello**,^n^n_world_.^n</pre>^n</td></tr></table>||<table><tr><td>^n<pre>^n**Hello**,^n<p><em>world</em>.^n</pre></p>^n</td></tr></table>||should support html (basic)",
                "<table>^n  <tr>^n    <td>^n           hi^n    </td>^n  </tr>^n</table>^n^nokay.||<table>^n  <tr>^n    <td>^n           hi^n    </td>^n  </tr>^n</table>^n<p>okay.</p>||should support html of type 6 (1)",

                " <div>^n  *hello*^n         <foo><a>|| <div>^n  *hello*^n         <foo><a>||should support html of type 6 (2)",

                "</div>^n*foo*||</div>^n*foo*||should support html starting w/ a closing tag",

                "<DIV CLASS=\"foo\">^n^n*Markdown*^n^n</DIV>||<DIV CLASS=\"foo\">^n<p><em>Markdown</em></p>^n</DIV>||should support html w/ markdown in between",

                "<div id=\"foo\"^n  class=\"bar\">^n</div>||<div id=\"foo\"^n  class=\"bar\">^n</div>||should support html w/ line endings (1)",

                "<div id=\"foo\" class=\"bar^n  baz\">^n</div>||<div id=\"foo\" class=\"bar^n  baz\">^n</div>||should support html w/ line endings (2)",

                "<div>^n*foo*^n^n*bar*||<div>^n*foo*^n<p><em>bar</em></p>||should support an unclosed html element",

                "<div id=\"foo\"^n*hi*||<div id=\"foo\"^n*hi*||should support garbage html (1)",

                "<div class^nfoo||<div class^nfoo||should support garbage html (2)",

                "<div *???-&&&-<---^n*foo*||<div *???-&&&-<---^n*foo*||should support garbage html (3)",

                "<div><a href=\"bar\">*foo*</a></div>||<div><a href=\"bar\">*foo*</a></div>||should support other tags in the opening (1)",

                "<table><tr><td>^nfoo^n</td></tr></table>||<table><tr><td>^nfoo^n</td></tr></table>||should support other tags in the opening (2)",

                "<div></div>^n``` c^nint x = 33;^n```||<div></div>^n``` c^nint x = 33;^n```||should include everything ’till a blank line",

                "> <div>^n> foo^n^nbar||<blockquote>^n<div>^nfoo^n</blockquote>^n<p>bar</p>||should support basic tags w/o ending in containers (1)",

                "- <div>^n- foo||<ul>^n<li>^n<div>^n</li>^n<li>foo</li>^n</ul>||should support basic tags w/o ending in containers (2)",

                "  <div>||  <div>||should support basic tags w/ indent",

                "    <div>||<pre><code>&lt;div&gt;^n</code></pre>||should not support basic tags w/ a 4 character indent",

                "Foo^n<div>^nbar^n</div>||<p>Foo</p>^n<div>^nbar^n</div>||should support interrupting paragraphs w/ basic tags",

                "<div>^nbar^n</div>^n*foo*||<div>^nbar^n</div>^n*foo*||should require a blank line to end",

                "<div>^n^n*Emphasized* text.^n^n</div>||<div>^n<p><em>Emphasized</em> text.</p>^n</div>||should support interleaving w/ blank lines",

                "<div>^n*Emphasized* text.^n</div>||<div>^n*Emphasized* text.^n</div>||should not support interleaving w/o blank lines",

                "<table>^n^n<tr>^n^n<td>^nHi^n</td>^n^n</tr>^n^n</table>||<table>^n<tr>^n<td>^nHi^n</td>^n</tr>^n</table>||should support blank lines between adjacent html",

                "<table>^n^n  <tr>^n^n    <td>^n      Hi^n    </td>^n^n  </tr>^n^n</table>||<table>^n  <tr>^n<pre><code>&lt;td&gt;^n  Hi^n&lt;/td&gt;^n</code></pre>^n  </tr>^n</table>||should not support indented, blank-line delimited, adjacent html",

                "</1>||<p>&lt;/1&gt;</p>||should not support basic tags w/ an incorrect name start character",

                "<div||<div||should support an eof directly after a basic tag name",

                "<div^n||<div^n||should support a line ending directly after a tag name",

                "<div ||<div ||should support an eof after a space directly after a tag name",

                "<div/||<p>&lt;div/</p>||should not support an eof directly after a self-closing slash",

                "<div/^n*asd*||<p>&lt;div/^n<em>asd</em></p>||should not support a line ending after a self-closing slash",

                "<div/>||<div/>||should support an eof after a self-closing tag",

                "<div/>^na||<div/>^na||should support a line ending after a self-closing tag",

                "<div/>a||<div/>a||should support another character after a self-closing tag",

                "<div>a||<div>a||should support another character after a basic opening tag",

                // Extra.
                "Foo^n<div/>||<p>Foo</p>^n<div/>||should support interrupting paragraphs w/ self-closing basic tags",

                "<div^n  ^n  ^n>||<div^n<blockquote>^n</blockquote>||should not support blank lines in basic",

                "> <div^na||<blockquote>^n<div^n</blockquote>^n<p>a</p>||should not support lazyness (1)",

                "> a^n<div||<blockquote>^n<p>a</p>^n</blockquote>^n<div||should not support lazyness (2)",

        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
        }

    }

    @Nested
    public class CompleteTest {
        @ParameterizedTest(name = "[{index}] {2}")
        @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
                "<a href=\"foo\">^n*bar*^n</a>||<a href=\"foo\">^n*bar*^n</a>||should support complete tags (type 7)",

                "<Warning>^n*bar*^n</Warning>||<Warning>^n*bar*^n</Warning>||should support non-html tag names",

                "<i class=\"foo\">^n*bar*^n</i>||<i class=\"foo\">^n*bar*^n</i>||should support non-“block” html tag names (1)",

                "<del>^n*foo*^n</del>||<del>^n*foo*^n</del>||should support non-“block” html tag names (2)",

                "</ins>^n*bar*||</ins>^n*bar*||should support closing tags",

                "<del>^n^n*foo*^n^n</del>||<del>^n<p><em>foo</em></p>^n</del>||should support interleaving",

                "<del>*foo*</del>||<p><del><em>foo</em></del></p>||should not support interleaving w/o blank lines",

                "<div>^n  ^nasd||<div>^n<p>asd</p>||should support interleaving w/ whitespace-only blank lines",

                "Foo^n<a href=\"bar\">^nbaz||<p>Foo^n<a href=\"bar\">^nbaz</p>||should not support interrupting paragraphs w/ complete tags",

                "<x||<p>&lt;x</p>||should not support an eof directly after a tag name",

                "<x/||<p>&lt;x/</p>||should not support an eof directly after a self-closing slash",

                "<x^n||<p>&lt;x</p>^n||should not support a line ending directly after a tag name",

                "<x ||<p>&lt;x</p>||should not support an eof after a space directly after a tag name",

                "<x/||<p>&lt;x/</p>||should not support an eof directly after a self-closing slash",

                "<x/^n*asd*||<p>&lt;x/^n<em>asd</em></p>||should not support a line ending after a self-closing slash",

                "<x/>||<x/>||should support an eof after a self-closing tag",

                "<x/>^na||<x/>^na||should support a line ending after a self-closing tag",

                "<x/>a||<p><x/>a</p>||should not support another character after a self-closing tag",

                "<x>a||<p><x>a</p>||should not support another character after an opening tag",

                "<x y>||<x y>||should support boolean attributes in a complete tag",

                "<x^ny>||<p><x^ny></p>||should not support a line ending before an attribute name",

                "<x^n  y>||<p><x^ny></p>||should not support a line ending w/ whitespace before an attribute name",

                "<x^n  ^ny>||<p>&lt;x</p>^n<p>y&gt;</p>||should not support a line ending w/ whitespace and another line ending before an attribute name",

                "<x y^nz>||<p><x y^nz></p>||should not support a line ending between attribute names",

                "<x y   z>||<x y   z>||should support whitespace between attribute names",

                "<x:y>||<p>&lt;x:y&gt;</p>||should not support a colon in a tag name",

                "<x_y>||<p>&lt;x_y&gt;</p>||should not support an underscore in a tag name",

                "<x.y>||<p>&lt;x.y&gt;</p>||should not support a dot in a tag name",

                "<x :y>||<x :y>||should support a colon to start an attribute name",

                "<x _y>||<x _y>||should support an underscore to start an attribute name",

                "<x .y>||<p>&lt;x .y&gt;</p>||should not support a dot to start an attribute name",

                "<x y:>||<x y:>||should support a colon to end an attribute name",

                "<x y_>||<x y_>||should support an underscore to end an attribute name",

                "<x y.>||<x y.>||should support a dot to end an attribute name",

                "<x y123>||<x y123>||should support numbers to end an attribute name",

                "<x data->||<x data->||should support a dash to end an attribute name",

                "<x y=>||<p>&lt;x y=&gt;</p>||should not support an initializer w/o a value",

                "<x y==>||<p>&lt;x y==&gt;</p>||should not support an equals to as an initializer",

                "<x y=z>||<x y=z>||should support a single character as an unquoted attribute value",

                "<x y=\"\">||<x y=\"\">||should support an empty double quoted attribute value",

                "<x y=\"\">||<x y=\"\">||should support an empty single quoted attribute value",

                "<x y=\"^n\">||<p><x y=\"^n\"></p>||should not support a line ending in a double quoted attribute value",

                "<x y=\"^n\">||<p><x y=\"^n\"></p>||should not support a line ending in a single quoted attribute value",

                "<w x=y^nz>||<p><w x=y^nz></p>||should not support a line ending in/after an unquoted attribute value",

                "<w x=y\"z>||<p>&lt;w x=y&quot;z&gt;</p>||should not support a double quote in/after an unquoted attribute value",

                "<w x=y'z>||<p>&lt;w x=y'z&gt;</p>||should not support a single quote in/after an unquoted attribute value",

                "<x y=\"\"z>||<p>&lt;x y=&quot;&quot;z&gt;</p>||should not support an attribute after a double quoted attribute value",

                "<x>^n  ^n  ^n>||<x>^n<blockquote>^n</blockquote>||should not support blank lines in complete",

                "> <a>^n*bar*||<blockquote>^n<a>^n</blockquote>^n<p><em>bar</em></p>||should not support lazyness (1)",

                "> a^n<a>||<blockquote>^n<p>a</p>^n</blockquote>^n<a>||should not support lazyness (2)",

        })
        public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
            TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
        }

    }

    @Test
    public void testDisabled() {

        TestUtil.assertGeneratedHtmlWithDisabled("<x>", "<p>&lt;x&gt;</p>", "should support turning off html (flow)", "htmlFlow");
    }

}
