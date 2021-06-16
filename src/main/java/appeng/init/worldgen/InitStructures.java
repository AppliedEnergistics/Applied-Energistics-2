package appeng.init.worldgen;

import java.util.Locale;

import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.mixins.structure.ConfiguredStructureFeaturesAccessor;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;

public final class InitStructures {

    private InitStructures() {
    }

    public static void init(IForgeRegistry<Structure<?>> registry) {
        MeteoriteStructurePiece.register();

        // Registering into the registry alone is INSUFFICIENT!
        // There's a bidirectional map in the Structure class itself primarily for the
        // purposes of NBT serialization
        registerStructure(registry, MeteoriteStructure.ID.toString(), MeteoriteStructure.INSTANCE,
                GenerationStage.Decoration.TOP_LAYER_MODIFICATION);

        ConfiguredStructureFeaturesAccessor.register(MeteoriteStructure.ID.toString(),
                MeteoriteStructure.CONFIGURED_INSTANCE);
    }

    // This mirrors the Vanilla registration method for structures, but uses the
    // Forge registry instead
    private static <F extends Structure<?>> void registerStructure(IForgeRegistry<Structure<?>> registry,
            String name,
            F structure,
            GenerationStage.Decoration stage) {
        Structure.NAME_STRUCTURE_BIMAP.put(name.toLowerCase(Locale.ROOT), structure);
        Structure.STRUCTURE_DECORATION_STAGE_MAP.put(structure, stage);
        structure.setRegistryName(name.toLowerCase(Locale.ROOT));
        registry.register(structure);
    }

}
