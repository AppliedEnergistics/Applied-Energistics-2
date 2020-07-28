package appeng.data;

import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.CreativeTab;
import appeng.data.providers.loot.BlockDropProvider;
import appeng.data.providers.recipes.SlabStairRecipes;
import appeng.data.providers.tags.ConventionTagProvider;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.Bootstrap;
import net.minecraft.data.DataGenerator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Entrypoint implements PreLaunchEntrypoint {

    public static void dump() throws Exception {
        Path output = Paths.get("../src/generated/resources");

        DataGenerator generator = new DataGenerator(output, Collections.emptyList());
        generator.install(new BlockDropProvider(output));
        generator.install(new SlabStairRecipes(output));
        generator.install(new ConventionTagProvider(output));
        generator.run();
    }

    @Override
    public void onPreLaunch() {
        if (!"true".equals(System.getProperty("appeng.generateData"))) {
            System.out.println("Applied Energistics 2: Skipping data generation. System property not set.");
            return;
        }

        Bootstrap.initialize();
        AEConfig.load(new File("config"));
        CreativeTab.init(); // Vanilla data gens use item tab associations
        Api.INSTANCE = new Api();

        try {
            dump();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

}
