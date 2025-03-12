package appeng.init.client;

import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;

import appeng.client.item.ColorApplicatorItemModel;
import appeng.client.render.model.MemoryCardItemModel;

public class InitItemModels {
    private InitItemModels() {
    }

    public static void init(RegisterItemModelsEvent event) {
        event.register(ColorApplicatorItemModel.Unbaked.ID, ColorApplicatorItemModel.Unbaked.MAP_CODEC);
        event.register(MemoryCardItemModel.Unbaked.ID, MemoryCardItemModel.Unbaked.MAP_CODEC);
    }
}
