package appeng.client.guidebook.compiler;

import appeng.client.guidebook.document.LytBlockContainer;
import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.libs.mdast.mdx.model.MdxJsxFlowElement;
import appeng.libs.mdast.mdx.model.MdxJsxTextElement;

public interface TagCompiler {
    default void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement el) {
        parent.append(compiler.createErrorBlock("Cannot use MDX tag " + el.name + " in block context", el));
    }

    default void compileFlowContext(PageCompiler compiler, LytFlowContainer parent, MdxJsxTextElement el) {
        parent.append(compiler.createErrorFlowContent("Cannot use MDX tag " + el.name() + " in flow context", el));
    }
}
