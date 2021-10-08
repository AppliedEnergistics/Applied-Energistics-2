/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts.automation;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.core.AEConfig;
import appeng.core.definitions.AEParts;
import appeng.hooks.AECustomEntityItem;
import appeng.items.parts.PartModels;
import appeng.me.storage.MEInventoryHandler;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.ItemFormationPlaneMenu;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalAEInventory;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

public class FormationPlanePart extends AbstractFormationPlanePart<IAEItemStack> {

    private static final PlaneModels MODELS = new PlaneModels("part/item_formation_plane",
            "part/item_formation_plane_on");
    private static final Random RANDOM_OFFSET = new Random();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final MEInventoryHandler<IAEItemStack> myHandler = new MEInventoryHandler<>(this,
            StorageChannels.items());
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 63);

    public FormationPlanePart(final ItemStack is) {
        super(is);

        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.PLACE_BLOCK, YesNo.YES);
        this.updateHandler();
    }

    @Override
    protected void updateHandler() {
        this.myHandler.setBaseAccess(AccessRestriction.WRITE);
        this.myHandler.setWhitelist(
                this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.myHandler.setPriority(this.getPriority());

        final IAEStackList<IAEItemStack> priorityList = StorageChannels.items().createList();

        final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
        for (int x = 0; x < this.config.size() && x < slotsToUse; x++) {
            final IAEItemStack is = this.config.getAEStackInSlot(x);
            if (is != null) {
                priorityList.add(is);
            }
        }

        if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            this.myHandler.setPartitionList(new FuzzyPriorityList<>(priorityList,
                    this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
        } else {
            this.myHandler.setPartitionList(new PrecisePriorityList<>(priorityList));
        }

        getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, removedStack, newStack);

        if (inv == this.config) {
            this.updateHandler();
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.config.writeToNBT(data, "config");
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.CONFIG)) {
            return config;
        } else {
            return super.getSubInventory(id);
        }
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(ItemFormationPlaneMenu.TYPE, player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public <T extends IAEStack> List<IMEInventoryHandler<T>> getCellArray(final IStorageChannel<T> channel) {
        if (this.getMainNode().isActive()
                && channel == StorageChannels.items()) {
            return List.of(this.myHandler.cast(channel));
        }
        return Collections.emptyList();
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack input, final Actionable type, final IActionSource src) {
        if (this.blocked || input == null || input.getStackSize() <= 0) {
            return input;
        }

        final YesNo placeBlock = this.getConfigManager().getSetting(Settings.PLACE_BLOCK);

        final ItemStack is = input.createItemStack();
        final Item i = is.getItem();

        long maxStorage = Math.min(input.getStackSize(), is.getMaxStackSize());
        boolean worked = false;

        final BlockEntity te = this.getHost().getBlockEntity();
        final Level level = te.getLevel();
        final Direction side = this.getSide();

        final BlockPos placePos = te.getBlockPos().relative(side);

        if (level.getBlockState(placePos).getMaterial().isReplaceable()) {
            if (placeBlock == YesNo.YES) {
                final Player player = Platform.getPlayer((ServerLevel) level);
                Platform.configurePlayer(player, side, this.getBlockEntity());
                // Seems to work without...
                // Hand hand = player.getActiveHand();
                // player.setHeldItem(hand, is);

                maxStorage = is.getCount();
                worked = true;
                if (type == Actionable.MODULATE) {
                    // The side the plane is attached to will be considered the look direction
                    // in terms of placing an item
                    Direction lookDirection = side;
                    PlaneDirectionalPlaceContext context = new PlaneDirectionalPlaceContext(level, player, placePos,
                            lookDirection, is, lookDirection.getOpposite());

                    i.useOn(context);
                    maxStorage -= is.getCount();

                } else {
                    maxStorage = 1;
                }

                // Seems to work without... Safe keeping
                // player.setHeldItem(hand, ItemStack.EMPTY);
            } else {
                final int sum = this.countEntitesAround(level, placePos);

                // Disable spawning once there is a certain amount of entities in an area.
                if (sum < AEConfig.instance().getFormationPlaneEntityLimit()) {
                    worked = true;

                    if (type == Actionable.MODULATE) {
                        is.setCount((int) maxStorage);
                        if (!spawnItemEntity(level, te, side, is)) {
                            // revert in case something prevents spawning.
                            worked = false;
                        }

                    }
                }
            }
        }

        this.blocked = !level.getBlockState(placePos).getMaterial().isReplaceable();

        if (worked) {
            final IAEItemStack out = input.copy();
            out.decStackSize(maxStorage);
            if (out.getStackSize() == 0) {
                return null;
            }
            return out;
        }

        return input;
    }

    private static boolean spawnItemEntity(Level level, BlockEntity te, Direction side, ItemStack is) {
        // The center of the block the plane is located in
        final double centerX = te.getBlockPos().getX() + .5;
        final double centerY = te.getBlockPos().getY();
        final double centerZ = te.getBlockPos().getZ() + .5;

        // Create an ItemEntity already at the position of the plane.
        // We don't know the final position, but need it for its size.
        Entity entity = new ItemEntity(level, centerX, centerY, centerZ, is.copy());

        // Replace it if there is a custom entity
        if (is.getItem() instanceof AECustomEntityItem customEntityItem) {
            Entity result = customEntityItem.replaceItemEntity((ServerLevel) level, (ItemEntity) entity, is);
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
        final double offsetX = side.getStepX() == 0 //
                ? RANDOM_OFFSET.nextFloat() * spawnAreaWidth - spawnAreaWidth / 2
                : side.getStepX() * (.525 + entity.getBbWidth() / 2);
        final double offsetY = side.getStepY() == 0 //
                ? RANDOM_OFFSET.nextFloat() * spawnAreaHeight
                : side.getStepY() + additionalYOffset;
        final double offsetZ = side.getStepZ() == 0 //
                ? RANDOM_OFFSET.nextFloat() * spawnAreaWidth - spawnAreaWidth / 2
                : side.getStepZ() * (.525 + entity.getBbWidth() / 2);

        final double absoluteX = centerX + offsetX;
        final double absoluteY = centerY + offsetY;
        final double absoluteZ = centerZ + offsetZ;

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

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return StorageChannels.items();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public Object getRenderAttachmentData() {
        return getConnections();
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.FORMATION_PLANE.stack();
    }

    @Override
    public MenuType<?> getMenuType() {
        return ItemFormationPlaneMenu.TYPE;
    }

    private int countEntitesAround(Level level, BlockPos pos) {
        final AABB t = new AABB(pos).inflate(8);
        final List<Entity> list = level.getEntitiesOfClass(Entity.class, t);

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
            return this.lookDirection.getAxis() == Axis.Y ? Direction.NORTH : this.lookDirection;
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
