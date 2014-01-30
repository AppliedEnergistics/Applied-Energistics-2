package appeng.block.solids;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQuartzOre;
import appeng.core.features.AEFeature;
import appeng.helpers.LocationRotation;

public class OreQuartz extends AEBaseBlock implements IOrientableBlock
{

	public int boostBrightnessLow;
	public int boostBrightnessHigh;
	public boolean enhanceBrightness;

	public OreQuartz(Class self) {
		super( self, Material.rock );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setHardness( 3.0F );
		setResistance( 5.0F );
		boostBrightnessLow = 0;
		boostBrightnessHigh = 1;
		enhanceBrightness = false;
	}

	@Override
	public void postInit()
	{
		OreDictionary.registerOre( "oreCertusQuartz", new ItemStack( this ) );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQuartzOre.class;
	}

	@Override
	public int getMixedBrightnessForBlock(IBlockAccess par1iBlockAccess, int par2, int par3, int par4)
	{
		int j1 = super.getMixedBrightnessForBlock( par1iBlockAccess, par2, par3, par4 );
		if ( enhanceBrightness )
		{
			j1 = Math.max( j1 >> 20, j1 >> 4 );

			if ( j1 > 4 )
				j1 += boostBrightnessHigh;
			else
				j1 += boostBrightnessLow;

			if ( j1 > 15 )
				j1 = 15;
			return j1 << 20 | j1 << 4;
		}
		return j1;
	}

	public OreQuartz() {
		this( OreQuartz.class );
	}

	ItemStack getItemDropped()
	{
		return AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 );
	}

	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		return new LocationRotation( w, x, y, z );
	}

	@Override
	public int idDropped(int id, Random rand, int meta)
	{
		return getItemDropped().itemID;
	}

	@Override
	public int damageDropped(int id)
	{
		return getItemDropped().getItemDamage();
	}

	@Override
	public int quantityDropped(Random rand)
	{
		return 1 + rand.nextInt( 2 );
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random rand)
	{
		if ( fortune > 0 && this.blockID != this.idDropped( 0, rand, fortune ) )
		{
			int j = rand.nextInt( fortune + 2 ) - 1;

			if ( j < 0 )
			{
				j = 0;
			}

			return this.quantityDropped( rand ) * (j + 1);
		}
		else
		{
			return this.quantityDropped( rand );
		}
	}

	@Override
	public void dropBlockAsItemWithChance(World w, int x, int y, int z, int blockid, float something, int meta)
	{
		super.dropBlockAsItemWithChance( w, x, y, z, blockid, something, meta );

		if ( this.idDropped( blockid, w.rand, meta ) != this.blockID )
		{
			int xp = MathHelper.getRandomIntegerInRange( w.rand, 2, 5 );

			this.dropXpOnBlockBreak( w, x, y, z, xp );
		}
	}

}
