package appeng.items.tools.fluix;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.SmithingTemplateItem;

import appeng.api.ids.AEItemIds;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;

public class FluixSmithingTemplateItem extends SmithingTemplateItem {
    // Copy-pasted from superclass
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    private static final Identifier EMPTY_SLOT_HOE = Identifier.parse("item/empty_slot_hoe");
    private static final Identifier EMPTY_SLOT_AXE = Identifier.parse("item/empty_slot_axe");
    private static final Identifier EMPTY_SLOT_SWORD = Identifier.parse("item/empty_slot_sword");
    private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.parse("item/empty_slot_shovel");
    private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.parse("item/empty_slot_pickaxe");

    private static final Identifier EMPTY_SLOT_BLOCK = AppEng.makeId("item/empty_slot_block");

    public FluixSmithingTemplateItem(Properties p) {
        super(
                GuiText.QuartzTools.text().withStyle(DESCRIPTION_FORMAT),
                Component.translatable(Util.makeDescriptionId("item", AEItemIds.FLUIX_UPGRADE_SMITHING_TEMPLATE))
                        .withStyle(TITLE_FORMAT),
                GuiText.PutAQuartzTool.text(),
                GuiText.PutAFluixBlock.text(),
                List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL),
                List.of(EMPTY_SLOT_BLOCK),
                p);
    }
}
