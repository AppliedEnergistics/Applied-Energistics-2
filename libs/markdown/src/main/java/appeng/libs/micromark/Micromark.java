package appeng.libs.micromark;

import appeng.libs.micromark.commonmark.Subtokenize;
import appeng.libs.micromark.html.ParseOptions;

import java.util.ArrayList;
import java.util.List;

public final class Micromark {
    private Micromark() {
    }

    public static ParseContext parse(List<Extension> extensions) {

        var actualExtensions = new ArrayList<Extension>();
        actualExtensions.add(DefaultExtension.create());
        actualExtensions.addAll(extensions);

        var constructs = Extensions.combineExtensions(actualExtensions);

        var parser = new ParseContext();
        parser.constructs = constructs;
        parser.content = create(parser, InitializeContent.content);
        parser.document = create(parser, InitializeDocument.document);
        parser.flow = create(parser, InitializeFlow.flow);
        parser.string = create(parser, InitializeText.string);
        parser.text = create(parser, InitializeText.text);
        return parser;
    }

    public static ParseContext.Create create(ParseContext parser, InitialConstruct initial) {
        return from -> Tokenizer.create(parser, initial, from);
    }

    public static List<Tokenizer.Event> parse(String text) {
        return parse(text, new ParseOptions());
    }

    public static List<Tokenizer.Event> parse(String text, ParseOptions options) {
        return parse(options.getExtensions()).document.create().write(Preprocessor.preprocess(text, true));
    }

    public static List<Tokenizer.Event> parseAndPostprocess(String text) {
        return parseAndPostprocess(text, new ParseOptions());
    }

    public static List<Tokenizer.Event> parseAndPostprocess(String text, ParseOptions options) {
        var events = parse(text, options);
        while (!Subtokenize.subtokenize(events)) {
            // Empty
        }
        return events;
    }
}
