package appeng.client.guidebook.compiler.tags;

import java.util.Set;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.TagCompiler;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.libs.mdast.mdx.model.MdxJsxFlowElement;

public class DivTagCompiler implements TagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("div");
    }

    @Override
    public void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement el) {
        compiler.compileBlockContext(el, parent);
    }
}
