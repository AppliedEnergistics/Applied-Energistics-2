package appeng.client.guidebook.scene.element;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.BlockHighlight;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class HighlightBlockElementCompiler implements SceneElementTagCompiler {
    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        var pos = MdxAttrs.getPos(compiler, errorSink, el);
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color");

        scene.addHighlight(new BlockHighlight(pos, color.x(), color.y(), color.z(), color.w()));
    }
}
