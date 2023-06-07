package appeng.items.materials;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;

public class EntangledSingularityItem extends AEBaseItem {

    public EntangledSingularityItem(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        if (hasChannel(stack)) {
            tooltipComponents.add(GuiText.SerialNumber.text(stack.getTag().get("freq")).withStyle(Tooltips.GREEN));
        } else {
            tooltipComponents.add(GuiText.Missing.text("Channel").withStyle(Tooltips.RED));
        }
    }

    public static boolean hasChannel(ItemStack singularity) {
        return singularity.hasTag() && singularity.getTag().contains("freq");
    }
}
