package appeng.forge.data.providers.tags;


import appeng.core.AppEng;
import appeng.forge.data.providers.IAE2DataProvider;
import appeng.recipes.AE2Tags;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeItemTagsProvider;

import javax.annotation.Nonnull;

import static appeng.recipes.AE2Tags.Items.*;
import static net.minecraftforge.common.Tags.Items.*;


public class ItemTagProvider extends ForgeItemTagsProvider implements IAE2DataProvider
{
	public ItemTagProvider( DataGenerator generatorIn )
	{
		super( generatorIn );
	}

	@Override
	public void registerTags()
	{
		getBuilder( CRYSTAL_PURE_CERTUS_QUARTZ ).add( MATERIALS.purifiedCertusQuartzCrystal().item() );
		getBuilder( CRYSTAL_PURE_NETHER_QUARTZ ).add( MATERIALS.purifiedNetherQuartzCrystal().item() );
		getBuilder( CRYSTAL_PURE_FLUIX ).add( MATERIALS.purifiedFluixCrystal().item() );
		getBuilder( CRYSTAL_PURE ).add(
				CRYSTAL_PURE_CERTUS_QUARTZ,
				CRYSTAL_PURE_NETHER_QUARTZ,
				CRYSTAL_PURE_FLUIX
		);

		getBuilder( DUSTS_ENDER ).add( MATERIALS.enderDust().item() );
		getBuilder( DUSTS_ENDER_PEARL ).add( MATERIALS.enderDust().item() );
		getBuilder( DUSTS_WHEAT ).add( MATERIALS.flour().item() );
		getBuilder( DUSTS_GOLD ).add( MATERIALS.goldDust().item() );
		getBuilder( DUSTS_IRON ).add( MATERIALS.ironDust().item() );
		getBuilder( DUSTS_FLUIX ).add( MATERIALS.fluixDust().item() );
		getBuilder( DUSTS_NETHER_QUARTZ ).add( MATERIALS.netherQuartzDust().item() );
		getBuilder( DUSTS_QUARTZ ).add( MATERIALS.netherQuartzDust().item() );
		getBuilder( DUSTS_CERTUS_QUARTZ ).add( MATERIALS.certusQuartzDust().item() );
		getBuilder( DUSTS ).add(
				DUSTS_ENDER,
				DUSTS_ENDER_PEARL,
				DUSTS_WHEAT,
				DUSTS_GOLD,
				DUSTS_IRON,
				DUSTS_FLUIX,
				DUSTS_NETHER_QUARTZ,
				DUSTS_QUARTZ,
				DUSTS_CERTUS_QUARTZ
		);

		getBuilder( GEMS_CERTUS_QUARTZ_CHARGED ).add( MATERIALS.certusQuartzCrystalCharged().item() );
		getBuilder( GEMS_CERTUS_QUARTZ ).add( MATERIALS.certusQuartzCrystal().item() );
		getBuilder( GEMS_FLUIX ).add( MATERIALS.fluixCrystal().item() );
		getBuilder( GEMS ).add(
				GEMS_CERTUS_QUARTZ,
				GEMS_FLUIX
		);
		getBuilder( GEMS_CERTUS_QUARTZ ).add(
				GEMS_CERTUS_QUARTZ_CHARGED
		);

		getBuilder( PEARL_FLUIX ).add( MATERIALS.fluixPearl().item() );

		getBuilder( GEARS_WOOD ).add( MATERIALS.woodenGear().item() );
		getBuilder( GEARS ).add(
				GEARS_WOOD
		);

		copy( AE2Tags.Blocks.STORAGE_BLOCKS_FLUIX, STORAGE_BLOCKS_FLUIX );
		copy( AE2Tags.Blocks.STORAGE_BLOCKS_CERTUS_QUARTZ, STORAGE_BLOCKS_CERTUS_QUARTZ );
		getBuilder( STORAGE_BLOCKS ).add(
				STORAGE_BLOCKS_FLUIX,
				STORAGE_BLOCKS_CERTUS_QUARTZ
		);

		copy( AE2Tags.Blocks.ORES_CERTUS_QUARTZ, ORES_CERTUS_QUARTZ );
		copy( AE2Tags.Blocks.ORES_CERTUS_QUARTZ_CHARGED, ORES_CERTUS_QUARTZ_CHARGED );
		getBuilder( ORES ).add(
				ORES_CERTUS_QUARTZ,
				ORES_CERTUS_QUARTZ_CHARGED
		);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return AppEng.MOD_NAME + " Item Tags";
	}

}
