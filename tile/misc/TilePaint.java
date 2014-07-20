package appeng.tile.misc;

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
import appeng.helpers.Splot;
import appeng.items.misc.ItemPaintBall;
import appeng.tile.AEBaseTile;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;

import com.google.common.collect.ImmutableList;

public class TilePaint extends AEBaseTile
{

	static final int LIGHT_PER_DOT = 12;

	int isLit = 0;
	ArrayList<Splot> dots = null;

	void writeBuffer(ByteBuf out)
	{
		if ( dots == null )
		{
			out.writeByte( 0 );
			return;
		}

		out.writeByte( dots.size() );

		for (Splot s : dots)
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
			dots.add( new Splot( in ) );

		isLit = 0;
		for (Splot s : dots)
		{
			if ( s.lumen )
			{
				isLit += LIGHT_PER_DOT;
			}
		}

		maxLit();
	}

	class PaintHandler extends AETileEventHandler
	{

		public PaintHandler() {
			super( TileEventType.NETWORK, TileEventType.WORLD_NBT );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			ByteBuf myDat = Unpooled.buffer();
			writeBuffer( myDat );
			if ( myDat.hasArray() )
				data.setByteArray( "dots", myDat.array() );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			if ( data.hasKey( "dots" ) )
				readBuffer( Unpooled.copiedBuffer( data.getByteArray( "dots" ) ) );
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			writeBuffer( data );
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			readBuffer( data );
			return true;
		}
	}

	public TilePaint() {
		addNewHandler( new PaintHandler() );
	}

	public void onNeighborBlockChange()
	{
		if ( dots == null )
			return;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			Block blk = worldObj.getBlock( xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ );
			if ( !blk.isSideSolid( worldObj, xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ, side.getOpposite() ) )
				removeSide( side );
		}

		isLit = 0;
		for (Splot s : dots)
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

	private void removeSide(ForgeDirection side)
	{
		Iterator<Splot> i = dots.iterator();
		while (i.hasNext())
		{
			Splot s = i.next();
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

			dots.add( new Splot( col, lit, side, hitVec ) );
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

	public Collection<Splot> getDots()
	{
		if ( dots == null )
			return ImmutableList.of();

		return dots;
	}
}
