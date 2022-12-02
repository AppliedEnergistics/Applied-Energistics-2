package appeng.client.guidebook.document;

public record LytSize(int width, int height) {

    public static LytSize empty() {
        return new LytSize(0, 0);
    }

}
