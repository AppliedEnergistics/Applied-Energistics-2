package appeng.parts.automation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.AELog;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.helpers.TickHandler;
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.server.ServerHelper;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartAnnihilationPlane extends PartBasicState implements IGridTickable, Callable
{

	public PartAnnihilationPlane(ItemStack is) {
		super( PartAnnihilationPlane.class, is );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(),
				CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 1, 1, 15, 15, 15, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		ForgeDirection e = rh.getWorldX();
		ForgeDirection u = rh.getWorldY();

		TileEntity te = getHost().getTile();

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), side ) )
			minX = 0;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), side ) )
			maxX = 16;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), side ) )
			minY = 0;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), side ) )
			maxY = 16;

		boolean isActive = (clientFlags & (POWERED_FLAG | CHANNEL_FLAG)) == (POWERED_FLAG | CHANNEL_FLAG);

		renderCache = rh.useSimpliedRendering( x, y, z, this, renderCache );
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockAnnihilationPlaneOn.getIcon() : is.getIconIndex(),
				CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( minX, minY, 15, maxX, maxY, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockAnnihilationPlaneOn.getIcon() : is.getIconIndex(),
				CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	private boolean isTransitionPlane(TileEntity blockTileEntity, ForgeDirection side)
	{
		if ( blockTileEntity instanceof IPartHost )
		{
			IPart p = ((IPartHost) blockTileEntity).getPart( side );
			return p instanceof PartAnnihilationPlane;
		}
		return false;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		TileEntity te = getHost().getTile();

		int x = te.xCoord;
		int y = te.yCoord;
		int z = te.zCoord;

		ForgeDirection e = bch.getWorldX();
		ForgeDirection u = bch.getWorldY();

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), side ) )
			minX = 0;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), side ) )
			maxX = 16;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), side ) )
			minY = 0;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), side ) )
			maxY = 16;

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( minX, minY, 15, maxX, maxY, bch.isBBCollision() ? 15 : 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	boolean breaking = false;
	LinkedList<IAEItemStack> Buffer = new LinkedList();
	BaseActionSource mySrc = new MachineSource( this );

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );

		data.setInteger( "bufferSize", Buffer.size() );
		for (int x = 0; x < Buffer.size(); x++)
		{
			NBTTagCompound pack = new NBTTagCompound();
			Buffer.get( x ).writeToNBT( pack );
			data.setTag( "buffer" + x, pack );
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );

		int size = data.getInteger( "bufferSize" );
		Buffer.clear();
		for (int x = 0; x < size; x++)
		{
			NBTTagCompound pack = (NBTTagCompound) data.getTag( "buffer" + x );
			IAEItemStack ais = AEItemStack.loadItemStackFromNBT( pack );
			if ( ais != null )
				Buffer.add( ais );
		}
	}

	private boolean isAccepting()
	{
		return Buffer.isEmpty();
	}

	@MENetworkEventSubscribe
	public void chanRender(MENetworkChannelsChanged c)
	{
		onNeighborChanged();
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		onNeighborChanged();
	}

	public TickRateModulation EatBlock(boolean eatForReal)
	{
		if ( isAccepting() && proxy.isActive() )
		{
			try
			{
				TileEntity te = getTile();
				WorldServer w = (WorldServer) te.getWorldObj();

				int x = te.xCoord + side.offsetX;
				int y = te.yCoord + side.offsetY;
				int z = te.zCoord + side.offsetZ;

				Block blk = w.getBlock( x, y, z );

				IStorageGrid storage = (IStorageGrid) proxy.getStorage();
				IEnergyGrid energy = (IEnergyGrid) proxy.getEnergy();

				Material mat = blk.getMaterial();
				boolean ignore = mat == Material.air || mat == Material.lava || mat == Material.water || mat.isLiquid() || blk == Blocks.bedrock
						|| blk == Blocks.end_portal || blk == Blocks.end_portal_frame || blk == Blocks.command_block;

				if ( !ignore )
				{
					if ( !w.isAirBlock( x, y, z ) && w.blockExists( x, y, z ) && blk != null && w.canMineBlock( Platform.getPlayer( w ), x, y, z ) )
					{
						float hardness = blk.getBlockHardness( w, x, y, z );
						if ( hardness >= 0.0 )
						{
							ItemStack[] out = Platform.getBlockDrops( w, x, y, z );
							float total = 1 + hardness;
							for (ItemStack is : out)
								total += is.stackSize;

							boolean hasPower = energy.extractAEPower( total, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > total - 0.1;
							if ( hasPower )
							{
								if ( eatForReal )
								{
									energy.extractAEPower( total, Actionable.MODULATE, PowerMultiplier.CONFIG );
									w.setBlock( x, y, z, Platform.air, 0, 3 );

									try
									{
										ServerHelper.proxy.sendToAllNearExcept( null, x, y, z, 64, w, new PacketTransitionEffect( x, y, z, side, true ) );
									}
									catch (IOException e)
									{
										AELog.error( e );
									}

									for (ItemStack is : out)
									{
										IAEItemStack storedItem = AEItemStack.create( is );
										storedItem = Platform.poweredInsert( energy, storage.getItemInventory(), storedItem, mySrc );
										if ( storedItem != null )
											Buffer.add( storedItem );
									}

									return TickRateModulation.URGENT;
								}
								else
								{
									breaking = true;
									TickHandler.instance.addCallable( this );
									return TickRateModulation.URGENT;
								}
							}
						}
					}
				}
			}
			catch (GridAccessException e1)
			{
				// :P
			}
		}

		// nothing to do here :)
		return TickRateModulation.SLEEP;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 2, 120, false, true );
	}

	@Override
	public void onNeighborChanged()
	{
		try
		{
			proxy.getTick().alertDevice( proxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		if ( entity instanceof EntityItem && !entity.isDead && isAccepting() )
		{
			boolean capture = false;

			switch (side)
			{
			case DOWN:
			case UP:
				if ( entity.posX > tile.xCoord && entity.posX < tile.xCoord + 1 )
					if ( entity.posZ > tile.zCoord && entity.posZ < tile.zCoord + 1 )
						if ( (entity.posY > tile.yCoord + 0.9 && side == ForgeDirection.UP) || (entity.posY < tile.yCoord + 0.1 && side == ForgeDirection.DOWN) )
							capture = true;
				break;
			case SOUTH:
			case NORTH:
				if ( entity.posX > tile.xCoord && entity.posX < tile.xCoord + 1 )
					if ( entity.posY > tile.yCoord && entity.posY < tile.yCoord + 1 )
						if ( (entity.posZ > tile.zCoord + 0.9 && side == ForgeDirection.SOUTH)
								|| (entity.posZ < tile.zCoord + 0.1 && side == ForgeDirection.NORTH) )
							capture = true;
				break;
			case EAST:
			case WEST:
				if ( entity.posZ > tile.zCoord && entity.posZ < tile.zCoord + 1 )
					if ( entity.posY > tile.yCoord && entity.posY < tile.yCoord + 1 )
						if ( (entity.posX > tile.xCoord + 0.9 && side == ForgeDirection.EAST)
								|| (entity.posX < tile.xCoord + 0.1 && side == ForgeDirection.WEST) )
							capture = true;
				break;
			default:
				// umm?
				break;
			}

			if ( capture && Platform.isServer() && proxy.isActive() )
			{
				IAEItemStack stack = AEItemStack.create( ((EntityItem) entity).getEntityItem() );
				if ( stack != null )
				{
					try
					{
						ServerHelper.proxy.sendToAllNearExcept( null, tile.xCoord, tile.yCoord, tile.zCoord, 64, tile.getWorldObj(),
								new PacketTransitionEffect( entity.posX, entity.posY, entity.posZ, side, false ) );
					}
					catch (IOException e)
					{
						AELog.error( e );
					}

					Buffer.add( stack );
					storeBuffer();
					entity.setDead();
				}
			}
		}
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( breaking )
			return TickRateModulation.URGENT;

		if ( isAccepting() )
			return EatBlock( false );
		else
		{
			storeBuffer();
			return TickRateModulation.IDLE;
		}
	}

	private void storeBuffer()
	{
		try
		{
			IStorageGrid storage = (IStorageGrid) proxy.getStorage();
			IEnergyGrid energy = (IEnergyGrid) proxy.getEnergy();

			while (!Buffer.isEmpty())
			{
				IAEItemStack storedItem = Buffer.pop();
				storedItem = Platform.poweredInsert( energy, storage.getItemInventory(), storedItem, mySrc );
				if ( storedItem != null )
				{
					Buffer.add( storedItem );
					break;
				}
			}
		}
		catch (GridAccessException e1)
		{
			// :P
		}
	}

	@Override
	public Object call() throws Exception
	{
		breaking = false;
		return EatBlock( true );
	}

}
