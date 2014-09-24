package appeng.entity;

import net.minecraft.entity.Entity;

public class EntityIds
{

	public static final int TINY_TNT = 10;
	public static final int SINGULARITY = 11;
	public static final int CHARGED_QUARTZ = 12;
	public static final int GROWING_CRYSTAL = 13;

	public static int get(Class<? extends Entity> droppedEntity)
	{
		if ( droppedEntity == EntityTinyTNTPrimed.class )
			return TINY_TNT;
		if ( droppedEntity == EntitySingularity.class )
			return SINGULARITY;
		if ( droppedEntity == EntityChargedQuartz.class )
			return CHARGED_QUARTZ;
		if ( droppedEntity == EntityGrowingCrystal.class )
			return GROWING_CRYSTAL;

		throw new RuntimeException( "Missing entity id: " + droppedEntity.getName() );
	}
}
