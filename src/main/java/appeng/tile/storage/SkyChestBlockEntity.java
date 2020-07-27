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

package appeng.tile.storage;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.block.storage.SkyChestBlock;
import appeng.tile.AEBaseInvBlockEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;

public class SkyChestBlockEntity extends AEBaseInvBlockEntity implements Tickable, ChestAnimationProgress {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9 * 4);

    // server
    private int numPlayersUsing;
    // client..
    private long lastEvent;
    private float lidAngle;
    private float prevLidAngle;

    public SkyChestBlockEntity(BlockEntityType<? extends SkyChestBlockEntity> type) {
        super(type);
    }

    @Override
    protected void writeToStream(final PacketByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.getPlayerOpen() > 0);
    }

    @Override
    protected boolean readFromStream(final PacketByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int wasOpen = this.getPlayerOpen();
        this.setPlayerOpen(data.readBoolean() ? 1 : 0);

        if (wasOpen != this.getPlayerOpen()) {
            this.setLastEvent(System.currentTimeMillis());
        }

        return c; // TESR yo!
    }

    @Override
    public FixedItemInv getInternalInventory() {
        return this.inv;
    }

    public void openInventory(final PlayerEntity player) {
        if (!player.isSpectator()) {
            this.setPlayerOpen(this.getPlayerOpen() + 1);
            onOpenOrClose();

            if (this.getPlayerOpen() == 1) {
                this.getWorld().playSound(player, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
                        this.pos.getZ() + 0.5D, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F,
                        this.getWorld().random.nextFloat() * 0.1F + 0.9F);
                this.markForUpdate();
            }
        }
    }

    public void closeInventory(final PlayerEntity player) {
        if (!player.isSpectator()) {
            this.setPlayerOpen(this.getPlayerOpen() - 1);
            onOpenOrClose();

            if (this.getPlayerOpen() < 0) {
                this.setPlayerOpen(0);
            }

            if (this.getPlayerOpen() == 0) {
                this.getWorld().playSound(player, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
                        this.pos.getZ() + 0.5D, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F,
                        this.getWorld().random.nextFloat() * 0.1F + 0.9F);
                this.markForUpdate();
            }
        }
    }

    // See ChestTileEntity
    private void onOpenOrClose() {
        Block block = getCachedState().getBlock();
        if (block instanceof SkyChestBlock) {
            this.world.addSyncedBlockEvent(this.pos, block, 1, this.numPlayersUsing);
            this.world.updateNeighborsAlways(this.pos, block);
            // FIXME: Uhm, we are we doing this?
            this.world.updateNeighborsAlways(this.pos.down(), block);
        }
    }

    @Override
    public void tick() {
        this.prevLidAngle = this.lidAngle;
        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            this.lidAngle = MathHelper.clamp(this.lidAngle, 0, 1);
        }
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {

    }

    public int getPlayerOpen() {
        return this.numPlayersUsing;
    }

    private void setPlayerOpen(final int playerOpen) {
        this.numPlayersUsing = playerOpen;
    }

    public long getLastEvent() {
        return this.lastEvent;
    }

    private void setLastEvent(final long lastEvent) {
        this.lastEvent = lastEvent;
    }

    @Override
    public float getAnimationProgress(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

}
