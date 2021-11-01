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

package appeng.parts;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.google.common.base.Preconditions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.helpers.IConfigurableFluidInventory;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.parts.automation.FluidLevelEmitterPart;
import appeng.parts.automation.ItemLevelEmitterPart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.fluid.AEFluidInventory;
import appeng.util.inv.AppEngInternalAEInventory;

public abstract class AEBasePart implements IPart, IActionHost, ICustomNameObject, ISegmentedInventory {

    private final IManagedGridNode mainNode;
    private final ItemStack is;
    private BlockEntity blockEntity = null;
    private IPartHost host = null;
    @Nullable
    private Direction side;

    public AEBasePart(final ItemStack is) {
        Preconditions.checkNotNull(is);

        this.is = is;
        this.mainNode = createMainNode()
                .setVisualRepresentation(is)
                .setExposedOnSides(EnumSet.noneOf(Direction.class));
    }

    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NodeListener.INSTANCE);
    }

    /**
     * Called if one of the properties that result in a node becoming active or inactive change.
     *
     * @param reason Indicates which of the properties has changed.
     */
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
    }

    public final boolean isRemote() {
        return this.blockEntity == null
                || this.blockEntity.getLevel() == null
                || this.blockEntity.getLevel().isClientSide();
    }

    public IPartHost getHost() {
        return this.host;
    }

    protected AEColor getColor() {
        if (this.host == null) {
            return AEColor.TRANSPARENT;
        }
        return this.host.getColor();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {

    }

    public IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public IGridNode getActionableNode() {
        return this.mainNode.getNode();
    }

    public final BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public Level getLevel() {
        return this.blockEntity.getLevel();
    }

    @Override
    public Component getCustomInventoryName() {
        return this.getItemStack().getHoverName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.getItemStack().hasCustomHoverName();
    }

    @Override
    public void addEntityCrashInfo(final CrashReportCategory crashreportcategory) {
        crashreportcategory.setDetail("Part Side", this.getSide());
        var beHost = getBlockEntity();
        if (beHost != null) {
            beHost.fillCrashReportCategory(crashreportcategory);
            var level = beHost.getLevel();
            if (level != null) {
                crashreportcategory.setDetail("Level", level.dimension());
            }
        }
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {
        if (type == PartItemStack.NETWORK) {
            final ItemStack copy = this.is.copy();
            copy.setTag(null);
            return copy;
        }
        return this.is;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public boolean canConnectRedstone() {
        return false;
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        this.mainNode.loadFromNBT(data);
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        this.mainNode.saveToNBT(data);
    }

    @Override
    public int isProvidingStrongPower() {
        return 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return 0;
    }

    @Override
    public void writeToStream(final FriendlyByteBuf data) throws IOException {

    }

    @Override
    public boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        return false;
    }

    @Override
    public IGridNode getGridNode() {
        return this.mainNode.getNode();
    }

    @Override
    public void onEntityCollision(final Entity entity) {

    }

    @Override
    public void removeFromWorld() {
        this.mainNode.destroy();
    }

    @Override
    public void addToWorld() {
        this.mainNode.create(getLevel(), blockEntity.getBlockPos());
    }

    @Override
    public void setPartHostInfo(final Direction side, final IPartHost host, final BlockEntity blockEntity) {
        this.setSide(side);
        this.blockEntity = blockEntity;
        this.host = host;
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(final Level level, final BlockPos pos, final Random r) {

    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {

    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 3;
    }

    @Override
    public boolean isLadder(final LivingEntity entity) {
        return false;
    }

    /**
     * depending on the from, different settings will be accepted, don't call this with null
     *
     * @param from     source of settings
     * @param compound compound of source
     */
    private void uploadSettings(final SettingsFrom from, final CompoundTag compound) {
        if (this instanceof IConfigurableObject configurableObject) {
            configurableObject.getConfigManager().readFromNBT(compound);
        }

        if (this instanceof IPriorityHost pHost) {
            pHost.setPriority(compound.getInt("priority"));
        }

        var inv = getSubInventory(ISegmentedInventory.CONFIG);
        if (inv instanceof AppEngInternalAEInventory target) {
            final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory(null, target.size());
            tmp.readFromNBT(compound, "config");
            for (int x = 0; x < tmp.size(); x++) {
                target.setItemDirect(x, tmp.getStackInSlot(x));
            }
            if (this instanceof ItemLevelEmitterPart partLevelEmitter) {
                partLevelEmitter.setReportingValue(compound.getLong("reportingValue"));
            }
        }

        if (this instanceof IConfigurableFluidInventory configurableFluidInventory) {
            var tank = configurableFluidInventory.getFluidInventoryByName("config");
            if (tank instanceof AEFluidInventory target) {
                var tmp = new AEFluidInventory(null, target.getSlots());
                tmp.readFromNBT(compound, "config");
                for (int x = 0; x < tmp.getSlots(); x++) {
                    target.setFluidInSlot(x, tmp.getFluidInSlot(x));
                }
            }
            if (this instanceof FluidLevelEmitterPart fluidLevelEmitterPart) {
                fluidLevelEmitterPart.setReportingValue(compound.getLong("reportingValue"));
            }
        }
    }

    /**
     * null means nothing to store...
     *
     * @param from source of settings
     * @return compound of source
     */
    private CompoundTag downloadSettings(final SettingsFrom from) {
        final CompoundTag output = new CompoundTag();

        if (this instanceof IConfigurableObject configurableObject) {
            configurableObject.getConfigManager().writeToNBT(output);
        }

        if (this instanceof IPriorityHost pHost) {
            output.putInt("priority", pHost.getPriority());
        }

        var inv = getSubInventory(ISegmentedInventory.CONFIG);
        if (inv instanceof AppEngInternalAEInventory) {
            ((AppEngInternalAEInventory) inv).writeToNBT(output, "config");
            if (this instanceof ItemLevelEmitterPart partLevelEmitter) {
                output.putLong("reportingValue", partLevelEmitter.getReportingValue());
            }
        }

        if (this instanceof IConfigurableFluidInventory configurableFluidInventory) {
            var tank = configurableFluidInventory.getFluidInventoryByName("config");
            ((AEFluidInventory) tank).writeToNBT(output, "config");
            if (this instanceof FluidLevelEmitterPart fluidLevelEmitterPart) {
                output.putLong("reportingValue", fluidLevelEmitterPart.getReportingValue());
            }
        }
        return output.isEmpty() ? null : output;
    }

    public boolean useStandardMemoryCard() {
        return true;
    }

    private boolean useMemoryCard(final Player player) {
        final ItemStack memCardIS = player.getInventory().getSelected();

        if (!memCardIS.isEmpty() && this.useStandardMemoryCard()
                && memCardIS.getItem() instanceof IMemoryCard memoryCard) {

            ItemStack is = this.getItemStack(PartItemStack.NETWORK);

            // Blocks and parts share the same soul!
            if (AEParts.ITEM_INTERFACE.isSameAs(is)) {
                is = AEBlocks.ITEM_INTERFACE.stack();
            }

            final String name = is.getDescriptionId();

            if (InteractionUtil.isInAlternateUseMode(player)) {
                final CompoundTag data = this.downloadSettings(SettingsFrom.MEMORY_CARD);
                if (data != null) {
                    memoryCard.setMemoryCardContents(memCardIS, name, data);
                    memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                }
            } else {
                final String storedName = memoryCard.getSettingsName(memCardIS);
                final CompoundTag data = memoryCard.getData(memCardIS);
                if (name.equals(storedName)) {
                    this.uploadSettings(SettingsFrom.MEMORY_CARD, data);
                    memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                } else {
                    memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean onActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (this.useMemoryCard(player)) {
            return true;
        }

        return this.onPartActivate(player, hand, pos);
    }

    @Override
    public final boolean onShiftActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (this.useMemoryCard(player)) {
            return true;
        }

        return this.onPartShiftActivate(player, hand, pos);
    }

    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        return false;
    }

    public boolean onPartShiftActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        return false;
    }

    @Override
    public void onPlacement(final Player player, final InteractionHand hand, final ItemStack held,
            final Direction side) {
        this.mainNode.setOwningPlayer(player);
    }

    @Override
    public boolean canBePlacedOn(final BusSupport what) {
        return what == BusSupport.CABLE;
    }

    @Override
    public boolean requireDynamicRender() {
        return false;
    }

    public Direction getSide() {
        return this.side;
    }

    private void setSide(final Direction side) {
        this.side = side;
    }

    public ItemStack getItemStack() {
        return this.is;
    }

    @Nullable
    @Override
    @OverridingMethodsMustInvokeSuper
    public InternalInventory getSubInventory(ResourceLocation id) {
        return null;
    }

    /**
     * Simple {@link IGridNodeListener} for {@link AEBasePart} that host nodes.
     */
    public static class NodeListener<T extends AEBasePart> implements IGridNodeListener<T> {

        public static final NodeListener<AEBasePart> INSTANCE = new NodeListener<>();

        @Override
        public void onSecurityBreak(T nodeOwner, IGridNode node) {
            var is = nodeOwner.getItemStack();
            if (is.getCount() > 0 && nodeOwner.getGridNode() != null) {
                var items = List.of(is.copy());
                nodeOwner.getHost().removePart(nodeOwner.getSide());
                var be = nodeOwner.getHost().getBlockEntity();
                Platform.spawnDrops(be.getLevel(), be.getBlockPos(), items);
                is.setCount(0);
            }
        }

        @Override
        public void onSaveChanges(T nodeOwner, IGridNode node) {
            nodeOwner.getHost().markForSave();
        }

        @Override
        public void onStateChanged(T nodeOwner, IGridNode node, State state) {
            nodeOwner.onMainNodeStateChanged(state);
        }
    }
}
