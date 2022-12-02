package appeng.libs.mdast;

import appeng.libs.mdast.model.MdAstBreak;
import appeng.libs.mdast.model.MdAstEmphasis;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.mdast.model.MdAstParagraph;
import appeng.libs.mdast.model.MdAstStrong;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Types;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MdAstTest extends AbstractMdAstTest {

    @Test
    public void shouldParseAnEmptyDocument() {
        assertJsonEquals(
                "",
                """
                        {
                         "type": "root",
                         "children": [],
                         "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 1, "offset": 0}
                         }}
                         """
        );
    }

    @Test
    public void shouldParseAParagraph() {
        assertJsonEquals(
                "a\nb",
                """
                         {
                           "type": "root",
                           "children": [
                             {
                               "type": "paragraph",
                               "children": [
                                 {
                                   "type": "text",
                                   "value": "a\\nb",
                                   "position": {
                                     "start": {"line": 1, "column": 1, "offset": 0},
                                     "end": {"line": 2, "column": 2, "offset": 3}
                                   }
                                 }
                               ],
                               "position": {
                                 "start": {"line": 1, "column": 1, "offset": 0 },
                                 "end": {"line": 2, "column": 2, "offset": 3}
                               }
                             }
                           ],
                           "position": {
                             "start": {"line": 1, "column": 1, "offset": 0},
                             "end": {"line": 2, "column": 2, "offset": 3}
                           }
                         }
                        """);
    }

    @Test
    public void shouldSupportExtensions() {
        var extension = MdastExtension.builder()
                .canContainEol("someType")
                .enter("lineEnding", (context, token) -> {
                    context.enter(new MdAstBreak(), token);
                })
                .exit("lineEnding", MdastContext::exit)
                .build();

        assertEquals(
                toJsonFirstNode("a\nb", new MdastOptions().withMdastExtension(extension)),
                normalizeJson("""
                        {
                           "type": "paragraph",
                            "children": [
                            {
                                "type": "text",
                                "value": "a",
                                "position": {
                                    "start": {"line": 1, "column": 1, "offset": 0},
                                    "end": {"line": 1, "column": 2, "offset": 1}
                                }
                            },
                            {
                                "type": "break",
                                "position": {
                                  "start": {"line": 1, "column": 2, "offset": 1},
                                  "end": {"line": 2, "column": 1, "offset": 2}
                               }
                            },
                            {
                                "type": "text",
                                "value": "b",
                                "position": {
                                    "start": {"line": 2, "column": 1, "offset": 2},
                                    "end": {"line": 2, "column": 2, "offset": 3}
                                 }
                            }
                              ],
                                "position": {
                                    "start": {"line": 1, "column": 1, "offset": 0},
                                    "end": {"line": 2, "column": 2, "offset": 3}
                                }
                            }
                        """)
        );
    }

    @Test
    public void shouldSupportMultipleExtensions() {
        var extension1 = MdastExtension.builder()
                .enter("lineEnding", (context, token) -> {
                    context.enter(new MdAstBreak(), token);
                })
                .build();
        var extension2 = MdastExtension.builder()
                .exit("lineEnding", MdastContext::exit)
                .build();

        var options = new MdastOptions().withMdastExtension(extension1).withMdastExtension(extension2);

        assertEquals(
                toJsonFirstNode("a\nb", options),
                normalizeJson("""
                                {
                                                        "type": "paragraph",
                                                "children": [
                                        {
                                            "type": "text",
                                                    "value": "a",
                                                "position": {
                                            "start": {"line": 1, "column": 1, "offset": 0},
                                            "end": {"line": 1, "column": 2, "offset": 1}
                                        }
                                        },
                                        {
                                            "type": "break",
                                                    "position": {
                                            "start": {"line": 1, "column": 2, "offset": 1},
                                            "end": {"line": 2, "column": 1, "offset": 2}
                                        }
                                        },
                                        {
                                            "type": "text",
                                                    "value": "b",
                                                "position": {
                                            "start": {"line": 2, "column": 1, "offset": 2},
                                            "end": {"line": 2, "column": 2, "offset": 3}
                                        }
                                        }
                                      ],
                                        "position": {
                                            "start": {"line": 1, "column": 1, "offset": 0},
                                            "end": {"line": 2, "column": 2, "offset": 3}
                                        }
                                    }
                        """)
        );
    }

    @Test
    void shouldSupportTransformsInExtensions() {

        var extension = MdastExtension.builder()
                .transform(tree -> {
                    var firstChild = (MdAstParagraph) tree.children().get(0);
                    var firstTextNode = (MdAstEmphasis) firstChild.children().get(0);
                    var replacement = new MdAstStrong();
                    for (var child : firstTextNode.children()) {
                        replacement.addChild((MdAstNode) child);
                    }
                    replacement.position = firstTextNode.position;
                    firstChild.replaceChild(firstTextNode, replacement);
                    return tree;
                })
                .build();

        var json = toJsonFirstNode("*a*", new MdastOptions().withMdastExtension(extension));
        var expectedJson = normalizeJson("""
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "strong",
                                    "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 3, "offset": 2}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 4, "offset": 3}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                    }
                """);

        assertEquals(expectedJson, json);
    }

    @Test
    public void shouldCrashIfATokenIsOpenedButNotClosed() {

        var extension = MdastExtension.builder()
                .enter(Types.paragraph, (context, token) -> {
                    context.enter(new MdAstParagraph(), token);
                })
                .exit(Types.paragraph, () -> {
                })
                .build();

        var e = assertThrows(RuntimeException.class, () -> {
            toJson("a", new MdastOptions().withMdastExtension(extension));
        });
        assertEquals("Cannot close document, a token (`paragraph`, 1:1-1:2) is still open", e.getMessage());

    }

    @Test
    public void shouldCrashWhenClosingATokenThatIsntOpen() {

        var extension = MdastExtension.builder()
                .enter(Types.paragraph, MdastContext::exit)
                .build();

        var e = assertThrows(RuntimeException.class, () -> {
            toJson("a", new MdastOptions().withMdastExtension(extension));
        });
        assertEquals(e.getMessage(), "Cannot close `paragraph` (1:1-1:2): it’s not open");

    }

    @Test
    public void shouldCrashWhenClosingATokenWhenADifferentOneIsOpen() {

        var extension = MdastExtension.builder()
                .exit(Types.paragraph, (context, token) -> {
                    var t = new Token(token);
                    t.type = "lol";
                    context.exit(t);
                })
                .build();

        var e = assertThrows(RuntimeException.class, () -> {
            toJson("a", new MdastOptions().withMdastExtension(extension));
        });
        assertEquals("Cannot close `lol` (1:1-1:2): a different token (`paragraph`, 1:1-1:2) is open", e.getMessage());

    }

    @Test
    public void shouldCrashWhenClosingATokenWhenADifferentOneIsOpenWithCustomHandler() {

        var extension = MdastExtension.builder()
                .exit(Types.paragraph, (context, token) -> {
                    var t = new Token();
                    t.type = "lol";
                    context.exit(t, (ctx, left, right) -> {
                        assertEquals("lol", left.type);
                        assertEquals(Types.paragraph, right.type);
                        throw new IllegalArgumentException("problem");
                    });
                })
                .build();

        var e = assertThrows(IllegalArgumentException.class, () -> {
            toJson("a", new MdastOptions().withMdastExtension(extension));
        });
        assertEquals(e.getMessage(), "problem");

    }

    @Test
    void shouldParseAnAutolinkProtocol() {
        assertJsonEqualsOnFirstNode("<tel:123>", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "link",
                                    "title": null,
                                "url": "tel:123",
                                "children": [
                            {
                                "type": "text",
                                        "value": "tel:123",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 9, "offset": 8}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 10, "offset": 9}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 10, "offset": 9}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAnAutolinkEmail() {
        assertJsonEqualsOnFirstNode("<aa@bb.cc>", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "link",
                                    "title": null,
                                "url": "mailto:aa@bb.cc",
                                "children": [
                            {
                                "type": "text",
                                        "value": "aa@bb.cc",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 10, "offset": 9}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 11, "offset": 10}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 11, "offset": 10}
                        }
                    }
                """);
    }


    @Test
    void shouldParseABlockQuote() {
        assertJsonEqualsOnFirstNode("> a", """
                {
                                        "type": "blockquote",
                                "children": [
                        {
                            "type": "paragraph",
                                    "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 3, "offset": 2},
                                "end": {"line": 1, "column": 4, "offset": 3}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 3, "offset": 2},
                                "end": {"line": 1, "column": 4, "offset": 3}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                    }
                """);
    }


    @Test
    void shouldParseACharacterEscape() {
        assertJsonEqualsOnFirstNode("a\\*b", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "text",
                                    "value": "a*b",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 5, "offset": 4}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 5, "offset": 4}
                        }
                    }
                """);
    }


    @Test
    void shouldParseACharacterReference() {
        assertJsonEqualsOnFirstNode("a&amp;b", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "text",
                                    "value": "a&b",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 8, "offset": 7}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 8, "offset": 7}
                        }
                    }
                """);
    }


    @Test
    void shouldParseCodeFenced() {
        assertJsonEqualsOnFirstNode("```a b\nc\n```", """
                {
                                        "type": "code",
                                "lang": "a",
                                "meta": "b",
                                "value": "c",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 3, "column": 4, "offset": 12}
                        }
                    }
                """);
    }


    @Test
    void shouldParseCodeIndented() {
        assertJsonEqualsOnFirstNode("    a", """
                {
                                        "type": "code",
                                "lang": null,
                                "meta": null,
                                "value": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 6, "offset": 5}
                        }
                    }
                """);
    }


    @Test
    void shouldParseCodeText() {
        assertJsonEqualsOnFirstNode("`a`", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "inlineCode",
                                    "value": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                    }
                """);
    }


    @Test
    void shouldParseADefinition() {
        assertJsonEqualsOnFirstNode("[a]: b \"c\"", """
                {
                                        "type": "definition",
                                "identifier": "a",
                                "label": "a",
                                "title": "c",
                                "url": "b",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 11, "offset": 10}
                        }
                    }
                """);
    }


    @Test
    void shouldParseEmphasis() {
        assertJsonEqualsOnFirstNode("*a*", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "emphasis",
                                    "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 3, "offset": 2}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 4, "offset": 3}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAHardBreakEscape() {
        assertJsonEqualsOnFirstNode("a\\\nb", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "text",
                                    "value": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 2, "offset": 1}
                        }
                        },
                        {
                            "type": "break",
                                    "position": {
                            "start": {"line": 1, "column": 2, "offset": 1},
                            "end": {"line": 2, "column": 1, "offset": 3}
                        }
                        },
                        {
                            "type": "text",
                                    "value": "b",
                                "position": {
                            "start": {"line": 2, "column": 1, "offset": 3},
                            "end": {"line": 2, "column": 2, "offset": 4}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 2, "column": 2, "offset": 4}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAHardBreakPrefix() {
        assertJsonEqualsOnFirstNode("a  \nb", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "text",
                                    "value": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 2, "offset": 1}
                        }
                        },
                        {
                            "type": "break",
                                    "position": {
                            "start": {"line": 1, "column": 2, "offset": 1},
                            "end": {"line": 2, "column": 1, "offset": 4}
                        }
                        },
                        {
                            "type": "text",
                                    "value": "b",
                                "position": {
                            "start": {"line": 2, "column": 1, "offset": 4},
                            "end": {"line": 2, "column": 2, "offset": 5}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 2, "column": 2, "offset": 5}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAHeadingAtx() {
        assertJsonEqualsOnFirstNode("## a", """
                {
                                        "type": "heading",
                                "depth": 2,
                                "children": [
                        {
                            "type": "text",
                                    "value": "a",
                                "position": {
                            "start": {"line": 1, "column": 4, "offset": 3},
                            "end": {"line": 1, "column": 5, "offset": 4}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 5, "offset": 4}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAHeadingSetext() {
        assertJsonEqualsOnFirstNode("a\n=", """
                {
                                        "type": "heading",
                                "depth": 1,
                                "children": [
                        {
                            "type": "text",
                                    "value": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 2, "offset": 1}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 2, "column": 2, "offset": 3}
                        }
                    }
                """);
    }


    @Test
    void shouldParseHtmlFlow() {
        assertJsonEqualsOnFirstNode("<a>\nb\n</a>", """
                {
                                        "type": "html",
                                "value": "<a>\\nb\\n</a>",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 3, "column": 5, "offset": 10}
                        }
                    }
                """);
    }


    @Test
    void shouldParseHtmlText() {
        assertJsonEqualsOnFirstNode("<a>b</a>", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "html",
                                    "value": "<a>",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                        },
                        {
                            "type": "text",
                                    "value": "b",
                                "position": {
                            "start": {"line": 1, "column": 4, "offset": 3},
                            "end": {"line": 1, "column": 5, "offset": 4}
                        }
                        },
                        {
                            "type": "html",
                                    "value": "</a>",
                                "position": {
                            "start": {"line": 1, "column": 5, "offset": 4},
                            "end": {"line": 1, "column": 9, "offset": 8}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 9, "offset": 8}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAnImageShortcutReference() {
        assertJsonEqualsOnFirstNode("![a]\n\n[a]: b", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "imageReference",
                                    "identifier": "a",
                                "label": "a",
                                "referenceType": "shortcut",
                                "alt": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 5, "offset": 4}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 5, "offset": 4}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAnImageCollapsedReference() {
        assertJsonEqualsOnFirstNode("![a][]\n\n[a]: b", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "imageReference",
                                    "identifier": "a",
                                "label": "a",
                                "referenceType": "collapsed",
                                "alt": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 7, "offset": 6}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 7, "offset": 6}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAnImageFullReference() {
        assertJsonEqualsOnFirstNode("![a][b]\n\n[b]: c", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "imageReference",
                                    "identifier": "b",
                                "label": "b",
                                "referenceType": "full",
                                "alt": "a",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 8, "offset": 7}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 8, "offset": 7}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAnImageResource() {
        assertJsonEqualsOnFirstNode("![a](b \"c\")", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "image",
                                    "title": "c",
                                "alt": "a",
                                "url": "b",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 12, "offset": 11}
                        }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 12, "offset": 11}
                        }
                    }
                """);
    }


    @Test
    void shouldParseALinkShortcutReference() {
        assertJsonEqualsOnFirstNode("[a]\n\n[a]: b", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "linkReference",
                                    "identifier": "a",
                                "label": "a",
                                "referenceType": "shortcut",
                                "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 3, "offset": 2}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 4, "offset": 3}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                    }
                """);
    }


    @Test
    void shouldParseALinkCollapsedReference() {
        assertJsonEqualsOnFirstNode("[a][]\n\n[a]: b", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "linkReference",
                                    "identifier": "a",
                                "label": "a",
                                "referenceType": "collapsed",
                                "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 3, "offset": 2}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 6, "offset": 5}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 6, "offset": 5}
                        }
                    }
                """);
    }


    @Test
    void shouldParseALinkCollapsedReferenceWithInlineCodeInTheLabel() {
        assertJsonEqualsOnFirstNode("[`a`][]\n\n[`a`]: b", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "linkReference",
                                    "children": [
                            {
                                "type": "inlineCode",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 5, "offset": 4}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 8, "offset": 7}
                            },
                            "identifier": "`a`",
                                    "label": "`a`",
                                "referenceType":"collapsed"
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 8, "offset": 7}
                        }
                    }
                """);
    }


    @Test
    void shouldParseALinkFullReference() {
        assertJsonEqualsOnFirstNode("[a][b]\n\n[b]: c", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "linkReference",
                                    "identifier": "b",
                                "label": "b",
                                "referenceType": "full",
                                "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 3, "offset": 2}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 7, "offset": 6}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 7, "offset": 6}
                        }
                    }
                """);
    }


    @Test
    void shouldParseALinkResource() {
        assertJsonEqualsOnFirstNode("[a](b \"c\")", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "link",
                                    "title": "c",
                                "url": "b",
                                "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 2, "offset": 1},
                                "end": {"line": 1, "column": 3, "offset": 2}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 11, "offset": 10}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 11, "offset": 10}
                        }
                    }
                """);
    }


    // List.

    @Test
    void shouldParseStrong() {
        assertJsonEqualsOnFirstNode("**a**", """
                {
                                        "type": "paragraph",
                                "children": [
                        {
                            "type": "strong",
                                    "children": [
                            {
                                "type": "text",
                                        "value": "a",
                                    "position": {
                                "start": {"line": 1, "column": 3, "offset": 2},
                                "end": {"line": 1, "column": 4, "offset": 3}
                            }
                            }
                          ],
                            "position": {
                                "start": {"line": 1, "column": 1, "offset": 0},
                                "end": {"line": 1, "column": 6, "offset": 5}
                            }
                        }
                      ],
                        "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 6, "offset": 5}
                        }
                    }
                """);
    }


    @Test
    void shouldParseAThematicBreak() {
        assertJsonEqualsOnFirstNode("***", """
                {
                                        "type": "thematicBreak",
                                "position": {
                            "start": {"line": 1, "column": 1, "offset": 0},
                            "end": {"line": 1, "column": 4, "offset": 3}
                        }
                    }
                """);
    }

    @TestFactory
    public Stream<DynamicTest> textFixtures() throws Exception {
        var anchor = MdAstTest.class.getResource("fixtures/attention.md");
        var fixtureFolder = Paths.get(anchor.toURI()).getParent();

        return Files.walk(fixtureFolder, 1)
                .filter(f -> f.getFileName().toString().endsWith(".md"))
                .map(mdFile -> {
                    String baseName = mdFile.getFileName().toString().replaceAll("\\.md$", "");
                    var refJsonPath = mdFile.resolveSibling(baseName + ".json");

                    return DynamicTest.dynamicTest(baseName, mdFile.toUri(), () -> {
                        runFixtureTest(mdFile, refJsonPath);
                    });
                });
    }

    private void runFixtureTest(Path markdownPath, Path jsonPath) throws Exception {
        var markdown = Files.readString(markdownPath);
        markdown = markdown.replace("\r\n", "\n");
        @Language("json") var expectedJson = Files.readString(jsonPath);

        assertJsonEquals(markdown, expectedJson);
    }

}
