package appeng.client.render.entity;

import net.minecraft.entity.Entity;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntitySingularity;
import appeng.entity.EntityTinyTNTPrimed;

public class EntityIds
{

	public static final int TINY_TNT = 10;
	public static final int SINGULARITY = 11;
	public static final int CHARGED_QUARTZ = 12;

	public static int get(Class<? extends Entity> droppedEntity)
	{
		if ( droppedEntity == EntityTinyTNTPrimed.class )
			return TINY_TNT;
		if ( droppedEntity == EntitySingularity.class )
			return SINGULARITY;
		if ( droppedEntity == EntityChargedQuartz.class )
			return CHARGED_QUARTZ;

		throw new RuntimeException( "Missing entity id: " + droppedEntity.getName() );
	}
}
