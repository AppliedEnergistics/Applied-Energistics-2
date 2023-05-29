package appeng.client.guidebook.compiler;

import java.util.Set;

import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.client.guidebook.extensions.Extension;
import appeng.client.guidebook.extensions.ExtensionPoint;
import appeng.libs.mdast.mdx.model.MdxJsxFlowElement;
import appeng.libs.mdast.mdx.model.MdxJsxTextElement;

/**
 * Tag compilers handle HTML-like tags found in Markdown content, such as <code>&lt;Image /&gt;</code> and similar.
 */
public interface TagCompiler extends Extension {
    ExtensionPoint<TagCompiler> EXTENSION_POINT = new ExtensionPoint<>(TagCompiler.class);

    /**
     * The tag names this compiler is responsible for.
     */
    Set<String> getTagNames();

    default void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement el) {
        parent.append(compiler.createErrorBlock("Cannot use MDX tag " + el.name + " in block context", el));
    }

    default void compileFlowContext(PageCompiler compiler, LytFlowParent parent, MdxJsxTextElement el) {
        parent.append(compiler.createErrorFlowContent("Cannot use MDX tag " + el.name() + " in flow context", el));
    }
}
