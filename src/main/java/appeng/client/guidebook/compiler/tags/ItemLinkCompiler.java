package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.client.guidebook.document.interaction.ItemTooltip;
import appeng.client.guidebook.indices.ItemIndex;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class ItemLinkCompiler extends FlowTagCompiler {
    @Override
    public void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var shortId = el.getAttributeString("id", null);

        ResourceLocation id;
        try {
            id = compiler.resolveId(shortId);
        } catch (Exception e) {
            parent.append(compiler.createErrorFlowContent("Invalid item id " + shortId, (MdAstNode) el));
            return;
        }

        var item = Registry.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            parent.append(compiler.createErrorFlowContent("Unable to find item " + id, (MdAstNode) el));
            return;
        }

        var linksTo = ItemIndex.INSTANCE.get(id);

        var stack = item.getDefaultInstance();

        var link = new LytFlowLink();
        compiler.compileComponentToFlow(stack.getHoverName(), link);

        link.setTooltip(new ItemTooltip(stack));
        link.setClickCallback(screen -> {
            screen.navigateTo(linksTo);
        });
        parent.append(link);
    }

}
