package appeng.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

final public class EntityFloatingItem extends EntityItem
{

	public static int ageStatic = 0;

	int superDeath = 0;
	private Entity parent;
	float progress = 0;

	public EntityFloatingItem(Entity parent, World p_i1710_1_, double p_i1710_2_, double p_i1710_4_, double p_i1710_6_, ItemStack p_i1710_8_) {
		super( p_i1710_1_, p_i1710_2_, p_i1710_4_, p_i1710_6_, p_i1710_8_ );
		motionX = motionY = motionZ = 0.0d;
		this.hoverStart = 0.5f;
		this.rotationYaw = 0;
		this.parent = parent;
	}

	// public boolean isEntityAlive()

	@Override
	public void onUpdate()
	{
		if ( !isDead && parent.isDead )
			this.setDead();

		if ( superDeath++ > 100 )
			setDead();

		this.age = ageStatic;
	}

	public void setProgress(float progress)
	{
		this.progress = progress;
		if ( this.progress > 0.99 )
			this.setDead();
	}

}
