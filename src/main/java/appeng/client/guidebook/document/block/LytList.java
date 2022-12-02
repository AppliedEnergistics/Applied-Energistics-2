package appeng.client.guidebook.document.block;

public class LytList extends LytVBox {
    private final boolean ordered;
    private final int start;

    public LytList(boolean ordered, int start) {
        this.ordered = ordered;
        this.start = start;
    }

    public int getDepth() {
        for (var parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof LytList parentList) {
                return parentList.getDepth() + 1;
            }
        }
        return 1;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public int getStart() {
        return start;
    }
}
