package appeng.client.gui.assets;

public record SpritePadding(int left, int top, int right, int bottom) {
    public int height() {
        return top + bottom;
    }

    public int width() {
        return left + right;
    }

    public SpritePadding expand(int padding) {
        return new SpritePadding(left + padding, top + padding, right + padding, bottom + padding);
    }

    public SpritePadding expand(int left, int top, int right, int bottom) {
        return new SpritePadding(this.left + left, this.top + top, this.right + right, this.bottom + bottom);
    }
}
