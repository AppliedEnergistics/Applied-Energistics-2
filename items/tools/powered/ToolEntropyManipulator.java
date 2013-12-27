package appeng.items.tools.powered;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.core.features.AEFeature;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.InWorldToolOperationResult;
import appeng.util.Platform;

public class ToolEntropyManipulator extends AEBasePoweredItem
{

	static private Hashtable<String, InWorldToolOperationResult> heatUp;
	static private Hashtable<String, InWorldToolOperationResult> coolDown;

	static public void heat(int BlockID, int Metadata, World w, int x, int y, int z)
	{
		InWorldToolOperationResult r = heatUp.get( BlockID + ":" + Metadata );

		if ( r == null )
		{
			r = heatUp.get( BlockID + ":*" );
		}

		if ( r.BlockItem != null )
		{
			w.setBlock( x, y, z, r.BlockItem.itemID, r.BlockItem.getItemDamage(), 3 );
		}
		else
		{
			w.setBlock( x, y, z, 0, 0, 3 );
		}

		if ( r.Drops != null )
		{
			Platform.spawnDrops( w, x, y, z, r.Drops );
		}
	}

	static public boolean canHeat(int BlockID, int Metadata)
	{
		InWorldToolOperationResult r = heatUp.get( BlockID + ":" + Metadata );

		if ( r == null )
		{
			r = heatUp.get( BlockID + ":*" );
		}

		return r != null;
	}

	static public void cool(int BlockID, int Metadata, World w, int x, int y, int z)
	{
		InWorldToolOperationResult r = coolDown.get( BlockID + ":" + Metadata );

		if ( r == null )
		{
			r = coolDown.get( BlockID + ":*" );
		}

		if ( r.BlockItem != null )
		{
			w.setBlock( x, y, z, r.BlockItem.itemID, r.BlockItem.getItemDamage(), 3 );
		}
		else
		{
			w.setBlock( x, y, z, 0, 0, 3 );
		}

		if ( r.Drops != null )
		{
			Platform.spawnDrops( w, x, y, z, r.Drops );
		}
	}

	static public boolean canCool(int BlockID, int Metadata)
	{
		InWorldToolOperationResult r = coolDown.get( BlockID + ":" + Metadata );

		if ( r == null )
		{
			r = coolDown.get( BlockID + ":*" );
		}

		return r != null;
	}

	public ToolEntropyManipulator() {
		super( ToolEntropyManipulator.class, null );
		setfeature( EnumSet.of( AEFeature.EntropyManipulator, AEFeature.PoweredTools ) );

		coolDown = new Hashtable<String, InWorldToolOperationResult>();
		coolDown.put( Block.stone.blockID + ":0", new InWorldToolOperationResult( new ItemStack( Block.cobblestone ) ) );
		coolDown.put( Block.stoneBrick.blockID + ":0", new InWorldToolOperationResult( new ItemStack( Block.stoneBrick, 1, 2 ) ) );
		coolDown.put( Block.lavaStill.blockID + ":*", new InWorldToolOperationResult( new ItemStack( Block.obsidian ) ) );
		coolDown.put( Block.lavaMoving.blockID + ":*", new InWorldToolOperationResult( new ItemStack( Block.obsidian ) ) );
		coolDown.put( Block.grass.blockID + ":*", new InWorldToolOperationResult( new ItemStack( Block.dirt ) ) );

		List<ItemStack> snowBalls = new ArrayList();
		snowBalls.add( new ItemStack( Item.snowball ) );
		coolDown.put( Block.waterMoving.blockID + ":*", new InWorldToolOperationResult( null, snowBalls ) );
		coolDown.put( Block.waterStill.blockID + ":*", new InWorldToolOperationResult( new ItemStack( Block.ice ) ) );

		heatUp = new Hashtable<String, InWorldToolOperationResult>();
		heatUp.put( Block.ice.blockID + ":0", new InWorldToolOperationResult( new ItemStack( Block.waterStill ) ) );
		heatUp.put( Block.waterMoving.blockID + ":*", new InWorldToolOperationResult() );
		heatUp.put( Block.waterStill.blockID + ":*", new InWorldToolOperationResult() );
		heatUp.put( Block.blockSnow.blockID + ":*", new InWorldToolOperationResult( new ItemStack( Block.waterStill ) ) );
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
			if ( target.typeOfHit == EnumMovingObjectType.TILE )
			{
				int x = target.blockX;
				int y = target.blockY;
				int z = target.blockZ;

				if ( w.getBlockMaterial( x, y, z ) == Material.lava || w.getBlockMaterial( x, y, z ) == Material.water )
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

			int BlockID = w.getBlockId( x, y, z );
			int Metadata = w.getBlockMetadata( x, y, z );

			if ( p.isSneaking() )
			{
				if ( canCool( BlockID, Metadata ) )
				{
					extractAEPower( item, 1600 );
					cool( BlockID, Metadata, w, x, y, z );
					return true;
				}
			}
			else
			{
				if ( canHeat( BlockID, Metadata ) )
				{
					extractAEPower( item, 1600 );
					heat( BlockID, Metadata, w, x, y, z );
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
							if ( ((ItemBlock) result.getItem()).getBlockID() == BlockID && result.getItem().getDamage( result ) == Metadata )
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
						w.setBlock( x, y, z, 0, 0, 3 );
					}
					else
					{
						w.setBlock( x, y, z, or.BlockItem.itemID, or.BlockItem.getItemDamage(), 3 );
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
						w.playSoundEffect( (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );
						w.setBlock( x, y, z, Block.fire.blockID );
					}

					return true;
				}
			}
		}

		return false;
	}
}
