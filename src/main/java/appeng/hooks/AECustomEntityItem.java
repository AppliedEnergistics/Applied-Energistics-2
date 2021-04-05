package appeng.hooks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;

public interface AECustomEntityItem {

    /**
     * @return Either a new entity (not added to the world yet), or the original one to keep it.
     */
    Entity replaceItemEntity(ServerWorld world, ItemEntity itemEntity, ItemStack itemStack);

}
