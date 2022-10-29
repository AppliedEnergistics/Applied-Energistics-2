package appeng.libs.micromark;

import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.ParseOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtil {

    public static void assertGeneratedHtmlWithDisabled(String markdown, String expectedHtml, String disabledConstruct) {
        var options = new ParseOptions();
        options.withExtension(new Extension() {
            {
                nullDisable.add(disabledConstruct);
            }
        });
        assertGeneratedHtml(markdown, expectedHtml, options);
    }

    public static void assertGeneratedHtml(String markdown, String expectedHtml) {
        assertGeneratedHtml(markdown, expectedHtml, new ParseOptions());
    }

    public static void assertGeneratedHtml(String markdown, String expectedHtml, ParseOptions options) {
        markdown = markdown != null ? markdown : "";
        markdown = markdown.replace("^n", "\n");
        expectedHtml = expectedHtml != null ? expectedHtml : "";
        expectedHtml = expectedHtml.replace("^n", "\n");

        var html = new HtmlCompiler().compile(Micromark.parseAndPostprocess(markdown, options));
        assertEquals(expectedHtml, html);
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
