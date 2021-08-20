package appeng.hooks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface AECustomEntityItem {

    /**
     * @return Either a new entity (not added to the world yet), or the original one to keep it.
     */
    Entity replaceItemEntity(ServerLevel level, ItemEntity location, ItemStack itemStack);

}
