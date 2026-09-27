package appeng.block.networking;

import net.neoforged.neoforge.model.data.ModelData;

import appeng.api.parts.IPartItem;

public record PartRenderState(IPartItem<?> partItem, ModelData modelData) {
}
