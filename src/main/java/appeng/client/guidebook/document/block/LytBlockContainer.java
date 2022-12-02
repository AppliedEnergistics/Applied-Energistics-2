package appeng.client.guidebook.document.block;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.unist.UnistNode;

public interface LytBlockContainer extends LytErrorSink {
    void append(LytBlock node);

    @Override
    default void appendError(PageCompiler compiler, String text, UnistNode node) {
        append(compiler.createErrorBlock(text, node));
    }
}
