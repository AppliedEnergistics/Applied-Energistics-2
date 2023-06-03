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
public class LineAnnotationElementCompiler extends AnnotationTagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("LineAnnotation");
    }

    @Override
    protected @Nullable SceneAnnotation createAnnotation(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el) {
        var x1 = MdxAttrs.getFloat(compiler, errorSink, el, "x1", 0.0f);
        var x2 = MdxAttrs.getFloat(compiler, errorSink, el, "x2", 0.0f);
        if (x2 < x1) {
            var tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        var y1 = MdxAttrs.getFloat(compiler, errorSink, el, "y1", 0.0f);
        var y2 = MdxAttrs.getFloat(compiler, errorSink, el, "y2", 0.0f);
        if (y2 < y1) {
            var tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        var z1 = MdxAttrs.getFloat(compiler, errorSink, el, "z1", 0.0f);
        var z2 = MdxAttrs.getFloat(compiler, errorSink, el, "z2", 0.0f);
        if (z2 < z1) {
            var tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", ConstantColor.WHITE);

        var thickness = MdxAttrs.getFloat(compiler, errorSink, el, "thickness",
                InWorldLineAnnotation.DEFAULT_THICKNESS);

        var alwaysOnTop = MdxAttrs.getBoolean(compiler, errorSink, el, "alwaysOnTop", false);

        var min = new Vector3f(x1, y1, z1);
        var max = new Vector3f(x2, y2, z2);

        var annotation = new InWorldLineAnnotation(min, max, color, thickness);
        annotation.setAlwaysOnTop(alwaysOnTop);
        return annotation;
    }
}
