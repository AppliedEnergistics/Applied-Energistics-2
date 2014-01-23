package appeng.helpers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.StepSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import appeng.api.AEApi;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.core.AppEng;
import appeng.core.sync.packets.PacketPartPlacement;
import appeng.facade.IFacadeItem;
import appeng.integration.abstraction.IBC;
import appeng.integration.abstraction.IFMP;
import appeng.util.LookDirection;
import appeng.util.Platform;
import cpw.mods.fml.common.network.PacketDispatcher;

public class PartPlacement
{

	private ThreadLocal<Object> placing = new ThreadLocal<Object>();

	@ForgeSubscribe
	public void playerInteract(PlayerInteractEvent event)
	{
		if ( event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.worldObj.isRemote )
		{
			if ( placing.get() != null )
				return;

			placing.set( event );

			ItemStack held = event.entityPlayer.getHeldItem();
			if ( place( held, event.x, event.y, event.z, event.face, event.entityPlayer, event.entityPlayer.worldObj, PlaceType.INTERACT_FIRST_PASS, 0 ) )
				event.setCanceled( true );

			placing.set( null );
		}
	}

	public enum PlaceType
	{
		PLACE_ITEM, INTERACT_FIRST_PASS, INTERACT_SECOND_PASS
	};

	public static boolean place(ItemStack held, int x, int y, int z, int face, EntityPlayer player, World world, PlaceType pass, int depth)
	{
		if ( depth > 3 )
			return false;

		ForgeDirection side = ForgeDirection.getOrientation( face );

		if ( held != null && Platform.isWrench( player, held, x, y, z ) && player.isSneaking() )
		{
			int block = world.getBlockId( x, y, z );
			TileEntity tile = world.getBlockTileEntity( x, y, z );
			IPartHost host = null;

			if ( tile instanceof IPartHost )
				host = (IPartHost) tile;

			if ( host != null )
			{
				if ( !world.isRemote )
				{
					LookDirection dir = Platform.getPlayerRay( player );
					MovingObjectPosition mop = Block.blocksList[block].collisionRayTrace( world, x, y, z, dir.a, dir.b );
					if ( mop != null )
					{
						List<ItemStack> is = new LinkedList();
						SelectedPart sp = host.selectPart( mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ ) );

						if ( sp.part != null )
						{
							is.add( sp.part.getItemStack( PartItemStack.Wrench ) );
							sp.part.getDrops( is, true );
							host.removePart( sp.side );
						}

						if ( sp.facade != null )
						{
							is.add( sp.facade.getItemStack() );
							host.getFacadeContainer().removeFacade( host, sp.side );
						}

						if ( host.isEmpty() )
							world.setBlock( x, y, z, 0 );

						if ( is != null && !is.isEmpty() )
						{
							Platform.spawnDrops( world, x, y, z, is );
						}
					}
				}
				else
				{
					player.swingItem();
					try
					{
						PacketDispatcher.sendPacketToServer( (new PacketPartPlacement( x, y, z, face )).getPacket() );
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				return true;
			}

			return false;
		}

		TileEntity tile = world.getBlockTileEntity( x, y, z );
		IPartHost host = null;

		if ( tile instanceof IPartHost )
			host = (IPartHost) tile;

		if ( held != null )
		{
			IFacadePart fp = isFacade( held, side );
			if ( fp != null )
			{
				if ( host != null )
				{
					if ( !world.isRemote )
					{
						if ( host.getPart( ForgeDirection.UNKNOWN ) == null )
							return false;

						if ( host.getFacadeContainer().addFacade( fp ) )
						{
							host.markForUpdate();
							if ( !player.capabilities.isCreativeMode )
							{
								held.stackSize--;
								if ( held.stackSize == 0 )
								{
									player.inventory.mainInventory[player.inventory.currentItem] = null;
									MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, held ) );
								}
							}
							return true;
						}
					}
					else
					{
						player.swingItem();
						try
						{
							PacketDispatcher.sendPacketToServer( (new PacketPartPlacement( x, y, z, face )).getPacket() );
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				return false;
			}
		}

		if ( host == null && tile != null && AppEng.instance.isIntegrationEnabled( "FMP" ) )
			host = ((IFMP) AppEng.instance.getIntegration( "FMP" )).getOrCreateHost( tile );

		// if ( held == null )
		{
			int block = world.getBlockId( x, y, z );
			if ( host != null && player.isSneaking() && Block.blocksList[block] != null )
			{
				LookDirection dir = Platform.getPlayerRay( player );
				MovingObjectPosition mop = Block.blocksList[block].collisionRayTrace( world, x, y, z, dir.a, dir.b );
				if ( mop != null )
				{
					mop.hitVec = mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ );
					SelectedPart sPart = host.selectPart( mop.hitVec );
					if ( sPart != null && sPart.part != null )
						if ( sPart.part.onShiftActivate( player, mop.hitVec ) )
						{
							if ( world.isRemote )
							{
								try
								{
									PacketDispatcher.sendPacketToServer( (new PacketPartPlacement( x, y, z, face )).getPacket() );
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
							return true;
						}
				}
			}

		}

		if ( held == null || !(held.getItem() instanceof IPartItem) )
			return false;

		int te_x = x;
		int te_y = y;
		int te_z = z;

		if ( host == null && pass == PlaceType.PLACE_ITEM )
		{
			ItemStack is = AEApi.instance().blocks().blockMultiPart.stack( 1 );
			ItemBlock ib = (ItemBlock) is.getItem();
			ForgeDirection offset = ForgeDirection.UNKNOWN;

			int blkID = world.getBlockId( x, y, z );
			if ( blkID != 0 && !Block.blocksList[blkID].isBlockReplaceable( world, x, y, z ) )
			{
				offset = side;
				if ( Platform.isServer() )
					side = side.getOpposite();
			}

			te_x = x + offset.offsetX;
			te_y = y + offset.offsetY;
			te_z = z + offset.offsetZ;

			tile = world.getBlockTileEntity( te_x, te_y, te_z );
			if ( tile instanceof IPartHost )
				host = (IPartHost) tile;

			if ( host == null && tile != null && AppEng.instance.isIntegrationEnabled( "FMP" ) )
				host = ((IFMP) AppEng.instance.getIntegration( "FMP" )).getOrCreateHost( tile );

			if ( host == null && AEApi.instance().blocks().blockMultiPart.block().canPlaceBlockAt( world, te_x, te_y, te_z )
					&& ib.placeBlockAt( is, player, world, te_x, te_y, te_z, side.ordinal(), 0.5f, 0.5f, 0.5f, 0 ) )
			{
				if ( !world.isRemote )
				{
					tile = world.getBlockTileEntity( te_x, te_y, te_z );

					if ( tile instanceof IPartHost )
						host = (IPartHost) tile;

					pass = PlaceType.INTERACT_SECOND_PASS;
				}
				else
				{
					player.swingItem();
					try
					{
						PacketDispatcher.sendPacketToServer( (new PacketPartPlacement( x, y, z, face )).getPacket() );
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					return true;
				}
			}
			else
				return false;
		}

		if ( host == null )
			return false;

		if ( !host.canAddPart( held, side ) )
		{
			if ( pass == PlaceType.INTERACT_FIRST_PASS || pass == PlaceType.PLACE_ITEM )
			{
				ForgeDirection offset = side;

				te_x = x + offset.offsetX;
				te_y = y + offset.offsetY;
				te_z = z + offset.offsetZ;

				int blkID = world.getBlockId( te_x, te_y, te_z );
				tile = world.getBlockTileEntity( te_x, te_y, te_z );

				if ( tile != null && AppEng.instance.isIntegrationEnabled( "FMP" ) )
					host = ((IFMP) AppEng.instance.getIntegration( "FMP" )).getOrCreateHost( tile );

				if ( (blkID == 0 || Block.blocksList[blkID].isBlockReplaceable( world, te_x, te_y, te_z ) || host != null) && offset != ForgeDirection.UNKNOWN )
					return place( held, te_x, te_y, te_z, side.getOpposite().ordinal(), player, world,
							pass == PlaceType.INTERACT_FIRST_PASS ? PlaceType.INTERACT_SECOND_PASS : PlaceType.PLACE_ITEM, depth + 1 );
			}
			return false;
		}

		if ( !world.isRemote )
		{
			ForgeDirection mySide = host.addPart( held, side );
			if ( mySide != null )
			{
				IPart newlyPlacedPart = host.getPart( mySide );
				if ( newlyPlacedPart != null )
					newlyPlacedPart.onPlacement( player, held, side );

				StepSound ss = AEApi.instance().blocks().blockMultiPart.block().stepSound;
				world.playSoundEffect( 0.5 + x, 0.5 + y, 0.5 + z, ss.getPlaceSound(), (ss.getVolume() + 1.0F) / 2.0F, ss.getPitch() * 0.8F );

				if ( !player.capabilities.isCreativeMode )
				{
					held.stackSize--;
					if ( held.stackSize == 0 )
					{
						player.inventory.mainInventory[player.inventory.currentItem] = null;
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, held ) );
					}
				}
			}
		}
		else
		{
			player.swingItem();
			try
			{
				PacketDispatcher.sendPacketToServer( (new PacketPartPlacement( x, y, z, face )).getPacket() );
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return true;
	}

	private static IFacadePart isFacade(ItemStack held, ForgeDirection side)
	{
		if ( held.getItem() instanceof IFacadeItem )
			return ((IFacadeItem) held.getItem()).createPartFromItemStack( held, side );
		if ( AppEng.instance.isIntegrationEnabled( "BC" ) )
		{
			IBC bc = (IBC) AppEng.instance.getIntegration( "BC" );
			if ( bc.isFacade( held ) )
				return bc.createFacadePart( held, side );
		}
		return null;
	}

}