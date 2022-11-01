package appeng.libs.mdast;

import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.micromark.Micromark;

public final class MdAst {
    private MdAst() {
    }

    public static MdAstRoot fromMarkdown(String markdown, MdastOptions options) {
        var evts = Micromark.parseAndPostprocess(markdown, options);
        return new MdastCompiler(options).compile(evts);
    }
}
