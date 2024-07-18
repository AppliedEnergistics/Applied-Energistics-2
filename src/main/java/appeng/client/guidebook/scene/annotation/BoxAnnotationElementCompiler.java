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

    public static final String TAG_NAME = "BoxAnnotation";

    @Override
    public Set<String> getTagNames() {
        return Set.of(TAG_NAME);
    }

    @Override
    protected @Nullable SceneAnnotation createAnnotation(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el) {

        var min = MdxAttrs.getVector3(compiler, errorSink, el, "min", new Vector3f());
        var max = MdxAttrs.getVector3(compiler, errorSink, el, "max", new Vector3f());
        ensureMinMax(min, max);

        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", ConstantColor.WHITE);
        var thickness = MdxAttrs.getFloat(compiler, errorSink, el, "thickness", InWorldBoxAnnotation.DEFAULT_THICKNESS);
        var alwaysOnTop = MdxAttrs.getBoolean(compiler, errorSink, el, "alwaysOnTop", false);

        var annotation = new InWorldBoxAnnotation(min, max, color, thickness);
        annotation.setAlwaysOnTop(alwaysOnTop);
        return annotation;
    }

    // Ensures component-wise that min has the minimum and max has the maximum values
    private void ensureMinMax(Vector3f min, Vector3f max) {
        for (var i = 0; i < 3; i++) {
            var minVal = min.get(i);
            var maxVal = max.get(i);
            if (minVal > maxVal) {
                min.setComponent(i, maxVal);
                max.setComponent(i, minVal);
            }
        }
    }
}
