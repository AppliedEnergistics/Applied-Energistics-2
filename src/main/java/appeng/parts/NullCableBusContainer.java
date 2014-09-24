package appeng.parts;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;

public class NullCableBusContainer implements ICableBusContainer
{

	@Override
	public int isProvidingStrongPower(ForgeDirection opposite)
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower(ForgeDirection opposite)
	{
		return 0;
	}

	@Override
	public boolean canConnectRedstone(EnumSet<ForgeDirection> of)
	{
		return false;
	}

	@Override
	public void onEntityCollision(Entity e)
	{

	}

	@Override
	public boolean activate(EntityPlayer player, Vec3 vecFromPool)
	{
		return false;
	}

	@Override
	public void onNeighborChanged()
	{

	}

	@Override
	public boolean isSolidOnSide(ForgeDirection side)
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public SelectedPart selectPart(Vec3 v3)
	{
		return new SelectedPart();
	}

	@Override
	public boolean recolourBlock(ForgeDirection side, AEColor colour, EntityPlayer who)
	{
		return false;
	}

	@Override
	public boolean isLadder(EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{

	}

	@Override
	public int getLightValue()
	{
		return 0;
	}

}
