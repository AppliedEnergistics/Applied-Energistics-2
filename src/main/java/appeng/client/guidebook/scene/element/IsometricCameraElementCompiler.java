package appeng.client.guidebook.scene.element;

import java.util.Set;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class IsometricCameraElementCompiler implements SceneElementTagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("IsometricCamera");
    }

    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        float yaw = MdxAttrs.getFloat(compiler, errorSink, el, "yaw", 0.0f);
        float pitch = MdxAttrs.getFloat(compiler, errorSink, el, "pitch", 0.0f);
        float roll = MdxAttrs.getFloat(compiler, errorSink, el, "roll", 0.0f);

        var cameraSettings = scene.getCameraSettings();
        cameraSettings.setIsometricYawPitchRoll(yaw, pitch, roll);
    }
}
