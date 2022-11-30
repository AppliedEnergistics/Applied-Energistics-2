package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.TagCompiler;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.flow.LytFlowInlineBlock;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.mdx.model.MdxJsxFlowElement;
import appeng.libs.mdast.mdx.model.MdxJsxTextElement;

/**
 * Compiler base-class for tag compilers that compile block content but allow the block content to be used in flow
 * context by wrapping it in an inline block.
 */
public abstract class BlockTagCompiler implements TagCompiler {
    protected abstract void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el);

    @Override
    public final void compileFlowContext(PageCompiler compiler, LytFlowParent parent, MdxJsxTextElement el) {
        compile(compiler, node -> {
            var inlineBlock = new LytFlowInlineBlock();
            inlineBlock.setBlock(node);
            parent.append(inlineBlock);
        }, el);
    }

    @Override
    public final void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement el) {
        compile(compiler, parent, el);
    }
}
