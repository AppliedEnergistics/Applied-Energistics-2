package appeng.libs.micromark.document;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListItemTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(value = {
            "A paragraph^nwith two lines.^n^n    indented code^n^n> A block quote.||<p>A paragraph^nwith two lines.</p>^n<pre><code>indented code^n</code></pre>^n<blockquote>^n<p>A block quote.</p>^n</blockquote>||should support documents",
            "1.  a^n    b.^n^n        c^n^n    > d.||<ol>^n<li>^n<p>a^nb.</p>^n<pre><code>c^n</code></pre>^n<blockquote>^n<p>d.</p>^n</blockquote>^n</li>^n</ol>||should support documents in list items",
            "- one^n^n two||<ul>^n<li>one</li>^n</ul>^n<p>two</p>||should not support 1 space for a two-character list prefix",
            "- a^n^n  b||<ul>^n<li>^n<p>a</p>^n<p>b</p>^n</li>^n</ul>||should support blank lines in list items",
            " -    one^n^n     two||<ul>^n<li>one</li>^n</ul>^n<pre><code> two^n</code></pre>||should support indented code after lists",
            "   > > 1.  one^n>>^n>>     two||<blockquote>^n<blockquote>^n<ol>^n<li>^n<p>one</p>^n<p>two</p>^n</li>^n</ol>^n</blockquote>^n</blockquote>||should support proper indent mixed w/ block quotes (1)",
            ">>- one^n>>^n  >  > two||<blockquote>^n<blockquote>^n<ul>^n<li>one</li>^n</ul>^n<p>two</p>^n</blockquote>^n</blockquote>||should support proper indent mixed w/ block quotes (2)",
            "-one^n^n2.two||<p>-one</p>^n<p>2.two</p>||should not support a missing space after marker",
            "- foo^n^n^n  bar||<ul>^n<li>^n<p>foo</p>^n<p>bar</p>^n</li>^n</ul>||should support multiple blank lines between items",
            "1.  foo^n^n    ```^n    bar^n    ```^n^n    baz^n^n    > bam||<ol>^n<li>^n<p>foo</p>^n<pre><code>bar^n</code></pre>^n<p>baz</p>^n<blockquote>^n<p>bam</p>^n</blockquote>^n</li>^n</ol>||should support flow in items",
            "- Foo^n^n      bar^n^n^n      baz||<ul>^n<li>^n<p>Foo</p>^n<pre><code>bar^n^n^nbaz^n</code></pre>^n</li>^n</ul>||should support blank lines in indented code in items",
            "123456789. ok||<ol start=\"123456789\">^n<li>ok</li>^n</ol>||should support start on the first list item",
            "1234567890. not ok||<p>1234567890. not ok</p>||should not support ordered item values over 10 digits",
            "0. ok||<ol start=\"0\">^n<li>ok</li>^n</ol>||should support ordered item values of `0`",
            "003. ok||<ol start=\"3\">^n<li>ok</li>^n</ol>||should support ordered item values starting w/ `0`s",
            "-1. not ok||<p>-1. not ok</p>||should not support “negative” ordered item values",
            "- foo^n^n      bar||<ul>^n<li>^n<p>foo</p>^n<pre><code>bar^n</code></pre>^n</li>^n</ul>||should support indented code in list items (1)",
            "  10.  foo^n^n           bar||<ol start=\"10\">^n<li>^n<p>foo</p>^n<pre><code>bar^n</code></pre>^n</li>^n</ol>||should support indented code in list items (2)",
            "    indented code^n^nparagraph^n^n    more code||<pre><code>indented code^n</code></pre>^n<p>paragraph</p>^n<pre><code>more code^n</code></pre>||should support indented code in list items (3)",
            "1.     indented code^n^n   paragraph^n^n       more code||<ol>^n<li>^n<pre><code>indented code^n</code></pre>^n<p>paragraph</p>^n<pre><code>more code^n</code></pre>^n</li>^n</ol>||should support indented code in list items (4)",
            "1.      indented code^n^n   paragraph^n^n       more code||<ol>^n<li>^n<pre><code> indented code^n</code></pre>^n<p>paragraph</p>^n<pre><code>more code^n</code></pre>^n</li>^n</ol>||should support indented code in list items (5)",
            "   foo^n^nbar||<p>foo</p>^n<p>bar</p>||should support indented code in list items (6)",
            "-    foo^n^n  bar||<ul>^n<li>foo</li>^n</ul>^n<p>bar</p>||should support indented code in list items (7)",
            "-  foo^n^n   bar||<ul>^n<li>^n<p>foo</p>^n<p>bar</p>^n</li>^n</ul>||should support indented code in list items (8)",
            "-^n  foo^n-^n  ```^n  bar^n  ```^n-^n      baz||<ul>^n<li>foo</li>^n<li>^n<pre><code>bar^n</code></pre>^n</li>^n<li>^n<pre><code>baz^n</code></pre>^n</li>^n</ul>||should support blank first lines (1)",
            "-   ^n  foo||<ul>^n<li>foo</li>^n</ul>||should support blank first lines (2)",
            "-^n^n  foo||<ul>^n<li></li>^n</ul>^n<p>foo</p>||should support empty only items",
            "- foo^n-^n- bar||<ul>^n<li>foo</li>^n<li></li>^n<li>bar</li>^n</ul>||should support empty continued items",
            "- foo^n-   ^n- bar||<ul>^n<li>foo</li>^n<li></li>^n<li>bar</li>^n</ul>||should support blank continued items",
            "1. foo^n2.^n3. bar||<ol>^n<li>foo</li>^n<li></li>^n<li>bar</li>^n</ol>||should support empty continued items (ordered)",
            "*||<ul>^n<li></li>^n</ul>||should support a single empty item",
            "foo^n*^n^nfoo^n1.||<p>foo^n*</p>^n<p>foo^n1.</p>||should not support empty items to interrupt paragraphs",
            " 1.  A paragraph^n     with two lines.^n^n         indented code^n^n     > A block quote.||<ol>^n<li>^n<p>A paragraph^nwith two lines.</p>^n<pre><code>indented code^n</code></pre>^n<blockquote>^n<p>A block quote.</p>^n</blockquote>^n</li>^n</ol>||should support indenting w/ 1 space",
            "  1.  A paragraph^n      with two lines.^n^n          indented code^n^n      > A block quote.||<ol>^n<li>^n<p>A paragraph^nwith two lines.</p>^n<pre><code>indented code^n</code></pre>^n<blockquote>^n<p>A block quote.</p>^n</blockquote>^n</li>^n</ol>||should support indenting w/ 2 spaces",
            "   1.  A paragraph^n       with two lines.^n^n           indented code^n^n       > A block quote.||<ol>^n<li>^n<p>A paragraph^nwith two lines.</p>^n<pre><code>indented code^n</code></pre>^n<blockquote>^n<p>A block quote.</p>^n</blockquote>^n</li>^n</ol>||should support indenting w/ 3 spaces",
            "    1.  A paragraph^n        with two lines.^n^n            indented code^n^n        > A block quote.||<pre><code>1.  A paragraph^n    with two lines.^n^n        indented code^n^n    &gt; A block quote.^n</code></pre>||should not support indenting w/ 4 spaces",
            "  1.  A paragraph^nwith two lines.^n^n          indented code^n^n      > A block quote.||<ol>^n<li>^n<p>A paragraph^nwith two lines.</p>^n<pre><code>indented code^n</code></pre>^n<blockquote>^n<p>A block quote.</p>^n</blockquote>^n</li>^n</ol>||should support lazy lines",
            "  1.  A paragraph^n    with two lines.||<ol>^n<li>A paragraph^nwith two lines.</li>^n</ol>||should support partially lazy lines",
            "> 1. > Blockquote^ncontinued here.||<blockquote>^n<ol>^n<li>^n<blockquote>^n<p>Blockquote^ncontinued here.</p>^n</blockquote>^n</li>^n</ol>^n</blockquote>||should support lazy lines combined w/ other containers",
            "> 1. > Blockquote^n> continued here.||<blockquote>^n<ol>^n<li>^n<blockquote>^n<p>Blockquote^ncontinued here.</p>^n</blockquote>^n</li>^n</ol>^n</blockquote>||should support partially continued, partially lazy lines combined w/ other containers",
            "- foo^n  - bar^n    - baz^n      - boo||<ul>^n<li>foo^n<ul>^n<li>bar^n<ul>^n<li>baz^n<ul>^n<li>boo</li>^n</ul>^n</li>^n</ul>^n</li>^n</ul>^n</li>^n</ul>||should support sublists w/ enough spaces (1)",
            "- foo^n - bar^n  - baz^n   - boo||<ul>^n<li>foo</li>^n<li>bar</li>^n<li>baz</li>^n<li>boo</li>^n</ul>||should not support sublists w/ too few spaces",
            "10) foo^n    - bar||<ol start=\"10\">^n<li>foo^n<ul>^n<li>bar</li>^n</ul>^n</li>^n</ol>||should support sublists w/ enough spaces (2)",
            "10) foo^n   - bar||<ol start=\"10\">^n<li>foo</li>^n</ol>^n<ul>^n<li>bar</li>^n</ul>||should not support sublists w/ too few spaces (2)",
            "- - foo||<ul>^n<li>^n<ul>^n<li>foo</li>^n</ul>^n</li>^n</ul>||should support sublists (1)",
            "1. - 2. foo||<ol>^n<li>^n<ul>^n<li>^n<ol start=\"2\">^n<li>foo</li>^n</ol>^n</li>^n</ul>^n</li>^n</ol>||should support sublists (2)",
            "- # Foo^n- Bar^n  ---^n  baz||<ul>^n<li>^n<h1>Foo</h1>^n</li>^n<li>^n<h2>Bar</h2>^nbaz</li>^n</ul>||should support headings in list items",
            "- foo^n- bar^n+ baz||<ul>^n<li>foo</li>^n<li>bar</li>^n</ul>^n<ul>^n<li>baz</li>^n</ul>||should support a new list by changing the marker (unordered)",
            "1. foo^n2. bar^n3) baz||<ol>^n<li>foo</li>^n<li>bar</li>^n</ol>^n<ol start=\"3\">^n<li>baz</li>^n</ol>||should support a new list by changing the marker (ordered)",
            "Foo^n- bar^n- baz||<p>Foo</p>^n<ul>^n<li>bar</li>^n<li>baz</li>^n</ul>||should support interrupting a paragraph",
            "a^n2. b||<p>a^n2. b</p>||should not support interrupting a paragraph with a non-1 numbered item",
            "^n2. a||<ol start=\"2\">^n<li>a</li>^n</ol>||should “interrupt” a blank line (1)",
            "a^n^n2. b||<p>a</p>^n<ol start=\"2\">^n<li>b</li>^n</ol>||should “interrupt” a blank line (2)",
            "a^n1. b||<p>a</p>^n<ol>^n<li>b</li>^n</ol>||should support interrupting a paragraph with a 1 numbered item",
            "- foo^n^n- bar^n^n^n- baz||<ul>^n<li>^n<p>foo</p>^n</li>^n<li>^n<p>bar</p>^n</li>^n<li>^n<p>baz</p>^n</li>^n</ul>||should support blank lines between items (1)",
            "- foo^n  - bar^n    - baz^n^n^n      bim||<ul>^n<li>foo^n<ul>^n<li>bar^n<ul>^n<li>^n<p>baz</p>^n<p>bim</p>^n</li>^n</ul>^n</li>^n</ul>^n</li>^n</ul>||should support blank lines between items (2)",
            "- a^n - b^n  - c^n   - d^n  - e^n - f^n- g||<ul>^n<li>a</li>^n<li>b</li>^n<li>c</li>^n<li>d</li>^n<li>e</li>^n<li>f</li>^n<li>g</li>^n</ul>||should not support lists in lists w/ too few spaces (1)",
            "1. a^n^n  2. b^n^n   3. c||<ol>^n<li>^n<p>a</p>^n</li>^n<li>^n<p>b</p>^n</li>^n<li>^n<p>c</p>^n</li>^n</ol>||should not support lists in lists w/ too few spaces (2)",
            "- a^n - b^n  - c^n   - d^n    - e||<ul>^n<li>a</li>^n<li>b</li>^n<li>c</li>^n<li>d^n- e</li>^n</ul>||should not support lists in lists w/ too few spaces (3)",
            "1. a^n^n  2. b^n^n    3. c||<ol>^n<li>^n<p>a</p>^n</li>^n<li>^n<p>b</p>^n</li>^n</ol>^n<pre><code>3. c^n</code></pre>||should not support lists in lists w/ too few spaces (3)",
            "- a^n- b^n^n- c||<ul>^n<li>^n<p>a</p>^n</li>^n<li>^n<p>b</p>^n</li>^n<li>^n<p>c</p>^n</li>^n</ul>||should support loose lists w/ a blank line between (1)",
            "* a^n*^n^n* c||<ul>^n<li>^n<p>a</p>^n</li>^n<li></li>^n<li>^n<p>c</p>^n</li>^n</ul>||should support loose lists w/ a blank line between (2)",
            "- a^n- b^n^n  c^n- d||<ul>^n<li>^n<p>a</p>^n</li>^n<li>^n<p>b</p>^n<p>c</p>^n</li>^n<li>^n<p>d</p>^n</li>^n</ul>||should support loose lists w/ a blank line in an item (1)",
            "- a^n- b^n^n  [ref]: /url^n- d||<ul>^n<li>^n<p>a</p>^n</li>^n<li>^n<p>b</p>^n</li>^n<li>^n<p>d</p>^n</li>^n</ul>||should support loose lists w/ a blank line in an item (2)",
            "- a^n- ```^n  b^n^n^n  ```^n- c||<ul>^n<li>a</li>^n<li>^n<pre><code>b^n^n^n</code></pre>^n</li>^n<li>c</li>^n</ul>||should support tight lists w/ a blank line in fenced code",
            "- a^n  - b^n^n    c^n- d||<ul>^n<li>a^n<ul>^n<li>^n<p>b</p>^n<p>c</p>^n</li>^n</ul>^n</li>^n<li>d</li>^n</ul>||should support tight lists w/ a blank line in a sublist",
            "* a^n  > b^n  >^n* c||<ul>^n<li>a^n<blockquote>^n<p>b</p>^n</blockquote>^n</li>^n<li>c</li>^n</ul>||should support tight lists w/ a blank line in a block quote",
            "- a^n  > b^n  ```^n  c^n  ```^n- d||<ul>^n<li>a^n<blockquote>^n<p>b</p>^n</blockquote>^n<pre><code>c^n</code></pre>^n</li>^n<li>d</li>^n</ul>||should support tight lists w/ flow w/o blank line",
            "- a||<ul>^n<li>a</li>^n</ul>||should support tight lists w/ a single content",
            "- a^n  - b||<ul>^n<li>a^n<ul>^n<li>b</li>^n</ul>^n</li>^n</ul>||should support tight lists w/ a sublist",
            "1. ```^n   foo^n   ```^n^n   bar||<ol>^n<li>^n<pre><code>foo^n</code></pre>^n<p>bar</p>^n</li>^n</ol>||should support loose lists w/ a blank line in an item",
            "* foo^n  * bar^n^n  baz||<ul>^n<li>^n<p>foo</p>^n<ul>^n<li>bar</li>^n</ul>^n<p>baz</p>^n</li>^n</ul>||should support loose lists w/ tight sublists (1)",
            "- a^n  - b^n  - c^n^n- d^n  - e^n  - f||<ul>^n<li>^n<p>a</p>^n<ul>^n<li>b</li>^n<li>c</li>^n</ul>^n</li>^n<li>^n<p>d</p>^n<ul>^n<li>e</li>^n<li>f</li>^n</ul>^n</li>^n</ul>||should support loose lists w/ tight sublists (2)",
            // Extra.
            "* a^n*^n^n  ^n\t^n* b||<ul>^n<li>^n<p>a</p>^n</li>^n<li></li>^n<li>^n<p>b</p>^n</li>^n</ul>||should support continued list items after an empty list item w/ many blank lines",
            "*^n  ~~~p^n^n  ~~~||<ul>^n<li>^n<pre><code class=\"language-p\">^n</code></pre>^n</li>^n</ul>||should support blank lines in code after an initial blank line",
            "* a tight item that ends with an html element: `x`^n^nParagraph||<ul>^n<li>a tight item that ends with an html element: <code>x</code></li>^n</ul>^n<p>Paragraph</p>||should ignore line endings after tight items ending in tags",
            "*   foo^n^n*^n^n*   bar||<ul>^n<li>^n<p>foo</p>^n</li>^n<li></li>^n<li>^n<p>bar</p>^n</li>^n</ul>||should support empty items in a spread list",
            "- ```^n^n  ```||<ul>^n<li>^n<pre><code>^n</code></pre>^n</li>^n</ul>||should remove indent of code (fenced) in list (0 space)",
            "- ```^n ^n  ```||<ul>^n<li>^n<pre><code>^n</code></pre>^n</li>^n</ul>||should remove indent of code (fenced) in list (1 space)",
            "- ```^n  ^n  ```||<ul>^n<li>^n<pre><code>^n</code></pre>^n</li>^n</ul>||should remove indent of code (fenced) in list (2 spaces)",
            "- ```^n   ^n  ```||<ul>^n<li>^n<pre><code> ^n</code></pre>^n</li>^n</ul>||should remove indent of code (fenced) in list (3 spaces)",
            "- ```^n    ^n  ```||<ul>^n<li>^n<pre><code>  ^n</code></pre>^n</li>^n</ul>||should remove indent of code (fenced) in list (4 spaces)",
            "- ```^n\t^n  ```||<ul>^n<li>^n<pre><code>  ^n</code></pre>^n</li>^n</ul>||should remove indent of code (fenced) in list (1 tab)",
            "- +^n-||<ul>^n<li>^n<ul>^n<li></li>^n</ul>^n</li>^n<li></li>^n</ul>||should support complex nested and empty lists (1)",
            "- 1.^n-||<ul>^n<li>^n<ol>^n<li></li>^n</ol>^n</li>^n<li></li>^n</ul>||should support complex nested and empty lists (2)",
            "* - +^n* -||<ul>^n<li>^n<ul>^n<li>^n<ul>^n<li></li>^n</ul>^n</li>^n</ul>^n</li>^n<li>^n<ul>^n<li></li>^n</ul>^n</li>^n</ul>||should support complex nested and empty lists (3)",
    }, delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false)
    public void testListItem(String markdown, String expectedHtml, String message) {
        markdown = markdown != null ? markdown : "";
        markdown = markdown.replace("^n", "\n");
        expectedHtml = expectedHtml != null ? expectedHtml : "";
        expectedHtml = expectedHtml.replace("^n", "\n");

        var html = new HtmlCompiler().compile(Micromark.parseAndPostprocess(markdown));
        assertEquals(expectedHtml, html);
    }

    @Test
    public void testDangerousHtmlListItems() {
        var dangerousHtmlOptions = new CompileOptions().allowDangerousHtml();

        assertEquals(
                "<ul>\n<li>foo</li>\n<li>bar</li>\n</ul>\n<!-- -->\n<ul>\n<li>baz</li>\n<li>bim</li>\n</ul>",
                new HtmlCompiler(dangerousHtmlOptions).compile(Micromark.parseAndPostprocess("- foo\n- bar\n\n<!-- -->\n\n- baz\n- bim")),
                "should support HTML comments between lists"
        );

        assertEquals(
                "<ul>\n<li>\n<p>foo</p>\n<p>notcode</p>\n</li>\n<li>\n<p>foo</p>\n</li>\n</ul>\n<!-- -->\n<pre><code>code\n</code></pre>",
                new HtmlCompiler(dangerousHtmlOptions).compile(Micromark.parseAndPostprocess("-   foo\n\n    notcode\n\n-   foo\n\n<!-- -->\n\n    code")),
                "should support HTML comments between lists and indented code"
        );

        assertEquals(
                "<ul>\n<li>a</li>\n</ul>\n<!---->\n<ul>\n<li>b</li>\n</ul>",
                new HtmlCompiler(dangerousHtmlOptions).compile(Micromark.parseAndPostprocess("* a\n\n<!---->\n\n* b")),
                "should support the common list breaking comment method"
        );

    }

    @Test
    public void testDisableListItems() {
        var parseOptions = new ParseOptions();
        parseOptions.withExtension(new Extension() {
            {
                nullDisable.add("list");
            }
        });

        assertEquals(
                "<p>- one</p>\n<p>two</p>",
                new HtmlCompiler().compile(Micromark.parseAndPostprocess("- one\n\n two", parseOptions)),
                "should support turning off lists"
        );
    }


}
