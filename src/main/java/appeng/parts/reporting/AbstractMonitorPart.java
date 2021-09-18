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

import java.io.IOException;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.implementations.parts.IStorageMonitorPart;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherNode;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.render.TesrRenderHelper;
import appeng.core.localization.PlayerMessages;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.item.AEItemStack;

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
        implements IStorageMonitorPart, IStackWatcherNode {
    private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;
    private IAEItemStack configuredItem;
    private String lastHumanReadableText;
    private boolean isLocked;
    private IStackWatcher myWatcher;

    public AbstractMonitorPart(final ItemStack is) {
        super(is);

        getMainNode().addService(IStackWatcherNode.class, this);
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);

        this.isLocked = data.getBoolean("isLocked");

        final CompoundTag myItem = data.getCompound("configuredItem");
        this.configuredItem = AEItemStack.fromNBT(myItem);
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);

        data.putBoolean("isLocked", this.isLocked);

        final CompoundTag myItem = new CompoundTag();
        if (this.configuredItem != null) {
            this.configuredItem.writeToNBT(myItem);
        }

        data.put("configuredItem", myItem);
    }

    @Override
    public void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);

        data.writeBoolean(this.isLocked);
        data.writeBoolean(this.configuredItem != null);
        if (this.configuredItem != null) {
            this.configuredItem.writeToPacket(data);
        }
    }

    @Override
    public boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        boolean needRedraw = super.readFromStream(data);

        final boolean isLocked = data.readBoolean();
        needRedraw = this.isLocked != isLocked;

        this.isLocked = isLocked;

        final boolean val = data.readBoolean();
        if (val) {
            this.configuredItem = AEItemStack.fromPacket(data);
        } else {
            this.configuredItem = null;
        }

        return needRedraw;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (isRemote()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }

        if (!this.isLocked) {
            final ItemStack eq = player.getItemInHand(hand);
            this.configuredItem = AEItemStack.fromItemStack(eq);
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
        if (isRemote()) {
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
            player.sendMessage((this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).get(),
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
                this.updateReportingValue(grid.getStorageService()
                        .getInventory(StorageChannels.items()));
            });
        }
    }

    private void updateReportingValue(final IMEMonitor<IAEItemStack> itemInventory) {
        if (this.configuredItem != null) {
            final IAEItemStack result = itemInventory.getStorageList().findPrecise(this.configuredItem);
            if (result == null) {
                this.configuredItem.setStackSize(0);
            } else {
                this.configuredItem.setStackSize(result.getStackSize());
            }
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

        final IAEItemStack ais = this.getDisplayed();

        if (ais == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5); // Move into the center of the block

        Direction facing = this.getSide();

        TesrRenderHelper.rotateToFace(poseStack, facing, this.getSpin());

        poseStack.translate(0, 0.05, 0.5);

        TesrRenderHelper.renderItem2dWithAmount(poseStack, buffers, ais, 0.4f, -0.23f, 15728880, combinedOverlayIn);

        poseStack.popPose();

    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    public IAEItemStack getDisplayed() {
        return this.configuredItem;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public void updateWatcher(final IStackWatcher newWatcher) {
        this.myWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public <T extends IAEStack> void onStackChange(IItemList<T> o, IAEStack fullStack, IAEStack diffStack,
            IActionSource src, IStorageChannel<T> chan) {
        if (this.configuredItem != null) {
            if (fullStack == null) {
                this.configuredItem.setStackSize(0);
            } else {
                this.configuredItem.setStackSize(fullStack.getStackSize());
            }

            final long stackSize = this.configuredItem.getStackSize();
            final String humanReadableText = NUMBER_CONVERTER.toWideReadableForm(stackSize);

            if (!humanReadableText.equals(this.lastHumanReadableText)) {
                this.lastHumanReadableText = humanReadableText;
                this.getHost().markForUpdate();
            }
        }
    }

    @Override
    public boolean showNetworkInfo(final HitResult where) {
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
