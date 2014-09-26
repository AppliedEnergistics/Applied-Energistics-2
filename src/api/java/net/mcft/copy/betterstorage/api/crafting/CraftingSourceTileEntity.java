package net.mcft.copy.betterstorage.api.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CraftingSourceTileEntity implements ICraftingSource {
	
	public final TileEntity entity;
	public final EntityPlayer player;
	public final World world;
	
	public CraftingSourceTileEntity(TileEntity entity, EntityPlayer player) {
		this.entity = entity;
		this.player = player;
		this.world = ((entity != null) ? entity.getWorldObj() : ((player != null) ? player.worldObj : null));
	}
	
	@Override
	public EntityPlayer getPlayer() { return player; }
	
	@Override
	public World getWorld() { return world; }
	
	@Override
	public double getX() { return ((entity != null) ? (entity.xCoord + 0.5) : 0); }
	@Override
	public double getY() { return ((entity != null) ? (entity.yCoord + 0.5) : 0); }
	@Override
	public double getZ() { return ((entity != null) ? (entity.zCoord + 0.5) : 0); }
	
}
