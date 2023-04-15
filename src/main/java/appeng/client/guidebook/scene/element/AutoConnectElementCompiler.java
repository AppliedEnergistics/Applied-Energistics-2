package appeng.client.guidebook.scene.element;

import appeng.api.parts.IPartHost;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.parts.networking.CablePart;

public class AutoConnectElementCompiler implements SceneElementTagCompiler {
    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        var level = scene.getLevel();

        var bes = level.getBlockEntities();

        for (var be : bes) {
            if (be instanceof IPartHost partHost && partHost.getPart(null) instanceof CablePart cablePart) {
                cablePart.autoConnectClientSide();
            }
        }
    }
}
