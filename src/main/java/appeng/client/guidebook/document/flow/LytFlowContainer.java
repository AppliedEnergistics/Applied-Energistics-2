package appeng.client.guidebook.document.flow;

public interface LytFlowContainer {
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
}
