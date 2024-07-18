package appeng.client.guidebook.scene.annotation;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.color.ConstantColor;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class BlockAnnotationElementCompiler extends AnnotationTagCompiler {

    public static final String TAG_NAME = "BlockAnnotation";

    @Override
    public Set<String> getTagNames() {
        return Set.of(TAG_NAME);
    }

    @Override
    protected @Nullable SceneAnnotation createAnnotation(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el) {
        var pos = MdxAttrs.getPos(compiler, errorSink, el);
        var color = MdxAttrs.getColor(compiler, errorSink, el, "color", ConstantColor.WHITE);

        return InWorldBoxAnnotation.forBlock(pos, color);
    }
}
