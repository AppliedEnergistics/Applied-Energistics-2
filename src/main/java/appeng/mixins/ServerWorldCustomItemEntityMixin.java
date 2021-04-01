package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import appeng.hooks.AECustomEntityItem;

/**
 * This Mixin will replace newly spawned ItemEntities with custom entities if the item wants it. This is not applied
 * retroactively to entities that are already spawned and just re-added to the world when a chunk is loaded.
 */
@Mixin(value = ServerWorld.class, priority = 9999)
public class ServerWorldCustomItemEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @ModifyVariable(method = { "addEntity" }, at = @At("HEAD"), argsOnly = true)
    public Entity onSpawnEntity(Entity entity) {
        if (entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity) entity;
            ItemStack stack = itemEntity.getItem();
            if (stack == null) {
                return entity;
            }
            Item item = stack.getItem();
            if (item instanceof AECustomEntityItem) {
                ServerWorld self = (ServerWorld) (Object) this;
                entity = ((AECustomEntityItem) item).replaceItemEntity(self, itemEntity, stack);
                if (entity != itemEntity) {
                    // Item may actually want to keep the original
                    itemEntity.remove();
                }
            }
        }
        return entity;
    }

}
