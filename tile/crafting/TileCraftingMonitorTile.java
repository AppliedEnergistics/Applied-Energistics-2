package appeng.tile.crafting;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileCraftingMonitorTile extends TileCraftingTile implements IColorableTile
{

	@SideOnly(Side.CLIENT)
	public Integer dspList;

	@SideOnly(Side.CLIENT)
	public boolean updateList;

	IAEItemStack dspPlay;
	AEColor paintedColor = AEColor.Transparent;

	class CraftingMonitorHandler extends AETileEventHandler
	{

		public CraftingMonitorHandler() {
			super( TileEventType.NETWORK, TileEventType.WORLD_NBT );
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			AEColor oldPaintedColor = paintedColor;
			paintedColor = AEColor.values()[data.readByte()];

			boolean hasItem = data.readBoolean();

			if ( hasItem )
				dspPlay = AEItemStack.loadItemStackFromPacket( data );
			else
				dspPlay = null;

			updateList = true;
			return oldPaintedColor != paintedColor; // tesr!
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			data.writeByte( paintedColor.ordinal() );

			if ( dspPlay == null )
				data.writeBoolean( false );
			else
			{
				data.writeBoolean( true );
				dspPlay.writeToPacket( data );
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			if ( data.hasKey( "paintedColor" ) )
				paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			data.setByte( "paintedColor", (byte) paintedColor.ordinal() );
		}

	};

	public TileCraftingMonitorTile() {
		addNewHandler( new CraftingMonitorHandler() );
	}

	public boolean isAccelerator()
	{
		return false;
	}

	public boolean isStatus()
	{
		return true;
	}

	public void setJob(IAEItemStack is)
	{
		if ( (is == null) != (dspPlay == null) )
		{
			dspPlay = is == null ? null : is.copy();
			markForUpdate();
		}
		else if ( is != null && dspPlay != null )
		{
			if ( is.getStackSize() != dspPlay.getStackSize() )
			{
				dspPlay = is == null ? null : is.copy();
				markForUpdate();
			}
		}
	}

	public IAEItemStack getJobProgress()
	{
		return dspPlay;// AEItemStack.create( new ItemStack( Items.diamond, 64 ) );
	}

	@Override
	public boolean requiresTESR()
	{
		return getJobProgress() != null;
	}

	public AEColor getColor()
	{
		return paintedColor;
	}

	@Override
	public boolean recolourBlock(ForgeDirection side, AEColor newPaintedColor, EntityPlayer who)
	{
		if ( paintedColor == newPaintedColor )
			return false;

		paintedColor = newPaintedColor;
		markDirty();
		markForUpdate();
		return true;
	}
}
