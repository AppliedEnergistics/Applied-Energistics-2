package appeng.client.guidebook.document.flow;

/**
 * Line-Break that also clears floats.
 */
public class LytFlowBreak extends LytFlowContent {
    private boolean clearLeft;
    private boolean clearRight;

    public boolean isClearLeft() {
        return clearLeft;
    }

    public void setClearLeft(boolean clearLeft) {
        this.clearLeft = clearLeft;
    }

    public boolean isClearRight() {
        return clearRight;
    }

    public void setClearRight(boolean clearRight) {
        this.clearRight = clearRight;
    }
}
