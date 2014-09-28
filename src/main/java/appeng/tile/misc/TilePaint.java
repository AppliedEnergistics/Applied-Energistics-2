package appeng.tile.misc;

import appeng.helpers.Splotch;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.AEColor;
import appeng.items.misc.ItemPaintBall;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;

import com.google.common.collect.ImmutableList;

public class TilePaint extends AEBaseTile
{

	static final int LIGHT_PER_DOT = 12;

	int isLit = 0;
	ArrayList<Splotch> dots = null;

	void writeBuffer(ByteBuf out)
	{
		if ( dots == null )
		{
			out.writeByte( 0 );
			return;
		}

		out.writeByte( dots.size() );

		for (Splotch s : dots)
			s.writeToStream( out );
	}

	void readBuffer(ByteBuf in)
	{
		byte howMany = in.readByte();

		if ( howMany == 0 )
		{
			isLit = 0;
			dots = null;
			return;
		}

		dots = new ArrayList( howMany );
		for (int x = 0; x < howMany; x++)
			dots.add( new Splotch( in ) );

		isLit = 0;
		for (Splotch s : dots)
		{
			if ( s.lumen )
			{
				isLit += LIGHT_PER_DOT;
			}
		}

		maxLit();
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TilePaint(NBTTagCompound data)
	{
		ByteBuf myDat = Unpooled.buffer();
		writeBuffer( myDat );
		if ( myDat.hasArray() )
			data.setByteArray( "dots", myDat.array() );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TilePaint(NBTTagCompound data)
	{
		if ( data.hasKey( "dots" ) )
			readBuffer( Unpooled.copiedBuffer( data.getByteArray( "dots" ) ) );
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TilePaint(ByteBuf data) throws IOException
	{
		writeBuffer( data );
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TilePaint(ByteBuf data) throws IOException
	{
		readBuffer( data );
		return true;
	}

	public void onNeighborBlockChange()
	{
		if ( dots == null )
			return;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if ( !isSideValid( side ) )
				removeSide( side );
		}

		updateData();
	}

	private void updateData()
	{
		isLit = 0;
		for (Splotch s : dots)
		{
			if ( s.lumen )
			{
				isLit += LIGHT_PER_DOT;
			}
		}

		maxLit();

		if ( dots.isEmpty() )
			dots = null;

		if ( dots == null )
			worldObj.setBlock( xCoord, yCoord, zCoord, Blocks.air );
	}

	public void cleanSide(ForgeDirection side)
	{
		if ( dots == null )
			return;

		removeSide( side );

		updateData();
	}

	public boolean isSideValid(ForgeDirection side)
	{
		Block blk = worldObj.getBlock( xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ );
		return blk.isSideSolid( worldObj, xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ, side.getOpposite() );
	}

	private void removeSide(ForgeDirection side)
	{
		Iterator<Splotch> i = dots.iterator();
		while (i.hasNext())
		{
			Splotch s = i.next();
			if ( s.side == side )
				i.remove();
		}

		markForUpdate();
		markDirty();
	}

	public int getLightLevel()
	{
		return isLit;
	}

	public void addBlot(ItemStack type, ForgeDirection side, Vec3 hitVec)
	{
		Block blk = worldObj.getBlock( xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ );
		if ( blk.isSideSolid( worldObj, xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ, side.getOpposite() ) )
		{
			ItemPaintBall ipb = (ItemPaintBall) type.getItem();

			AEColor col = ipb.getColor( type );
			boolean lit = ipb.isLumen( type );

			if ( dots == null )
				dots = new ArrayList();

			if ( dots.size() > 20 )
				dots.remove( 0 );

			dots.add( new Splotch( col, lit, side, hitVec ) );
			if ( lit )
				isLit += LIGHT_PER_DOT;

			maxLit();
			markForUpdate();
			markDirty();
		}
	}

	private void maxLit()
	{
		if ( isLit > 14 )
			isLit = 14;

		if ( worldObj != null )
			worldObj.updateLightByType( EnumSkyBlock.Block, xCoord, yCoord, zCoord );
	}

	public Collection<Splotch> getDots()
	{
		if ( dots == null )
			return ImmutableList.of();

		return dots;
	}
}
