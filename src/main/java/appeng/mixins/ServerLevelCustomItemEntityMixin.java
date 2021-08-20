package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.hooks.AECustomEntityItem;

/**
 * This Mixin will replace newly spawned ItemEntities with custom entities if the item wants it. This is not applied
 * retroactively to entities that are already spawned and just re-added to the world when a chunk is loaded.
 */
@Mixin(value = ServerLevel.class, priority = 9999)
public class ServerLevelCustomItemEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @ModifyVariable(method = { "addEntity" }, at = @At("HEAD"), argsOnly = true)
    public Entity onSpawnEntity(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack == null) {
                return entity;
            }
            Item item = stack.getItem();
            if (item instanceof AECustomEntityItem) {
                ServerLevel self = (ServerLevel) (Object) this;
                entity = ((AECustomEntityItem) item).replaceItemEntity(self, itemEntity, stack);
                if (entity != itemEntity) {
                    // Item may actually want to keep the original
                    itemEntity.discard();
                }
            }
        }
        return entity;
    }

}
