package appeng.datagen.providers.models;

import appeng.api.util.AEColor;
import appeng.client.api.model.parts.StaticPartModel;
import appeng.client.model.CableAnchorPartModel;
import appeng.client.model.LevelEmitterPartModel;
import appeng.client.model.LockableMonitorPartModel;
import appeng.client.model.P2PFrequencyPartModel;
import appeng.client.model.PlanePartModel;
import appeng.client.model.StatusIndicatorPartModel;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static appeng.core.AppEng.makeId;

public class PartModelProvider extends ModelSubProvider {

    public static final TextureSlot BASE = TextureSlot.create("base");
    public static final TextureSlot TYPE = TextureSlot.create("type");

    public PartModelProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels, PartModelOutput partModels) {
        super(blockModels, itemModels, partModels);
    }

    @Override
    protected void register() {
        registerCables();
        registerP2PTunnels();

        partModels.staticModel(AEParts.QUARTZ_FIBER, makeId("part/quartz_fiber"));
        itemModels.declareCustomModelItem(AEParts.QUARTZ_FIBER.asItem());
        partModels.composite(AEParts.TOGGLE_BUS,
                new StaticPartModel.Unbaked(makeId("part/toggle_bus_base")),
                partStatusIndicator(makeId("part/toggle_bus_status"))
        );
        itemModels.declareCustomModelItem(AEParts.TOGGLE_BUS.asItem());
        partModels.composite(AEParts.INVERTED_TOGGLE_BUS,
                new StaticPartModel.Unbaked(makeId("part/inverted_toggle_bus_base")),
                partStatusIndicator(makeId("part/toggle_bus_status"))
        );
        itemModels.declareCustomModelItem(AEParts.INVERTED_TOGGLE_BUS.asItem());
        partModels.accept(AEParts.CABLE_ANCHOR.asItem(), new CableAnchorPartModel.Unbaked(
                makeId("part/cable_anchor"),
                makeId("part/cable_anchor_short")
        ));
        usePartModelAsItemModel(AEParts.CABLE_ANCHOR);
        partModels.composite(AEParts.MONITOR,
                new StaticPartModel.Unbaked(makeId("part/display_base")),
                new StatusIndicatorPartModel.Unbaked(
                        makeId("part/monitor_bright_on"),
                        makeId("part/monitor_bright_off"),
                        makeId("part/monitor_bright_off")
                )
        );
        itemModels.declareCustomModelItem(AEParts.MONITOR.asItem());
        partModels.composite(AEParts.SEMI_DARK_MONITOR,
                new StaticPartModel.Unbaked(makeId("part/display_base")),
                new StatusIndicatorPartModel.Unbaked(
                        makeId("part/monitor_medium_on"),
                        makeId("part/monitor_medium_off"),
                        makeId("part/monitor_medium_off")
                )
        );
        itemModels.declareCustomModelItem(AEParts.SEMI_DARK_MONITOR.asItem());
        partModels.composite(AEParts.DARK_MONITOR,
                new StaticPartModel.Unbaked(makeId("part/display_base")),
                new StatusIndicatorPartModel.Unbaked(
                        makeId("part/monitor_dark_on"),
                        makeId("part/monitor_dark_off"),
                        makeId("part/monitor_dark_off")
                )
        );
        itemModels.declareCustomModelItem(AEParts.DARK_MONITOR.asItem());
        partModels.composite(AEParts.STORAGE_BUS,
                new StaticPartModel.Unbaked(makeId("part/storage_bus_base")),
                partStatusIndicator(makeId("part/storage_bus"))
        );
        itemModels.declareCustomModelItem(AEParts.STORAGE_BUS.asItem());
        partModels.composite(AEParts.IMPORT_BUS,
                new StaticPartModel.Unbaked(makeId("part/import_bus_base")),
                partStatusIndicator(makeId("part/import_bus"))
        );
        itemModels.declareCustomModelItem(AEParts.IMPORT_BUS.asItem());
        partModels.composite(AEParts.EXPORT_BUS,
                new StaticPartModel.Unbaked(makeId("part/export_bus_base")),
                partStatusIndicator(makeId("part/export_bus"))
        );
        itemModels.declareCustomModelItem(AEParts.EXPORT_BUS.asItem());
        for (var part : List.of(AEParts.LEVEL_EMITTER, AEParts.ENERGY_LEVEL_EMITTER)) {
            itemModels.declareCustomModelItem(part.asItem());
            partModels.composite(
                    part,
                    new LevelEmitterPartModel.Unbaked(
                            makeId("part/level_emitter_base_on"),
                            makeId("part/level_emitter_base_off")
                    ),
                    partStatusIndicator(makeId("part/level_emitter_status"))
            );
        }
        partModels.composite(
                AEParts.ANNIHILATION_PLANE,
                new PlanePartModel.Unbaked(
                        makeId("part/annihilation_plane"),
                        makeId("part/plane_sides"),
                        makeId("part/transition_plane_back")
                )
        );
        itemModels.declareCustomModelItem(AEParts.ANNIHILATION_PLANE.asItem());
        partModels.composite(
                AEParts.FORMATION_PLANE,
                new PlanePartModel.Unbaked(
                        makeId("part/formation_plane"),
                        makeId("part/plane_sides"),
                        makeId("part/transition_plane_back")
                )
        );
        itemModels.declareCustomModelItem(AEParts.FORMATION_PLANE.asItem());
        terminal(AEParts.PATTERN_ENCODING_TERMINAL, makeId("part/pattern_encoding_terminal"));
        itemModels.declareCustomModelItem(AEParts.PATTERN_ENCODING_TERMINAL.asItem());
        terminal(AEParts.CRAFTING_TERMINAL, makeId("part/crafting_terminal"));
        itemModels.declareCustomModelItem(AEParts.CRAFTING_TERMINAL.asItem());
        terminal(AEParts.TERMINAL, makeId("part/terminal"));
        itemModels.declareCustomModelItem(AEParts.TERMINAL.asItem());
        partModels.composite(AEParts.STORAGE_MONITOR,
                new StaticPartModel.Unbaked(makeId("part/display_base")),
                new LockableMonitorPartModel.Unbaked(
                        makeId("part/storage_monitor_off"),
                        makeId("part/storage_monitor_on"),
                        makeId("part/storage_monitor_locked_off"),
                        makeId("part/storage_monitor_locked_on")
                ),
                partStatusIndicator(makeId("part/display_status"))
        );
        itemModels.declareCustomModelItem(AEParts.STORAGE_MONITOR.asItem());
        partModels.composite(AEParts.CONVERSION_MONITOR,
                new StaticPartModel.Unbaked(makeId("part/display_base")),
                new LockableMonitorPartModel.Unbaked(
                        makeId("part/conversion_monitor_off"),
                        makeId("part/conversion_monitor_on"),
                        makeId("part/conversion_monitor_locked_off"),
                        makeId("part/conversion_monitor_locked_on")
                ),
                partStatusIndicator(makeId("part/display_status"))
        );
        itemModels.declareCustomModelItem(AEParts.CONVERSION_MONITOR.asItem());
        partModels.composite(AEParts.PATTERN_PROVIDER,
                new StaticPartModel.Unbaked(makeId("part/pattern_provider_base")),
                partStatusIndicator(makeId("part/interface"))
        );
        itemModels.declareCustomModelItem(AEParts.PATTERN_PROVIDER.asItem());
        partModels.composite(AEParts.INTERFACE,
                new StaticPartModel.Unbaked(makeId("part/interface_base")),
                partStatusIndicator(makeId("part/interface"))
        );
        itemModels.declareCustomModelItem(AEParts.INTERFACE.asItem());
        terminal(AEParts.PATTERN_ACCESS_TERMINAL, makeId("part/pattern_access_terminal"));
        itemModels.declareCustomModelItem(AEParts.PATTERN_ACCESS_TERMINAL.asItem());
        partModels.staticModel(AEParts.ENERGY_ACCEPTOR, makeId("part/energy_acceptor"));
        itemModels.declareCustomModelItem(AEParts.ENERGY_ACCEPTOR.asItem());
    }

    private static StatusIndicatorPartModel.Unbaked partStatusIndicator(ResourceLocation baseModel) {
        return new StatusIndicatorPartModel.Unbaked(
                baseModel.withSuffix("_has_channel"),
                baseModel.withSuffix("_on"),
                baseModel.withSuffix("_off")
        );
    }

    private void terminal(ItemLike part, ResourceLocation modelBase) {
        partModels.composite(part,
                new StaticPartModel.Unbaked(makeId("part/display_base")),
                new StatusIndicatorPartModel.Unbaked(
                        modelBase.withSuffix("_on"),
                        modelBase.withSuffix("_on"),
                        modelBase.withSuffix("_off")
                ),
                partStatusIndicator(makeId("part/display_status"))
        );
    }

    private void registerP2PTunnels() {
        var tunnels = Map.of(
                AEParts.ME_P2P_TUNNEL, "part/p2p_tunnel_me",
                AEParts.REDSTONE_P2P_TUNNEL, "part/p2p_tunnel_redstone",
                AEParts.ITEM_P2P_TUNNEL, "part/p2p_tunnel_item",
                AEParts.FLUID_P2P_TUNNEL, "part/p2p_tunnel_fluid",
                AEParts.FE_P2P_TUNNEL, "part/p2p_tunnel_fe",
                AEParts.LIGHT_P2P_TUNNEL, "part/p2p_tunnel_light");

        for (var entry : tunnels.entrySet()) {
            var model = ModelTemplates.createItem("ae2:p2p_tunnel_base", TYPE).create(
                    entry.getKey().asItem(),
                    new TextureMapping().put(TYPE, makeId(entry.getValue())),
                    modelOutput);
            blockModels.registerSimpleItemModel(entry.getKey().asItem(), model);
            partModels.composite(
                    entry.getKey(),
                    new StaticPartModel.Unbaked(makeId(entry.getValue())),
                    new P2PFrequencyPartModel.Unbaked(),
                    partStatusIndicator(makeId("part/p2p_tunnel_status"))
            );
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
            var cableTexture = makeId(textureBase + color.name().toLowerCase(Locale.ROOT));
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
        EMPTY_MODEL.create(makeId(name), TRANSPARENT_PARTICLE, modelOutput);
    }
}
