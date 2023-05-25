package appeng.client.guidebook.scene.element;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.client.guidebook.scene.HighlightedBox;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import org.joml.Vector3f;

public class HighlightBoxElementCompiler implements SceneElementTagCompiler {
    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {

        var x1 = MdxAttrs.getFloat(compiler, errorSink, el, "x1", 0.0f);
        var x2 = MdxAttrs.getFloat(compiler, errorSink, el, "x2", 0.0f);
        var y1 = MdxAttrs.getFloat(compiler, errorSink, el, "y1", 0.0f);
        var y2 = MdxAttrs.getFloat(compiler, errorSink, el, "y2", 0.0f);
        var z1 = MdxAttrs.getFloat(compiler, errorSink, el, "z1", 0.0f);
        var z2 = MdxAttrs.getFloat(compiler, errorSink, el, "z2", 0.0f);
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", -1);

        var min = new Vector3f(x1, y1, z1);
        var max = new Vector3f(x2, y2, z2);

        scene.addHighlight(new HighlightedBox(min, max, color));
    }
}
