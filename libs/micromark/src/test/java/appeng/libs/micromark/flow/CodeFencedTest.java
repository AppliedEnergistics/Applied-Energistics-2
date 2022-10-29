package appeng.libs.micromark.flow;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CodeFencedTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "```^n<^n >^n```||<pre><code>&lt;^n &gt;^n</code></pre>||should support fenced code w/ grave accents",

            "~~~^n<^n >^n~~~||<pre><code>&lt;^n &gt;^n</code></pre>||should support fenced code w/ tildes",

            "``^nfoo^n``||<p><code>foo</code></p>||should not support fenced code w/ less than three markers",

            "```^naaa^n~~~^n```||<pre><code>aaa^n~~~^n</code></pre>||should not support a tilde closing sequence for a grave accent opening sequence",

            "~~~^naaa^n```^n~~~||<pre><code>aaa^n```^n</code></pre>||should not support a grave accent closing sequence for a tilde opening sequence",

            "````^naaa^n```^n``````||<pre><code>aaa^n```^n</code></pre>||should support a closing sequence longer, but not shorter than, the opening",

            "~~~~^naaa^n~~~^n~~~~||<pre><code>aaa^n~~~^n</code></pre>||should support a closing sequence equal to, but not shorter than, the opening",

            "```||<pre><code></code></pre>^n||should support an eof right after an opening sequence",

            "`````^n^n```^naaa^n||<pre><code>^n```^naaa^n</code></pre>^n||should support an eof somewhere in content",

            "> ```^n> aaa^n^nbbb||<blockquote>^n<pre><code>aaa^n</code></pre>^n</blockquote>^n<p>bbb</p>||should support no closing sequence in a block quote",

            "```^n^n  ^n```||<pre><code>^n  ^n</code></pre>||should support blank lines in fenced code",

            "```^n```||<pre><code></code></pre>||should support empty fenced code",

            " ```^n aaa^naaa^n```||<pre><code>aaa^naaa^n</code></pre>||should remove up to one space from the content if the opening sequence is indented w/ 1 space",

            "  ```^naaa^n  aaa^naaa^n  ```||<pre><code>aaa^naaa^naaa^n</code></pre>||should remove up to two space from the content if the opening sequence is indented w/ 2 spaces",

            "   ```^n   aaa^n    aaa^n  aaa^n   ```||<pre><code>aaa^n aaa^naaa^n</code></pre>||should remove up to three space from the content if the opening sequence is indented w/ 3 spaces",

            "    ```^n    aaa^n    ```||<pre><code>```^naaa^n```^n</code></pre>||should not support indenteding the opening sequence w/ 4 spaces",

            "```^naaa^n  ```||<pre><code>aaa^n</code></pre>||should support an indented closing sequence",

            "   ```^naaa^n  ```||<pre><code>aaa^n</code></pre>||should support a differently indented closing sequence than the opening sequence",

            "```^naaa^n    ```^n||<pre><code>aaa^n    ```^n</code></pre>^n||should not support an indented closing sequence w/ 4 spaces",

            "``` ```^naaa||<p><code> </code>^naaa</p>||should not support grave accents in the opening fence after the opening sequence",

            "~~~~~~^naaa^n~~~ ~~^n||<pre><code>aaa^n~~~ ~~^n</code></pre>^n||should not support spaces in the closing sequence",

            "foo^n```^nbar^n```^nbaz||<p>foo</p>^n<pre><code>bar^n</code></pre>^n<p>baz</p>||should support interrupting paragraphs",

            "foo^n---^n~~~^nbar^n~~~^n# baz||<h2>foo</h2>^n<pre><code>bar^n</code></pre>^n<h1>baz</h1>||should support interrupting other content",

            "```ruby^ndef foo(x)^n  return 3^nend^n```||<pre><code class=\"language-ruby\">def foo(x)^n  return 3^nend^n</code></pre>||should support the info string as a `language-` class (1)",

            "````;^n````||<pre><code class=\"language-;\"></code></pre>||should support the info string as a `language-` class (2)",

            "~~~~    ruby startline=3 $%@#$^ndef foo(x)^n  return 3^nend^n~~~~~~~||<pre><code class=\"language-ruby\">def foo(x)^n  return 3^nend^n</code></pre>||should support the info string as a `language-` class, but not the meta string",

            "``` aa ```^nfoo||<p><code>aa</code>^nfoo</p>||should not support grave accents in the meta string",

            "~~~ aa ``` ~~~^nfoo^n~~~||<pre><code class=\"language-aa\">foo^n</code></pre>||should support grave accents and tildes in the meta string of tilde fenced code",

            "```^n``` aaa^n```||<pre><code>``` aaa^n</code></pre>||should not support info string on closing sequences",

            // Our own:
            "```  ||<pre><code></code></pre>^n||should support an eof after whitespace, after the start fence sequence",

            "```  js^nalert(1)^n```||<pre><code class=\"language-js\">alert(1)^n</code></pre>||should support whitespace between the sequence and the info string",

            "```js||<pre><code class=\"language-js\"></code></pre>^n||should support an eof after the info string",

            "```  js ^nalert(1)^n```||<pre><code class=\"language-js\">alert(1)^n</code></pre>||should support whitespace after the info string",

            "```^n  ||<pre><code>  ^n</code></pre>^n||should support an eof after whitespace in content",

            "  ```^n ||<pre><code></code></pre>^n||should support an eof in the prefix, in content",

            "```j\\+s&copy;||<pre><code class=\"language-j+s©\"></code></pre>^n||should support character escapes and character references in info strings",

            "   ```^naaa^n    ```||<pre><code>aaa^n ```^n</code></pre>^n||should not support a closing sequence w/ too much indent, regardless of opening sequence (1)",

            "> ```^n>^n>^n>^n^na||<blockquote>^n<pre><code>^n^n^n</code></pre>^n</blockquote>^n<p>a</p>||should not support a closing sequence w/ too much indent, regardless of opening sequence (2)",

            "> ```a^nb||<blockquote>^n<pre><code class=\"language-a\"></code></pre>^n</blockquote>^n<p>b</p>||should not support lazyness (1)",

            "> a^n```b||<blockquote>^n<p>a</p>^n</blockquote>^n<pre><code class=\"language-b\"></code></pre>^n||should not support lazyness (2)",

            "> ```a^n```||<blockquote>^n<pre><code class=\"language-a\"></code></pre>^n</blockquote>^n<pre><code></code></pre>^n||should not support lazyness (3)",

    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @Test
    public void testDisabled() {
        TestUtil.assertGeneratedHtmlWithDisabled("```", "<p>```</p>", "should support turning off code (fenced)", "codeFenced");
    }

}
