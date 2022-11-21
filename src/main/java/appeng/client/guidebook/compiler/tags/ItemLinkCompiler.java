package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.client.guidebook.document.interaction.ItemTooltip;
import appeng.core.AppEng;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class ItemLinkCompiler extends FlowTagCompiler {
    @Override
    public void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var shortId = el.getAttributeString("id", null);

        ResourceLocation id;
        if (shortId.contains(":")) {
            id = new ResourceLocation(shortId);
        } else {
            id = AppEng.makeId(shortId);
        }

        var item = Registry.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            parent.append(compiler.createErrorFlowContent("Unable to find item " + id, (MdAstNode) el));
            return;
        }

        var stack = item.getDefaultInstance();

        var link = new LytFlowLink();
        compiler.compileComponentToFlow(stack.getHoverName(), link);

        link.setTooltip(new ItemTooltip(stack));
        parent.append(link);
    }

}
