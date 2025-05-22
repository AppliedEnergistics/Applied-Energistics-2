package appeng.decorative.solid;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import appeng.block.misc.MysteriousCubeBlock;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.decorative.AEDecorativeBlock;

public class NotSoMysteriousCubeBlock extends AEDecorativeBlock {
    public NotSoMysteriousCubeBlock() {
        super(MysteriousCubeBlock.PROPERTIES);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Tooltips.of(GuiText.NotSoMysteriousQuote, Tooltips.QUOTE_TEXT));
    }
}
