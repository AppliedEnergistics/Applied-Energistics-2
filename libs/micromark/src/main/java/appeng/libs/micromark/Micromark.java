package appeng.libs.micromark;

import java.util.ArrayList;
import java.util.List;

public final class Micromark {

    public ParseContext parse(List<Extension> extensions) {

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

    ParseContext.Create create(ParseContext parser, InitialConstruct initial) {
        return from -> Tokenizer.create(parser, initial, from);
    }
}
