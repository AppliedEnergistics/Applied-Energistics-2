package powercrystals.minefactoryreloaded.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.WeightedRandom;

public class RandomMob extends WeightedRandom.Item
{
	private Entity _mob;
	public final boolean shouldInit;
	
	public RandomMob(Entity savedMob, int weight, boolean init)
	{
		super(weight);
		_mob = savedMob;
		shouldInit = init;
	}
	
	public RandomMob(Entity savedMob, int weight)
	{
		this(savedMob, weight, true);
	}
	
	public Entity getMob()
	{
		if(_mob == null) return null;
		return _mob;
	}
}
