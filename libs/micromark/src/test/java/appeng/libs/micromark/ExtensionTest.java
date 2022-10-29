package appeng.libs.micromark;

import appeng.libs.micromark.Construct;
import appeng.libs.micromark.ConstructPrecedence;
import appeng.libs.micromark.ContentType;
import appeng.libs.micromark.Extension;
import appeng.libs.micromark.Micromark;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.html.CompileContext;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.HtmlCompiler;
import appeng.libs.micromark.html.HtmlExtension;
import appeng.libs.micromark.html.ParseOptions;
import appeng.libs.micromark.symbol.Codes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtensionTest {

    private static String micromark(String markdown) {
        return micromark(markdown, new CompileOptions(), new ParseOptions());
    }

    private static String micromark(String markdown, CompileOptions compileOptions) {
        return micromark(markdown, compileOptions, new ParseOptions());
    }

    private static String micromark(String markdown, ParseOptions parseOptions) {
        return micromark(markdown, new CompileOptions(), parseOptions);
    }

    private static String micromark(String markdown, CompileOptions compileOptions, ParseOptions parseOptions) {
        return new HtmlCompiler(compileOptions).compile(Micromark.parseAndPostprocess(markdown, parseOptions));
    }

    @Test
    void syntaxExtension() {
        Extension syntax = new Extension();
        // A proper construct (slash, not used).
        syntax.flow.put(47, List.of(createFunkyThematicBreak(47)));
        // A proper construct (less than, used for html).
        syntax.flow.put(60, List.of(createFunkyThematicBreak(60)));

        assertEquals(micromark("///"), "<p>///</p>", "baseline (slash)");
        assertEquals(micromark("<<<"), "<p>&lt;&lt;&lt;</p>", "baseline (less than)");

        assertEquals(
                micromark("///", new ParseOptions().withExtension(syntax)),
                "<hr />",
                "should support syntax extensions (slash)"
        );

        assertEquals(
                micromark("<<<", new ParseOptions().withExtension(syntax)),
                "<hr />",
                "should support syntax extensions for an existing hook (less than)"
        );

        assertEquals(micromark("///"), "<p>///</p>", "should not taint (slash)");

        assertEquals(
                micromark("<<<"),
                "<p>&lt;&lt;&lt;</p>",
                "should not taint (less than)"
        );

        var tokenizeLessThanExtension = new Extension();
        var tokenizeLessThanConstruct = new Construct();
        tokenizeLessThanConstruct.tokenize = this::tokenizeJustALessThan;
        tokenizeLessThanExtension.text.put(60, List.of(tokenizeLessThanConstruct));

        assertEquals(
                micromark("a <i> b, 1 < 3",
                        new CompileOptions().allowDangerousHtml(),
                        new ParseOptions().withExtension(tokenizeLessThanExtension)
                ),
                "<p>a i&gt; b, 1  3</p>",
                "should precede over previously attached constructs by default"
        );

        var tokenizeLessThanExtensionAfter = new Extension();
        var tokenizeLessThanConstructAfter = new Construct();
        tokenizeLessThanConstructAfter.tokenize = this::tokenizeJustALessThan;
        tokenizeLessThanConstructAfter.add = ConstructPrecedence.AFTER;
        tokenizeLessThanExtensionAfter.text.put(60, List.of(tokenizeLessThanConstructAfter));
        assertEquals(
                micromark("a <i> b, 1 < 3",
                        new CompileOptions().allowDangerousHtml(),
                        new ParseOptions().withExtension(tokenizeLessThanExtensionAfter)),
                "<p>a <i> b, 1  3</p>",
                "should go after previously attached constructs w/ `add: after`"
        );
    }

    @Test
    public void testHtmlExtension() {
        var syntax = new Extension();
        var commentConstruct = new Construct();
        commentConstruct.tokenize = this::tokenizeCommentLine;
        syntax.flow.put(47, List.of(commentConstruct));

        var html = HtmlExtension.builder()
                .enter("commentLine", this::enterComment)
                .exit("commentLine", this::exitComment)
                .build();

        assertEquals(micromark("// a\n//\rb"), "<p>// a\n//\rb</p>", "baseline");

        assertEquals(
                micromark("// a\n//\rb",
                        new CompileOptions().withExtension(html),
                        new ParseOptions().withExtension(syntax)
                ),
                "<p>b</p>",
                "should support html extensions"
        );

        assertEquals(
                micromark("// a\n//\rb"),
                "<p>// a\n//\rb</p>",
                "should not taint"
        );

        var htmlDocExtension = HtmlExtension.builder()
                .enterDocument(this::enterDocument)
                .exitDocument(this::exitDocument)
                .build();

        assertEquals(
                micromark("!",
                        new CompileOptions().withExtension(htmlDocExtension)
                ),
                "+\n<p>!</p>-",
                "should support html extensions for documents"
        );

        assertEquals(
                micromark("", new CompileOptions().withExtension(htmlDocExtension)),
                "+-",
                "should support html extensions for empty documents"
        );
    }

    private Construct createFunkyThematicBreak(int marker) {
        return new Construct() {
            {
                tokenize = this::tokenizeFunkyThematicBreak;
            }

            private State tokenizeFunkyThematicBreak(TokenizeContext tokenizeContext, Tokenizer.Effects effects, State ok, State nok) {
                class StateMachine {
                    private int size = 0;

                    public State start(int code) {
                        if (code != marker) {
                            return nok.step(code);
                        }

                        effects.enter("thematicBreak");
                        return atBreak(code);
                    }

                    private State atBreak(int code) {
                        // Plus.
                        if (code == marker) {
                            effects.enter("thematicBreakSequence");
                            return sequence(code);
                        }

                        // Whitespace.
                        if (code == -2 || code == -1 || code == 32) {
                            effects.enter("whitespace");
                            return whitespace(code);
                        }

                        // Eol or eof.
                        if (
                                size >= 3 && (code == Codes.eof || code == -5 || code == -4 || code == -3)
                        ) {
                            effects.exit("thematicBreak");
                            return ok.step(code);
                        }

                        return nok.step(code);
                    }

                    private State sequence(int code) {
                        if (code == marker) {
                            effects.consume(code);
                            size++;
                            return this::sequence;
                        }

                        effects.exit("thematicBreakSequence");
                        return atBreak(code);
                    }

                    private State whitespace(int code) {
                        if (code == -2 || code == -1 || code == 32) {
                            effects.consume(code);
                            return this::whitespace;
                        }

                        effects.exit("whitespace");
                        return atBreak(code);
                    }
                }

                return new StateMachine()::start;
            }
        };
    }

    private State tokenizeCommentLine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        class StateMachine {
            private State start(int code) {
                if (code != 47) {
                    return nok.step(code);
                }

                effects.enter("commentLine");
                effects.enter("commentLineSequence");
                effects.consume(code);
                return this::insideSlashes;
            }

            private State insideSlashes(int code) {
                if (code == 47) {
                    effects.consume(code);
                    effects.exit("commentLineSequence");
                    return this::afterSlashes;
                }

                return nok.step(code);
            }

            private State afterSlashes(int code) {
                // Eol or eof.
                if (code == Codes.eof || code == -5 || code == -4 || code == -3) {
                    effects.exit("commentLine");
                    return ok.step(code);
                }

                // Anything else: allow character references and escapes.
                var tokenFields = new Token();
                tokenFields.contentType = ContentType.STRING;
                effects.enter("chunkString", tokenFields);
                return insideValue(code);
            }

            private State insideValue(int code) {
                // Eol or eof.
                if (code == Codes.eof || code == -5 || code == -4 || code == -3) {
                    effects.exit("chunkString");
                    effects.exit("commentLine");
                    return ok.step(code);
                }

                // Anything else.
                effects.consume(code);
                return this::insideValue;
            }
        }

        return new StateMachine()::start;
    }

    private State tokenizeJustALessThan(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        return code -> {
            if (code != 60) {
                return nok.step(code);
            }

            effects.enter("lessThan");
            effects.consume(code);
            effects.exit("lessThan");
            return ok;
        };
    }

    private void enterComment(CompileContext context, Token token) {
        context.buffer();
    }

    private void exitComment(CompileContext context, Token token) {
        context.resume();
        context.setData("slurpOneLineEnding", true);
    }

    private void enterDocument(CompileContext context) {
        context.raw("+");
    }

    private void exitDocument(CompileContext context) {
        context.raw("-");
    }

}
