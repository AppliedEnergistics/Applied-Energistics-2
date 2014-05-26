package appeng.items.tools.powered;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import appeng.block.misc.BlockTinyTNT;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.hooks.DispenserBlockTool;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.InWorldToolOperationResult;
import appeng.util.Platform;

public class ToolEntropyManipulator extends AEBasePoweredItem implements IBlockTool
{

	static class Combo
	{

		final public Block blk;
		final public int meta;

		public Combo(Block b, int m) {
			blk = b;
			meta = m;
		}

		@Override
		public int hashCode()
		{
			return blk.hashCode() ^ meta;
		}

		@Override
		public boolean equals(Object obj)
		{
			return blk == ((Combo) obj).blk && meta == ((Combo) obj).meta;
		}

	};

	static private Hashtable<Combo, InWorldToolOperationResult> heatUp;
	static private Hashtable<Combo, InWorldToolOperationResult> coolDown;

	static public void heat(Block BlockID, int Metadata, World w, int x, int y, int z)
	{
		InWorldToolOperationResult r = heatUp.get( new Combo( BlockID, Metadata ) );

		if ( r == null )
		{
			r = heatUp.get( new Combo( BlockID, OreDictionary.WILDCARD_VALUE ) );
		}

		if ( r.BlockItem != null )
		{
			w.setBlock( x, y, z, Block.getBlockFromItem( r.BlockItem.getItem() ), r.BlockItem.getItemDamage(), 3 );
		}
		else
		{
			w.setBlock( x, y, z, Platform.air, 0, 3 );
		}

		if ( r.Drops != null )
		{
			Platform.spawnDrops( w, x, y, z, r.Drops );
		}
	}

	static public boolean canHeat(Block BlockID, int Metadata)
	{
		InWorldToolOperationResult r = heatUp.get( new Combo( BlockID, Metadata ) );

		if ( r == null )
		{
			r = heatUp.get( new Combo( BlockID, OreDictionary.WILDCARD_VALUE ) );
		}

		return r != null;
	}

	static public void cool(Block BlockID, int Metadata, World w, int x, int y, int z)
	{
		InWorldToolOperationResult r = coolDown.get( new Combo( BlockID, Metadata ) );

		if ( r == null )
		{
			r = coolDown.get( new Combo( BlockID, OreDictionary.WILDCARD_VALUE ) );
		}

		if ( r.BlockItem != null )
		{
			w.setBlock( x, y, z, Block.getBlockFromItem( r.BlockItem.getItem() ), r.BlockItem.getItemDamage(), 3 );
		}
		else
		{
			w.setBlock( x, y, z, Platform.air, 0, 3 );
		}

		if ( r.Drops != null )
		{
			Platform.spawnDrops( w, x, y, z, r.Drops );
		}
	}

	static public boolean canCool(Block BlockID, int Metadata)
	{
		InWorldToolOperationResult r = coolDown.get( new Combo( BlockID, Metadata ) );

		if ( r == null )
		{
			r = coolDown.get( new Combo( BlockID, OreDictionary.WILDCARD_VALUE ) );
		}

		return r != null;
	}

