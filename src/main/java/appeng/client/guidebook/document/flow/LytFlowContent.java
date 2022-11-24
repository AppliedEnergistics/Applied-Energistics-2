package appeng.client.guidebook.document.flow;

public class LytFlowContent {
    private LytFlowSpan parentSpan;

    public LytFlowSpan getParentSpan() {
        return parentSpan;
    }

    public void setParentSpan(LytFlowSpan parentSpan) {
        this.parentSpan = parentSpan;
    }

    public boolean isInclusiveAncestor(LytFlowContent flowContent) {
        for (var content = flowContent; content != null; content = content.getParentSpan()) {
            if (content == this) {
                return true;
            }
        }
        return false;
    }
}
