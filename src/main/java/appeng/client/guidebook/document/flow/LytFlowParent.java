package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.unist.UnistNode;

public interface LytFlowParent extends LytErrorSink {
    void append(LytFlowContent child);

    default LytFlowText appendText(String text) {
        var node = new LytFlowText();
        node.setText(text);
        append(node);
        return node;
    }

    default void appendBreak() {
        var br = new LytFlowBreak();
        append(br);
    }

    @Override
    default void appendError(PageCompiler compiler, String text, UnistNode node) {
        append(compiler.createErrorFlowContent(text, node));
    }
}
