package powercrystals.minefactoryreloaded.api;

import java.util.List;

import net.minecraft.world.World;


public interface IRandomMobProvider
{
	public List<RandomMob> getRandomMobs(World world); 
}