package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.flow.LytFlowBreak;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;

public class BreakCompiler extends FlowTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var br = new LytFlowBreak();
        var clear = el.getAttributeString("clear", "none");
        switch (clear) {
            case "left" -> br.setClearLeft(true);
            case "right" -> br.setClearRight(true);
            case "all" -> {
                br.setClearLeft(true);
                br.setClearRight(true);
            }
            case "none" -> {
            }
            default -> parent.append(compiler.createErrorFlowContent("Invalid 'clear' attribute", (MdAstNode) el));
        }

        parent.append(br);
    }
}
