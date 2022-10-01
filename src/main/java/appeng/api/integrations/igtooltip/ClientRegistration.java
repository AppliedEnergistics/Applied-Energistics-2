package appeng.api.integrations.igtooltip;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface ClientRegistration {
    default <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id,
            BodyProvider<? super T> provider) {
        addBlockEntityBody(blockEntityClass, blockClass, id, provider, 1000);
    }

    <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id,
            BodyProvider<? super T> provider,
            int priority);

    default <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id,
            IconProvider<? super T> provider) {
        addBlockEntityIcon(blockEntityClass, blockClass, id, provider, TooltipProvider.DEFAULT_PRIORITY);
    }

    <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id,
            IconProvider<? super T> provider,
            int priority);

    default <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id,
            NameProvider<? super T> provider) {
        addBlockEntityName(blockEntityClass, blockClass, id, provider, TooltipProvider.DEFAULT_PRIORITY);
    }

    <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id, NameProvider<? super T> provider,
            int priority);

    default <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id,
            ModNameProvider<? super T> provider) {
        addBlockEntityModName(blockEntityClass, blockClass, id, provider, 1000);
    }

    <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass,
            Class<? extends Block> blockClass,
            ResourceLocation id,
            ModNameProvider<? super T> provider,
            int priority);

}
