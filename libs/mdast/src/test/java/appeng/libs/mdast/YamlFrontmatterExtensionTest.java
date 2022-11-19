package appeng.libs.mdast;

import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.micromark.extensions.YamlFrontmatterSyntax;
import org.junit.jupiter.api.Test;

class YamlFrontmatterExtensionTest extends AbstractMdAstTest {

    @Test
    void shouldNotSupportASingleYamlFenceThematicBreak() {
        assertJsonEquals(fromMarkdown("---"), """
                    {"type": "root", "children": [{"type": "thematicBreak"}]}
                """);
    }

    @Test
    void shouldParseEmptyYaml() {
        assertJsonEquals(fromMarkdown("---\n---"), """
                {"type": "root", "children": [{"type": "yamlFrontmatter", "value": ""}]}
                """);
    }

    @Test
    void shouldNotSupportAPrefixIndentBeforeAYamlOpeningFence() {
        assertJsonEquals(fromMarkdown(" ---\n---"),
                """                        
                            {
                                "type": "root",
                                        "children": [{"type": "thematicBreak"}, {"type": "thematicBreak"}]
                            }
                        """);

    }

    @Test
    void shouldNotSupportAPrefixIndentBeforeAYamlClosingFence() {
        assertJsonEquals(fromMarkdown("---\n ---"), """
                {
                    "type": "root",
                            "children": [{"type": "thematicBreak"}, {"type": "thematicBreak"}]
                }
                """);
    }

    @Test
    void shouldParseAnArbitrarySuffixAfterTheOpeningAndClosingFenceOfYaml() {
        assertJsonEquals(fromMarkdown("---  \n---\t "),
                """
                        {"type": "root", "children": [{"type": "yamlFrontmatter", "value": ""}]}
                        """);
    }

    @Test
    void shouldNotSupportOtherCharactersAfterTheSuffixOnTheOpeningFenceOfYaml() {
        assertJsonEquals(fromMarkdown("--- --\n---"), """
                {
                    "type": "root",
                            "children": [{"type": "thematicBreak"}, {"type": "thematicBreak"}]
                }
                """);
    }

    @Test
    void shouldNotSupportOtherCharactersAfterTheSuffixOnTheClosingFenceOfYaml() {
        assertJsonEquals(fromMarkdown("---\n--- x"), """
                  {
                      "type": "root",
                              "children": [
                      {"type": "thematicBreak"},
                      {"type": "paragraph", "children": [{"type": "text", "value": "--- x"}]}
                ]
                  }
                  """);
    }

    @Test
    void shouldNotSupportAnOpeningYamlFenceOfMoreThan3Characters() {
        assertJsonEquals(fromMarkdown("----\n---"), """
                     {
                         "type": "root",
                                 "children": [{"type": "thematicBreak"}, {"type": "thematicBreak"}]
                     }
                """);
    }

    @Test
    void shouldNotSupportAClosingYamlFenceOfMoreThan3Characters() {
        assertJsonEquals(fromMarkdown("---\n----"), """
                     {
                         "type": "root",
                                 "children": [{"type": "thematicBreak"}, {"type": "thematicBreak"}]
                     }
                """);
    }

    @Test
    void shouldNotSupportAnOpeningYamlFenceOfLessThan3Characters() {
        assertJsonEquals(fromMarkdown("--\n---"), """
                     {
                         "type": "root",
                                 "children": [
                         {"type": "heading", "depth": 2, "children": [{"type": "text", "value": "--"}]}
                   ]
                     }
                """);
    }

    @Test
    void shouldNotSupportAClosingYamlFenceOfLessThan3Characters() {
        assertJsonEquals(fromMarkdown("---\n--"), """
                     {
                         "type": "root",
                                 "children": [
                         {"type": "thematicBreak"},
                         {"type": "paragraph", "children": [{"type": "text", "value": "--"}]}
                   ]
                     }
                """);
    }

    @Test
    void shouldSupportContentInYaml() {
        assertJsonEquals(fromMarkdown("---\na\nb\n---"), """
                     {"type": "root", "children": [{"type": "yamlFrontmatter", "value": "a\\nb"}]}
                """);
    }

    @Test
    void shouldSupportBlankLinesInYaml() {
        assertJsonEquals(fromMarkdown("---\na\n\nb\n---"), """
                     {"type": "root", "children": [{"type": "yamlFrontmatter", "value": "a\\n\\nb"}]}
                """);
    }

    @Test
    void shouldNotSupportYamlFrontmatterInTheMiddle() {
        assertJsonEquals(fromMarkdown("# Hello\n---\na\n\nb\n---\n+++"), """
                     {
                         "type": "root",
                                 "children": [
                         {"type": "heading", "depth": 1, "children": [{"type": "text", "value": "Hello"}]},
                         {"type": "thematicBreak"},
                         {"type": "paragraph", "children": [{"type": "text", "value": "a"}]},
                         {"type": "heading", "depth": 2, "children": [{"type": "text", "value": "b"}]},
                         {"type": "paragraph", "children": [{"type": "text", "value": "+++"}]}
                   ]
                     }
                """);
    }

    private MdAstRoot fromMarkdown(String markdown) {
        var options = new MdastOptions();
        options.withSyntaxExtension(YamlFrontmatterSyntax.INSTANCE);
        options.withMdastExtension(YamlFrontmatterExtension.INSTANCE);

        return (MdAstRoot) removePosition(MdAst.fromMarkdown(markdown, options));
    }

}