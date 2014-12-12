package appeng.api.features;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public interface IWorldGen
{

	public enum WorldGenType
	{
		CertusQuartz, ChargedCertusQuartz, Meteorites
	}

	public void disableWorldGenForProviderID(WorldGenType type, Class<? extends WorldProvider> provider);

	public void enableWorldGenForDimension(WorldGenType type, int dimID);

	public void disableWorldGenForDimension(WorldGenType type, int dimID);

	boolean isWorldGenEnabled(WorldGenType type, World w);

}
