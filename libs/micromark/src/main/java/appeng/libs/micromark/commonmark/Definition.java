package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.NormalizeIdentifier;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactoryDestination;
import appeng.libs.micromark.factory.FactoryLabel;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.factory.FactoryTitle;
import appeng.libs.micromark.factory.FactoryWhitespace;
import appeng.libs.micromark.symbol.Codes;

public final class Definition {
    private Definition() {
    }

    public static final Construct definition;
    public static final Construct titleConstruct;
    static {
        definition = new Construct();
        definition.name = "definition";
        definition.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;

        titleConstruct = new Construct();
        titleConstruct.tokenize = (context, effects, ok, nok) -> new TitleStateMachine(context, effects, ok, nok)::start;
        titleConstruct.partial = true;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        private String identifier;
        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        
        private State start(int code) {
            Assert.check(code == Codes.leftSquareBracket, "expected `[`");
            effects.enter(Types.definition);
            return FactoryLabel.create(
                    context,
                    effects,
                    this::labelAfter,
                    nok,
                    Types.definitionLabel,
                    Types.definitionLabelMarker,
                    Types.definitionLabelString
            ).step(code);
        }

        private State labelAfter(int code) {
            var lastTokenText = context.sliceSerialize(context.getLastEvent().token());
            identifier = NormalizeIdentifier.normalizeIdentifier(
                    lastTokenText.substring(1, lastTokenText.length() - 1)
            );

            if (code == Codes.colon) {
                effects.enter(Types.definitionMarker);
                effects.consume(code);
                effects.exit(Types.definitionMarker);

                // Note: blank lines can’t exist in content.
                return FactoryWhitespace.create(
                        effects,
                        FactoryDestination.create(
                                effects,
                                effects.attempt.hook(
                                        titleConstruct,
                                        FactorySpace.create(effects, this::after, Types.whitespace),
                                        FactorySpace.create(effects, this::after, Types.whitespace)
                                ),
                                nok,
                                Types.definitionDestination,
                                Types.definitionDestinationLiteral,
                                Types.definitionDestinationLiteralMarker,
                                Types.definitionDestinationRaw,
                                Types.definitionDestinationString
                        )
                );
            }

            return nok.step(code);
        }

        
        private State after(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.definition);

                if (!context.parser.defined.contains(identifier)) {
                    context.parser.defined.add(identifier);
                }

                return ok.step(code);
            }

            return nok.step(code);
        }

    }

    private static class TitleStateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public TitleStateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        
        private State start(int code) {
            return CharUtil.markdownLineEndingOrSpace(code)
                    ? FactoryWhitespace.create(effects, this::before).step(code)
      : nok.step(code);
        }

        
        private State before(int code) {
            if (
                    code == Codes.quotationMark ||
                            code == Codes.apostrophe ||
                            code == Codes.leftParenthesis
            ) {
                return FactoryTitle.create(
                        effects,
                        FactorySpace.create(effects, this::after, Types.whitespace),
                        nok,
                        Types.definitionTitle,
                        Types.definitionTitleMarker,
                        Types.definitionTitleString
                ).step(code);
            }

            return nok.step(code);
        }

        
        private State after(int code) {
            return code == Codes.eof || CharUtil.markdownLineEnding(code) ? ok.step(code) : nok.step(code);
        }
    }
}
