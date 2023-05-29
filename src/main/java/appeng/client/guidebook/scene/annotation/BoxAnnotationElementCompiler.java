package appeng.client.guidebook.scene.annotation;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import appeng.client.guidebook.color.ConstantColor;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

/**
 * Compiles a <code>&lt;AnnotationBox</code> tag into {@link InWorldBoxAnnotation}.
 */
public class BoxAnnotationElementCompiler extends AnnotationTagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("BoxAnnotation");
    }

    @Override
    protected @Nullable SceneAnnotation createAnnotation(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el) {
        var x1 = MdxAttrs.getFloat(compiler, errorSink, el, "x1", 0.0f);
        var x2 = MdxAttrs.getFloat(compiler, errorSink, el, "x2", 0.0f);
        var y1 = MdxAttrs.getFloat(compiler, errorSink, el, "y1", 0.0f);
        var y2 = MdxAttrs.getFloat(compiler, errorSink, el, "y2", 0.0f);
        var z1 = MdxAttrs.getFloat(compiler, errorSink, el, "z1", 0.0f);
        var z2 = MdxAttrs.getFloat(compiler, errorSink, el, "z2", 0.0f);
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", ConstantColor.WHITE);

        var min = new Vector3f(x1, y1, z1);
        var max = new Vector3f(x2, y2, z2);

        return new InWorldBoxAnnotation(min, max, color);
    }
}
