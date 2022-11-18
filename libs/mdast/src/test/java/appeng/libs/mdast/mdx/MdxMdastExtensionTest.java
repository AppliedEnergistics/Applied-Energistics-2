package appeng.libs.mdast.mdx;

import appeng.libs.mdast.AbstractMdAstTest;
import appeng.libs.mdast.MdAst;
import appeng.libs.mdast.MdastOptions;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.mdx.MdxSyntax;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MdxMdastExtensionTest extends AbstractMdAstTest {

    @Test
    void shouldSupportFlowJsxAgnostic() {
        assertJsonEquals(
                fromMdx("<a />"),
                """
                                    {
                                    "type": "root",
                                            "children": [
                                    {
                                        "type": "mdxJsxFlowElement",
                                                "name": "a",
                                            "attributes": [],
                                        "children": [],
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
    void shouldSupportFlowJsxWithJustWhitespace() {
        assertJsonEquals(
                removePosition(fromMdx("<x>\t \n</x>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {"type": "mdxJsxFlowElement", "name": "x", "attributes": [], "children": []}
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportSelfClosingTextJsx() {
        assertJsonEquals(
                removePosition(fromMdx("a <b/> c.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [],
                                      "children": []
                                  },
                                  {"type": "text", "value": " c."}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportAClosedTextJsx() {
        assertJsonEquals(
                removePosition(fromMdx("a <b></b> c.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [],
                                      "children": []
                                  },
                                  {"type": "text", "value": " c."}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportTextJsxWithContent() {
        assertJsonEquals(
                removePosition(fromMdx("a <b>c</b> d.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [],
                                      "children": [{"type": "text", "value": "c"}]
                                  },
                                  {"type": "text", "value": " d."}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportTextJsxWithMarkdownContent() {
        assertJsonEquals(
                removePosition(fromMdx("a <b>*c*</b> d.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [],
                                      "children": [
                                      {"type": "emphasis", "children": [{"type": "text", "value": "c"}]}
                                ]
                                  },
                                  {"type": "text", "value": " d."}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportAFragmentTextJsx() {
        assertJsonEquals(
                removePosition(fromMdx("a <></> b.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": null,
                                          "attributes": [],
                                      "children": []
                                  },
                                  {"type": "text", "value": " b."}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldCrashOnAnUnclosedTextJsx() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <b> c");
        });
        assertEquals("Expected a closing tag for `<b>` (1:3-1:6) before the end of `paragraph`", e.getMessage());
    }

    @Test
    void shouldCrashOnAnUnclosedFlowJsx() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("<a>");
        });
        assertEquals("Expected a closing tag for `<a>` (1:1-1:4)", e.getMessage());
    }

    @Test
    void shouldSupportAnAttributeExpressionInTextJsx() {
        assertJsonEquals(
                removePosition(fromMdx("a <b {1 + 1} /> c")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [{"type": "mdxJsxExpressionAttribute", "value": "1 + 1"}],
                                      "children": []
                                  },
                                  {"type": "text", "value": " c"}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportAnAttributeValueExpressionInTextJsx() {
        assertJsonEquals(
                removePosition(fromMdx("a <b c={1 + 1} /> d")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [
                                      {
                                          "type": "mdxJsxAttribute",
                                                  "name": "c",
                                              "value": {
                                          "type": "mdxJsxAttributeValueExpression",
                                                  "value": "1 + 1"
                                      }
                                      }
                                ],
                                      "children": []
                                  },
                                  {"type": "text", "value": " d"}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldNotSupportWhitespaceInTheOpeningTagFragment() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a < \t>b</>");
        });
        assertEquals("Unexpected closing slash `/` in tag, expected an open tag first", e.getMessage());
    }

    @Test
    void shouldSupportWhitespaceInTheOpeningTagNamed() {
        assertJsonEquals(
                removePosition(fromMdx("a <b\t>c</b>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [],
                                      "children": [{"type": "text", "value": "c"}]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportNonAsciiIdentifierStartCharacters() {
        assertJsonEquals(
                removePosition(fromMdx("<π />")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {"type": "mdxJsxFlowElement", "name": "π", "attributes": [], "children": []}
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportNonAsciiIdentifierContinuationCharacters() {
        assertJsonEquals(
                removePosition(fromMdx("<a\u200Cb />")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {"type": "mdxJsxFlowElement", "name": "a‌b", "attributes": [], "children": []}
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportDotsInNamesForMethodNames() {
        assertJsonEquals(
                removePosition(fromMdx("<abc . def.ghi />")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "abc.def.ghi",
                                      "attributes": [],
                                  "children": []
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportColonsInNamesForLocalNames() {
        assertJsonEquals(
                removePosition(fromMdx("<svg: rect>b</  svg :rect>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "svg:rect",
                                          "attributes": [],
                                      "children": [{"type": "text", "value": "b"}]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportAttributes() {
        assertJsonEquals(
                removePosition(fromMdx("a <b c     d=\"d\"\t\tefg='h'>i</b>.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [
                                      {"type": "mdxJsxAttribute", "name": "c", "value": null},
                                      {"type": "mdxJsxAttribute", "name": "d", "value": "d"},
                                      {"type": "mdxJsxAttribute", "name": "efg", "value": "h"}
                                ],
                                      "children": [{"type": "text", "value": "i"}]
                                  },
                                  {"type": "text", "value": "."}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportPrefixedAttributes() {
        assertJsonEquals(
                removePosition(fromMdx("<a xml :\tlang\n= \"de-CH\" foo:bar/>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "a",
                                      "attributes": [
                                  {"type": "mdxJsxAttribute", "name": "xml:lang", "value": "de-CH"},
                                  {"type": "mdxJsxAttribute", "name": "foo:bar", "value": null}
                            ],
                                  "children": []
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportPrefixedAndNormalAttributes() {
        assertJsonEquals(
                removePosition(fromMdx("<b a b : c d : e = \"f\" g/>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "b",
                                      "attributes": [
                                  {"type": "mdxJsxAttribute", "name": "a", "value": null},
                                  {"type": "mdxJsxAttribute", "name": "b:c", "value": null},
                                  {"type": "mdxJsxAttribute", "name": "d:e", "value": "f"},
                                  {"type": "mdxJsxAttribute", "name": "g", "value": null}
                            ],
                                  "children": []
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportCodeTextInJsxText() {
        assertJsonEquals(
                removePosition(fromMdx("a <>`<`</> c")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": null,
                                          "attributes": [],
                                      "children": [{"type": "inlineCode", "value": "<"}]
                                  },
                                  {"type": "text", "value": " c"}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportCodeFencedInJsxFlow() {
        assertJsonEquals(
                removePosition(fromMdx("<>\n```js\n<\n```\n</>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": null,
                                      "attributes": [],
                                  "children": [{"type": "code", "lang": "js", "meta": null, "value": "<"}]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldCrashOnAClosingTagWithoOpenElementsText() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a </> c");
        });
        assertEquals("Unexpected closing slash `/` in tag, expected an open tag first", e.getMessage());
    }

    @Test
    void shouldCrashOnAClosingTagWithoOpenElementsFlow() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("</>");
        });
        assertEquals("Unexpected closing slash `/` in tag, expected an open tag first", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags1() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <></b>");
        });
        assertEquals("Unexpected closing tag `</b>`, expected corresponding closing tag for `<>` (1:3-1:5)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags2() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <b></>");
        });
        assertEquals("Unexpected closing tag `</>`, expected corresponding closing tag for `<b>` (1:3-1:6)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags3() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <a.b></a>");
        });
        assertEquals("Unexpected closing tag `</a>`, expected corresponding closing tag for `<a.b>` (1:3-1:8)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags4() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <a></a.b>");
        });
        assertEquals("Unexpected closing tag `</a.b>`, expected corresponding closing tag for `<a>` (1:3-1:6)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags5() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <a.b></a.c>");
        });
        assertEquals("Unexpected closing tag `</a.c>`, expected corresponding closing tag for `<a.b>` (1:3-1:8)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags6() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <a:b></a>");
        });
        assertEquals("Unexpected closing tag `</a>`, expected corresponding closing tag for `<a:b>` (1:3-1:8)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags7() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <a></a:b>");
        });
        assertEquals("Unexpected closing tag `</a:b>`, expected corresponding closing tag for `<a>` (1:3-1:6)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags8() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <a:b></a:c>");
        });
        assertEquals("Unexpected closing tag `</a:c>`, expected corresponding closing tag for `<a:b>` (1:3-1:8)", e.getMessage());
    }

    @Test
    void shouldCrashOnMismatchedTags9() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a <a:b></a.b>");
        });
        assertEquals("Unexpected closing tag `</a.b>`, expected corresponding closing tag for `<a:b>` (1:3-1:8)", e.getMessage());
    }

    @Test
    void shouldCrashOnAClosingSelfClosingTag() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("<a>b</a/>");
        });
        assertEquals("Unexpected self-closing slash `/` in closing tag, expected the end of the tag", e.getMessage());
    }

    @Test
    void shouldCrashOnAClosingTagWithAttributes() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("<a>b</a b>");
        });
        assertEquals("Unexpected attribute in closing tag, expected the end of the tag", e.getMessage());
    }

    @Test
    void shouldSupportNestedJsxText() {
        assertJsonEquals(
                removePosition(fromMdx("a <b>c <>d</> e</b>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "a "},
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "b",
                                          "attributes": [],
                                      "children": [
                                      {"type": "text", "value": "c "},
                                      {
                                          "type": "mdxJsxTextElement",
                                                  "name": null,
                                              "attributes": [],
                                          "children": [{"type": "text", "value": "d"}]
                                      },
                                      {"type": "text", "value": " e"}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportNestedJsxFlow() {
        assertJsonEquals(
                removePosition(fromMdx("<a> <>\nb\n</>\n</a>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "a",
                                      "attributes": [],
                                  "children": [
                                  {
                                      "type": "mdxJsxFlowElement",
                                              "name": null,
                                          "attributes": [],
                                      "children": [
                                      {"type": "paragraph", "children": [{"type": "text", "value": "b"}]}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportCharacterReferencesInAttributeValues() {
        assertJsonEquals(
                removePosition(fromMdx("<x y=\"Character references can be used: &quot;, &apos;, &lt;, &gt;, &#x7B;, and &#x7D;, they can be named, decimal, or hexadecimal: &copy; &#8800; &#x1D306;\" />")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "x",
                                      "attributes": [
                                  {
                                      "type": "mdxJsxAttribute",
                                              "name": "y",
                                          "value":
                                      "Character references can be used: \\", ', <, >, {, and }, they can be named, decimal, or hexadecimal: © ≠ 𝌆"
                                  }
                            ],
                                  "children": []
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportAsTextIfTheTagIsNotTheLastThing() {
        assertJsonEquals(
                removePosition(fromMdx("<x />.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {
                                      "type": "mdxJsxTextElement",
                                              "name": "x",
                                          "attributes": [],
                                      "children": []
                                  },
                                  {"type": "text", "value": "."}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportAsTextIfTheTagIsNotTheFirstThing() {
        assertJsonEquals(
                removePosition(fromMdx(".<x />")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "paragraph",
                                          "children": [
                                  {"type": "text", "value": "."},
                                  {"type": "mdxJsxTextElement", "name": "x", "attributes": [], "children": []}
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldCrashWhenMisnestingWithAttentionEmphasis() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a *open <b> close* </b> c.");
        });
        assertEquals("Expected a closing tag for `<b>` (1:9-1:12) before the end of `emphasis`", e.getMessage());
    }

    @Test
    void shouldCrashWhenMisnestingWithAttentionStrong() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a **open <b> close** </b> c.");
        });
        assertEquals("Expected a closing tag for `<b>` (1:10-1:13) before the end of `strong`", e.getMessage());
    }

    @Test
    void shouldCrashWhenMisnestingWithLabelLink() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a [open <b> close](c) </b> d.");
        });
        assertEquals("Expected a closing tag for `<b>` (1:9-1:12) before the end of `link`", e.getMessage());
    }

    @Test
    void shouldCrashWhenMisnestingWithLabelImage() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("a ![open <b> close](c) </b> d.");
        });
        assertEquals("Expected a closing tag for `<b>` (1:10-1:13) before the end of `image`", e.getMessage());
    }

    @Test
    void shouldCrashWhenMisnestingWithAttentionEmphasisClosingTag() {
        var e = assertThrows(Exception.class, () -> {
            fromMdx("<b> a *open </b> close* d.");
        });
        assertEquals("Expected the closing tag `</b>` either after the end of `emphasis` (1:24) or another opening tag after the start of `emphasis` (1:7)", e.getMessage());
    }

    @Test
    void shouldSupportLineEndingsInElements() {
        assertJsonEquals(
                removePosition(fromMdx("> a <b>\n> c </b> d.")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "blockquote",
                                          "children": [
                                  {
                                      "type": "paragraph",
                                              "children": [
                                      {"type": "text", "value": "a "},
                                      {
                                          "type": "mdxJsxTextElement",
                                                  "name": "b",
                                              "attributes": [],
                                          "children": [{"type": "text", "value": "\\nc "}]
                                      },
                                      {"type": "text", "value": " d."}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportLineEndingsInAttributeValues() {
        assertJsonEquals(
                removePosition(fromMdx("> a <b c=\"d\n> e\" /> f")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "blockquote",
                                          "children": [
                                  {
                                      "type": "paragraph",
                                              "children": [
                                      {"type": "text", "value": "a "},
                                      {
                                          "type": "mdxJsxTextElement",
                                                  "name": "b",
                                              "attributes": [
                                          {"type": "mdxJsxAttribute", "name": "c", "value": "d\\ne"}
                                    ],
                                          "children": []
                                      },
                                      {"type": "text", "value": " f"}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportLineEndingsInAttributeValueExpressions() {
        assertJsonEquals(
                removePosition(fromMdx("> a <b c={d\n> e} /> f")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "blockquote",
                                          "children": [
                                  {
                                      "type": "paragraph",
                                              "children": [
                                      {"type": "text", "value": "a "},
                                      {
                                          "type": "mdxJsxTextElement",
                                                  "name": "b",
                                              "attributes": [
                                          {
                                              "type": "mdxJsxAttribute",
                                                      "name": "c",
                                                  "value": {
                                              "type": "mdxJsxAttributeValueExpression",
                                                      "value": "d\\ne"
                                                      }
                                          }
                                    ],
                                          "children": []
                                      },
                                      {"type": "text", "value": " f"}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportLineEndingsInAttributeExpressions() {
        assertJsonEquals(
                removePosition(fromMdx("> a <b {c\n> d} /> e")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "blockquote",
                                          "children": [
                                  {
                                      "type": "paragraph",
                                              "children": [
                                      {"type": "text", "value": "a "},
                                      {
                                          "type": "mdxJsxTextElement",
                                                  "name": "b",
                                              "attributes": [
                                          {"type": "mdxJsxExpressionAttribute", "value": "c\\nd"}
                                    ],
                                          "children": []
                                      },
                                      {"type": "text", "value": " e"}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }


    @Test
    void shouldSupportBlockQuotesInFlow() {
        assertJsonEquals(
                removePosition(fromMdx("<a>\n> b\nc\n> d\n</a>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "a",
                                      "attributes": [],
                                  "children": [
                                  {
                                      "type": "blockquote",
                                              "children": [
                                      {
                                          "type": "paragraph",
                                                  "children": [{"type": "text", "value": "b\\nc\\nd"}]
                                      }
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportListsInFlow() {
        assertJsonEquals(
                removePosition(fromMdx("<a>\n- b\nc\n- d\n</a>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "a",
                                      "attributes": [],
                                  "children": [
                                  {
                                      "type": "list",
                                              "ordered": false,
                                          "start": null,
                                          "spread": false,
                                          "children": [
                                      {
                                          "type": "listItem",
                                                  "spread": false,
                                              "checked": null,
                                              "children": [
                                          {
                                              "type": "paragraph",
                                                      "children": [{"type": "text", "value": "b\\nc"}]
                                          }
                                    ]
                                      },
                                      {
                                          "type": "listItem",
                                                  "spread": false,
                                              "checked": null,
                                              "children": [
                                          {"type": "paragraph", "children": [{"type": "text", "value": "d"}]}
                                    ]
                                      }
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportNormalMarkdownWithoJsx() {
        assertJsonEquals(
                removePosition(fromMdx("> a\n- b\nc\n- d")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "blockquote",
                                          "children": [
                                  {"type": "paragraph", "children": [{"type": "text", "value": "a"}]}
                            ]
                              },
                              {
                                  "type": "list",
                                          "ordered": false,
                                      "start": null,
                                      "spread": false,
                                      "children": [
                                  {
                                      "type": "listItem",
                                              "spread": false,
                                          "checked": null,
                                          "children": [
                                      {"type": "paragraph", "children": [{"type": "text", "value": "b\\nc"}]}
                                ]
                                  },
                                  {
                                      "type": "listItem",
                                              "spread": false,
                                          "checked": null,
                                          "children": [
                                      {"type": "paragraph", "children": [{"type": "text", "value": "d"}]}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    @Test
    void shouldSupportMultipleFlowElementsWithTheirTagsOnTheSameLine() {
        assertJsonEquals(
                removePosition(fromMdx("<x><y>\n\nz\n\n</y></x>")),
                """

                              {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "mdxJsxFlowElement",
                                          "name": "x",
                                      "attributes": [],
                                  "children": [
                                  {
                                      "type": "mdxJsxFlowElement",
                                              "name": "y",
                                          "attributes": [],
                                      "children": [
                                      {"type": "paragraph", "children": [{"type": "text", "value": "z"}]}
                                ]
                                  }
                            ]
                              }
                        ]
                          }

                               """
        );
    }

    private static MdAstRoot fromMdx(String markdown) {
        var options = new MdastOptions();
        options.withExtension(MdxSyntax.EXTENSION);
        options.withMdastExtension(MdxMdastExtension.INSTANCE);
        return MdAst.fromMarkdown(markdown, options);
    }

}
