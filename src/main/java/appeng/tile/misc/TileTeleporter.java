package appeng.tile.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;

public class TileTeleporter extends AENetworkTile
{

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileTeleporter( NBTTagCompound data )
	{
		data.setLong( "freq", freq );
		data.setBoolean( "output", output );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileTeleporter( NBTTagCompound data )
	{
		freq = data.getLong( "freq" );
		output = data.getBoolean( "output" );
	}

	public TileTeleporter()
	{
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	public long freq;
	public boolean output = false;

	public void teleport( EntityPlayer p ) throws GridAccessException
	{
		TileTeleporter tt;
		List<TileTeleporter> tps = new ArrayList<TileTeleporter>();
		for (IGridNode gn : gridProxy.getGrid().getMachines( TileTeleporter.class ))
		{
			tt = (TileTeleporter) gn.getMachine();
			if ( !output && tt != this && tt.output && freq == tt.freq )
			{
				boolean dimTravel = p.worldObj != tt.worldObj;
				double distance = Math.abs( xCoord - tt.xCoord ) + Math.abs( yCoord - tt.yCoord ) + Math.abs( zCoord - tt.zCoord );
				double drain = AEConfig.instance.teleporter_getDrain( distance, dimTravel );
				if ( drain > 0 && gridProxy.getEnergy().getStoredPower() >= drain )
				{
					gridProxy.getEnergy().extractAEPower( drain, Actionable.MODULATE, PowerMultiplier.CONFIG );
					if ( p.worldObj != tt.worldObj )
						p.travelToDimension( tt.worldObj.provider.dimensionId );
					p.mountEntity( null );
					p.setPositionAndUpdate( tt.xCoord + 0.5, tt.yCoord + 1, tt.zCoord + 0.5 );
					p.worldObj.playSoundEffect( p.posX, p.posY, p.posZ, "mob.endermen.portal", 1F, 1F );
					return;
				}
			}
			if ( output && !tt.output && freq == tt.freq )
			{
				tps.add( tt );
			}
		}
		if ( !tps.isEmpty() )
		{
			tt = tps.get( new Random().nextInt( tps.size() ) );

			boolean dimTravel = p.worldObj != tt.worldObj;
			double distance = Math.abs( xCoord - tt.xCoord ) + Math.abs( yCoord - tt.yCoord ) + Math.abs( zCoord - tt.zCoord );
			double drain = AEConfig.instance.teleporter_getDrain( distance, dimTravel );
			if ( drain > 0 && gridProxy.getEnergy().getStoredPower() >= drain )
			{
				gridProxy.getEnergy().extractAEPower( drain, Actionable.MODULATE, PowerMultiplier.CONFIG );
				if ( p.worldObj != tt.worldObj )
					p.travelToDimension( tt.worldObj.provider.dimensionId );
				p.mountEntity( null );
				p.setPositionAndUpdate( tt.xCoord + 0.5, tt.yCoord + 1, tt.zCoord + 0.5 );
				p.worldObj.playSoundEffect( p.posX, p.posY, p.posZ, "mob.endermen.portal", 1F, 1F );
			}
			return;
		}
	}

	public boolean onTileActivated( EntityPlayer player )
	{
		ItemStack is = player.inventory.getCurrentItem();
		if ( is != null && is.getItem() instanceof IMemoryCard )
		{
			IMemoryCard mc = (IMemoryCard) is.getItem();
			if ( player.isSneaking() )
			{
				NBTTagCompound data = new NBTTagCompound();
				output = true;
				data.setString( "type", "teleporter" );
				this.setFrequency( System.currentTimeMillis() );
				data.setLong( "freq", freq );
				mc.setMemoryCardContents( is, this.getBlockType().getUnlocalizedName(), data );
				mc.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
			}
			else
			{
				NBTTagCompound data = mc.getData( is );
				ItemStack newType = ItemStack.loadItemStackFromNBT( data );
				String type = data.getString( "type" );
				long freq = data.getLong( "freq" );
				if ( type == "teleporter" )
				{
					output = false;
					this.setFrequency( freq );
				}
				mc.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
			}
			return true;
		}
		return false;
	}

	public void setFrequency( long freq )
	{
		this.freq = freq;
	}

	public long getFrequency()
	{
		return freq;
	}

}
