package appeng.items.tools;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import guideme.GuidesCommon;

import appeng.core.AppEng;
import appeng.items.AEBaseItem;

/**
 * Shows the guidebook when used.
 */
public class GuideItem extends AEBaseItem {
    public static final ResourceLocation GUIDE_ID = AppEng.makeId("guide");

    public GuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            GuidesCommon.openGuide(player, GUIDE_ID);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }
}
