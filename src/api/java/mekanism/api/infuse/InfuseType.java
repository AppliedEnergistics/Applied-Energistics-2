package mekanism.api.infuse;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

/**
 * The types of infuse currently available in Mekanism.
 * @author AidanBrady
 *
 */
public final class InfuseType
{
	/** The name of this infusion */
	public String name;

	/** The location of this infuse's GUI texture */
	public ResourceLocation texture;

	/** The infuse's GUI texture X offset. */
	public int texX;

	/** The infuse's GUI texture Y offset. */
	public int texY;

	/** The unlocalized name of this type. */
	public String unlocalizedName;

	public InfuseType(String s, ResourceLocation location, int x, int y)
	{
		name = s;
		texture = location;
		texX = x;
		texY = y;
	}

	public InfuseType setUnlocalizedName(String name)
	{
		unlocalizedName = name;

		return this;
	}

	public String getLocalizedName()
	{
		return StatCollector.translateToLocal(unlocalizedName);
	}
}
