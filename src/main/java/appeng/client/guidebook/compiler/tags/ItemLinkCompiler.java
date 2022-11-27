package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.client.guidebook.document.flow.LytTooltipSpan;
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
        // We'll error out for item-links to our own mod because we expect them to have a page
        // while we don't have pages for Vanilla items or items from other mods.
        if (linksTo == null && id.getNamespace().equals(compiler.getId().getNamespace())) {
            parent.append(compiler.createErrorFlowContent("No page found for item " + id, (MdAstNode) el));
            return;
        }

        var stack = item.getDefaultInstance();

        // If the item link is already on the page we're linking to, replace it with an underlined
        // text that has a tooltip.
        if (linksTo == null || linksTo.anchor() == null && compiler.getId().equals(linksTo.pageId())) {
            var span = new LytTooltipSpan();
            span.modifyStyle(style -> style.italic(true));
            compiler.compileComponentToFlow(stack.getHoverName(), span);
            span.setTooltip(new ItemTooltip(stack));
            parent.append(span);
        } else {
            var link = new LytFlowLink();
            link.setClickCallback(screen -> {
                screen.navigateTo(linksTo);
            });
            compiler.compileComponentToFlow(stack.getHoverName(), link);
            link.setTooltip(new ItemTooltip(stack));
            parent.append(link);
        }
    }

}
