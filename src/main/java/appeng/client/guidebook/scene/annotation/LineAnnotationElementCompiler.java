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

        var from = MdxAttrs.getVector3(compiler, errorSink, el, "from", new Vector3f());
        var to = MdxAttrs.getVector3(compiler, errorSink, el, "to", new Vector3f());
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", ConstantColor.WHITE);

        var thickness = MdxAttrs.getFloat(compiler, errorSink, el, "thickness",
                InWorldLineAnnotation.DEFAULT_THICKNESS);

        var alwaysOnTop = MdxAttrs.getBoolean(compiler, errorSink, el, "alwaysOnTop", false);

        var annotation = new InWorldLineAnnotation(from, to, color, thickness);
        annotation.setAlwaysOnTop(alwaysOnTop);
        return annotation;
    }
}
