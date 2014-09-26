package mekanism.api;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Coord4D - an integer-based way to keep track of and perform operations on blocks in a Minecraft-based environment. This also takes
 * in account the dimension the coordinate is in.
 * @author aidancbrady
 *
 */
public class Coord4D
{
	public int xCoord;
	public int yCoord;
	public int zCoord;

	public int dimensionId;

	/**
	 * Creates a Coord4D WITHOUT a dimensionId. Don't use unless absolutely necessary.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 */
	public Coord4D(int x, int y, int z)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;

		dimensionId = 0;
	}

	/**
	 * Creates a Coord4D from the defined x, y, z, and dimension values.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @param dimension - dimension ID
	 */
	public Coord4D(int x, int y, int z, int dimension)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;

		dimensionId = dimension;
	}

	/**
	 * Gets the metadata of the block representing this Coord4D.
	 * @param world - world this Coord4D is in
	 * @return the metadata of this Coord4D's block
	 */
	public int getMetadata(IBlockAccess world)
	{
		return world.getBlockMetadata(xCoord, yCoord, zCoord);
	}

	/**
	 * Gets the TileEntity of the block representing this Coord4D.
	 * @param world - world this Coord4D is in
	 * @return the TileEntity of this Coord4D's block
	 */
	public TileEntity getTileEntity(IBlockAccess world)
	{
		if(world instanceof World && !exists((World)world))
		{
			return null;
		}

		return world.getTileEntity(xCoord, yCoord, zCoord);
	}

	/**
	 * Gets the Block value of the block representing this Coord4D.
	 * @param world - world this Coord4D is in
	 * @return the Block value of this Coord4D's block
	 */
	public Block getBlock(IBlockAccess world)
	{
		if(world instanceof World && !exists((World)world))
		{
			return null;
		}
		
		return world.getBlock(xCoord, yCoord, zCoord);
	}

	/**
	 * Writes this Coord4D's data to an NBTTagCompound.
	 * @param nbtTags - tag compound to write to
	 * @return the tag compound with this Coord4D's data
	 */
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("x", xCoord);
		nbtTags.setInteger("y", yCoord);
		nbtTags.setInteger("z", zCoord);
		nbtTags.setInteger("dimensionId", dimensionId);

		return nbtTags;
	}

	/**
	 * Writes this Coord4D's data to an ArrayList for packet transfer.
	 * @param data - the ArrayList to add the data to
	 */
	public void write(ArrayList data)
	{
		data.add(xCoord);
		data.add(yCoord);
		data.add(zCoord);
		data.add(dimensionId);
	}
	
	/**
	 * Writes this Coord4D's data to a ByteBuf for packet transfer.
	 * @param dataStream - the ByteBuf to add the data to
	 */
	public void write(ByteBuf dataStream)
	{
		dataStream.writeInt(xCoord);
		dataStream.writeInt(yCoord);
		dataStream.writeInt(zCoord);
		dataStream.writeInt(dimensionId);
	}

	/**
	 * Translates this Coord4D by the defined x, y, and z values.
	 * @param x - x value to translate
	 * @param y - y value to translate
	 * @param z - z value to translate
	 * @return translated Coord4D
	 */
	public Coord4D translate(int x, int y, int z)
	{
		xCoord += x;
		yCoord += y;
		zCoord += z;

		return this;
	}

	/**
	 * Creates and returns a new Coord4D translated to the defined offsets of the side.
	 * @param side - side to translate this Coord4D to
	 * @return translated Coord4D
	 */
	public Coord4D getFromSide(ForgeDirection side)
	{
		return getFromSide(side, 1);
	}

	public Coord4D getFromSide(ForgeDirection side, int amount)
	{
		return new Coord4D(xCoord+(side.offsetX*amount), yCoord+(side.offsetY*amount), zCoord+(side.offsetZ*amount), dimensionId);
	}

	/**
	 * Returns a new Coord4D from a defined TileEntity's xCoord, yCoord and zCoord values.
	 * @param tileEntity - TileEntity at the location that will represent this Coord4D
	 * @return the Coord4D object from the TileEntity
	 */
	public static Coord4D get(TileEntity tileEntity)
	{
		return new Coord4D(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, tileEntity.getWorldObj().provider.dimensionId);
	}

	/**
	 * Returns a new Coord4D from a tag compound.
	 * @param data - tag compound to read from
	 * @return the Coord4D from the tag compound
	 */
    public static Coord4D read(NBTTagCompound tag)
    {
        return new Coord4D(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getInteger("id"));
    }

	/**
	 * Returns a new Coord4D from a ByteBuf.
	 * @param dataStream - data input to read from
	 * @return the Coord4D from the data input
	 */
	public static Coord4D read(ByteBuf dataStream)
	{
		return new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}

	/**
	 * Creates and returns a new Coord4D with values representing the difference between the defined Coord4D
	 * @param other - the Coord4D to subtract from this
	 * @return a Coord4D representing the distance between the defined Coord4D
	 */
	public Coord4D difference(Coord4D other)
	{
		return new Coord4D(xCoord-other.xCoord, yCoord-other.yCoord, zCoord-other.zCoord, dimensionId);
	}

	/**
	 * A method used to find the ForgeDirection represented by the distance of the defined Coord4D. Most likely won't have many
	 * applicable uses.
	 * @param other - Coord4D to find the side difference of
	 * @return ForgeDirection representing the side the defined relative Coord4D is on to this
	 */
	public ForgeDirection sideDifference(Coord4D other)
	{
		Coord4D diff = difference(other);

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(side.offsetX == diff.xCoord && side.offsetY == diff.yCoord && side.offsetZ == diff.zCoord)
			{
				return side;
			}
		}

		return ForgeDirection.UNKNOWN;
	}

	/**
	 * Gets the distance to a defined Coord4D.
	 * @param obj - the Coord4D to find the distance to
	 * @return the distance to the defined Coord4D
	 */
	public int distanceTo(Coord4D obj)
	{
		int subX = xCoord - obj.xCoord;
		int subY = yCoord - obj.yCoord;
		int subZ = zCoord - obj.zCoord;
		return (int)MathHelper.sqrt_double(subX * subX + subY * subY + subZ * subZ);
	}

	/**
	 * Whether or not the defined side of this Coord4D is visible.
	 * @param side - side to check
	 * @param world - world this Coord4D is in
	 * @return
	 */
	public boolean sideVisible(ForgeDirection side, IBlockAccess world)
	{
		return world.isAirBlock(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
	}
	
	/**
	 * Gets a TargetPoint with the defined range from this Coord4D with the appropriate coordinates and dimension ID.
	 * @param range - the range the packet can be sent in of this Coord4D
	 * @return TargetPoint relative to this Coord4D
	 */
	public TargetPoint getTargetPoint(double range)
	{
		return new TargetPoint(dimensionId, xCoord, yCoord, zCoord, range);
	}

	/**
	 * Steps this Coord4D in the defined side's offset without creating a new value.
	 * @param side - side to step towards
	 * @return this Coord4D
	 */
	public Coord4D step(ForgeDirection side)
	{
		return translate(side.offsetX, side.offsetY, side.offsetZ);
	}

	/**
	 * Whether or not the chunk this Coord4D is in exists and is loaded.
	 * @param world - world this Coord4D is in
	 * @return the chunk of this Coord4D
	 */
	public boolean exists(World world)
	{
		return world.getChunkProvider().chunkExists(xCoord >> 4, zCoord >> 4);
	}

	/**
	 * Gets the chunk this Coord4D is in.
	 * @param world - world this Coord4D is in
	 * @return the chunk of this Coord4D
	 */
	public Chunk getChunk(World world)
	{
		return world.getChunkFromBlockCoords(xCoord, zCoord);
	}
	
	/**
	 * Gets the Chunk3D object with chunk coordinates correlating to this Coord4D's location
	 * @return Chunk3D with correlating chunk coordinates.
	 */
	public Chunk3D getChunk3D()
	{
		return new Chunk3D(this);
	}

	/**
	 * Whether or not the block this Coord4D represents is an air block.
	 * @param world - world this Coord4D is in
	 * @return if this Coord4D is an air block
	 */
	public boolean isAirBlock(IBlockAccess world)
	{
		return world.isAirBlock(xCoord, yCoord, zCoord);
	}
	
	/**
	 * Gets a bounding box that contains the area this Coord4D would take up in a world.
	 * @return this Coord4D's bounding box
	 */
	public AxisAlignedBB getBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+1, zCoord+1);
	}

	@Override
	public Coord4D clone()
	{
		return new Coord4D(xCoord, yCoord, zCoord, dimensionId);
	}

	@Override
	public String toString()
	{
		return "[Coord4D: " + xCoord + ", " + yCoord + ", " + zCoord + ", dim=" + dimensionId + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Coord4D &&
				((Coord4D)obj).xCoord == xCoord &&
				((Coord4D)obj).yCoord == yCoord &&
				((Coord4D)obj).zCoord == zCoord &&
				((Coord4D)obj).dimensionId == dimensionId;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + xCoord;
		code = 31 * code + yCoord;
		code = 31 * code + zCoord;
		code = 31 * code + dimensionId;
		return code;
	}
}