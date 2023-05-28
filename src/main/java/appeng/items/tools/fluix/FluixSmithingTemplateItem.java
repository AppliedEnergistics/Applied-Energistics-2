package appeng.items.tools.fluix;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import appeng.api.ids.AEItemIds;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;

public class FluixSmithingTemplateItem extends SmithingTemplateItem {
    // Copy-pasted from superclass
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    private static final ResourceLocation EMPTY_SLOT_HOE = new ResourceLocation("item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_AXE = new ResourceLocation("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = new ResourceLocation("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = new ResourceLocation("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = new ResourceLocation("item/empty_slot_pickaxe");

    private static final ResourceLocation EMPTY_SLOT_BLOCK = AppEng.makeId("item/empty_slot_block");

    public FluixSmithingTemplateItem() {
        super(
                GuiText.QuartzTools.text().withStyle(DESCRIPTION_FORMAT),
                Component.translatable(Util.makeDescriptionId("item", AEItemIds.FLUIX_CRYSTAL))
                        .withStyle(DESCRIPTION_FORMAT),
                Component.translatable(Util.makeDescriptionId("item", AEItemIds.FLUIX_UPGRADE_SMITHING_TEMPLATE))
                        .withStyle(TITLE_FORMAT),
                GuiText.PutAQuartzTool.text(),
                GuiText.PutAFluixBlock.text(),
                List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL),
                List.of(EMPTY_SLOT_BLOCK));
    }
}
