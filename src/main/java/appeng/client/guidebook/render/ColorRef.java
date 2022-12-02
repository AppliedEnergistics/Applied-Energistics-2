package appeng.client.guidebook.render;

public class ColorRef {
    public static final ColorRef WHITE = new ColorRef(-1);

    final SymbolicColor symbolic;
    final int concrete;

    ColorRef(SymbolicColor color) {
        this.symbolic = color;
        this.concrete = 0;
    }

    public ColorRef(int concrete) {
        this.concrete = concrete;
        this.symbolic = null;
    }
}
