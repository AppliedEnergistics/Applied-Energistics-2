package appeng.libs.micromark.extensions;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.Extension;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

import java.util.List;

/**
 * Add support for parsing YAML frontmatter in markdown.
 */
public class YamlFrontmatterSyntax {

    /**
     * YAML Frontmatter Fence.
     */
    private static final String FENCE = "---";

    /**
     * Token type for the entire front-matter section.
     */
    public static final String TYPE = "yaml";
    /**
     * Token type for a value in the front-matter section.
     */
    public static final String VALUE_TYPE = "yamlValue";
    private static final String fenceType = "yamlFence";
    private static final String sequenceType = "yamlFenceSequence";

    public static final Extension INSTANCE;

    private static final Construct fenceConstruct;

    static {
        fenceConstruct = new Construct();
        fenceConstruct.tokenize = YamlFrontmatterSyntax::tokenizeFence;
        fenceConstruct.partial = true;

        var construct = new Construct();
        construct.concrete = true;
        construct.tokenize = YamlFrontmatterSyntax::tokenizeFrontmatter;

        INSTANCE = new Extension();
        INSTANCE.flow.put((int) FENCE.charAt(0), List.of(construct));
    }

    private static State tokenizeFrontmatter(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        class StateMachine {
            State start(int code) {
                var position = context.now();

                if (position.column() != 1 || position.line() != 1) {
                    return nok.step(code);
                }

                effects.enter(TYPE);
                return effects.attempt.hook(fenceConstruct, this::afterOpeningFence, nok).step(code);
            }

            State afterOpeningFence(int code) {
                return lineEnd(code);
            }

            State lineStart(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    return lineEnd(code);
                }

                effects.enter(VALUE_TYPE);
                return lineData(code);
            }

            State lineData(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    effects.exit(VALUE_TYPE);
                    return lineEnd(code);
                }

                effects.consume(code);
                return this::lineData;
            }

            State lineEnd(int code) {
                // Require a closing fence.
                if (code == Codes.eof) {
                    return nok.step(code);
                }

                // Can only be an eol.
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return effects.attempt.hook(fenceConstruct, this::after, this::lineStart);
            }

            State after(int code) {
                effects.exit(TYPE);
                return ok.step(code);
            }
        }

        return new StateMachine()::start;
    }

    private static State tokenizeFence(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        class StateMachine {
            int bufferIndex = 0;

            State start(int code) {
                if (code == FENCE.charAt(bufferIndex)) {
                    effects.enter(fenceType);
                    effects.enter(sequenceType);
                    return insideSequence(code);
                }

                return nok.step(code);
            }

            State insideSequence(int code) {
                if (bufferIndex == FENCE.length()) {
                    effects.exit(sequenceType);

                    if (CharUtil.markdownSpace(code)) {
                        effects.enter(Types.whitespace);
                        return insideWhitespace(code);
                    }

                    return fenceEnd(code);
                }

                if (code == FENCE.charAt(bufferIndex++)) {
                    effects.consume(code);
                    return this::insideSequence;
                }

                return nok.step(code);
            }

            State insideWhitespace(int code) {
                if (CharUtil.markdownSpace(code)) {
                    effects.consume(code);
                    return this::insideWhitespace;
                }

                effects.exit(Types.whitespace);
                return fenceEnd(code);
            }

            State fenceEnd(int code) {
                if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                    effects.exit(fenceType);
                    return ok.step(code);
                }

                return nok.step(code);
            }
        }
        return new StateMachine()::start;
    }
}
