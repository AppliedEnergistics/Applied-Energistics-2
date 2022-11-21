package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.TagCompiler;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.libs.mdast.mdx.model.MdxJsxTextElement;

public class ATagCompiler implements TagCompiler {
    @Override
    public void compileFlowContext(PageCompiler compiler, LytFlowParent parent, MdxJsxTextElement el) {
        var link = new LytFlowLink();
        // TODO: HREF, TITLE
        compiler.compileFlowContext(el, link);
        parent.append(link);
    }
}
