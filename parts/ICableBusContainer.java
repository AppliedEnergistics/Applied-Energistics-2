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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ICableBusContainer
{

	int isProvidingStrongPower(ForgeDirection opposite);

	int isProvidingWeakPower(ForgeDirection opposite);

	boolean canConnectRedstone(EnumSet<ForgeDirection> of);

	void onEntityCollision(Entity e);

	boolean activate(EntityPlayer player, Vec3 vecFromPool);

	void onNeighborChanged();

	boolean isSolidOnSide(ForgeDirection side);

	boolean isEmpty();

	SelectedPart selectPart(Vec3 v3);

	boolean recolourBlock(ForgeDirection side, AEColor colour, EntityPlayer who);

	boolean isLadder(EntityLivingBase entity);

	@SideOnly(Side.CLIENT)
	void randomDisplayTick(World world, int x, int y, int z, Random r);

	int getLightValue();

}
