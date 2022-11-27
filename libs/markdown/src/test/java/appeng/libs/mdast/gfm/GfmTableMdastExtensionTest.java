package appeng.libs.mdast.gfm;

import appeng.libs.mdast.AbstractMdAstTest;
import appeng.libs.mdast.MdAst;
import appeng.libs.mdast.MdastOptions;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.micromark.extensions.gfm.GfmTableSyntax;
import org.junit.jupiter.api.Test;

class GfmTableMdastExtensionTest extends AbstractMdAstTest {

    private MdAstNode parse(String markdown) {
        return removePosition(parseWithPosition(markdown));
    }

    private MdAstNode parseWithPosition(String markdown) {
        var options = new MdastOptions();
        options.withMdastExtension(GfmTableMdastExtension.INSTANCE);
        options.withSyntaxExtension(GfmTableSyntax.INSTANCE);
        return MdAst.fromMarkdown(markdown, options);
    }

    @Test
    void shouldSupportTables() {
        assertJsonEquals(
                parseWithPosition("| a\n| -"),
        """
                      {
                      "type": "root",
                              "children": [
                      {
                          "type": "table",
                                  "align": [null],
                          "children": [
                          {
                              "type": "tableRow",
                                      "children": [
                              {
                                  "type": "tableCell",
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
                                      "end": {"line": 1, "column": 4, "offset": 3}
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
                              "end": {"line": 2, "column": 4, "offset": 7}
                          }
                      }
                ],
                      "position": {
                          "start": {"line": 1, "column": 1, "offset": 0},
                          "end": {"line": 2, "column": 4, "offset": 7}
                      }
                  }
                  """
        );
    }

    @Test
    void shouldSupportAlignment() {
        assertJsonEquals(
                parse("| a | b | c | d |\n| - | :- | -: | :-: |"),
                """
                            {
                                "type": "root",
                                        "children": [
                                {
                                    "type": "table",
                                            "align": [null, "left", "right", "center"],
                                    "children": [
                                    {
                                        "type": "tableRow",
                                                "children": [
                                        {"type": "tableCell", "children": [{"type": "text", "value": "a"}]},
                                        {"type": "tableCell", "children": [{"type": "text", "value": "b"}]},
                                        {"type": "tableCell", "children": [{"type": "text", "value": "c"}]},
                                        {"type": "tableCell", "children": [{"type": "text", "value": "d"}]}
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
    void shouldSupportAnEscapedPipeInCodeInATableCell() {
        assertJsonEquals(
                parse("| `\\|` |\n | --- |"),
                """
                          {
                              "type": "root",
                                      "children": [
                              {
                                  "type": "table",
                                          "align": [null],
                                  "children": [
                                  {
                                      "type": "tableRow",
                                              "children": [
                                      {
                                          "type": "tableCell",
                                                  "children": [{"type": "inlineCode", "value": "|"}]
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
    void shouldNotSupportAnEscapedPipeInCodeNotInATableCell() {
        assertJsonEquals(
                parse("`\\|`"),
                """
                                    {
                                        "type": "root",
                                                "children": [
                                        {"type": "paragraph", "children": [{"type": "inlineCode", "value": "\\\\|"}]}
                                  ]
                                    }
                        """
        );
    }

    @Test
    void shouldNotSupportAnEscapedEscapeInCodeInATableCell() {
        assertJsonEquals(
                parse("| `\\\\|`\\\\` b |\n | --- | --- |"),
                """
                                {
                                    "type": "root",
                                            "children": [
                                    {
                                        "type": "table",
                                                "align": [null, null],
                                        "children": [
                                        {
                                            "type": "tableRow",
                                                    "children": [
                                            {"type": "tableCell", "children": [{"type": "text", "value": "`\\\\"}]},
                                            {
                                                "type": "tableCell",
                                                        "children": [
                                                {"type": "inlineCode", "value": "\\\\"},
                                                {"type": "text", "value": " b"}
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

}