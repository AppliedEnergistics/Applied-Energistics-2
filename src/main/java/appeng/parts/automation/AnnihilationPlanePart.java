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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;

import appeng.api.behaviors.PickupStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.parts.BasicStatePart;
import appeng.util.EnchantmentUtil;
import appeng.util.SettingsFrom;

public class AnnihilationPlanePart extends BasicStatePart implements IGridTickable {

    private static final PlaneModels MODELS = new PlaneModels("part/annihilation_plane",
            "part/annihilation_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final IActionSource actionSource = new MachineSource(this);

    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);

    @Nullable
    protected List<PickupStrategy> pickupStrategies;

    private PickupStrategy pendingPickupStrategy;

    /**
     * Enchantments found on the plane when it was placed will be used to enchant the fake tool used for picking up
     * blocks.
     */
    @Nullable
    private Map<Enchantment, Integer> enchantments;

    // Allows annihilation planes to stop pickup and instead go into a continuous generation mode
    private ContinuousGeneration continuousGeneration;
    private int continuousGenerationTicks;

    public AnnihilationPlanePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();

        var host = getBlockEntity();
        var buildHeight = host.getLevel().getMaxBuildHeight();

        continuousGenerationTicks = 0;
        continuousGeneration = null;
        // When placed at max build height facing up, continuously generate 1 sky stone dust / 10 seconds
        if (host.getBlockPos().getY() + 1 >= buildHeight && getSide() == Direction.UP) {
            continuousGeneration = new ContinuousGeneration(
                    AEItemKey.of(AEItems.SKY_DUST),
                    1,
                    200);
        }
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        readEnchantments(data);
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        writeEnchantments(data);
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag data, @Nullable Player player) {
        super.importSettings(mode, data, player);
        // Import enchants only when the plan is placed, not from memory cards
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            readEnchantments(data);
        }
        pickupStrategies = null;
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag data) {
        super.exportSettings(mode, data);
        // Save enchants only when the actual plane is dismantled
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            writeEnchantments(data);
        }
    }

    private void readEnchantments(CompoundTag data) {
        enchantments = EnchantmentUtil.getEnchantments(data);
    }

    private void writeEnchantments(CompoundTag data) {
        if (enchantments != null) {
            EnchantmentUtil.setEnchantments(data, enchantments);
        }
    }

    @Nullable
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    protected List<PickupStrategy> getPickupStrategies() {
        if (pickupStrategies == null) {
            var self = this.getHost().getBlockEntity();
            var pos = self.getBlockPos().relative(this.getSide());
            var side = getSide().getOpposite();
            pickupStrategies = StackWorldBehaviors.createPickupStrategies((ServerLevel) self.getLevel(),
                    pos, side, self, enchantments);
        }
        return pickupStrategies;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {

        // For collision, we're using a simplified bounding box
        if (bch.isBBCollision()) {
            // The smaller collision hitbox here is needed to allow for the entity collision event
            bch.addBox(0, 0, 14, 16, 16, 15.5);
            return;
        }

        connectionHelper.getBoxes(bch);

    }

    /**
     * @return An object describing which adjacent planes this plane connects to visually.
     */
    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals(neighbor)) {
            if (!isClientSide()) {
                this.refresh();
            }
        } else {
            connectionHelper.updateConnections();
        }
    }

    @Override
    public void onEntityCollision(Entity entity) {
        if (!entity.isAlive() || isClientSide() || !this.getMainNode().isActive()) {
            return;
        }

        var grid = getMainNode().getGrid();
        if (grid == null) {
            return;
        }

        PickupStrategy strategy = null;
        for (PickupStrategy pickupStrategy : getPickupStrategies()) {
            if (pickupStrategy.canPickUpEntity(entity)) {
                strategy = pickupStrategy;
                break;
            }
        }
        if (strategy == null) {
            return;
        }

        var pos = getHost().getBlockEntity().getBlockPos();
        var planePosX = pos.getX();
        var planePosY = pos.getY();
        var planePosZ = pos.getZ();

        // This is the middle point of the entities BB, which is better suited for comparisons
        // that don't rely on it "touching" the plane
        var posYMiddle = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D;
        var entityPosX = entity.getX();
        var entityPosY = entity.getY();
        var entityPosZ = entity.getZ();

        var captureX = entityPosX > planePosX && entityPosX < planePosX + 1;
        var captureY = posYMiddle > planePosY && posYMiddle < planePosY + 1;
        var captureZ = entityPosZ > planePosZ && entityPosZ < planePosZ + 1;

        var capture = switch (getSide()) {
            case DOWN -> captureX && captureZ && entityPosY < planePosY + 0.1;
            case UP -> captureX && captureZ && entityPosY > planePosY + 0.9;
            case SOUTH -> captureX && captureY && entityPosZ > planePosZ + 0.9;
            case NORTH -> captureX && captureY && entityPosZ < planePosZ + 0.1;
            case EAST -> captureZ && captureY && entityPosX > planePosX + 0.9;
            case WEST -> captureZ && captureY && entityPosX < planePosX + 0.1;
        };

        if (capture) {
            if (!strategy.pickUpEntity(grid.getEnergyService(), this::insertIntoGrid, entity)) {
                // we need to wake up the block entity in case an entity pickup fails
                // to reset the "blocked" flags internal to the pickup strategy.
                getMainNode().ifPresent((g, n) -> g.getTickManager().alertDevice(n));
            }
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);

        if (getMainNode().hasGridBooted()) {
            this.refresh();
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.AnnihilationPlane, false,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var grid = node.getGrid();

        if (isActive() && continuousGeneration != null) {
            continuousGenerationTicks += ticksSinceLastCall;
            if (continuousGenerationTicks >= continuousGeneration.ticks) {
                long amount = continuousGenerationTicks / continuousGeneration.ticks;
                insertIntoGrid(continuousGeneration.what, amount, Actionable.MODULATE);
                continuousGenerationTicks -= amount * continuousGeneration.ticks;
            }
            return TickRateModulation.IDLE;
        }

        if (pendingPickupStrategy != null) {
            pendingPickupStrategy.completePickup(grid.getEnergyService(), this::insertIntoGrid);
            pendingPickupStrategy = null;
        }

        // Reset to allow more entity pickups
        for (var pickupStrategy : getPickupStrategies()) {
            pickupStrategy.reset();
        }

        for (PickupStrategy pickupStrategy : getPickupStrategies()) {
            var pickupResult = pickupStrategy.tryStartPickup(grid.getEnergyService(), this::insertIntoGrid);

            if (pickupResult == PickupStrategy.Result.PICKED_UP) {
                pendingPickupStrategy = pickupStrategy;
                return TickRateModulation.URGENT;
            } else if (pickupResult == PickupStrategy.Result.CANT_STORE) {
                // If there's a compatible block, but we can't store it, wait longer
                return TickRateModulation.IDLE;
            }
        }

        return TickRateModulation.SLEEP;
    }

    private void refresh() {
        for (var pickupStrategy : getPickupStrategies()) {
            pickupStrategy.reset();
        }

        getMainNode().ifPresent((g, n) -> g.getTickManager().alertDevice(n));
    }

    private long insertIntoGrid(AEKey what, long amount, Actionable mode) {
        var grid = getMainNode().getGrid();
        if (grid == null) {
            return 0;
        }
        return StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(),
                what, amount, this.actionSource, mode);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public Object getRenderAttachmentData() {
        return getConnections();
    }

    private record ContinuousGeneration(
            AEKey what,
            long amount,
            int ticks) {
    }
}
