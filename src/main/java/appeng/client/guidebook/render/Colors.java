package appeng.client.guidebook.render;

final class Colors {
    public static int argb(int a, int r, int g, int b) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8  | b & 0xFF;
    }
}
