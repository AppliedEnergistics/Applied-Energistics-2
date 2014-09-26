package powercrystals.minefactoryreloaded.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ReplacementBlock
{
	protected byte _hasMeta;
	protected int _meta;
	protected final Block _block;
	protected final NBTTagCompound _tileTag;
	
	/**
	 * Called to replace a block in the world.
	 * @param world The world object
	 * @param x The X coord
	 * @param y The Y coord
	 * @param z The Z coord
	 * @param stack The ItemStack being used to replace the block (may be null)
	 * @return True if the block was set successfully
	 */
	public boolean replaceBlock(World world, int x, int y, int z, ItemStack stack)
	{
		int meta = getMeta(world, x, y, z, stack);
		if (world.setBlock(x, y, z, _block, meta, 3))
		{
			if (hasTag(stack) && _block.hasTileEntity(meta))
			{
				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile != null)
					tile.readFromNBT(getTag(world, x, y, z, stack));
			}
			return true;
		}
		return false;
	}

	/**
	 * Called to get the metadata of the replacement block in the world.
	 * @param world The world object
	 * @param x The X coord
	 * @param y The Y coord
	 * @param z The Z coord
	 * @param stack The ItemStack being used to replace the block (may be null)
	 * @return The metadata of the block
	 */
	protected int getMeta(World world, int x, int y, int z, ItemStack stack)
	{
		int m = 0;
		if (_hasMeta > 0)
		{
			if (_hasMeta > 1)
				return _meta;
			m = stack.getItemDamage();
			Item item = stack.getItem();
			if (item instanceof ItemBlock)
				m = ((ItemBlock)item).getMetadata(m);
		}
		return m;
	}
	
	/**
	 * Called to set the metdata of this ReplacementBlock to a fixed value
	 * @param meta The metadata of the block 
	 * @return This instance
	 */
	public ReplacementBlock setMeta(int meta)
	{
		if (meta >= 0)
		{
			_hasMeta = 2;
			_meta = meta;
		}
		return this;
	}
	
	/**
	 * Called to set the metdata of this ReplacementBlock to a value read from an ItemStack
	 * @param meta The metadata of the block 
	 * @return This instance
	 */
	public ReplacementBlock setMeta(boolean hasMeta)
	{
		_hasMeta = (byte) (hasMeta ? 1 : 0);
		return this;
	}
	
	/**
	 * Called to get the NBTTagCompound a TileEntity will read its state from
	 * @param world The world object
	 * @param x The X coord
	 * @param y The Y coord
	 * @param z The Z coord
	 * @param stack The ItemStack being used to replace the block (may be null)
	 * @return The NBTTagCompound a TileEntity will read its state from
	 */
	protected NBTTagCompound getTag(World world, int x, int y, int z, ItemStack stack)
	{
		return _tileTag;
	}
	
	/**
	 * Called to see if a TileEntity should have its state set
	 * @param stack The ItemStack being used to replace the block (may be null)
	 * @return True if the TileEntity should have its state set
	 */
	protected boolean hasTag(ItemStack stack)
	{
		return _tileTag != null;
	}
	
	public ReplacementBlock(Item block)
	{
		this(Block.getBlockFromItem(block));
	}
	
	public ReplacementBlock(Item block, NBTTagCompound tag)
	{
		this(Block.getBlockFromItem(block), tag);
	}
	
	public ReplacementBlock(Block block)
	{
		this(block, null);
	}
	
	public ReplacementBlock(Block block, NBTTagCompound tag)
	{
		_block = block;
		_tileTag = tag;
	}
}
