package appeng.parts.automation;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Actionable;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.core.AEConfig;
import appeng.hooks.AECustomEntityItem;
import appeng.util.Platform;

public class ItemPlacementStrategy implements PlacementStrategy {
    private static final Random RANDOM_OFFSET = new Random();
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    private final BlockEntity host;
    private boolean blocked = false;

    public ItemPlacementStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.host = host;
    }

    public void clearBlocked() {
        this.blocked = !level.getBlockState(pos).getMaterial().isReplaceable();
    }

    public final long placeInWorld(AEKey what, long amount, Actionable type, boolean placeAsEntity) {
        if (this.blocked || !(what instanceof AEItemKey itemKey) || amount <= 0) {
            return 0;
        }

        var i = itemKey.getItem();

        var maxStorage = (int) Math.min(amount, i.getMaxStackSize());
        var is = itemKey.toStack(maxStorage);
        var worked = false;

        var side = this.side.getOpposite();

        final var placePos = pos;

        if (level.getBlockState(placePos).getMaterial().isReplaceable()) {
            if (placeAsEntity) {
                final var player = Platform.getPlayer(level);
                Platform.configurePlayer(player, side, host);

                maxStorage = is.getCount();
                worked = true;
                if (type == Actionable.MODULATE) {
                    // The side the plane is attached to will be considered the look direction
                    // in terms of placing an item
                    var lookDirection = side;
                    var context = new PlaneDirectionalPlaceContext(level, player, placePos,
                            lookDirection, is, lookDirection.getOpposite());

                    i.useOn(context);
                    maxStorage -= is.getCount();

                } else {
                    maxStorage = 1;
                }

                // Seems to work without... Safe keeping
                // player.setHeldItem(hand, ItemStack.EMPTY);
            } else {
                final var sum = this.countEntitesAround(level, placePos);

                // Disable spawning once there is a certain amount of entities in an area.
                if (sum < AEConfig.instance().getFormationPlaneEntityLimit()) {
                    worked = true;

                    if (type == Actionable.MODULATE) {
                        is.setCount(maxStorage);
                        if (!spawnItemEntity(level, host, side, is)) {
                            // revert in case something prevents spawning.
                            worked = false;
                        }

                    }
                }
            }
        }

        this.blocked = !level.getBlockState(placePos).getMaterial().isReplaceable();

        if (worked) {
            return maxStorage;
        }

        return 0;
    }

    private static boolean spawnItemEntity(Level level, BlockEntity te, Direction side, ItemStack is) {
        // The center of the block the plane is located in
        final var centerX = te.getBlockPos().getX() + .5;
        final double centerY = te.getBlockPos().getY();
        final var centerZ = te.getBlockPos().getZ() + .5;

        // Create an ItemEntity already at the position of the plane.
        // We don't know the final position, but need it for its size.
        Entity entity = new ItemEntity(level, centerX, centerY, centerZ, is.copy());

        // Replace it if there is a custom entity
        if (is.getItem() instanceof AECustomEntityItem customEntityItem) {
            var result = customEntityItem.replaceItemEntity((ServerLevel) level, (ItemEntity) entity, is);
            // Destroy the old one, in case it's spawned somehow and replace with the new
            // one.
            if (result != null) {
                entity.discard();
                entity = result;
            }
        }

        // When spawning downwards, we have to take into account that it spawns it at
        // their "feet" and not center like x or z. So we move it up to be flush with
        // the plane
        final double additionalYOffset = side.getStepY() == -1 ? 1 - entity.getBbHeight() : 0;

        // Calculate the maximum spawn area so an entity hitbox will always be inside
        // the block.
        final double spawnAreaHeight = Math.max(0, 1 - entity.getBbHeight());
        final double spawnAreaWidth = Math.max(0, 1 - entity.getBbWidth());

        // Calculate the offsets to spawn it into the adjacent block, taking the sign
        // into account.
        // Spawn it 0.8 blocks away from the center pos when facing in this direction
        // Every other direction will select a position in a .5 block area around the
        // block center.
        final var offsetX = side.getStepX() == 0 //
                ? RANDOM_OFFSET.nextFloat() * spawnAreaWidth - spawnAreaWidth / 2
                : side.getStepX() * (.525 + entity.getBbWidth() / 2);
        final var offsetY = side.getStepY() == 0 //
                ? RANDOM_OFFSET.nextFloat() * spawnAreaHeight
                : side.getStepY() + additionalYOffset;
        final var offsetZ = side.getStepZ() == 0 //
                ? RANDOM_OFFSET.nextFloat() * spawnAreaWidth - spawnAreaWidth / 2
                : side.getStepZ() * (.525 + entity.getBbWidth() / 2);

        final var absoluteX = centerX + offsetX;
        final var absoluteY = centerY + offsetY;
        final var absoluteZ = centerZ + offsetZ;

        // Set to correct position and slow the motion down a bit
        entity.setPos(absoluteX, absoluteY, absoluteZ);
        entity.setDeltaMovement(side.getStepX() * .1, side.getStepY() * 0.1, side.getStepZ() * 0.1);

        // Try to spawn it and destroy it in case it's not possible
        if (!level.addFreshEntity(entity)) {
            entity.discard();
            return false;
        }
        return true;
    }

    private int countEntitesAround(Level level, BlockPos pos) {
        final var t = new AABB(pos).inflate(8);
        final var list = level.getEntitiesOfClass(Entity.class, t);

        return list.size();
    }

    /**
     * A custom {@link DirectionalPlaceContext} which also accepts a player needed various blocks like seeds.
     * <p>
     * Also removed {@link DirectionalPlaceContext#replacingClickedOnBlock} as this can cause a
     * {@link StackOverflowError} for certain replaceable blocks.
     */
    private static class PlaneDirectionalPlaceContext extends BlockPlaceContext {
        private final Direction lookDirection;

        public PlaneDirectionalPlaceContext(Level level, Player player, BlockPos pos, Direction lookDirection,
                ItemStack itemStack, Direction facing) {
            super(level, player, InteractionHand.MAIN_HAND, itemStack,
                    new BlockHitResult(Vec3.atBottomCenterOf(pos), facing, pos, false));
            this.lookDirection = lookDirection;
        }

        @Override
        public BlockPos getClickedPos() {
            return this.getHitResult().getBlockPos();
        }

        @Override
        public boolean canPlace() {
            return getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
        }

        @Override
        public Direction getNearestLookingDirection() {
            return Direction.DOWN;
        }

        @Override
        public Direction[] getNearestLookingDirections() {
            return switch (this.lookDirection) {
                default -> new Direction[] { Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH,
                        Direction.WEST, Direction.UP };
                case UP -> new Direction[] { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST,
                        Direction.SOUTH, Direction.WEST };
                case NORTH -> new Direction[] { Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.WEST,
                        Direction.UP, Direction.SOUTH };
                case SOUTH -> new Direction[] { Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.WEST,
                        Direction.UP, Direction.NORTH };
                case WEST -> new Direction[] { Direction.DOWN, Direction.WEST, Direction.SOUTH, Direction.UP,
                        Direction.NORTH, Direction.EAST };
                case EAST -> new Direction[] { Direction.DOWN, Direction.EAST, Direction.SOUTH, Direction.UP,
                        Direction.NORTH, Direction.WEST };
            };
        }

        @Override
        public Direction getHorizontalDirection() {
            return this.lookDirection.getAxis() == Direction.Axis.Y ? Direction.NORTH : this.lookDirection;
        }

        @Override
        public boolean isSecondaryUseActive() {
            return false;
        }

        @Override
        public float getRotation() {
            return this.lookDirection.get2DDataValue() * 90;
        }
    }

}
