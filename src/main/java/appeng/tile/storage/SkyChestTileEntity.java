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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.block.storage.SkyChestBlock;
import appeng.tile.AEBaseInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;

@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class SkyChestTileEntity extends AEBaseInvTileEntity implements ITickableTileEntity, IChestLid {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9 * 4);

    // server
    private int numPlayersUsing;
    // client..
    private long lastEvent;
    private float lidAngle;
    private float prevLidAngle;

    public SkyChestTileEntity(TileEntityType<? extends SkyChestTileEntity> type) {
        super(type);
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.numPlayersUsing > 0);
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int wasOpen = this.numPlayersUsing;
        this.numPlayersUsing = data.readBoolean() ? 1 : 0;

        if (wasOpen != this.numPlayersUsing) {
            this.setLastEvent(System.currentTimeMillis());
        }

        return c; // TESR yo!
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    public void openInventory(final PlayerEntity player) {
        // Ignore calls to this function on the client, since the server is responsible for
        // calculating the numPlayersUsing count.
        if (!player.isSpectator() && !player.getEntityWorld().isRemote()) {
            this.numPlayersUsing++;
            onOpenOrClose();
        }
    }

    public void closeInventory(final PlayerEntity player) {
        // Ignore calls to this function on the client, since the server is responsible for
        // calculating the numPlayersUsing count.
        if (!player.isSpectator() && !player.getEntityWorld().isRemote()) {
            this.numPlayersUsing = Math.max(this.numPlayersUsing - 1, 0);
            onOpenOrClose();
        }
    }

    // See ChestTileEntity
    private void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof SkyChestBlock) {
            this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, block);
            // FIXME: Uhm, we are we doing this?
            // this.world.notifyNeighborsOfStateChange(this.pos.down(), block);
        }
    }

    @Override
    public void tick() {
        this.prevLidAngle = this.lidAngle;
        // Play a sound on initial opening.
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            this.playSound(SoundEvents.BLOCK_CHEST_OPEN);
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F) {
            this.lidAngle = Math.max(this.lidAngle - 0.1F, 0);
            // Play a sound on the way down.
            if (this.lidAngle < 0.5F && this.prevLidAngle >= 0.5F) {
                this.playSound(SoundEvents.BLOCK_CHEST_CLOSE);
            }
        } else if (this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            this.lidAngle = Math.min(this.lidAngle + 0.1F, 1);
        }
    }

    private void playSound(SoundEvent soundIn) {
        double d0 = (double) this.pos.getX() + 0.5D;
        double d1 = (double) this.pos.getY() + 0.5D;
        double d2 = (double) this.pos.getZ() + 0.5D;
        this.world.playSound((PlayerEntity) null, d0, d1, d2, soundIn, SoundCategory.BLOCKS, 0.5F,
                this.world.rand.nextFloat() * 0.1F + 0.9F);
    }

    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {

    }

    public long getLastEvent() {
        return this.lastEvent;
    }

    private void setLastEvent(final long lastEvent) {
        this.lastEvent = lastEvent;
    }

    @Override
    public float getLidAngle(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

}