	public ToolEntropyManipulator() {
		super( ToolEntropyManipulator.class, null );
		setfeature( EnumSet.of( AEFeature.EntropyManipulator, AEFeature.PoweredTools ) );
		maxStoredPower = AEConfig.instance.manipulator_battery;

		coolDown = new Hashtable<Combo, InWorldToolOperationResult>();
		coolDown.put( new Combo( Blocks.stone, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.cobblestone ) ) );
		coolDown.put( new Combo( Blocks.stonebrick, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.stonebrick, 1, 2 ) ) );
		coolDown.put( new Combo( Blocks.lava, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		coolDown.put( new Combo( Blocks.flowing_lava, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		coolDown.put( new Combo( Blocks.grass, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.dirt ) ) );

		List<ItemStack> snowBalls = new ArrayList();
		snowBalls.add( new ItemStack( Items.snowball ) );
		coolDown.put( new Combo( Blocks.flowing_water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( null, snowBalls ) );
		coolDown.put( new Combo( Blocks.water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.ice ) ) );

		heatUp = new Hashtable<Combo, InWorldToolOperationResult>();
		heatUp.put( new Combo( Blocks.ice, 0 ), new InWorldToolOperationResult( new ItemStack( Blocks.water ) ) );
		heatUp.put( new Combo( Blocks.flowing_water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult() );
		heatUp.put( new Combo( Blocks.water, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult() );
		heatUp.put( new Combo( Blocks.snow, OreDictionary.WILDCARD_VALUE ), new InWorldToolOperationResult( new ItemStack( Blocks.flowing_water ) ) );
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( this, new DispenserBlockTool() );
	}

	@Override
	public boolean hitEntity(ItemStack item, EntityLivingBase target, EntityLivingBase hitter)
	{
		if ( this.getAECurrentPower( item ) > 1600 )
		{
			extractAEPower( item, 1600 );
			target.setFire( 8 );
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer p)
	{
		MovingObjectPosition target = this.getMovingObjectPositionFromPlayer( w, p, true );

		if ( target == null )
			return item;
		else
		{
			if ( target.typeOfHit == MovingObjectType.BLOCK )
			{
				int x = target.blockX;
				int y = target.blockY;
				int z = target.blockZ;

				if ( w.getBlock( x, y, z ).getMaterial() == Material.lava || w.getBlock( x, y, z ).getMaterial() == Material.water )
				{
					if ( w.canMineBlock( p, x, y, z ) )
					{
						onItemUse( item, p, w, x, y, z, 0, 0.0F, 0.0F, 0.0F );
					}
				}
			}
		}

		return item;
	}

	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer p, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if ( this.getAECurrentPower( item ) > 1600 )
		{
			if ( !p.canPlayerEdit( x, y, z, side, item ) )
				return false;

			Block Blk = w.getBlock( x, y, z );
			int Metadata = w.getBlockMetadata( x, y, z );

			if ( p.isSneaking() )
			{
				if ( canCool( Blk, Metadata ) )
				{
					extractAEPower( item, 1600 );
					cool( Blk, Metadata, w, x, y, z );
					return true;
				}
			}
			else
			{
				if ( Blk instanceof BlockTNT )
				{
					w.setBlock( x, y, z, Platform.air, 0, 3 );
					((BlockTNT) Blk).func_150114_a( w, x, y, z, 1, p );
					return true;
				}

				if ( Blk instanceof BlockTinyTNT )
				{
					w.setBlock( x, y, z, Platform.air, 0, 3 );
					((BlockTinyTNT) Blk).startFuse( w, x, y, z, p );
					return true;
				}

				if ( canHeat( Blk, Metadata ) )
				{
					extractAEPower( item, 1600 );
					heat( Blk, Metadata, w, x, y, z );
					return true;
				}

				ItemStack[] stack = Platform.getBlockDrops( w, x, y, z );
				List<ItemStack> out = new ArrayList<ItemStack>();
				boolean hasFurnaceable = false;
				boolean canFurnaceable = true;

				for (ItemStack i : stack)
				{
					ItemStack result = FurnaceRecipes.smelting().getSmeltingResult( i );

					if ( result != null )
					{
						if ( result.getItem() instanceof ItemBlock )
						{
							if ( Block.getBlockFromItem( (ItemBlock) result.getItem() ) == Blk && result.getItem().getDamage( result ) == Metadata )
							{
								canFurnaceable = false;
							}
						}
						hasFurnaceable = true;
						out.add( result );
					}
					else
					{
						canFurnaceable = false;
						out.add( i );
					}
				}

				if ( hasFurnaceable && canFurnaceable )
				{
					extractAEPower( item, 1600 );
					InWorldToolOperationResult or = InWorldToolOperationResult.getBlockOperationResult( out.toArray( new ItemStack[out.size()] ) );
					w.playSoundEffect( (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );

					if ( or.BlockItem == null )
					{
						w.setBlock( x, y, z, Platform.air, 0, 3 );
					}
					else
					{
						w.setBlock( x, y, z, Block.getBlockFromItem( or.BlockItem.getItem() ), or.BlockItem.getItemDamage(), 3 );
					}

					if ( or.Drops != null )
					{
						Platform.spawnDrops( w, x, y, z, or.Drops );
					}

					return true;
				}
				else
				{
					ForgeDirection dir = ForgeDirection.getOrientation( side );
					x += dir.offsetX;
					y += dir.offsetY;
					z += dir.offsetZ;

					if ( !p.canPlayerEdit( x, y, z, side, item ) )
						return false;

					if ( w.isAirBlock( x, y, z ) )
					{
						extractAEPower( item, 1600 );
						w.playSoundEffect( (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );
						w.setBlock( x, y, z, Blocks.fire );
					}

					return true;
				}
			}
		}

		return false;
	}
}
