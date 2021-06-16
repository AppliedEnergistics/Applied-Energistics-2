package appeng.items.materials;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.items.AEBaseItem;

/**
 * Used for items that use a different entity for when they're dropped.
 */
public class CustomEntityItem extends AEBaseItem {
    private final EntityFactory factory;

    public CustomEntityItem(Properties properties, EntityFactory factory) {
        super(properties);
        this.factory = factory;
    }

    @Override
    public boolean hasCustomEntity(final ItemStack is) {
        return true;
    }

    @Override
    public Entity createEntity(final World w, final Entity location, final ItemStack itemstack) {
        ItemEntity eqi = factory.create(w, location.getPosX(), location.getPosY(), location.getPosZ(),
                itemstack);

        eqi.setMotion(location.getMotion());

        if (location instanceof ItemEntity) {
            eqi.setDefaultPickupDelay();
        }

        return eqi;
    }

    @FunctionalInterface
    public interface EntityFactory {
        ItemEntity create(World w, double x, double y, double z, ItemStack is);
    }

}
