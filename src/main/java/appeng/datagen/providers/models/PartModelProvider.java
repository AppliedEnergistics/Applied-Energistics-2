package appeng.datagen.providers.models;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;

import java.util.Locale;
import java.util.Map;

public class PartModelProvider extends ModelSubProvider {

    public static final TextureSlot BASE = TextureSlot.create("base");
    public static final TextureSlot TYPE = TextureSlot.create("type");

    public PartModelProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        super(blockModels, itemModels);
    }

    @Override
    protected void register() {
        registerCables();
        registerP2PTunnels();

        addBuiltInModel("part/annihilation_plane");
        addBuiltInModel("part/annihilation_plane_on");
        addBuiltInModel("part/identity_annihilation_plane");
        addBuiltInModel("part/identity_annihilation_plane_on");
        addBuiltInModel("part/formation_plane");
        addBuiltInModel("part/formation_plane_on");

        itemModels.declareCustomModelItem(AEParts.QUARTZ_FIBER.asItem());
        itemModels.declareCustomModelItem(AEParts.TOGGLE_BUS.asItem());
        itemModels.declareCustomModelItem(AEParts.INVERTED_TOGGLE_BUS.asItem());
        usePartModelAsItemModel(AEParts.CABLE_ANCHOR);
        itemModels.declareCustomModelItem(AEParts.MONITOR.asItem());
        itemModels.declareCustomModelItem(AEParts.SEMI_DARK_MONITOR.asItem());
        itemModels.declareCustomModelItem(AEParts.DARK_MONITOR.asItem());
        itemModels.declareCustomModelItem(AEParts.STORAGE_BUS.asItem());
        itemModels.declareCustomModelItem(AEParts.IMPORT_BUS.asItem());
        itemModels.declareCustomModelItem(AEParts.EXPORT_BUS.asItem());
        itemModels.declareCustomModelItem(AEParts.LEVEL_EMITTER.asItem());
        itemModels.declareCustomModelItem(AEParts.ENERGY_LEVEL_EMITTER.asItem());
        itemModels.declareCustomModelItem(AEParts.ANNIHILATION_PLANE.asItem());
        itemModels.declareCustomModelItem(AEParts.FORMATION_PLANE.asItem());
        itemModels.declareCustomModelItem(AEParts.PATTERN_ENCODING_TERMINAL.asItem());
        itemModels.declareCustomModelItem(AEParts.CRAFTING_TERMINAL.asItem());
        itemModels.declareCustomModelItem(AEParts.TERMINAL.asItem());
        itemModels.declareCustomModelItem(AEParts.STORAGE_MONITOR.asItem());
        itemModels.declareCustomModelItem(AEParts.CONVERSION_MONITOR.asItem());
        itemModels.declareCustomModelItem(AEParts.PATTERN_PROVIDER.asItem());
        itemModels.declareCustomModelItem(AEParts.INTERFACE.asItem());
        itemModels.declareCustomModelItem(AEParts.PATTERN_ACCESS_TERMINAL.asItem());
        itemModels.declareCustomModelItem(AEParts.ENERGY_ACCEPTOR.asItem());
    }

    private void registerP2PTunnels() {
        addBuiltInModel("part/p2p/p2p_tunnel_frequency");

        var tunnels = Map.of(
                AEParts.ME_P2P_TUNNEL, "part/p2p_tunnel_me",
                AEParts.REDSTONE_P2P_TUNNEL, "part/p2p_tunnel_redstone",
                AEParts.ITEM_P2P_TUNNEL, "part/p2p_tunnel_item",
                AEParts.FLUID_P2P_TUNNEL, "part/p2p_tunnel_fluid",
                AEParts.FE_P2P_TUNNEL, "part/p2p_tunnel_energy",
                AEParts.LIGHT_P2P_TUNNEL, "part/p2p_tunnel_light"
        );

        for (var entry : tunnels.entrySet()) {
            var model = ModelTemplates.createItem("ae2:p2p_tunnel_base", TYPE).create(
                    entry.getKey().asItem(),
                    new TextureMapping().put(TYPE, AppEng.makeId(entry.getValue())),
                    modelOutput);
            blockModels.registerSimpleItemModel(entry.getKey().asItem(), model);
        }
    }

    private void usePartModelAsItemModel(ItemDefinition<?> partItem) {
        blockModels.registerSimpleItemModel(partItem.asItem(), partItem.id().withPrefix("part/"));
    }

    private void registerCables() {
        buildCableItems(AEParts.GLASS_CABLE, "ae2:glass_cable_base", "part/cable/glass/");
        buildCableItems(AEParts.COVERED_CABLE, "ae2:covered_cable_base", "part/cable/covered/");
        buildCableItems(AEParts.COVERED_DENSE_CABLE, "ae2:covered_dense_cable_base", "part/cable/dense_covered/");
        buildCableItems(AEParts.SMART_CABLE, "ae2:smart_cable_base", "part/cable/smart/");
        buildCableItems(AEParts.SMART_DENSE_CABLE, "ae2:smart_dense_cable_base", "part/cable/dense_smart/");
    }

    private void buildCableItems(ColoredItemDefinition<?> cable, String baseModel, String textureBase) {
        for (var color : AEColor.values()) {
            var item = cable.item(color);
            var cableTexture = AppEng.makeId(textureBase + color.name().toLowerCase(Locale.ROOT));
            var model = ModelTemplates.createItem(baseModel, BASE).create(
                    item,
                    new TextureMapping().put(BASE, cableTexture),
                    modelOutput);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        }
    }

    /**
     * The files need to exist for Fabric's post-processor to pick them up. The content is ignored. For Forge, we still
     * set the loader name, since it'll be used there.
     */
    private void addBuiltInModel(String name) {
        EMPTY_MODEL.create(AppEng.makeId(name), TRANSPARENT_PARTICLE, modelOutput);
    }
}
