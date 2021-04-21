package appeng.client;

import net.minecraft.client.renderer.Rectangle2d;

public final class Point {

    public static final Point ZERO = new Point(0, 0);

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point fromTopLeft(Rectangle2d bounds) {
        return new Point(bounds.getX(), bounds.getY());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point move(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public boolean isIn(Rectangle2d rect) {
        return x >= rect.getX()
                && y >= rect.getY()
                && x < rect.getX() + rect.getWidth()
                && y < rect.getY() + rect.getHeight();
    }

}
