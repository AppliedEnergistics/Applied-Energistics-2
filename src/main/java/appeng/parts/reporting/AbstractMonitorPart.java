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

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.implementations.parts.IStorageMonitorPart;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.storage.IStorageWatcherNode;
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
        implements IStorageMonitorPart, IStorageWatcherNode {
    @Nullable
    private AEKey configuredItem;
    private long amount;
    private String lastHumanReadableText;
    private boolean isLocked;
    private IStackWatcher myWatcher;

    public AbstractMonitorPart(IPartItem<?> partItem, boolean requireChannel) {
        super(partItem, requireChannel);

        getMainNode().addService(IStorageWatcherNode.class, this);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);

        this.isLocked = data.getBoolean("isLocked");

        this.configuredItem = AEKey.fromTagGeneric(data.getCompound("configuredItem"));
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
            this.configuredItem.writeToPacket(data);
            data.writeVarLong(this.amount);
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
            this.configuredItem = AEItemKey.fromPacket(data);
            this.amount = data.readVarLong();
        } else {
            this.configuredItem = null;
            this.amount = 0;
        }

        return needRedraw;
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
            this.configuredItem = AEItemKey.of(eq);
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
            player.sendMessage((this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).text(),
                    Util.NIL_UUID);
            this.getHost().markForSave();
            this.getHost().markForUpdate();
        }

        return true;
    }

    // update the system...
    private void configureWatchers() {
        if (this.myWatcher != null) {
            this.myWatcher.reset();
        }

        if (this.configuredItem != null) {
            if (this.myWatcher != null) {
                this.myWatcher.add(this.configuredItem);
            }

            getMainNode().ifPresent(grid -> {
                this.updateReportingValue(grid.getStorageService());
            });
        }
    }

    private void updateReportingValue(IStorageService storageService) {
        this.lastHumanReadableText = null;
        if (this.configuredItem != null) {
            this.amount = storageService.getCachedInventory().get(this.configuredItem);
        } else {
            this.amount = 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
            int combinedLightIn, int combinedOverlayIn) {

        if ((this.getClientFlags() & (PanelPart.POWERED_FLAG | PanelPart.CHANNEL_FLAG)) != (PanelPart.POWERED_FLAG
                | PanelPart.CHANNEL_FLAG)) {
            return;
        }

        if (configuredItem == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5); // Move into the center of the block

        BlockEntityRenderHelper.rotateToFace(poseStack, getSide(), this.getSpin());

        poseStack.translate(0, 0.05, 0.5);

        BlockEntityRenderHelper.renderItem2dWithAmount(poseStack, buffers, getDisplayed(), amount,
                0.4f, -0.23f, LightTexture.FULL_BRIGHT);

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

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
        getHost().markForUpdate();
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        this.myWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onStackChange(AEKey what, long amount) {
        if (what.equals(this.configuredItem)) {
            this.amount = amount;

            var humanReadableText = what.formatAmount(amount, AmountFormat.PREVIEW_REGULAR);

            // Try throttling to only relevant updates
            if (!humanReadableText.equals(this.lastHumanReadableText)) {
                this.lastHumanReadableText = humanReadableText;
                this.getHost().markForUpdate();
            }
        }
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
