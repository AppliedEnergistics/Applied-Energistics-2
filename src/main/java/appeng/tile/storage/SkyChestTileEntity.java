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

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.block.storage.SkyChestBlock;
import appeng.tile.AEBaseInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;

@OnlyIn(value = Dist.CLIENT, _interface = LidBlockEntity.class)
public class SkyChestTileEntity extends AEBaseInvTileEntity implements TickableBlockEntity, LidBlockEntity {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9 * 4);

    private int numPlayersUsing;
    private float lidAngle;
    private float prevLidAngle;

    public SkyChestTileEntity(net.minecraft.world.level.block.entity.BlockEntityType<? extends SkyChestTileEntity> type) {
        super(type);
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.numPlayersUsing > 0);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int wasOpen = this.numPlayersUsing;
        this.numPlayersUsing = data.readBoolean() ? 1 : 0;

        return wasOpen != numPlayersUsing || c;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    public void openInventory(final Player player) {
        // Ignore calls to this function on the client, since the server is responsible for
        // calculating the numPlayersUsing count.
        if (!player.isSpectator() && !isRemote()) {
            this.numPlayersUsing++;
            onOpenOrClose();
        }
    }

    public void closeInventory(final Player player) {
        // Ignore calls to this function on the client, since the server is responsible for
        // calculating the numPlayersUsing count.
        if (!player.isSpectator() && !isRemote()) {
            this.numPlayersUsing = Math.max(this.numPlayersUsing - 1, 0);
            onOpenOrClose();
        }
    }

    // See ChestTileEntity
    private void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof SkyChestBlock) {
            this.level.blockEvent(this.worldPosition, block, 1, this.numPlayersUsing);
            this.level.updateNeighborsAt(this.worldPosition, block);
        }
    }

    @Override
    public void tick() {
        this.prevLidAngle = this.lidAngle;
        // Play a sound on initial opening.
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0f) {
            this.playSound(net.minecraft.sounds.SoundEvents.CHEST_OPEN);
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0f) {
            this.lidAngle = Math.max(this.lidAngle - 0.1f, 0);
            // Play a sound on the way down.
            if (this.lidAngle < 0.5f && this.prevLidAngle >= 0.5f) {
                this.playSound(SoundEvents.CHEST_CLOSE);
            }
        } else if (this.numPlayersUsing > 0 && this.lidAngle < 1.0f) {
            this.lidAngle = Math.min(this.lidAngle + 0.1f, 1);
        }
    }

    private void playSound(SoundEvent soundIn) {
        double d0 = this.worldPosition.getX() + 0.5d;
        double d1 = this.worldPosition.getY() + 0.5d;
        double d2 = this.worldPosition.getZ() + 0.5d;
        this.level.playSound(null, d0, d1, d2, soundIn, SoundSource.BLOCKS, 0.5f,
                this.level.random.nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final net.minecraft.world.item.ItemStack added) {

    }

    @Override
    public float getOpenNess(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

}
