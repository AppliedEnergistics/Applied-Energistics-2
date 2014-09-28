package appeng.integration.modules;

import java.lang.reflect.Method;

import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import mods.immibis.core.api.multipart.IPartContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.parts.IPartHost;
import appeng.core.AELog;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IImmibisMicroblocks;

public class ImmibisMicroblocks extends BaseModule implements IImmibisMicroblocks
{

	public static ImmibisMicroblocks instance;

	boolean canConvertTiles = false;

	private Class MicroblockAPIUtils;
	private Method mergeIntoMicroblockContainer;

	@Override
	public void Init() throws Throwable
	{
		TestClass( IMultipartTile.class );
		TestClass( ICoverSystem.class );
		TestClass( IPartContainer.class );

		try
		{
			MicroblockAPIUtils = Class.forName( "mods.immibis.microblocks.api.MicroblockAPIUtils" );
			mergeIntoMicroblockContainer = MicroblockAPIUtils.getMethod( "mergeIntoMicroblockContainer", ItemStack.class, EntityPlayer.class, World.class,
					int.class, int.class, int.class, int.class, Block.class, int.class );
			canConvertTiles = true;
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

	@Override
	public boolean leaveParts(TileEntity te)
	{
		if ( te instanceof IMultipartTile )
		{
			ICoverSystem ci = ((IMultipartTile) te).getCoverSystem();
			if ( ci != null )
				ci.convertToContainerBlock();

			return true;
		}
		return false;
	}

	@Override
	public IPartHost getOrCreateHost(EntityPlayer player, int side, TileEntity te)
	{
		if ( te instanceof IMultipartTile && canConvertTiles )
		{
			Block blk = AEApi.instance().blocks().blockMultiPart.block();
			ItemStack what = AEApi.instance().blocks().blockMultiPart.stack( 1 );

			World w = te.getWorldObj();
			int x = te.xCoord;
			int y = te.yCoord;
			int z = te.zCoord;

			try
			{
				// ItemStack.class, EntityPlayer.class, World.class,
				// int.class, int.class, int.class, int.class, Block.class, int.class );
				mergeIntoMicroblockContainer.invoke( null, what, player, w, x, y, z, side, blk, 0 );
			}
			catch (Throwable e)
			{
				canConvertTiles = false;
				return null;
			}

			TileEntity tx = w.getTileEntity( x, y, z );
			if ( tx instanceof IPartHost )
				return (IPartHost) tx;
		}

		return null;
	}
}
