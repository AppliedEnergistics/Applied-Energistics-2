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
import java.util.Map;
import java.util.Objects;

import appeng.parts.automation.PartModelData;
import com.google.gson.stream.JsonWriter;

import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

import it.unimi.dsi.fastutil.objects.Reference2IntMap;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridNodeListener.State;
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
import appeng.util.IDebugExportable;
import appeng.util.InteractionUtil;
import appeng.util.JsonStreamUtil;
import appeng.util.SettingsFrom;

public abstract class AEBasePart
        implements IPart, IActionHost, ISegmentedInventory, IPowerChannelState, Nameable, IDebugExportable {

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
    protected void onMainNodeStateChanged(State reason) {
        // Client flags shouldn't depend on grid boot, optimize!
        if (reason != State.GRID_BOOT) {
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

    public AEColor getColor() {
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
        return Objects.requireNonNullElse(this.customName, partItem.asItem().getName());
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
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.mainNode.loadFromNBT(data);

        if (data.contains("customName")) {
            try {
                this.customName = Component.Serializer.fromJson(data.getStringOr("customName", ""), registries);
            } catch (Exception ignored) {
            }
        }

        data.getCompound("visual").ifPresent(this::readVisualStateFromNBT);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.mainNode.saveToNBT(data);

        if (this.customName != null) {
            data.putString("customName", Component.Serializer.toJson(this.customName, registries));
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
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
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
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
     * {@link IPart#writeToStream(RegistryFriendlyByteBuf)} and {@link IPart#readFromStream(RegistryFriendlyByteBuf)}.
     * Any data that is saved to the NBT tag in {@link IPart#writeToNBT(CompoundTag, HolderLookup.Provider)} already
     * does not need to be saved here again.
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
        this.clientSidePowered = data.getBooleanOr("powered", false);
        this.clientSideMissingChannel = data.getBooleanOr("missingChannel", false);
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
    @Override
    @MustBeInvokedByOverriders
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            this.customName = input.get(DataComponents.CUSTOM_NAME);
        } else if (mode == SettingsFrom.MEMORY_CARD) {
            this.customName = input.get(AEComponents.EXPORTED_CUSTOM_NAME);
        }

        MemoryCardItem.importGenericSettings(this, input, player);
    }

    @Override
    @MustBeInvokedByOverriders
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            builder.set(DataComponents.CUSTOM_NAME, this.customName);
        } else if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(AEComponents.EXPORTED_CUSTOM_NAME, this.customName);
        }

        if (mode == SettingsFrom.MEMORY_CARD) {
            MemoryCardItem.exportGenericSettings(this, builder);
            builder.set(AEComponents.EXPORTED_SETTINGS_SOURCE, getPartItem().asItem().getName());
        }
    }

    public final DataComponentMap exportSettings(SettingsFrom mode) {
        var builder = DataComponentMap.builder();
        exportSettings(mode, builder);
        return builder.build();
    }

    public boolean useStandardMemoryCard() {
        return true;
    }

    private boolean useMemoryCard(ItemStack memCardIS, Player player) {
        if (!this.useStandardMemoryCard() || !(memCardIS.getItem() instanceof IMemoryCard memoryCard)) {
            return false;
        }

        Item partItem = getPartItem().asItem();

        // Blocks and parts share the same soul!
        if (AEParts.INTERFACE.asItem() == partItem) {
            partItem = AEBlocks.INTERFACE.asItem();
        } else if (AEParts.PATTERN_PROVIDER.asItem() == partItem) {
            partItem = AEBlocks.PATTERN_PROVIDER.asItem();
        }

        var name = partItem.getName();

        if (InteractionUtil.isInAlternateUseMode(player)) {
            var settings = exportSettings(SettingsFrom.MEMORY_CARD);
            if (!settings.isEmpty()) {
                MemoryCardItem.clearCard(memCardIS);
                memCardIS.applyComponents(settings);
                memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
            }
        } else {
            var storedName = memCardIS.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
            if (name.equals(storedName)) {
                importSettings(SettingsFrom.MEMORY_CARD, memCardIS.getComponents(), player);
                memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
            } else {
                MemoryCardItem.importGenericSettingsAndNotify(this, memCardIS.getComponents(), player);
            }
        }
        return true;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (useMemoryCard(heldItem, player)) {
            return true;
        }
        return IPart.super.onUseItemOn(heldItem, player, hand, pos);
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
    @MustBeInvokedByOverriders
    public InternalInventory getSubInventory(ResourceLocation id) {
        return null;
    }

    /**
     * Simple {@link IGridNodeListener} for {@link AEBasePart} that host nodes.
     */
    public static class NodeListener<T extends AEBasePart> implements IGridNodeListener<T> {

        public static final NodeListener<AEBasePart> INSTANCE = new NodeListener<>();

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

    @Nullable
    @Override
    public void collectModelData(ModelData.Builder builder) {
        PartModelData.StatusIndicatorState state;
        if (isActive() && isPowered()) {
            state = PartModelData.StatusIndicatorState.ACTIVE;
        } else if (isPowered()) {
            state = PartModelData.StatusIndicatorState.POWERED;
        } else {
            state = PartModelData.StatusIndicatorState.UNPOWERED;
        }

        builder.with(PartModelData.STATUS_INDICATOR, state);
    }

    @Override
    public void debugExport(JsonWriter writer, HolderLookup.Provider registries, Reference2IntMap<Object> machineIds,
            Reference2IntMap<IGridNode> nodeIds)
            throws IOException {
        var myId = machineIds.getOrDefault(this, -1);
        JsonStreamUtil.writeProperties(Map.of(
                "id", myId,
                "item", BuiltInRegistries.ITEM.getKey(getPartItem().asItem()).toString(),
                "mainNodeId", nodeIds.getOrDefault(mainNode.getNode(), -1)), writer);
    }
}
