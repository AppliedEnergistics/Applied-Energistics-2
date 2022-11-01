package appeng.libs.mdx;

import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.ParseException;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.html.CompileContext;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.HtmlExtension;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MdxTest {

    private final HtmlExtension HTML = HtmlExtension.builder()
            .enter("mdxJsxTextTag", this::start)
            .enter("mdxJsxFlowTag", this::start)
            .exit("mdxJsxTextTag", this::end)
            .exit("mdxJsxFlowTag", this::end)
            .build();

    private void start(CompileContext context, Token token) {
        context.buffer();
    }

    private void end(CompileContext context, Token token) {
        context.resume();
        context.setSlurpOneLineEnding(true);
    }

    @Nested
    class Core extends TestBase {
        {
            addTest("a <b/> c.", "<p>a  c.</p>", "should support a self-closing element");

            addTest("a <b></b> c.", "<p>a  c.</p>", "should support a closed element");

            addTest("a <></> c.", "<p>a  c.</p>", "should support fragments");

            addTest("a <b>*b*</b> c.", "<p>a <em>b</em> c.</p>", "should support markdown inside elements");
        }
    }

    @Nested
    class TextAgnostic extends TestBase {
        {
            addTest("a <b /> c", "<p>a  c</p>", "should support a self-closing element");

            addTest("a <b> c </b> d", "<p>a  c  d</p>", "should support a closed element");

            addTest("a <b> c", "<p>a  c</p>", "should support an unclosed element");

            addTest("a <b {1 + 1} /> c", "<p>a  c</p>", "should support an attribute expression");

            addTest("a <b c={1 + 1} /> d", "<p>a  d</p>", "should support an attribute value expression");
        }
    }

    @Nested
    class TextComplete extends TestBase {
        {
            addTest("a <b> c", "<p>a  c</p>", "should support an unclosed element");

            addTest("a <> c", "<p>a  c</p>", "should support an unclosed fragment");

            addTest("a < \t>b</>", "<p>a &lt; \t&gt;b</p>", "should *not* support whitespace in the opening tag (fragment)");

            addTest("a < \nb\t>b</b>", "<p>a &lt;\nb\t&gt;b</p>", "should *not* support whitespace in the opening tag (named)");

            addErrorTest(
                    "a <!> b",
                    "Unexpected character `!` \\(U\\+0021\\) before name, expected a character that can start a name, such as a letter, `\\$`, or `_`",
                    "should crash on a nonconforming start identifier"
            );

            addErrorTest(
                    "a <a></(> b.",
                    "Unexpected character `\\(` \\(U\\+0028\\) before name, expected a character that can start a name, such as a letter, `\\$`, or `_`",
                    "should crash on a nonconforming start identifier in a closing tag"
            );

            addTest("a <π /> b.", "<p>a  b.</p>", "should support non-ascii identifier start characters");

            addErrorTest(
                    "a <© /> b.",
                    "Unexpected character `©` \\(U\\+00A9\\) before name, expected a character that can start a name, such as a letter, `\\$`, or `_`",
                    "should crash on non-conforming non-ascii identifier start characters"
            );

            addErrorTest(
                    "a <!--b-->",
                    "Unexpected character `!` \\(U\\+0021\\) before name, expected a character that can start a name, such as a letter, `\\$`, or `_` \\(note: to create a comment in MDX, use `\\{\\/\\* text \\*\\/}`\\)",
                    "should crash nicely on what might be a comment"
            );

            addErrorTest(
                    "a <// b\nc/>",
                    "Unexpected character `\\/` \\(U\\+002F\\) before name, expected a character that can start a name, such as a letter, `\\$`, or `_` \\(note: JS comments in JSX tags are not supported in MDX\\)",
                    "should crash nicely JS line comments inside tags (1)"
            );

            addErrorTest(
                    "a <b// c\nd/>",
                    "Unexpected character `\\/` \\(U\\+002F\\) after self-closing slash, expected `>` to end the tag \\(note: JS comments in JSX tags are not supported in MDX\\)",
                    "should crash nicely JS line comments inside tags (2)"
            );

            addErrorTest(
                    "a </*b*/c>",
                    "Unexpected character `\\*` \\(U\\+002A\\) before name, expected a character that can start a name, such as a letter, `\\$`, or `_` \\(note: JS comments in JSX tags are not supported in MDX\\)",
                    "should crash nicely JS multiline comments inside tags (1)"
            );

            addErrorTest(
                    "a <b/*c*/>",
                    "Unexpected character `\\*` \\(U\\+002A\\) after self-closing slash, expected `>` to end the tag \\(note: JS comments in JSX tags are not supported in MDX\\)",
                    "should crash nicely JS multiline comments inside tags (2)"
            );

            addTest("a <a\u200Cb /> b.", "<p>a  b.</p>", "should support non-ascii identifier continuation characters");

            addErrorTest(
                    "a <a¬ /> b.",
                    "Unexpected character `¬` \\(U\\+00AC\\) in name, expected a name character such as letters, digits, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on non-conforming non-ascii identifier continuation characters"
            );

            addErrorTest(
                    "a <b@c.d>",
                    "Unexpected character `@` \\(U\\+0040\\) in name, expected a name character such as letters, digits, `\\$`, or `_`; whitespace before attributes; or the end of the tag \\(note: to create a link in MDX, use `\\[text]\\(url\\)`\\)",
                    "should crash nicely on what might be an email link"
            );

            addTest("a <a-->b</a-->.", "<p>a b.</p>", "should support dashes in names");

            addErrorTest(
                    "a <a?> c.",
                    "Unexpected character `\\?` \\(U\\+003F\\) in name, expected a name character such as letters, digits, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on nonconforming identifier continuation characters"
            );

            addTest("a <abc . def.ghi>b</abc.def . ghi>.", "<p>a b.</p>", "should support dots in names for method names");

            addErrorTest(
                    "a <b.c@d.e>",
                    "Unexpected character `@` \\(U\\+0040\\) in member name, expected a name character such as letters, digits, `\\$`, or `_`; whitespace before attributes; or the end of the tag \\(note: to create a link in MDX, use `\\[text]\\(url\\)`\\)",
                    "should crash nicely on what might be an email link in member names"
            );

            addTest("a <svg: rect>b</  svg :rect>.", "<p>a b.</p>", "should support colons in names for local names");

            addErrorTest(
                    "a <a:+> c.",
                    "Unexpected character `\\+` \\(U\\+002B\\) before local name, expected a character that can start a name, such as a letter, `\\$`, or `_` \\(note: to create a link in MDX, use `\\[text]\\(url\\)`\\)",
                    "should crash on a nonconforming character to start a local name"
            );

            addErrorTest(
                    "a <http://example.com>",
                    "Unexpected character `\\/` \\(U\\+002F\\) before local name, expected a character that can start a name, such as a letter, `\\$`, or `_` \\(note: to create a link in MDX, use `\\[text]\\(url\\)`\\)",
                    "should crash nicely on what might be a protocol in local names"
            );

            addErrorTest(
                    "a <http: >",
                    "Unexpected character `>` \\(U\\+003E\\) before local name, expected a character that can start a name, such as a letter, `\\$`, or `_`",
                    "should crash nicely on what might be a protocol in local names"
            );

            addErrorTest(
                    "a <a:b|> c.",
                    "Unexpected character `\\|` \\(U\\+007C\\) in local name, expected a name character such as letters, digits, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character in a local name"
            );

            addErrorTest(
                    "a <a..> c.",
                    "Unexpected character `\\.` \\(U\\+002E\\) before member name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character to start a member name"
            );

            addErrorTest(
                    "a <a.b,> c.",
                    "Unexpected character `,` \\(U\\+002C\\) in member name, expected a name character such as letters, digits, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character in a member name"
            );

            addErrorTest(
                    "a <a:b .> c.",
                    "Unexpected character `\\.` \\(U\\+002E\\) after local name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character after a local name"
            );

            addErrorTest(
                    "a <a.b :> c.",
                    "Unexpected character `:` \\(U\\+003A\\) after member name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character after a member name"
            );

            addErrorTest(
                    "a <a => c.",
                    "Unexpected character `=` \\(U\\+003D\\) after name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character after name"
            );

            addTest("a <b {...props} {...rest}>c</b>.", "<p>a c.</p>", "should support attribute expressions");

            addTest("a <b {...{\"a\": \"b\"}}>c</b>.", "<p>a c.</p>", "should support nested balanced braces in attribute expressions");

            addTest("<a{...b}/>.", "<p>.</p>", "should support attribute expressions directly after a name");

            addTest("<a.b{...c}/>.", "<p>.</p>", "should support attribute expressions directly after a member name");

            addTest("<a:b{...c}/>.", "<p>.</p>", "should support attribute expressions directly after a local name");

            addTest("a <b c{...d}/>.", "<p>a .</p>", "should support attribute expressions directly after boolean attributes");

            addTest("a <b c:d{...e}/>.", "<p>a .</p>", "should support attribute expressions directly after boolean qualified attributes");

            addTest("a <b a {...props} b>c</b>.", "<p>a c.</p>", "should support attribute expressions and normal attributes");

            addTest("a <b c     d=\"d\"\t\tefg='e'>c</b>.", "<p>a c.</p>", "should support attributes");

            addErrorTest(
                    "a <b {...p}~>c</b>.",
                    "Unexpected character `~` \\(U\\+007E\\) before attribute name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character before an attribute name"
            );

            addErrorTest(
                    "a <b {...",
                    "Unexpected end of file in expression, expected a corresponding closing brace for `\\{`",
                    "should crash on a missing closing brace in attribute expression"
            );

            addErrorTest(
                    "a <a b@> c.",
                    "Unexpected character `@` \\(U\\+0040\\) in attribute name, expected an attribute name character such as letters, digits, `\\$`, or `_`; `=` to initialize a value; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character in attribute name"
            );

            addTest("a <b xml :\tlang\n= \"de-CH\" foo:bar>c</b>.", "<p>a c.</p>", "should support prefixed attributes");

            addTest("a <b a b : c d : e = \"f\" g/>.", "<p>a .</p>", "should support prefixed and normal attributes");

            addErrorTest(
                    "a <a b 1> c.",
                    "Unexpected character `1` \\(U\\+0031\\) after attribute name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; `=` to initialize a value; or the end of the tag",
                    "should crash on a nonconforming character after an attribute name"
            );

            addErrorTest(
                    "a <a b:#> c.",
                    "Unexpected character `#` \\(U\\+0023\\) before local attribute name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`",
                    "should crash on a nonconforming character to start a local attribute name"
            );

            addErrorTest(
                    "a <a b:c%> c.",
                    "Unexpected character `%` \\(U\\+0025\\) in local attribute name, expected an attribute name character such as letters, digits, `\\$`, or `_`; `=` to initialize a value; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character in a local attribute name"
            );

            addErrorTest(
                    "a <a b:c ^> c.",
                    "Unexpected character `\\^` \\(U\\+005E\\) after local attribute name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; `=` to initialize a value; or the end of the tag",
                    "should crash on a nonconforming character after a local attribute name"
            );

            addTest("a <b c={1 + 1}>c</b>.", "<p>a c.</p>", "should support attribute value expressions");

            addTest("a <b c={1 + ({a: 1}).a}>c</b>.", "<p>a c.</p>", "should support nested balanced braces in attribute value expressions");

            addErrorTest(
                    "a <a b=``> c.",
                    "Unexpected character `` ` `` \\(U\\+0060\\) before attribute value, expected a character that can start an attribute value, such as `\"`, `'`, or `\\{`",
                    "should crash on a nonconforming character before an attribute value"
            );

            addErrorTest(
                    "a <a b=<c />> d.",
                    "Unexpected character `<` \\(U\\+003C\\) before attribute value, expected a character that can start an attribute value, such as `\"`, `'`, or `\\{` \\(note: to use an element or fragment as a prop value in MDX, use `\\{<element \\/>}`\\)",
                    "should crash nicely on what might be a fragment, element as prop value"
            );

            addErrorTest(
                    "a <a b=\"> c.",
                    "Unexpected end of file in attribute value, expected a corresponding closing quote `\"`",
                    "should crash on a missing closing quote in double quoted attribute value"
            );

            addErrorTest(
                    "a <a b='> c.",
                    "Unexpected end of file in attribute value, expected a corresponding closing quote `'`",
                    "should crash on a missing closing quote in single quoted attribute value"
            );

            addErrorTest(
                    "a <a b={> c.",
                    "Unexpected end of file in expression, expected a corresponding closing brace for `\\{`",
                    "should crash on a missing closing brace in an attribute value expression"
            );

            addErrorTest(
                    "a <a b=\"\"*> c.",
                    "Unexpected character `\\*` \\(U\\+002A\\) before attribute name, expected a character that can start an attribute name, such as a letter, `\\$`, or `_`; whitespace before attributes; or the end of the tag",
                    "should crash on a nonconforming character after an attribute value"
            );

            addTest("<a b=\"\"c/>.", "<p>.</p>", "should support an attribute directly after a value");

            addTest("<a{...b}c/>.", "<p>.</p>", "should support an attribute directly after an attribute expression");

            addErrorTest(
                    "a <a/b> c.",
                    "Unexpected character `b` \\(U\\+0062\\) after self-closing slash, expected `>` to end the tag",
                    "should crash on a nonconforming character after a self-closing slash"
            );

            addTest("<a/ \t>.", "<p>.</p>", "should support whitespace directly after closing slash");


            Assertions.assertDoesNotThrow(() -> {
                compile("a > c.");
            }, "should *not* crash on closing angle in text");

            Assertions.assertDoesNotThrow(() -> {
                compile("a <>`<`</> c.");
            }, "should *not* crash on opening angle in tick code in an element");

            Assertions.assertDoesNotThrow(() -> {
                compile("a <>`` ``` ``</>");
            }, "should *not* crash on ticks in tick code in an element");

            addTest("a </> c.", "<p>a  c.</p>", "should support a closing tag w/o open elements");

            addTest("a <></b>", "<p>a </p>", "should support mismatched tags (1)");
            addTest("a <b></>", "<p>a </p>", "should support mismatched tags (2)");
            addTest("a <a.b></a>", "<p>a </p>", "should support mismatched tags (3)");
            addTest("a <a></a.b>", "<p>a </p>", "should support mismatched tags (4)");
            addTest("a <a.b></a.c>", "<p>a </p>", "should support mismatched tags (5)");
            addTest("a <a:b></a>", "<p>a </p>", "should support mismatched tags (6)");
            addTest("a <a></a:b>", "<p>a </p>", "should support mismatched tags (7)");
            addTest("a <a:b></a:c>", "<p>a </p>", "should support mismatched tags (8)");
            addTest("a <a:b></a.b>", "<p>a </p>", "should support mismatched tags (9)");

            addTest("a <a>b</a/>", "<p>a b</p>", "should support a closing self-closing tag");

            addTest("a <a>b</a b>", "<p>a b</p>", "should support a closing tag w/ attributes");

            addTest("a <>b <>c</> d</>.", "<p>a b c d.</p>", "should support nested tags");

            addTest(
                    "<x y=\"Character references can be used: &quot;, &apos;, &lt;, &gt;, &#x7B;, and &#x7D;, they can be named, decimal, or hexadecimal: &copy; &#8800; &#x1D306;\" />.",
                    "<p>.</p>",
                    "should support character references in attribute values"
            );

            addTest(
                    "<x>Character references can be used: &quot;, &apos;, &lt;, &gt;, &#x7B;, and &#x7D;, they can be named, decimal, or hexadecimal: &copy; &#8800; &#x1D306;</x>.",
                    "<p>Character references can be used: &quot;, ', &lt;, &gt;, {, and }, they can be named, decimal, or hexadecimal: © ≠ 팆.</p>",
                    "should support character references in text"
            );

            addTest("<x />.", "<p>.</p>", "should support as text if the closing tag is not the last thing");

            addTest("a <x />", "<p>a </p>", "should support as text if the opening is not the first thing");

            addTest("a *open <b> close* </b> c.", "<p>a <em>open  close</em>  c.</p>", "should not care about precedence between attention (emphasis)");

            addTest("a **open <b> close** </b> c.", "<p>a <strong>open  close</strong>  c.</p>", "should not care about precedence between attention (strong)");

            addTest("a [open <b> close](c) </b> d.", "<p>a <a href=\"c\">open  close</a>  d.</p>", "should not care about precedence between label (link)");

            addTest("a ![open <b> close](c) </b> d.", "<p>a <img src=\"c\" alt=\"open  close\" />  d.</p>", "should not care about precedence between label (image)");

            addTest("> a <b>\n> c </b> d.", "<blockquote>\n<p>a c  d.</p>\n</blockquote>", "should support line endings in elements");

            addTest("> a <b c=\"d\ne\" /> f", "<blockquote>\n<p>a  f</p>\n</blockquote>", "should support line endings in attribute values");

            addTest("> a <b c={d\ne} /> f", "<blockquote>\n<p>a  f</p>\n</blockquote>", "should support line endings in attribute value expressions");

            addTest("> a <b {c\nd} /> e", "<blockquote>\n<p>a  e</p>\n</blockquote>", "should support line endings in attribute expressions");

            addTest("1 < 3", "<p>1 &lt; 3</p>", "should allow `<` followed by markdown whitespace as text in markdown");
        }
    }

    @Nested
    class FlowAgnostic extends TestBase {
        {
            addTest("<a />", "", "should support a self-closing element");

            addTest("<a></a>", "", "should support a closed element");

            addTest("<a>\nb\n</a>", "<p>b</p>\n", "should support an element w/ content");

            addTest("<a>\n- b\n</a>", "<ul>\n<li>b</li>\n</ul>\n", "should support an element w/ containers as content");

            addTest("<a b c:d e=\"\" f={/* g */} {...h} />", "", "should support attributes");
        }
    }

    // Flow is mostly the same as `text`, so we only test the relevant
    // differences.
    @Nested
    class FlowEssence extends TestBase {
        {
            addTest("<a />", "", "should support an element");

            addTest("<a>\n- b\n</a>", "<ul>\n<li>b</li>\n</ul>\n", "should support an element around a container");

            addTest("<x\n  y\n>  \nb\n  </x>", "<p>b</p>\n", "should support a dangling `>` in a tag (not a block quote)");

            addTest("<a>  \nb\n  </a>", "<p>b</p>\n", "should support trailing initial and final whitespace around tags");

            addTest("<a> <b>\t\nc\n  </b> </a>", "<p>c</p>\n", "should support tags after tags");

            addErrorTest(
                    "> <X\n/>",
                    "Unexpected lazy line in container",
                    "should not support lazy flow (1)"
            );

            addErrorTest(
                    "> a\n> <X\n/>",
                    "Unexpected lazy line in container",
                    "should not support lazy flow (2)"
            );

            addTest("> a\n<X />", "<blockquote>\n<p>a</p>\n</blockquote>\n", "should not support lazy flow (3)");

        }
    }

    abstract class TestBase {
        private final List<DynamicNode> tests = new ArrayList<>();

        @TestFactory
        public List<DynamicNode> generateTests() {
            return tests;
        }

        protected final void addTest(String markdown, String expectedHtml, String message) {
            tests.add(DynamicTest.dynamicTest(message, () -> {
                assertEquals(expectedHtml, compile(markdown));
            }));
        }

        protected final void addErrorTest(String markdown, String expectedErrorMessageRegex, String message) {
            tests.add(DynamicTest.dynamicTest(message, () -> {
                var e = assertThrows(ParseException.class, () -> {
                    compile(markdown);
                });
                assertTrue(
                        Pattern.compile(expectedErrorMessageRegex).matcher(e.getMessage()).find(),
                        "Error message '" + e.getMessage() + "' didn't match expected regexp"
                );
            }));
        }

        protected final String compile(String markdown) {
            return new HtmlCompiler(new CompileOptions().withExtension(HTML)).compile(
                    Micromark.parseAndPostprocess(markdown, new ParseOptions().withExtension(MdxSyntax.EXTENSION))
            );
        }
    }
}

