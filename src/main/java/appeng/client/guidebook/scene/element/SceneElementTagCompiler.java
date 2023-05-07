package appeng.client.guidebook.scene.element;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public interface SceneElementTagCompiler {
    void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el);
}
