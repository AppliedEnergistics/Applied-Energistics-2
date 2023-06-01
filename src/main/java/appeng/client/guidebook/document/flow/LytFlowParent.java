package appeng.client.guidebook.document.flow;

import java.util.Optional;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.unist.UnistNode;

public interface LytFlowParent extends LytErrorSink {
    void append(LytFlowContent child);

    default LytFlowText appendText(String text) {
        var node = new LytFlowText();
        node.setText(text);
        append(node);
        return node;
    }

    /**
     * Converts formatted Minecraft text into our flow content.
     */
    default void appendComponent(FormattedText formattedText) {
        formattedText.visit((style, text) -> {
            if (style.isEmpty()) {
                appendText(text);
            } else {
                var span = new LytFlowSpan();
                // TODO: Convert style
                span.appendText(text);
                append(span);
            }
            return Optional.empty();
        }, Style.EMPTY);
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
