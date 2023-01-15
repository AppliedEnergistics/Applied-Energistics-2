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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.jetbrains.annotations.MustBeInvokedByOverriders;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.items.tools.MemoryCardItem;
import appeng.util.CustomNameUtil;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class AEBasePart
        implements IPart, IActionHost, ISegmentedInventory, IPowerChannelState, Nameable {

    private final IManagedGridNode mainNode;
    private IPartItem<?> partItem;
    private BlockEntity blockEntity = null;
    private IPartHost host = null;
    @Nullable
    private Direction side;
    @Nullable
    private Component customName;

    // On the client-side this is the state last sent by the server.
    // On the server it's the state last sent to the client.
    private boolean clientSidePowered;
    private boolean clientSideMissingChannel;

    public AEBasePart(IPartItem<?> partItem) {
        this.partItem = Objects.requireNonNull(partItem, "partItem");
        this.mainNode = createMainNode().setVisualRepresentation(AEItemKey.of(this.partItem))
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
    @MustBeInvokedByOverriders
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        // Client flags shouldn't depend on grid boot, optimize!
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            markForUpdateIfClientFlagsChanged();
        }
    }

    public final boolean isClientSide() {
        return this.blockEntity == null || this.blockEntity.getLevel() == null
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
    public Component getName() {
        return Objects.requireNonNullElse(this.customName, partItem.asItem().getDescription());
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    @Override
    public void addEntityCrashInfo(CrashReportCategory crashreportcategory) {
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
    public IPartItem<?> getPartItem() {
        return this.partItem;
    }

    /**
     * Advanced method. Take care to properly update any grid related state and update the host after changing the part
     * item.
     */
    protected void setPartItem(IPartItem<?> partItem) {
        if (partItem != this.partItem) {
            this.partItem = Objects.requireNonNull(partItem);
            this.getMainNode().setVisualRepresentation(partItem);
        }
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        this.mainNode.loadFromNBT(data);

        if (data.contains("customName")) {
            try {
                this.customName = Component.Serializer.fromJson(data.getString("customName"));
            } catch (Exception ignored) {
            }
        }

        if (data.contains("visual", Tag.TAG_COMPOUND)) {
            readVisualStateFromNBT(data.getCompound("visual"));
        }
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        this.mainNode.saveToNBT(data);

        if (this.customName != null) {
            data.putString("customName", Component.Serializer.toJson(this.customName));
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void writeToStream(FriendlyByteBuf data) {
        this.clientSidePowered = this.isPowered();
        this.clientSideMissingChannel = this.isMissingChannel();

        var flags = 0;
        if (clientSidePowered) {
            flags |= 1;
        }
        if (clientSideMissingChannel) {
            flags |= 2;
        }
        data.writeByte(flags);
    }

    @MustBeInvokedByOverriders
    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        var flags = data.readByte();

        var wasPowered = this.clientSidePowered;
        var wasMissingChannel = this.clientSideMissingChannel;

        this.clientSidePowered = (flags & 1) != 0;
        this.clientSideMissingChannel = (flags & 2) != 0;

        return shouldSendPowerStateToClient() && clientSidePowered != wasPowered
                || shouldSendMissingChannelStateToClient() && clientSideMissingChannel != wasMissingChannel;
    }

    /**
     * Used to store the state that is synchronized to clients for the visual appearance of this part as NBT. This is
     * only used to store this state for tools such as Create Ponders in Structure NBT. Actual synchronization uses
     * {@link #writeToStream(FriendlyByteBuf)} and {@link #readFromStream(FriendlyByteBuf)}. Any data that is saved to
     * the NBT tag in {@link #writeToNBT(CompoundTag)} already does not need to be saved here again.
     */
    @MustBeInvokedByOverriders
    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        data.putBoolean("powered", this.isPowered());
        data.putBoolean("missingChannel", this.isMissingChannel());
    }

    /**
     * Loads data saved by {@link #writeVisualStateToNBT(CompoundTag)}.
     */
    @MustBeInvokedByOverriders
    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        this.clientSidePowered = data.getBoolean("powered");
        this.clientSideMissingChannel = data.getBoolean("missingChannel");
    }

    @Override
    public IGridNode getGridNode() {
        return this.mainNode.getNode();
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
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        this.setSide(side);
        this.blockEntity = blockEntity;
        this.host = host;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 3;
    }

    /**
     * depending on the from, different settings will be accepted
     *
     * @param mode   source of settings
     * @param input  compound of source
     * @param player the optional player who is importing the settings
     */
    @OverridingMethodsMustInvokeSuper
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        this.customName = CustomNameUtil.getCustomName(input);

        MemoryCardItem.importGenericSettings(this, input, player);
    }

    @OverridingMethodsMustInvokeSuper
    public void exportSettings(SettingsFrom mode, CompoundTag output) {
        CustomNameUtil.setCustomName(output, this.customName);

        if (mode == SettingsFrom.MEMORY_CARD) {
            MemoryCardItem.exportGenericSettings(this, output);
        }
    }

    public boolean useStandardMemoryCard() {
        return true;
    }

    private boolean useMemoryCard(Player player) {
        final ItemStack memCardIS = player.getInventory().getSelected();

        if (!memCardIS.isEmpty() && this.useStandardMemoryCard()
                && memCardIS.getItem() instanceof IMemoryCard memoryCard) {

            Item partItem = getPartItem().asItem();

            // Blocks and parts share the same soul!
            if (AEParts.INTERFACE.asItem() == partItem) {
                partItem = AEBlocks.INTERFACE.asItem();
            } else if (AEParts.PATTERN_PROVIDER.asItem() == partItem) {
                partItem = AEBlocks.PATTERN_PROVIDER.asItem();
            }

            var name = partItem.getDescriptionId();

            if (InteractionUtil.isInAlternateUseMode(player)) {
                var data = new CompoundTag();
                exportSettings(SettingsFrom.MEMORY_CARD, data);
                if (!data.isEmpty()) {
                    memoryCard.setMemoryCardContents(memCardIS, name, data);
                    memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                }
            } else {
                var storedName = memoryCard.getSettingsName(memCardIS);
                var data = memoryCard.getData(memCardIS);
                if (name.equals(storedName)) {
                    importSettings(SettingsFrom.MEMORY_CARD, data, player);
                    memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                } else {
                    MemoryCardItem.importGenericSettingsAndNotify(this, data, player);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean onActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (this.useMemoryCard(player)) {
            return true;
        }

        return this.onPartActivate(player, hand, pos);
    }

    @Override
    public final boolean onShiftActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (this.useMemoryCard(player)) {
            return true;
        }

        return this.onPartShiftActivate(player, hand, pos);
    }

    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        return false;
    }

    public boolean onPartShiftActivate(Player player, InteractionHand hand, Vec3 pos) {
        return false;
    }

    @Override
    public void onPlacement(Player player) {
        this.mainNode.setOwningPlayer(player);
    }

    public Direction getSide() {
        return this.side;
    }

    private void setSide(Direction side) {
        this.side = side;
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
            // Only drop items if the part is still attached at that side
            if (nodeOwner.getHost().getPart(nodeOwner.getSide()) == nodeOwner) {
                var items = new ArrayList<ItemStack>();
                nodeOwner.addPartDrop(items, false);
                nodeOwner.addAdditionalDrops(items, false, false);
                nodeOwner.getHost().removePartFromSide(nodeOwner.getSide());
                if (!items.isEmpty()) {
                    var be = nodeOwner.getHost().getBlockEntity();
                    Platform.spawnDrops(be.getLevel(), be.getBlockPos(), items);
                }
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

    public boolean isPowered() {
        if (isClientSide()) {
            return clientSidePowered;
        } else {
            var node = getGridNode();
            return node != null && node.isPowered();
        }
    }

    public boolean isMissingChannel() {
        if (isClientSide()) {
            return clientSideMissingChannel;
        } else {
            var node = getGridNode();
            return node == null || !node.meetsChannelRequirements();
        }
    }

    @Override
    public boolean isActive() {
        return isPowered() && !isMissingChannel();
    }

    /**
     * Updates the entire part on the client-side if one of the flags relevant for its visual appearance has changed.
     */
    private void markForUpdateIfClientFlagsChanged() {
        var changed = false;

        if (shouldSendPowerStateToClient()) {
            if (isPowered() != this.clientSidePowered) {
                changed = true;
            }
        }

        if (!changed && shouldSendMissingChannelStateToClient()) {
            if (isMissingChannel() != this.clientSideMissingChannel) {
                changed = true;
            }
        }

        if (changed) {
            getHost().markForUpdate();
        }
    }

    /**
     * Override and return false if your part has no visual indicator for the power state and doesn't need this info on
     * the client.
     */
    protected boolean shouldSendPowerStateToClient() {
        return true;
    }

    /**
     * Override and return false if your part has no visual indicator for the missing channel state and doesn't need
     * this info on the client.
     */
    protected boolean shouldSendMissingChannelStateToClient() {
        return true;
    }
}
