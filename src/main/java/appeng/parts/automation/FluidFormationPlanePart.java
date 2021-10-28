/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.core.definitions.AEParts;
import appeng.helpers.IConfigurableFluidInventory;
import appeng.items.parts.PartModels;
import appeng.me.storage.MEInventoryHandler;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.FluidFormationPlaneMenu;
import appeng.util.fluid.AEFluidInventory;
import appeng.util.fluid.IAEFluidTank;
import appeng.util.inv.IAEFluidInventory;
import appeng.util.prioritylist.PrecisePriorityList;

public class FluidFormationPlanePart extends AbstractFormationPlanePart<IAEFluidStack>
        implements IAEFluidInventory, IConfigurableFluidInventory {
    private static final PlaneModels MODELS = new PlaneModels("part/fluid_formation_plane",
            "part/fluid_formation_plane_on");

    /**
     * {@link System#currentTimeMillis()} of when the last sound/visual effect was played by this plane.
     */
    private long lastEffect;

    /**
     * The fluids that we tried to place unsuccessfully.
     */
    private final Set<Fluid> blocked = new HashSet<>();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final MEInventoryHandler<IAEFluidStack> myHandler = new MEInventoryHandler<>(this);
    private final AEFluidInventory config = new AEFluidInventory(this, 63);

    public FluidFormationPlanePart(final ItemStack is) {
        super(is);
        this.updateHandler();
    }

    @Override
    protected void updateHandler() {
        this.myHandler.setBaseAccess(AccessRestriction.WRITE);
        this.myHandler.setWhitelist(
                this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.myHandler.setPriority(this.getPriority());

        final IAEStackList<IAEFluidStack> priorityList = StorageChannels.fluids().createList();

        final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
        for (int x = 0; x < this.config.getSlots() && x < slotsToUse; x++) {
            final IAEFluidStack is = this.config.getFluidInSlot(x);
            if (is != null) {
                priorityList.add(is);
            }
        }
        this.myHandler.setPartitionList(new PrecisePriorityList<>(priorityList));

        getMainNode().ifPresent(g -> g.postEvent(new GridCellArrayUpdate()));
    }

    @Override
    protected void clearBlocked(BlockGetter level, BlockPos pos) {
        blocked.clear();
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
        if (input == null || input.getStackSize() < FluidConstants.BLOCK) {
            // need a full bucket
            return input;
        }

        var fluid = input.getFluid().getFluid();

        // We previously tried placing this fluid unsuccessfully, so don't check it again.
        if (blocked.contains(fluid)) {
            return input;
        }

        // We do not support placing fluids with NBT for now
        if (input.getFluid().hasNbt()) {
            return input;
        }

        var te = this.getHost().getBlockEntity();
        var level = te.getLevel();
        var side = this.getSide();
        var pos = te.getBlockPos().relative(side);
        var state = level.getBlockState(pos);

        if (!this.canPlace(level, state, pos, fluid)) {
            // Remember that this fluid cannot be placed right now.
            blocked.add(fluid);
            return input;
        }

        if (type == Actionable.MODULATE) {
            // Placing water in nether voids the fluid, but plays effects
            if (level.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
                playEvaporationEffect(level, pos);
            } else if (state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer
                    && fluid == Fluids.WATER) {
                liquidBlockContainer.placeLiquid(level, pos, state, ((FlowingFluid) fluid).getSource(false));
                playEmptySound(level, pos, fluid);
            } else {
                if (state.canBeReplaced(fluid) && !state.getMaterial().isLiquid()) {
                    level.destroyBlock(pos, true);
                }

                if (!level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE)
                        && !state.getFluidState().isSource()) {
                    return input;
                } else {
                    playEmptySound(level, pos, fluid);
                }
            }
        }

        var ret = input.copy();
        ret.decStackSize(FluidConstants.BLOCK);
        return ret.getStackSize() == 0 ? null : ret;
    }

    private void playEmptySound(Level level, BlockPos pos, Fluid fluid) {
        if (throttleEffect()) {
            return;
        }

        SoundEvent soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.FLUID_PLACE, pos);
    }

    private void playEvaporationEffect(Level level, BlockPos pos) {
        if (throttleEffect()) {
            return;
        }

        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

        for (int l = 0; l < 8; ++l) {
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    (double) pos.getX() + Math.random(),
                    (double) pos.getY() + Math.random(),
                    (double) pos.getZ() + Math.random(),
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    /**
     * Only play the effect every 250ms.
     */
    private boolean throttleEffect() {
        long now = System.currentTimeMillis();
        if (now < lastEffect + 250) {
            return true;
        }
        lastEffect = now;
        return false;
    }

    /**
     * Checks from {@link net.minecraft.world.item.BucketItem#emptyContents}
     */
    private boolean canPlace(Level level, BlockState state, BlockPos pos, Fluid fluid) {
        if (!(fluid instanceof FlowingFluid)) {
            return false;
        }

        // This check is in addition to vanilla's checks. If the fluid is already in place,
        // don't place it again. This is for water, since water is otherwise replaceable by water.
        if (state == fluid.defaultFluidState().createLegacyBlock()) {
            return false;
        }

        return state.isAir()
                || state.canBeReplaced(fluid)
                || state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer
                        && liquidBlockContainer.canPlaceLiquid(level, pos, state, fluid);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
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
    public Storage<FluidVariant> getFluidInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(FluidFormationPlaneMenu.TYPE, player, MenuLocator.forPart(this));
        }

        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return StorageChannels.fluids();
    }

    @Override
    public <T extends IAEStack> List<IMEInventoryHandler<T>> getCellArray(final IStorageChannel<T> channel) {
        if (this.getMainNode().isActive()
                && channel == StorageChannels.fluids()) {
            return List.of(this.myHandler.cast(channel));
        }
        return Collections.emptyList();
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

    public IAEFluidTank getConfig() {
        return this.config;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.FLUID_FORMATION_PLANE.stack();
    }

    @Override
    public MenuType<?> getMenuType() {
        return FluidFormationPlaneMenu.TYPE;
    }
}
