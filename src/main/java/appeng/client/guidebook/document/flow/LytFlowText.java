package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.document.block.LytVisitor;

public class LytFlowText extends LytFlowContent {
    private String text = "";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static LytFlowText of(String text) {
        var node = new LytFlowText();
        node.setText(text);
        return node;
    }

    @Override
    protected void visitChildren(LytVisitor visitor) {
        visitor.text(text);
    }
}
