package appeng.datagen.providers.models;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import appeng.api.parts.IPartItem;
import appeng.client.api.model.parts.ClientPart;
import appeng.client.api.model.parts.CompositePartModel;
import appeng.client.api.model.parts.PartModel;
import appeng.client.api.model.parts.StaticPartModel;

public interface PartModelOutput {
    void accept(IPartItem<?> part, PartModel.Unbaked model);

    void accept(IPartItem<?> part, PartModel.Unbaked model, ClientPart.Properties properties);

    void copy(IPartItem<?> item1, IPartItem<?> item2);

    default void staticModel(ItemLike part, ResourceLocation model) {
        if (!(part.asItem() instanceof IPartItem<?> partItem)) {
            throw new IllegalArgumentException("Can only register parts for items that implement IPartItem<?>");
        }
        accept(partItem, new StaticPartModel.Unbaked(model));
    }

    default void composite(ItemLike part, PartModel.Unbaked... models) {
        if (!(part.asItem() instanceof IPartItem<?> partItem)) {
            throw new IllegalArgumentException("Can only register parts for items that implement IPartItem<?>");
        }
        accept(partItem, new CompositePartModel.Unbaked(List.of(models)));
    }

    default void staticModel(ItemLike part) {
        if (!(part.asItem() instanceof IPartItem<?> partItem)) {
            throw new IllegalArgumentException("Can only register parts for items that implement IPartItem<?>");
        }
        staticModel(part, IPartItem.getId(partItem));
    }
}
