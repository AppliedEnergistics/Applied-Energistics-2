package appeng.client.api.model.parts;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Subscribe to this event on your mod event bus to register part model types with AE2.
 */
public class RegisterPartModelsEvent extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends PartModel.Unbaked>> modelIdMapper;

    @ApiStatus.Internal
    public RegisterPartModelsEvent(
            ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends PartModel.Unbaked>> modelIdMapper) {
        this.modelIdMapper = modelIdMapper;
    }

    public void registerModelType(Identifier type, MapCodec<? extends PartModel.Unbaked> codec) {
        this.modelIdMapper.put(type, codec);
    }
}
