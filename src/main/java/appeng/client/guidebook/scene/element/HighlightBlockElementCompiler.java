package appeng.client.guidebook.scene.element;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.client.guidebook.scene.HighlightedBox;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class HighlightBlockElementCompiler implements SceneElementTagCompiler {
    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        var pos = MdxAttrs.getPos(compiler, errorSink, el);
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", -1);

        scene.addHighlight(HighlightedBox.forBlock(pos, color));
    }
}
