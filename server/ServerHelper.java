package appeng.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.items.tools.ToolNetworkTool;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;

public class ServerHelper extends CommonHelper
{

	@Override
	public void doRenderItem(ItemStack sis, World tile)
	{

	}

	@Override
	public List<EntityPlayer> getPlayers()
	{
		if ( !Platform.isClient() )
		{
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

			if ( server != null )
				return server.getConfigurationManager().playerEntityList;
		}

		return new ArrayList();
	}

	@Override
	public void sendToAllNearExcept(EntityPlayer p, double x, double y, double z, double dist, World w, AppEngPacket packet)
	{
		if ( Platform.isClient() )
			return;

		for (EntityPlayer o : getPlayers())
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP) o;

			if ( entityplayermp != p && entityplayermp.worldObj == w )
			{
				double dX = x - entityplayermp.posX;
				double dY = y - entityplayermp.posY;
				double dZ = z - entityplayermp.posZ;

				if ( dX * dX + dY * dY + dZ * dZ < dist * dist )
				{
					NetworkHandler.instance.sendTo( packet, entityplayermp );
				}
			}
		}
	}

	@Override
	public void init()
	{

	}

	@Override
	public void postinit()
	{

	}

	@Override
	public World getWorld()
	{
		throw new RuntimeException( "This is a server..." );
	}

	@Override
	public void bindTileEntitySpecialRenderer(Class tile, AEBaseBlock blk)
	{
		throw new RuntimeException( "This is a server..." );
	}

	@Override
	public void spawnEffect(EffectType type, World worldObj, double posX, double posY, double posZ, Object o)
	{
		// :P
	}

	@Override
	public boolean shouldAddParticles(Random r)
	{
		return false;
	}

	@Override
	public MovingObjectPosition getMOP()
	{
		return null;
	}

	@Override
	public CableRenderMode getRenderMode()
	{
		if ( renderModeBased == null )
			return CableRenderMode.Standard;

		return renderModeForPlayer( renderModeBased );
	}

	private EntityPlayer renderModeBased;

	@Override
	public void updateRenderMode(EntityPlayer player)
	{
		renderModeBased = player;
	}

	protected CableRenderMode renderModeForPlayer(EntityPlayer player)
	{
		if ( player != null )
		{
			for (int x = 0; x < InventoryPlayer.getHotbarSize(); x++)
			{
				ItemStack is = player.inventory.getStackInSlot( x );

				if ( is != null && is.getItem() instanceof ToolNetworkTool )
				{
					NBTTagCompound c = is.getTagCompound();
					if ( c != null && c.getBoolean( "hideFacades" ) )
						return CableRenderMode.CableView;
				}
			}
		}

		return CableRenderMode.Standard;
	}

	@Override
	public void triggerUpdates()
	{

	}

	@Override
	public void missingCoreMod()
	{
		throw new RuntimeException( "Unable to Load Core Mod, please verify that AE2 is properly install in the mods folder, with a .jar extention." );
	}
}
