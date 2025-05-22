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

package appeng.parts.reporting;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.implementations.parts.IStorageMonitorPart;
import appeng.api.networking.IGrid;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.core.localization.PlayerMessages;
import appeng.util.Platform;

/**
 * A basic subclass for any item monitor like display with an item icon and an amount.
 * <p>
 * It can also be used to extract items from somewhere and spawned into the level.
 *
 * @author AlgorithmX2
 * @author thatsIch
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractMonitorPart extends AbstractDisplayPart
        implements IStorageMonitorPart {
    @Nullable
    private AEKey configuredItem;
    private long amount;
    private boolean canCraft;
    private String lastHumanReadableText;
    private boolean isLocked;
    private IStackWatcher storageWatcher;
    private IStackWatcher craftingWatcher;

    public AbstractMonitorPart(IPartItem<?> partItem, boolean requireChannel) {
        super(partItem, requireChannel);

        getMainNode().addService(IStorageWatcherNode.class, new IStorageWatcherNode() {
            @Override
            public void updateWatcher(IStackWatcher newWatcher) {
                storageWatcher = newWatcher;
                configureWatchers();
            }

            @Override
            public void onStackChange(AEKey what, long amount) {
                if (what.equals(configuredItem)) {
                    AbstractMonitorPart.this.amount = amount;

                    var humanReadableText = amount == 0 && canCraft ? "Craft"
                            : what.formatAmount(amount, AmountFormat.SLOT);

                    // Try throttling to only relevant updates
                    if (!humanReadableText.equals(lastHumanReadableText)) {
                        lastHumanReadableText = humanReadableText;
                        getHost().markForUpdate();
                    }
                }
            }
        });

        getMainNode().addService(ICraftingWatcherNode.class, new ICraftingWatcherNode() {
            @Override
            public void updateWatcher(IStackWatcher newWatcher) {
                craftingWatcher = newWatcher;
                configureWatchers();
            }

            @Override
            public void onRequestChange(AEKey what) {
            }

            @Override
            public void onCraftableChange(AEKey what) {
                getMainNode().ifPresent(AbstractMonitorPart.this::updateReportingValue);
            }
        });
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);

        this.isLocked = data.getBoolean("isLocked");

        if (data.contains("configuredItem", Tag.TAG_COMPOUND)) {
            this.configuredItem = AEKey.fromTagGeneric(data.getCompound("configuredItem"));
        } else {
            this.configuredItem = null;
        }
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);

        data.putBoolean("isLocked", this.isLocked);

        if (this.configuredItem != null) {
            data.put("configuredItem", this.configuredItem.toTagGeneric());
        }
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        data.writeBoolean(this.isLocked);
        data.writeBoolean(this.configuredItem != null);
        if (this.configuredItem != null) {
            AEKey.writeKey(data, this.configuredItem);
            data.writeVarLong(this.amount);
            data.writeBoolean(this.canCraft);
        }
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        boolean needRedraw = super.readFromStream(data);

        var isLocked = data.readBoolean();
        needRedraw |= this.isLocked != isLocked;

        this.isLocked = isLocked;

        // This item is rendered dynamically and doesn't need to trigger a chunk update
        if (data.readBoolean()) {
            this.configuredItem = AEKey.readKey(data);
            this.amount = data.readVarLong();
            this.canCraft = data.readBoolean();
        } else {
            this.configuredItem = null;
            this.amount = 0;
            this.canCraft = false;
        }

        return needRedraw;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        data.putLong("amount", this.amount);
        data.putBoolean("canCraft", this.canCraft);
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        this.amount = data.getLong("amount");
        this.canCraft = data.getBoolean("canCraft");
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (isClientSide()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }

        if (!this.isLocked) {
            var eq = player.getItemInHand(hand);
            if (AEItemKey.matches(this.configuredItem, eq)) {
                // Already matches: try to swap to key contained in the item.
                var containedStack = ContainerItemStrategies.getContainedStack(eq);
                if (containedStack != null) {
                    this.configuredItem = containedStack.what();
                }
            } else {
                this.configuredItem = AEItemKey.of(eq);
            }
            this.configureWatchers();
            this.getHost().markForSave();
            this.getHost().markForUpdate();
        } else {
            return super.onPartActivate(player, hand, pos);
        }

        return true;
    }

    @Override
    public boolean onPartShiftActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (isClientSide()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }

        if (player.getItemInHand(hand).isEmpty()) {
            this.isLocked = !this.isLocked;
            player.displayClientMessage(
                    (this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).text(), true);
            this.getHost().markForSave();
            this.getHost().markForUpdate();
        }

        return true;
    }

    // update the system...
    protected void configureWatchers() {
        if (this.storageWatcher != null) {
            this.storageWatcher.reset();
        }

        if (this.craftingWatcher != null) {
            this.craftingWatcher.reset();
        }

        if (this.configuredItem != null) {
            if (this.storageWatcher != null) {
                this.storageWatcher.add(this.configuredItem);
            }

            if (this.craftingWatcher != null) {
                this.craftingWatcher.add(this.configuredItem);
            }

            getMainNode().ifPresent(this::updateReportingValue);
        }
    }

    protected void updateReportingValue(IGrid grid) {
        this.lastHumanReadableText = null;
        if (this.configuredItem != null) {
            this.amount = grid.getStorageService().getCachedInventory().get(this.configuredItem);
            this.canCraft = grid.getCraftingService().isCraftable(this.configuredItem);
        } else {
            this.amount = 0;
            this.canCraft = false;
        }
        getHost().markForUpdate();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
            int combinedLightIn, int combinedOverlayIn) {

        if (!isActive()) {
            return;
        }

        if (configuredItem == null) {
            return;
        }

        poseStack.pushPose();

        var orientation = BlockOrientation.get(getSide(), getSpin());

        poseStack.translate(0.5, 0.5, 0.5); // Move into the center of the block
        BlockEntityRenderHelper.rotateToFace(poseStack, orientation);
        poseStack.translate(0, 0.05, 0.5);

        BlockEntityRenderHelper.renderItem2dWithAmount(poseStack, buffers, getDisplayed(), amount, canCraft,
                0.4f, -0.23f, getColor().contrastTextColor, getLevel());

        poseStack.popPose();

    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Nullable
    @Override
    public AEKey getDisplayed() {
        return this.configuredItem;
    }

    public void setConfiguredItem(@Nullable AEKey configuredItem) {
        this.configuredItem = configuredItem;
        getHost().markForUpdate();
    }

    @Override
    public long getAmount() {
        return amount;
    }

    public boolean canCraft() {
        return canCraft;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
        getHost().markForUpdate();
    }

    @Override
    public boolean showNetworkInfo(UseOnContext context) {
        return false;
    }

    protected IPartModel selectModel(IPartModel off, IPartModel on, IPartModel hasChannel, IPartModel lockedOff,
            IPartModel lockedOn, IPartModel lockedHasChannel) {
        if (this.isActive()) {
            if (this.isLocked()) {
                return lockedHasChannel;
            } else {
                return hasChannel;
            }
        } else if (this.isPowered()) {
            if (this.isLocked()) {
                return lockedOn;
            } else {
                return on;
            }
        } else if (this.isLocked()) {
            return lockedOff;
        } else {
            return off;
        }
    }

}
