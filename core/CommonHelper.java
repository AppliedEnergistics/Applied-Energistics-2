package appeng.core;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.sync.AppEngPacket;
import cpw.mods.fml.common.SidedProxy;

public abstract class CommonHelper
{

	@SidedProxy(clientSide = "appeng.client.ClientHelper", serverSide = "appeng.server.ServerHelper")
	public static CommonHelper proxy;

	public abstract void init();

	public abstract World getWorld();

	public abstract void bindTileEntitySpecialRenderer(Class tile, AEBaseBlock blk);

	public abstract List<EntityPlayer> getPlayers();

	public abstract void sendToAllNearExcept(EntityPlayer p, double x, double y, double z, double dist, World w, AppEngPacket packet);

	public abstract void spawnLightning(World worldObj, double posX, double posY, double posZ);

	public abstract void spawnEnergy(World w, double posX, double posY, double posZ);

	public abstract boolean shouldAddParticles(Random r);

	public abstract MovingObjectPosition getMOP();

}
