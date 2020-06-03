package appeng.items.materials;


import appeng.items.AEBaseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public class ItemCustomEntity extends AEBaseItem
{

	private final IItemEntityFactory factory;

	public ItemCustomEntity( IItemEntityFactory factory, Properties properties )
	{
		super( properties );
		this.factory = factory;
	}

	@Override public boolean hasCustomEntity( ItemStack stack )
	{
		return true;
	}

	@Override
	public Entity createEntity( final World w, final Entity location, final ItemStack itemstack )
	{
		final Entity entity;

		entity = factory.create( w, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack );

		entity.setMotion( location.getMotion() );

		if( location instanceof ItemEntity && entity instanceof ItemEntity )
		{
			( (ItemEntity) entity ).setDefaultPickupDelay();
		}

		return entity;
	}

	@FunctionalInterface
	public interface IItemEntityFactory
	{

		Entity create( World world, double x, double y, double z, ItemStack stack );

	}

}
