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
import appeng.api.util.IConfigurableObject;
import appeng.client.render.FacingToRotation;
import appeng.client.render.PartIndicatorLightRenderer;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.util.CustomNameUtil;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AEBasePart implements IPart, IActionHost, ICustomNameObject, ISegmentedInventory {

    private static final Map<Class<?>, IndicatorLightCache> INDICATOR_LIGHT_CACHE = new IdentityHashMap<>();
    private final IManagedGridNode mainNode;
    private final IPartItem<?> partItem;
    private BlockEntity blockEntity = null;
    private IPartHost host = null;
    @Nullable
    private Direction side;
    @Nullable
    private Component customName;
    private IndicatorState lastReportedIndicator = IndicatorState.OFF;
    private final int visualSeed;

    public AEBasePart(IPartItem<?> partItem) {
        this.partItem = Objects.requireNonNull(partItem, "partItem");
        this.mainNode = createMainNode()
                .setVisualRepresentation(AEItemKey.of(this.partItem))
                .setExposedOnSides(EnumSet.noneOf(Direction.class));
        this.visualSeed = ThreadLocalRandom.current().nextInt();
        INDICATOR_LIGHT_CACHE.clear();
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

    public final boolean isClientSide() {
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
        return Objects.requireNonNullElse(customName, TextComponent.EMPTY);
    }

    @Override
    public boolean hasCustomInventoryName() {
        return customName != null;
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

    @Environment(EnvType.CLIENT)
    @Override
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        if (lastReportedIndicator == IndicatorState.OFF) {
            // The OFF state is the default state shown by the baked model
            return;
        }

        var positions = getIndicatorVertexPositions();
        if (positions != null) {
            double animationTime = (getLevel().getGameTime() % Integer.MAX_VALUE) + partialTicks;

            PartIndicatorLightRenderer.renderIndicatorLights(
                    positions,
                    lastReportedIndicator,
                    animationTime,
                    poseStack,
                    buffers
            );
        }
    }

    @Override
    public boolean requireDynamicRender() {
        return getIndicatorVertexPositions() != null;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        this.mainNode.loadFromNBT(data);
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        this.mainNode.saveToNBT(data);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void writeToStream(FriendlyByteBuf data) {
        data.writeByte((byte) lastReportedIndicator.ordinal());
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public boolean readFromStream(FriendlyByteBuf data) {
        lastReportedIndicator = IndicatorState.values()[data.readByte()];
        return false; // Indicator state is rendered using a Block Entity Renderer, no remesh needed
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

        if (this instanceof IConfigurableObject configurableObject) {
            configurableObject.getConfigManager().readFromNBT(input);
        }

        if (this instanceof IPriorityHost pHost) {
            pHost.setPriority(input.getInt("priority"));
        }
    }

    @OverridingMethodsMustInvokeSuper
    public void exportSettings(SettingsFrom mode, CompoundTag output) {
        CustomNameUtil.setCustomName(output, this.customName);

        if (mode == SettingsFrom.MEMORY_CARD) {
            if (this instanceof IConfigurableObject configurableObject) {
                configurableObject.getConfigManager().writeToNBT(output);
            }

            if (this instanceof IPriorityHost pHost) {
                output.putInt("priority", pHost.getPriority());
            }
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
                    memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
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
                nodeOwner.addAdditionalDrops(items, false);
                nodeOwner.getHost().removePart(nodeOwner.getSide());
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
            nodeOwner.updateIndicatorStateForClients();
            nodeOwner.onMainNodeStateChanged(state);
        }
    }

    public int getVisualSeed() {
        return visualSeed;
    }

    protected final void updateIndicatorStateForClients() {
        if (isClientSide()) {
            return;
        }
        if (updateIndicatorState()) {
            getHost().markForUpdate();
        }
    }

    private boolean updateIndicatorState() {
        var state = getIndicatorState();
        if (state != lastReportedIndicator) {
            lastReportedIndicator = state;
            return true;
        }
        return false;
    }

    /**
     * @return The current LED indicator state for this part, if it uses the machine state system to transfer
     * state information to the client to show it on indicator LEDs. This is called server-side.
     */
    public IndicatorState getIndicatorState() {
        if (isClientSide()) {
            return lastReportedIndicator;
        }
        var mainNode = getMainNode();
        var node = mainNode.getNode();
        if (node == null) {
            return IndicatorState.OFF;
        }
        if (node.isActive()) {
            return IndicatorState.ONLINE;
        } else if (node.getGrid().getPathingService().isNetworkBooting()) {
            return IndicatorState.BOOTING;
        } else if (!node.meetsChannelRequirements()) {
            return IndicatorState.MISSING_CHANNEL;
        } else {
            return IndicatorState.OFF;
        }
    }

    /**
     * The vertex positions forming a quad list to show the indicator color for this part.
     * May be null if this part has no indicators.
     */
    @Nullable
    public float[] getIndicatorVertexPositions() {
        var cached = INDICATOR_LIGHT_CACHE.get(getClass());

        if (cached == null) {
            var builder = new IndicatorLightBuilder();
            buildIndicatorLights(builder);
            var northPositions = builder.getVertices();
            if (northPositions.length == 0) {
                cached = new IndicatorLightCache(null);
            } else {
                // Generate the versions for the other sides by rotating the northern version
                var positionsBySide = new EnumMap<Direction, float[]>(Direction.class);
                positionsBySide.put(Direction.NORTH, northPositions);
                var v = new Vector3f();
                for (var side : Direction.values()) {
                    if (side != Direction.NORTH) {
                        var rotatedSide = new float[northPositions.length];
                        positionsBySide.put(side, rotatedSide);

                        var rotation = FacingToRotation.get(side, side.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP).getRot();
                        for (int i = 0; i < northPositions.length; i += 3) {
                            // Move to center before rotating
                            v.set(northPositions[i] - 0.5f,
                                    northPositions[i + 1] - 0.5f,
                                    northPositions[i + 2] - 0.5f);
                            v.transform(rotation);
                            rotatedSide[i] = v.x() + 0.5f;
                            rotatedSide[i + 1] = v.y() + 0.5f;
                            rotatedSide[i + 2] = v.z() + 0.5f;
                        }
                    }
                }
                cached = new IndicatorLightCache(positionsBySide);
            }
            INDICATOR_LIGHT_CACHE.put(getClass(), cached);
        }

        return cached.positions == null ? null : cached.positions.get(getSide());
    }

    /**
     * Override to add indicator lights to this part. The results will be cached. The indicators should be added
     * for a part oriented NORTH (the default orientation), as they'll be automatically rotated for each side.
     */
    protected void buildIndicatorLights(IndicatorLightBuilder builder) {
    }

    private record IndicatorLightCache(@Nullable Map<Direction, float[]> positions) {
    }
}
