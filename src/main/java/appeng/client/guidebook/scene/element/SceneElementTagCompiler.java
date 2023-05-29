package appeng.client.guidebook.scene.element;

import java.util.Set;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.client.guidebook.extensions.Extension;
import appeng.client.guidebook.extensions.ExtensionPoint;
import appeng.client.guidebook.scene.GuidebookScene;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

/**
 * Contributed by {@link SceneElementCompilerPlugin}.
 */
public interface SceneElementTagCompiler extends Extension {
    ExtensionPoint<SceneElementTagCompiler> EXTENSION_POINT = new ExtensionPoint<>(SceneElementTagCompiler.class);

    Set<String> getTagNames();

    void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el);
}
