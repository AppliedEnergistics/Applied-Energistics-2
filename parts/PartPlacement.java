package appeng.parts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import appeng.api.AEApi;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.core.sync.packets.PacketPartPlacement;
import appeng.facade.IFacadeItem;
import appeng.integration.abstraction.IBC;
import appeng.integration.abstraction.IFMP;
import appeng.util.LookDirection;
import appeng.util.Platform;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PartPlacement
{

	private ThreadLocal<Object> placing = new ThreadLocal<Object>();

	@SubscribeEvent
	public void playerInteract(PlayerInteractEvent event)
	{
		if ( event.action == Action.RIGHT_CLICK_AIR && event.entityPlayer.worldObj.isRemote )
		{
			// re-check to see if this event was already channeled, cause these two events are really stupid...
			MovingObjectPosition mop = Platform.rayTrace( event.entityPlayer, true, false );
			Minecraft mc = Minecraft.getMinecraft();

			float f = 1.0F;
			double d0 = (double) mc.playerController.getBlockReachDistance();
			Vec3 vec3 = mc.renderViewEntity.getPosition( f );

			if ( mop != null && mop.hitVec.distanceTo( vec3 ) < d0 )
			{
				World w = event.entity.worldObj;
				TileEntity te = w.getTileEntity( mop.blockX, mop.blockY, mop.blockZ );
				if ( te instanceof IPartHost )
					event.setCanceled( true );
			}
			else if ( event.entityPlayer != null )
			{
				ItemStack held = event.entityPlayer.getHeldItem();
				if ( event.entityPlayer.isSneaking() && held != null && AEApi.instance().items().itemMemoryCard.sameAsStack( held ) )
				{
					try
					{
						NetworkHandler.instance.sendToServer( new PacketClick( event.x, event.y, event.z, event.face, 0, 0, 0 ) );
					}
					catch (IOException e)
					{
						// :P
					}
				}
			}
		}
		else if ( event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.worldObj.isRemote )
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
			Block block = world.getBlock( x, y, z );
			TileEntity tile = world.getTileEntity( x, y, z );
			IPartHost host = null;

			if ( tile instanceof IPartHost )
				host = (IPartHost) tile;

			if ( host != null )
			{
				if ( !world.isRemote )
				{
					LookDirection dir = Platform.getPlayerRay( player );
					MovingObjectPosition mop = block.collisionRayTrace( world, x, y, z, dir.a, dir.b );
					if ( mop != null )
					{
						List<ItemStack> is = new LinkedList();
						SelectedPart sp = host.selectPart( mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ ) );

						if ( sp.part != null )
						{
							is.add( sp.part.getItemStack( PartItemStack.Wrench ) );
							sp.part.getDrops( is, true );
							host.removePart( sp.side, false );
						}

						if ( sp.facade != null )
						{
							is.add( sp.facade.getItemStack() );
							host.getFacadeContainer().removeFacade( host, sp.side );
							world.notifyBlocksOfNeighborChange( x, y, z, Platform.air );
						}

						if ( host.isEmpty() )
							host.cleanup();

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
						NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face ) );
					}
					catch (IOException e)
					{
						AELog.error( e );
					}
				}
				return true;
			}

			return false;
		}

		TileEntity tile = world.getTileEntity( x, y, z );
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

						if ( host.canAddPart( held, side ) )
						{
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
					}
					else
					{
						player.swingItem();
						try
						{
							NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face ) );
							return true;
						}
						catch (IOException e)
						{
							AELog.error( e );
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
			Block block = world.getBlock( x, y, z );
			if ( host != null && player.isSneaking() && block != null )
			{
				LookDirection dir = Platform.getPlayerRay( player );
				MovingObjectPosition mop = block.collisionRayTrace( world, x, y, z, dir.a, dir.b );
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
									NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face ) );
								}
								catch (IOException e)
								{
									AELog.error( e );
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

			Block blkID = world.getBlock( x, y, z );
			if ( blkID != null && !blkID.isReplaceable( world, x, y, z ) )
			{
				offset = side;
				if ( Platform.isServer() )
					side = side.getOpposite();
			}

			te_x = x + offset.offsetX;
			te_y = y + offset.offsetY;
			te_z = z + offset.offsetZ;

			tile = world.getTileEntity( te_x, te_y, te_z );
			if ( tile instanceof IPartHost )
				host = (IPartHost) tile;

			if ( host == null && tile != null && AppEng.instance.isIntegrationEnabled( "FMP" ) )
				host = ((IFMP) AppEng.instance.getIntegration( "FMP" )).getOrCreateHost( tile );

			if ( host == null && AEApi.instance().blocks().blockMultiPart.block().canPlaceBlockAt( world, te_x, te_y, te_z )
					&& ib.placeBlockAt( is, player, world, te_x, te_y, te_z, side.ordinal(), 0.5f, 0.5f, 0.5f, 0 ) )
			{
				if ( !world.isRemote )
				{
					tile = world.getTileEntity( te_x, te_y, te_z );

					if ( tile instanceof IPartHost )
						host = (IPartHost) tile;

					pass = PlaceType.INTERACT_SECOND_PASS;
				}
				else
				{
					player.swingItem();
					try
					{
						NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face ) );
					}
					catch (IOException e)
					{
						AELog.error( e );
					}
					return true;
				}
			}
			else if ( host != null && !host.canAddPart( held, side ) )
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

				Block blkID = world.getBlock( te_x, te_y, te_z );
				tile = world.getTileEntity( te_x, te_y, te_z );

				if ( tile != null && AppEng.instance.isIntegrationEnabled( "FMP" ) )
					host = ((IFMP) AppEng.instance.getIntegration( "FMP" )).getOrCreateHost( tile );

				if ( (blkID == null || blkID.isReplaceable( world, te_x, te_y, te_z ) || host != null) && offset != ForgeDirection.UNKNOWN )
					return place( held, te_x, te_y, te_z, side.getOpposite().ordinal(), player, world,
							pass == PlaceType.INTERACT_FIRST_PASS ? PlaceType.INTERACT_SECOND_PASS : PlaceType.PLACE_ITEM, depth + 1 );
			}
			return false;
		}

		if ( !world.isRemote )
		{
			Block block = world.getBlock( x, y, z );
			LookDirection dir = Platform.getPlayerRay( player );
			MovingObjectPosition mop = block.collisionRayTrace( world, x, y, z, dir.a, dir.b );
			if ( mop != null )
			{
				SelectedPart sp = host.selectPart( mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ ) );

				if ( sp.part != null )
				{
					if ( !player.isSneaking() && sp.part.onActivate( player, mop.hitVec ) )
						return false;
				}
			}

			ForgeDirection mySide = host.addPart( held, side, player );
			if ( mySide != null )
			{
				SoundType ss = AEApi.instance().blocks().blockMultiPart.block().stepSound;

				// ss.getPlaceSound()
				world.playSoundEffect( 0.5 + x, 0.5 + y, 0.5 + z, ss.func_150496_b(), (ss.getVolume() + 1.0F) / 2.0F, ss.getPitch() * 0.8F );

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
				NetworkHandler.instance.sendToServer( new PacketPartPlacement( x, y, z, face ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
		return true;
	}

	public static IFacadePart isFacade(ItemStack held, ForgeDirection side)
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