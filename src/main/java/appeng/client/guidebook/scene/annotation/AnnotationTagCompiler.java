package appeng.client.guidebook.scene.annotation;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.block.LytVBox;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.client.guidebook.scene.element.SceneElementTagCompiler;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public abstract class AnnotationTagCompiler implements SceneElementTagCompiler {

    @Override
    public final void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el) {
        var annotation = createAnnotation(compiler, errorSink, el);
        if (annotation == null) {
            return; // Likely parsing error
        }

        var contentBox = new LytVBox();
        compiler.compileBlockContext(el.children(), contentBox);
        if (!contentBox.getChildren().isEmpty()) {
            // Clear any top and bottom margin around the entire content
            var firstChild = contentBox.getChildren().get(0);
            if (firstChild instanceof LytBlock block) {
                block.setMarginTop(0);
            }
            var lastChild = contentBox.getChildren().get(contentBox.getChildren().size() - 1);
            if (lastChild instanceof LytBlock block) {
                block.setMarginBottom(0);
            }

            annotation.setTooltipContent(contentBox);
        }

        scene.addAnnotation(annotation);
    }

    @Nullable
    protected abstract SceneAnnotation createAnnotation(PageCompiler compiler,
            LytErrorSink errorSink,
            MdxJsxElementFields el);
}
