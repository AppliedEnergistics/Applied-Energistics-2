package powercrystals.minefactoryreloaded.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface INeedleAmmo
{
	public boolean onHitEntity(ItemStack stac, EntityPlayer owner, Entity hit, double distance);
	public void onHitBlock(ItemStack stac, EntityPlayer owner, World world, int x, int y, int z, int side, double distance);
	public float getSpread(ItemStack stack);
}
