package appeng.libs.micromark.gfm.tables;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GfmTableTest {

    private final List<DynamicNode> tests = new ArrayList<>();

    @TestFactory
    public List<DynamicNode> generateTests() {
        makeTests();
        loadFixtures();
        return tests;
    }

    private void makeTests() {

        addTest("| a |",
                "<p>| a |</p>",
                "should not support a table w/ the head row ending in an eof (1)"
        );
        addTest("| a",
                "<p>| a</p>",
                "should not support a table w/ the head row ending in an eof (2)"
        );
        addTest("a |",
                "<p>a |</p>",
                "should not support a table w/ the head row ending in an eof (3)"
        );
        addTest("| a |\n| - |",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>",
                "should support a table w/ a delimiter row ending in an eof (1)"
        );
        addTest("| a\n| -",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>",
                "should support a table w/ a delimiter row ending in an eof (2)"
        );
        addTest("| a |\n| - |\n| b |",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>b</td>\n</tr>\n</tbody>\n</table>",
                "should support a table w/ a body row ending in an eof (1)"
        );
        addTest("| a\n| -\n| b",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>b</td>\n</tr>\n</tbody>\n</table>",
                "should support a table w/ a body row ending in an eof (2)"
        );
        addTest("a|b\n-|-\nc|d",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n<th>b</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>c</td>\n<td>d</td>\n</tr>\n</tbody>\n</table>",
                "should support a table w/ a body row ending in an eof (3)"
        );
        addTest("| a  \n| -\t\n| b |     ",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>b</td>\n</tr>\n</tbody>\n</table>",
                "should support rows w/ trailing whitespace (1)"
        );
        addTest("| a | \n| - |",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>",
                "should support rows w/ trailing whitespace (2)"
        );
        addTest("| a |\n| - | ",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>",
                "should support rows w/ trailing whitespace (3)"
        );
        addTest("| a |\n| - |\n| b | ",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>b</td>\n</tr>\n</tbody>\n</table>",
                "should support rows w/ trailing whitespace (4)"
        );
        addTest("||a|\n|-|-|",
                "<table>\n<thead>\n<tr>\n<th></th>\n<th>a</th>\n</tr>\n</thead>\n</table>",
                "should support empty first header cells"
        );
        addTest("|a||\n|-|-|",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n<th></th>\n</tr>\n</thead>\n</table>",
                "should support empty last header cells"
        );
        addTest("a||b\n-|-|-",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n<th></th>\n<th>b</th>\n</tr>\n</thead>\n</table>",
                "should support empty header cells"
        );
        addTest("|a|b|\n|-|-|\n||c|",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n<th>b</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td></td>\n<td>c</td>\n</tr>\n</tbody>\n</table>",
                "should support empty first body cells"
        );
        addTest("|a|b|\n|-|-|\n|c||",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n<th>b</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>c</td>\n<td></td>\n</tr>\n</tbody>\n</table>",
                "should support empty last body cells"
        );
        addTest("a|b|c\n-|-|-\nd||e",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n<th>b</th>\n<th>c</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>d</td>\n<td></td>\n<td>e</td>\n</tr>\n</tbody>\n</table>",
                "should support empty body cells"
        );
        addTest("| a |\n| - |\n- b",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<ul>\n<li>b</li>\n</ul>",
                "should support a list after a table"
        );
        addTest("> | a |\n| - |",
                "<blockquote>\n<p>| a |\n| - |</p>\n</blockquote>",
                "should not support a lazy delimiter row (1)"
        );
        addTest("> a\n> | b |\n| - |",
                "<blockquote>\n<p>a\n| b |\n| - |</p>\n</blockquote>",
                "should not support a lazy delimiter row (2)"
        );
        addTest("| a |\n> | - |",
                "<p>| a |</p>\n<blockquote>\n<p>| - |</p>\n</blockquote>",
                "should not support a lazy delimiter row (3)"
        );
        addTest("> a\n> | b |\n|-",
                "<blockquote>\n<p>a\n| b |\n|-</p>\n</blockquote>",
                "should not support a lazy delimiter row (4)"
        );
        addTest("> | a |\n> | - |\n| b |",
                "<blockquote>\n<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n</blockquote>\n<p>| b |</p>",
                "should not support a lazy body row (1)"
        );
        addTest("> a\n> | b |\n> | - |\n| c |",
                "<blockquote>\n<p>a</p>\n<table>\n<thead>\n<tr>\n<th>b</th>\n</tr>\n</thead>\n</table>\n</blockquote>\n<p>| c |</p>",
                "should not support a lazy body row (2)"
        );
        addTest("> | A |\n> | - |\n> | 1 |\n| 2 |",
                "<blockquote>\n<table>\n<thead>\n<tr>\n<th>A</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>1</td>\n</tr>\n</tbody>\n</table>\n</blockquote>\n<p>| 2 |</p>",
                "should not support a lazy body row (3)"
        );

        addTest("| a |\n   | - |",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>",
                "should form a table if the delimiter row is indented w/ 3 spaces"
        );
        addTest("| a |\n    | - |",
                "<p>| a |\n| - |</p>",
                "should not form a table if the delimiter row is indented w/ 4 spaces"
        );
        addTest("| a |\n    | - |",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>",
                "should form a table if the delimiter row is indented w/ 4 spaces and indented code is turned off",
                (parseOptions, compileOptions) -> {
                    parseOptions.withExtension(new Extension() {
                        {
                            nullDisable.add("codeIndented");
                        }
                    });
                }
        );
        addTest("| a |\n| - |\n> block quote?",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<blockquote>\n<p>block quote?</p>\n</blockquote>",
                "should be interrupted by a block quote"
        );
        addTest("| a |\n| - |\n>",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<blockquote>\n</blockquote>",
                "should be interrupted by a block quote (empty)"
        );
        addTest("| a |\n| - |\n- list?",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<ul>\n<li>list?</li>\n</ul>",
                "should be interrupted by a list"
        );
        addTest("| a |\n| - |\n-",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<ul>\n<li></li>\n</ul>",
                "should be interrupted by a list (empty)"
        );
        addTest("| a |\n| - |\n<!-- HTML? -->",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<!-- HTML? -->",
                "should be interrupted by HTML (flow)",
                (parseOptions, compileOptions) -> {
                    compileOptions.allowDangerousHtml();
                }
        );
        addTest("| a |\n| - |\n\tcode?",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<pre><code>code?\n</code></pre>",
                "should be interrupted by code (indented)",
                (parseOptions, compileOptions) -> {
                    compileOptions.allowDangerousHtml();
                }
        );
        addTest("| a |\n| - |\n```js\ncode?",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<pre><code class=\"language-js\">code?\n</code></pre>\n",
                "should be interrupted by code (fenced)",
                (parseOptions, compileOptions) -> {
                    compileOptions.allowDangerousHtml();
                }
        );
        addTest("| a |\n| - |\n***",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<hr />",
                "should be interrupted by a thematic break",
                (parseOptions, compileOptions) -> {
                    compileOptions.allowDangerousHtml();
                }
        );
        addTest("| a |\n| - |\n# heading?",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n</table>\n<h1>heading?</h1>",
                "should be interrupted by a heading (ATX)"
        );
        addTest("| a |\n| - |\nheading\n=",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>heading</td>\n</tr>\n<tr>\n<td>=</td>\n</tr>\n</tbody>\n</table>",
                "should *not* be interrupted by a heading (setext)"
        );
        addTest("| a |\n| - |\nheading\n---",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>heading</td>\n</tr>\n</tbody>\n</table>\n<hr />",
                "should *not* be interrupted by a heading (setext, but interrupt if the underline is also a thematic break"
        );
        addTest("| a |\n| - |\nheading\n-",
                "<table>\n<thead>\n<tr>\n<th>a</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>heading</td>\n</tr>\n</tbody>\n</table>\n<ul>\n<li></li>\n</ul>",
                "should *not* be interrupted by a heading (setext, but interrupt if the underline is also an empty list item bullet"
        );
    }

    private void loadFixtures() {
        var fixtureNames = new String[]{
                "align",
                "basic",
                "containers",
                "gfm",
                "grave",
                "indent",
                "loose",
                "some-escapes"
        };

        for (String fixtureName : fixtureNames) {
            tests.add(DynamicTest.dynamicTest(fixtureName, () -> {
                String markdown, expectedHtml;
                try (var input = GfmTableTest.class.getResourceAsStream(fixtureName + ".md")) {
                    markdown = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                }
                try (var input = GfmTableTest.class.getResourceAsStream(fixtureName + ".html")) {
                    expectedHtml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                }

                var html = toHtmlWithGfm(markdown, (parseOptions, compileOptions) -> {
                    compileOptions.allowDangerousHtml();
                    compileOptions.allowDangerousProtocol();
                });

                if (!html.endsWith("\n")) {
                    html += "\n";
                }

                if (fixtureName.equals("some-escapes")) {
                    expectedHtml = expectedHtml
                            .replace("C | Charlie", "C \\")
                            .replace("E \\| Echo", "E \\\\");
                }

                assertEquals(expectedHtml, html);
            }));
        }
    }

    @Test
    public void testShouldNotChangeHowListsAndLazynessWork() {
        var doc = "   - d\n    - e";

        assertEquals(
                new HtmlCompiler().compile(Micromark.parseAndPostprocess(doc)),
                toHtmlWithGfm(doc, null)
        );
    }

    private void addTest(String markdown, String expectedHtml, String test) {
        addTest(markdown, expectedHtml, test, null);
    }

    private void addTest(String markdown, String expectedHtml, String test, BiConsumer<ParseOptions, CompileOptions> options) {
        tests.add(DynamicTest.dynamicTest(test, () -> {
            var html = toHtmlWithGfm(markdown, options);
            assertEquals(expectedHtml, html);
        }));
    }

    private String toHtmlWithGfm(String markdown, BiConsumer<ParseOptions, CompileOptions> options) {
        var parseOptions = new ParseOptions();
        parseOptions.withExtension(GfmTable.INSTANCE);

        var compileOptions = new CompileOptions();
        compileOptions.withExtension(GfmTableHtml.EXTENSION);

        if (options != null) {
            options.accept(parseOptions, compileOptions);
        }

        return new HtmlCompiler(compileOptions).compile(Micromark.parseAndPostprocess(markdown, parseOptions));
    }

}
