package appeng.tile.grindstone;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.tiles.ICrankable;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;

public class TileCrank extends AEBaseTile implements ICustomCollision
{

	final int ticksPerRoation = 18;

	// sided values..
	public float visibleRotation = 0;
	public int charge = 0;

	public int hits = 0;
	public int rotation = 0;

	public TileCrank() {
		addNewHandler( new AETileEventHandler( TileEventType.NETWORK, TileEventType.TICK ) {

			@Override
			public void Tick()
			{
				if ( rotation > 0 )
				{
					visibleRotation -= 360 / (ticksPerRoation);
					charge++;
					if ( charge >= ticksPerRoation )
					{
						charge -= ticksPerRoation;
						ICrankable g = getGrinder();
						if ( g != null )
							g.applyTurn();
					}

					rotation--;
				}
			}

			@Override
			public boolean readFromStream(ByteBuf data) throws java.io.IOException
			{
				rotation = data.readInt();
				return false;
			}

			@Override
			public void writeToStream(ByteBuf data) throws java.io.IOException
			{
				data.writeInt( rotation );
			}

		} );
	}

	public ICrankable getGrinder()
	{
		if ( Platform.isClient() )
			return null;

		ForgeDirection grinder = getUp().getOpposite();
		TileEntity te = worldObj.getTileEntity( xCoord + grinder.offsetX, yCoord + grinder.offsetY, zCoord + grinder.offsetZ );
		if ( te instanceof ICrankable )
			return (ICrankable) te;
		return null;
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		getBlockType().onNeighborBlockChange( worldObj, xCoord, yCoord, zCoord, Platform.air );
	}

	public void power()
	{
		if ( Platform.isClient() )
			return;

		if ( rotation < 3 )
		{
			ICrankable g = getGrinder();
			if ( g != null )
			{
				if ( g.canTurn() )
				{
					hits = 0;
					rotation += ticksPerRoation;
					this.markForUpdate();
				}
				else
				{
					hits++;
					if ( hits > 10 )
					{
						worldObj.func_147480_a( xCoord, yCoord, zCoord, false );
						// worldObj.destroyBlock( xCoord, yCoord, zCoord, false );
					}
				}
			}
		}
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		double xOff = -0.15 * getUp().offsetX;
		double yOff = -0.15 * getUp().offsetY;
		double zOff = -0.15 * getUp().offsetZ;
		return Arrays
				.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( xOff + 0.15, yOff + 0.15, zOff + 0.15, xOff + 0.85, yOff + 0.85, zOff + 0.85 ) } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		double xOff = -0.15 * getUp().offsetX;
		double yOff = -0.15 * getUp().offsetY;
		double zOff = -0.15 * getUp().offsetZ;
		out.add( AxisAlignedBB.getBoundingBox( xOff + (double) 0.15, yOff + (double) 0.15, zOff + (double) 0.15,// ahh
				xOff + (double) 0.85, yOff + (double) 0.85, zOff + (double) 0.85 ) );
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}
}
