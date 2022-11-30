package appeng.client.guidebook.document.block;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.model.MdAstNode;

public interface LytBlockContainer extends LytErrorSink {
    void append(LytBlock node);

    @Override
    default void appendError(PageCompiler compiler, String text, MdAstNode node) {
        append(compiler.createErrorBlock(text, node));
    }
}
