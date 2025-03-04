package appeng.parts;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.core.AELog;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.parts.networking.CablePart;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class PartPlacement {
    public static InteractionResult place(UseOnContext context) {

        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var partStack = context.getItemInHand();
        var side = context.getClickedFace();

        if (!(partStack.getItem() instanceof IPartItem<?> partItem)) {
            return InteractionResult.PASS;
        }

        // Determine where the part would be placed
        var placement = getPartPlacement(player, level, partStack, pos, side, context.getClickLocation());
        if (placement == null) {
            return InteractionResult.FAIL;
        }

        // Then try to place it
        var part = placePart(player, level, partItem, partStack.getComponents(), placement.pos(), placement.side());
        if (part == null) {
            // Resend the host to the client. Failure to connect for security reasons is only possible to know
            // server-side, and this will cause ghost parts on the client.
            Platform.sendImmediateBlockEntityUpdate(player, pos);

            return InteractionResult.FAIL;
        }

        // Consume one of the part item
        if (!level.isClientSide && player != null && !player.isCreative()) {
            partStack.shrink(1);
            if (partStack.getCount() == 0) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            }
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Executes placing a part at a location determined by {@link #getPartPlacement}.
     */
    @Nullable
    public static <T extends IPart> T placePart(@Nullable Player player,
            Level level,
            IPartItem<T> partItem,
            @Nullable DataComponentMap configData,
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

        // Check collisions with entities and revert placement
        // Due to cables being able to react to parts being added to the host, we can't really
        // simulate this without really placing the part
        var collisionShape = host.getCollisionShape(null);
        if (!collisionShape.isEmpty()
                && !level.isUnobstructed(null, collisionShape.move(pos.getX(), pos.getY(), pos.getZ()))) {
            host.removePart(addedPart);
            if (host.isEmpty()) {
                host.cleanup();
            }
            return null;
        }

        // Import settings from the item if possible
        if (configData != null) {
            try {
                addedPart.importSettings(SettingsFrom.DISMANTLE_ITEM, configData, player);
            } catch (Exception e) {
                AELog.warn(e, "Failed to import part settings during placement.");
            }
        }

        var state = level.getBlockState(pos);
        var ss = state.getSoundType(level, pos, player);
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
            Direction side,
            Vec3 clickLocation) {

        // If a cable segment was clicked, try replacing that cable segment by the part
        var replaceCablePlacement = tryReplaceCableSegment(level, partStack, pos, clickLocation);
        if (replaceCablePlacement != null) {
            side = replaceCablePlacement;
        }

        if (player != null) {
            side = player.getData(AEAttachmentTypes.HOLDING_CTRL) ? side.getOpposite() : side;
        }

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

    @Nullable
    private static Direction tryReplaceCableSegment(Level level, ItemStack partStack, BlockPos pos,
            Vec3 clickLocation) {
        // Check if there exists a host with a cable in its center
        var host = PartHelper.getPartHost(level, pos);
        if (host == null) {
            return null;
        }
        var cable = host.getPart(null);
        if (!(cable instanceof CablePart cablePart)) {
            return null;
        }

        // Find hit side
        Direction hitSide = null;

        var localClickLocation = clickLocation.subtract(pos.getX(), pos.getY(), pos.getZ());
        sideLoop: for (var side : Direction.values()) {
            List<AABB> boxes = new ArrayList<>();
            var bch = new BusCollisionHelper(boxes, null, true);
            cablePart.getBoxes(bch, boxSide -> boxSide == side);

            for (var box : boxes) {
                if (box.inflate(0.02).contains(localClickLocation)) {
                    hitSide = side;
                    break sideLoop;
                }
            }
        }

        if (host.canAddPart(partStack, hitSide)) {
            return hitSide;
        } else {
            return null;
        }
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
