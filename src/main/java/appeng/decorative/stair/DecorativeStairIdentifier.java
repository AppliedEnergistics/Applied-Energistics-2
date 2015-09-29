package appeng.decorative.stair;


import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;

import appeng.decorative.solid.Identifier;


/**
 * An refactor safe implementation for stair identifiers.
 * The internal Strings are not supposed to be changed,
 * they are used to persist the blocks into the save file.
 *
 * If you change these, you will break worlds if not countered
 *
 * @author thatsIch
 * @version rv3 - 29.06.2015
 * @since rv3 29.06.2015
 */
public enum DecorativeStairIdentifier implements Identifier
{
	FLUIX("stair.fluix"),
	CHISELED_CERTUS_QUARTZ("stair.quartz.certus.chiseled"),
	CERTUS_QUARTZ("stair.quartz.certus"),
	CERTUS_QUARTZ_PILLAR("stair.quartz.certus.pillar"),
	SKYSTONE("stair.skystone"),
	SKYSTONE_BLOCK("stair.skystone.block"),
	SKYSTONE_BRICK("stair.skystone.brick"),
	SKYSTONE_SMALL_BRICK("stair.skystone.brick.small");

	private final String name;

	/**
	 * Used to register the specific blocks
	 *
	 * @param name id of the block or a unique name
	 *
	 * @see net.minecraftforge.fml.common.registry.GameRegistry#registerBlock(Block, String)
	 */
	DecorativeStairIdentifier( @Nonnull String name )
	{
		Preconditions.checkNotNull( name );
		Preconditions.checkArgument( !name.isEmpty() );

		this.name = name;
	}

	@Nonnull
	@Override
	public String identifier()
	{
		return this.name;
	}
}
