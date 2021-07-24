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

import com.google.common.base.Preconditions;

import net.minecraft.CrashReportCategory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
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
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.core.Api;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.parts.FluidLevelEmitterPart;
import appeng.fluids.util.AEFluidInventory;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.parts.automation.LevelEmitterPart;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class AEBasePart implements IPart, IActionHost, IUpgradeableHost, ICustomNameObject {

    private final IManagedGridNode mainNode;
    private final ItemStack is;
    private BlockEntity tile = null;
    private IPartHost host = null;
    private AEPartLocation side = null;

    public AEBasePart(final ItemStack is) {
        Preconditions.checkNotNull(is);

        this.is = is;
        this.mainNode = createMainNode()
                .setVisualRepresentation(is)
                .setExposedOnSides(EnumSet.noneOf(Direction.class));
    }

    protected IManagedGridNode createMainNode() {
        return Api.instance().grid().createManagedNode(this, NodeListener.INSTANCE);
    }

    /**
     * Called if one of the properties that result in a node becoming active or inactive change.
     *
     * @param reason Indicates which of the properties has changed.
     */
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
    }

    public final boolean isRemote() {
        return this.tile == null
                || this.tile.getLevel() == null
                || this.tile.getLevel().isClientSide();
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

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return 0;
    }

    @Override
    public BlockEntity getTile() {
        return this.tile;
    }

    public IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public IGridNode getActionableNode() {
        return this.mainNode.getNode();
    }

    public Level getWorld() {
        return this.tile.getLevel();
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
    public void onNeighborChanged(BlockGetter w, BlockPos pos, BlockPos neighbor) {

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
        this.mainNode.create(getWorld(), getTile().getBlockPos());
    }

    @Override
    public void setPartHostInfo(final AEPartLocation side, final IPartHost host, final BlockEntity tile) {
        this.setSide(side);
        this.tile = tile;
        this.host = host;
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final Level world, final BlockPos pos, final Random r) {

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

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        return null;
    }

    /**
     * depending on the from, different settings will be accepted, don't call this with null
     *
     * @param from     source of settings
     * @param compound compound of source
     */
    private void uploadSettings(final SettingsFrom from, final CompoundTag compound) {
        final IConfigManager cm = this.getConfigManager();
        if (cm != null) {
            cm.readFromNBT(compound);
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            pHost.setPriority(compound.getInt("priority"));
        }

        final IItemHandler inv = this.getInventoryByName("config");
        if (inv instanceof AppEngInternalAEInventory) {
            final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
            final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory(null, target.getSlots());
            tmp.readFromNBT(compound, "config");
            for (int x = 0; x < tmp.getSlots(); x++) {
                target.setStackInSlot(x, tmp.getStackInSlot(x));
            }
            if (this instanceof LevelEmitterPart) {
                final LevelEmitterPart partLevelEmitter = (LevelEmitterPart) this;
                partLevelEmitter.setReportingValue(compound.getLong("reportingValue"));
            }
        }

        if (this instanceof IConfigurableFluidInventory) {
            final IFluidHandler tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
            if (tank instanceof AEFluidInventory) {
                final AEFluidInventory target = (AEFluidInventory) tank;
                final AEFluidInventory tmp = new AEFluidInventory(null, target.getSlots());
                tmp.readFromNBT(compound, "config");
                for (int x = 0; x < tmp.getSlots(); x++) {
                    target.setFluidInSlot(x, tmp.getFluidInSlot(x));
                }
            }
            if (this instanceof FluidLevelEmitterPart) {
                final FluidLevelEmitterPart fluidLevelEmitterPart = (FluidLevelEmitterPart) this;
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

        final IConfigManager cm = this.getConfigManager();
        if (cm != null) {
            cm.writeToNBT(output);
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            output.putInt("priority", pHost.getPriority());
        }

        final IItemHandler inv = this.getInventoryByName("config");
        if (inv instanceof AppEngInternalAEInventory) {
            ((AppEngInternalAEInventory) inv).writeToNBT(output, "config");
            if (this instanceof LevelEmitterPart) {
                final LevelEmitterPart partLevelEmitter = (LevelEmitterPart) this;
                output.putLong("reportingValue", partLevelEmitter.getReportingValue());
            }
        }

        if (this instanceof IConfigurableFluidInventory) {
            final IFluidHandler tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
            ((AEFluidInventory) tank).writeToNBT(output, "config");
            if (this instanceof FluidLevelEmitterPart) {
                final FluidLevelEmitterPart fluidLevelEmitterPart = (FluidLevelEmitterPart) this;
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

        if (!memCardIS.isEmpty() && this.useStandardMemoryCard() && memCardIS.getItem() instanceof IMemoryCard) {
            final IMemoryCard memoryCard = (IMemoryCard) memCardIS.getItem();

            ItemStack is = this.getItemStack(PartItemStack.NETWORK);

            // Blocks and parts share the same soul!
            if (AEParts.INTERFACE.isSameAs(is)) {
                is = AEBlocks.INTERFACE.stack();
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
                            final AEPartLocation side) {
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

    public AEPartLocation getSide() {
        return this.side;
    }

    private void setSide(final AEPartLocation side) {
        this.side = side;
    }

    public ItemStack getItemStack() {
        return this.is;
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
                nodeOwner.getHost().removePart(nodeOwner.getSide(), false);
                var tile = nodeOwner.getTile();
                Platform.spawnDrops(tile.getLevel(), tile.getBlockPos(), items);
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
