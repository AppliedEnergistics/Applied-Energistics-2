
package appeng.client.guidebook.scene;

import java.util.HashMap;
import java.util.Map;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.BlockTagCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.scene.element.ImportStructureElementCompiler;
import appeng.client.guidebook.scene.element.IsometricCameraElementCompiler;
import appeng.client.guidebook.scene.element.SceneBlockElementCompiler;
import appeng.client.guidebook.scene.element.SceneElementTagCompiler;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class SceneTagCompiler extends BlockTagCompiler {

    private final Map<String, SceneElementTagCompiler> elementTagCompilers = new HashMap<>();

    public SceneTagCompiler() {
        elementTagCompilers.put("Block", new SceneBlockElementCompiler());
        elementTagCompilers.put("ImportStructure", new ImportStructureElementCompiler());
        elementTagCompilers.put("IsometricCamera", new IsometricCameraElementCompiler());
    }

    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var zoom = MdxAttrs.getFloat(compiler, parent, el, "zoom", 1.0f);

        var level = new GuidebookLevel();
        var cameraSettings = new CameraSettings();
        cameraSettings.setZoom(zoom);

        var scene = new GuidebookScene(level, cameraSettings);

        for (var child : el.children()) {
            if (child instanceof MdxJsxElementFields childEl) {
                var childTagName = childEl.name();
                var childCompiler = elementTagCompilers.get(childTagName);
                if (childCompiler == null) {
                    parent.appendError(compiler, "Unknown scene element", child);
                } else {
                    childCompiler.compile(scene, compiler, parent, childEl);
                }
            }
        }

        var lytScene = new LytGuidebookScene();
        lytScene.setScene(scene);
        parent.append(lytScene);
    }
}
