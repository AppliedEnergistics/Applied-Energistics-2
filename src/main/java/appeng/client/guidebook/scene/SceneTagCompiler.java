
package appeng.client.guidebook.scene;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.BlockTagCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.extensions.Extension;
import appeng.client.guidebook.extensions.ExtensionCollection;
import appeng.client.guidebook.scene.element.SceneElementTagCompiler;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class SceneTagCompiler extends BlockTagCompiler implements Extension {
    private final Map<String, SceneElementTagCompiler> elementTagCompilers = new HashMap<>();

    @Override
    public Set<String> getTagNames() {
        return Set.of("GameScene");
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
        if ("true".equals(el.getAttributeString("interactive", "true"))) {
            lytScene.setInteractive(true);
        }
        parent.append(lytScene);
    }

    @Override
    public void onExtensionsBuilt(ExtensionCollection extensions) {
        for (var sceneElementTag : extensions.get(SceneElementTagCompiler.EXTENSION_POINT)) {
            for (var tagName : sceneElementTag.getTagNames()) {
                // This explicitly allows for overrides
                elementTagCompilers.put(tagName, sceneElementTag);
            }
        }
    }
}
