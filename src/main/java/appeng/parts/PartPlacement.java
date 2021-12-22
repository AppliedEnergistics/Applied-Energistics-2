package appeng.parts;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.util.SettingsFrom;

public class PartPlacement {

    public static InteractionResult place(UseOnContext context) {

        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var partStack = context.getItemInHand();
        var side = context.getClickedFace();

        if (!(partStack.getItem() instanceof IPartItem<?>partItem)) {
            return InteractionResult.PASS;
        }

        // Determine where the part would be placed
        var placement = getPartPlacement(player, level, partStack, pos, side);
        if (placement == null) {
            return InteractionResult.FAIL;
        }

        // Then try to place it
        var part = placePart(player, level, partItem, partStack.getTag(), placement.pos(), placement.side());
        if (part == null) {
            return InteractionResult.FAIL;
        }

        // Consume one of the part item
        if (!level.isClientSide && player != null && !player.isCreative()) {
            partStack.shrink(1);
            if (partStack.getCount() == 0) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * Executes placing a part at a location determined by {@link #getPartPlacement}.
     */
    @Nullable
    public static <T extends IPart> T placePart(@Nullable Player player,
            Level level,
            IPartItem<T> partItem,
            @Nullable CompoundTag configTag,
            BlockPos pos,
            Direction side) {

        // Execute the placement
        var host = PartHelper.getOrPlacePartHost(level, pos, false, player);
        if (host == null) {
            return null;
        }
        var addedPart = host.addPart(partItem, side, player);
        if (addedPart == null) {
            if (host.isEmpty()) {
                host.cleanup();
            }
            return null;
        }

        // Import settings from the item if possible
        if (configTag != null) {
            try {
                addedPart.importSettings(SettingsFrom.DISMANTLE_ITEM, configTag, player);
            } catch (Exception e) {
                AELog.warn(e, "Failed to import part settings during placement.");
            }
        }

        var ss = AEBlocks.CABLE_BUS.block().getSoundType(AEBlocks.CABLE_BUS.block().defaultBlockState());
        level.playSound(null, pos, ss.getPlaceSound(), SoundSource.BLOCKS, (ss.getVolume() + 1.0F) / 2.0F,
                ss.getPitch() * 0.8F);
        return addedPart;
    }

    /**
     * Determines where exactly a part would be placed if it was clicked on the given side of a block position.
     */
    @Nullable
    public static Placement getPartPlacement(@Nullable Player player,
            Level level,
            ItemStack partStack,
            BlockPos pos,
            Direction side) {

        if (canPlacePartOnBlock(player, level, partStack, pos, side)) {
            return new Placement(pos, side);
        }

        // If the part cannot be placed directly in the block, try the opposite side of
        // the adjacent block. This is somewhat similar to how torches are placed.
        pos = pos.relative(side);
        side = side.getOpposite();
        if (canPlacePartOnBlock(player, level, partStack, pos, side)) {
            return new Placement(pos, side);
        }

        // Can't place the part
        return null;
    }

    /**
     * Determine how a part would be placed inside a block given a position and clicked side. This method will not move
     * the position to the adjacent block if placement fails.
     *
     * @return null If the part cannot be placed, otherwise the position and side.
     */
    public static boolean canPlacePartOnBlock(@Nullable Player player,
            Level level,
            ItemStack partStack,
            BlockPos pos,
            Direction side) {
        var host = PartHelper.getPartHost(level, pos);

        // There is no host at the location, we also cannot place one
        if (host == null && !PartHelper.canPlacePartHost(player, level, pos)) {
            return false;
        }

        // Either there is no host, then we assume a frehsly placed host will always accept our part,
        // or there is a host, and it has a free side.
        return host == null || host.canAddPart(partStack, side);
    }

    public record Placement(BlockPos pos, Direction side) {
    }

}
