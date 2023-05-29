package appeng.client.guidebook.scene.annotation;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import appeng.client.guidebook.color.ConstantColor;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class DiamondAnnotationElementCompiler extends AnnotationTagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("DiamondAnnotation");
    }

    @Override
    protected @Nullable SceneAnnotation createAnnotation(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el) {
        var x = MdxAttrs.getFloat(compiler, errorSink, el, "x", 0.0f);
        var y = MdxAttrs.getFloat(compiler, errorSink, el, "y", 0.0f);
        var z = MdxAttrs.getFloat(compiler, errorSink, el, "z", 0.0f);
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", ConstantColor.WHITE);

        var pos = new Vector3f(x, y, z);

        return new DiamondAnnotation(pos, color);
    }
}
