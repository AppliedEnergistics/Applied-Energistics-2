package appeng.libs.micromark;

import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtil {

    public static void assertGeneratedHtmlWithDisabled(String markdown, String expectedHtml, String disabledConstruct) {
        assertGeneratedHtmlWithDisabled(markdown, expectedHtml, null, disabledConstruct);
    }

    public static void assertGeneratedHtmlWithDisabled(String markdown, String expectedHtml, String message, String disabledConstruct) {
        var options = new ParseOptions();
        options.withExtension(new Extension() {
            {
                nullDisable.add(disabledConstruct);
            }
        });
        assertGeneratedHtml(markdown, expectedHtml, message, options);
    }

    public static void assertGeneratedHtmlLines(List<String> markdown, List<String> expectedHtml) {
        assertGeneratedHtml(String.join("\n", markdown), String.join("\n", expectedHtml), null, new ParseOptions());
    }

    public static void assertGeneratedHtmlLinesUnsafe(List<String> markdown, List<String> expectedHtml) {
        assertGeneratedDangerousHtml(String.join("\n", markdown), String.join("\n", expectedHtml));
    }

    public static void assertGeneratedHtml(String markdown, String expectedHtml) {
        assertGeneratedHtml(markdown, expectedHtml, null, new ParseOptions());
    }

    public static void assertGeneratedHtml(String markdown, String expectedHtml, String message) {
        assertGeneratedHtml(markdown, expectedHtml, message, new ParseOptions(), new CompileOptions());
    }

    public static void assertGeneratedHtml(String markdown, String expectedHtml, String message, ParseOptions options) {
        assertGeneratedHtml(markdown, expectedHtml, message, options, new CompileOptions());
    }

    public static void assertGeneratedHtml(String markdown, String expectedHtml, String message, ParseOptions options, CompileOptions compileOptions) {
        markdown = markdown != null ? markdown : "";
        markdown = markdown.replace("^n", "\n");
        expectedHtml = expectedHtml != null ? expectedHtml : "";
        expectedHtml = expectedHtml.replace("^n", "\n");

        var html = new HtmlCompiler(compileOptions).compile(Micromark.parseAndPostprocess(markdown, options));
        assertEquals(expectedHtml, html, message);
    }

    public static void assertGeneratedDangerousHtml(String markdown, String expectedHtml, String message) {
        markdown = markdown != null ? markdown : "";
        markdown = markdown.replace("^n", "\n");
        expectedHtml = expectedHtml != null ? expectedHtml : "";
        expectedHtml = expectedHtml.replace("^n", "\n");

        var htmlOptions = new CompileOptions().allowDangerousHtml();

        var html = new HtmlCompiler(htmlOptions).compile(Micromark.parseAndPostprocess(markdown));
        assertEquals(expectedHtml, html, message);
    }

    public static void assertGeneratedDangerousHtml(String markdown, String expectedHtml) {
        assertGeneratedDangerousHtml(markdown, expectedHtml, null);
    }

}
