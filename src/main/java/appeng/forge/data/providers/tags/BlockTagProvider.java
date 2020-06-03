package appeng.forge.data.providers.tags;


import appeng.core.AppEng;
import appeng.forge.data.providers.IAE2DataProvider;
import appeng.recipes.AE2Tags;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;

import static appeng.recipes.AE2Tags.Blocks.*;
import static net.minecraftforge.common.Tags.Blocks.ORES;
import static net.minecraftforge.common.Tags.Blocks.STORAGE_BLOCKS;


public class BlockTagProvider extends BlockTagsProvider implements IAE2DataProvider
{

	public BlockTagProvider( DataGenerator generatorIn )
	{
		super( generatorIn );
	}

	@Override
	protected void registerTags()
	{
		getBuilder( STORAGE_BLOCKS_FLUIX ).add( BLOCKS.fluixBlock().block() );
		getBuilder( STORAGE_BLOCKS_CERTUS_QUARTZ ).add( BLOCKS.quartzBlock().block() );
		getBuilder( STORAGE_BLOCKS ).add(
				STORAGE_BLOCKS_FLUIX,
				STORAGE_BLOCKS_CERTUS_QUARTZ
		);

		getBuilder( ORES_CERTUS_QUARTZ ).add( BLOCKS.quartzOre().block() );
		getBuilder( ORES_CERTUS_QUARTZ_CHARGED ).add( BLOCKS.quartzOreCharged().block() );
		getBuilder( ORES ).add(
				ORES_CERTUS_QUARTZ,
				ORES_CERTUS_QUARTZ_CHARGED
		);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return AppEng.MOD_NAME + " Block Tags";
	}

}
