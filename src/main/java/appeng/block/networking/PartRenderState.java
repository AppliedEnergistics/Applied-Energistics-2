package appeng.block.networking;

import appeng.api.parts.IPartItem;
import net.neoforged.neoforge.model.data.ModelData;

public record PartRenderState(IPartItem<?> partItem, ModelData modelData) {
}
