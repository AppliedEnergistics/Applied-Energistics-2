package appeng.siteexport;

class Scene {
    SceneRenderSettings settings;
    String filename;
    FakeWorld world = new FakeWorld();

    public Scene(SceneRenderSettings settings, String filename) {
        this.settings = settings;
        this.filename = filename;
    }
}
