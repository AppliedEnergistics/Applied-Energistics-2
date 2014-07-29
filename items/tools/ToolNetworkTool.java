package appeng.items.tools;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.INetworkToolAgent;
import appeng.client.ClientHelper;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.transformer.annotations.integration.Interface;
import appeng.util.Platform;
import buildcraft.api.tools.IToolWrench;

@Interface(iface = "buildcraft.api.tools.IToolWrench", iname = "BC")
public class ToolNetworkTool extends AEBaseItem implements IGuiItem, IAEWrench, IToolWrench
{

	public ToolNetworkTool() {
		super( ToolNetworkTool.class, null );
		setfeature( EnumSet.of( AEFeature.NetworkTool ) );
		setMaxStackSize( 1 );
	}

	@Override
	public IGuiItemObject getGuiObject(ItemStack is, World world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity( x, y, z );
		return new NetworkToolViewer( is, (IGridHost) (te instanceof IGridHost ? te : null) );
	}

	@Override
	public ItemStack onItemRightClick(ItemStack it, World w, EntityPlayer p)
	{
		if ( Platform.isClient() )
		{
			MovingObjectPosition mop = ClientHelper.proxy.getMOP();

			if ( mop == null )
			{
				onItemUseFirst( it, p, w, 0, 0, 0, -1, 0, 0, 0 );
			}
			else
			{
				int i = mop.blockX;
				int j = mop.blockY;
				int k = mop.blockZ;

				if ( w.getBlock( i, j, k ).isAir( w, i, j, k ) )
					onItemUseFirst( it, p, w, 0, 0, 0, -1, 0, 0, 0 );
			}
		}

		return it;
	}

	@Override
	public boolean onItemUseFirst(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		MovingObjectPosition mop = new MovingObjectPosition( x, y, z, side, Vec3.createVectorHelper( hitX, hitY, hitZ ) );
		TileEntity te = world.getTileEntity( x, y, z );
		if ( te instanceof IPartHost )
		{
			SelectedPart part = ((IPartHost) te).selectPart( mop.hitVec );
			if ( part.part != null )
			{
				if ( part.part instanceof INetworkToolAgent && !((INetworkToolAgent) part.part).showNetworkInfo( mop ) )
					return false;
			}
		}
		else if ( te instanceof INetworkToolAgent && !((INetworkToolAgent) te).showNetworkInfo( mop ) )
		{
			return false;
		}

		if ( Platform.isClient() )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketClick( x, y, z, side, hitX, hitY, hitZ ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
		return true;
	}

	public boolean serverSideToolLogic(ItemStack is, EntityPlayer p, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if ( side >= 0 )
		{
			Block b = w.getBlock( x, y, z );
			if ( b != null && !p.isSneaking() )
			{
				TileEntity te = w.getTileEntity( x, y, z );
				if ( !(te instanceof IGridHost) )
				{
					if ( b.rotateBlock( w, x, y, z, ForgeDirection.getOrientation( side ) ) )
					{
						b.onNeighborBlockChange( w, x, y, z, Platform.air );
						p.swingItem();
						return !w.isRemote;
					}
				}
			}

			if ( !p.isSneaking() )
			{
				if ( p.openContainer instanceof AEBaseContainer )
					return true;

				TileEntity te = w.getTileEntity( x, y, z );

				if ( te instanceof IGridHost )
					Platform.openGUI( p, te, ForgeDirection.getOrientation( side ), GuiBridge.GUI_NETWORK_STATUS );
				else
					Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_NETWORK_TOOL );

				return true;
			}
			else
				b.onBlockActivated( w, x, y, z, p, side, hitX, hitY, hitZ );
		}
		else
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_NETWORK_TOOL );

		return false;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean canWrench(ItemStack is, EntityPlayer player, int x, int y, int z)
	{
		return true;
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z)
	{
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z)
	{
		player.swingItem();
	}

}
