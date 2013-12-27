package appeng.items.materials;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.implementations.IStorageComponent;
import appeng.items.AEBaseItem;

public class ItemMaterial extends AEBaseItem implements IStorageComponent
{

	final MaterialType material;

	public ItemMaterial(MaterialType type) {
		super( ItemMaterial.class, type.name() );
		setfeature( type.getFeature() );
		material = type;

		if ( type.getOreName() != null )
			OreDictionary.registerOre( type.getOreName(), this );
	}

	@Override
	public boolean hasCustomEntity(ItemStack is)
	{
		return material.hasCustomEntity();
	}

	@Override
	public Entity createEntity(World w, Entity location, ItemStack itemstack)
	{
		Class<? extends Entity> droppedEntity = material.getCustomEntityClass();
		Entity eqi;

		try
		{
			eqi = droppedEntity.getConstructor( World.class, double.class, double.class, double.class, ItemStack.class ).newInstance( w, location.posX,
					location.posY, location.posZ, itemstack );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}

		eqi.motionX = location.motionX;
		eqi.motionY = location.motionY;
		eqi.motionZ = location.motionZ;

		if ( location instanceof EntityItem && eqi instanceof EntityItem )
			((EntityItem) eqi).delayBeforeCanPickup = ((EntityItem) location).delayBeforeCanPickup;

		return eqi;
	}

	@Override
	public int getBytes(ItemStack is)
	{
		switch (material)
		{
		case Cell1kPart:
			return 1024;
		case Cell4kPart:
			return 1024 * 4;
		case Cell16kPart:
			return 1024 * 16;
		case Cell64kPart:
			return 1024 * 64;
		default:
		}
		return 0;
	}

	@Override
	public boolean isStorageComponent(ItemStack is)
	{
		switch (material)
		{
		case Cell1kPart:
		case Cell4kPart:
		case Cell16kPart:
		case Cell64kPart:
			return true;
		default:
		}
		return false;
	}

}
