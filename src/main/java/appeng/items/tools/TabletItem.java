package appeng.items.tools;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.patchouli.api.PatchouliAPI;

import appeng.core.AppEng;
import appeng.items.AEBaseItem;

public class TabletItem extends AEBaseItem {
    public static final ResourceLocation BOOK_ID = AppEng.makeId("guide");

    public TabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (playerIn instanceof ServerPlayer) {
            PatchouliAPI.get().openBookGUI((ServerPlayer) playerIn, BOOK_ID);

            // This plays the sound to others nearby, playing to the actual opening player handled from the packet
//            playerIn.playSound(sfx, 1F, (float) (0.7 + Math.random() * 0.4));
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
