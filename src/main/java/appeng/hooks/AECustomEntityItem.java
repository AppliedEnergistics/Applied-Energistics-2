package appeng.hooks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

public interface AECustomEntityItem {

    /**
     * @return Either a new entity (not added to the world yet), or the original one to keep it.
     */
    Entity replaceItemEntity(ServerWorld world, ItemEntity itemEntity, ItemStack itemStack);

}
