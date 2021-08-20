package appeng.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEngClientStartup;
import appeng.datagen.AE2DataGenerators;

public class Entrypoint implements ClientModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void dump(Path outputPath, List<Path> existingDataPaths) throws Exception {
        LOGGER.info("Writing generated resources to {}", outputPath.toAbsolutePath());

        DataGenerator generator = new DataGenerator(outputPath, Collections.emptyList());
        var existingFileHelper = new ExistingFileHelper(existingDataPaths, Collections.emptySet(),
                true, null, null);
        AE2DataGenerators.onGatherData(generator, existingFileHelper);
        generator.run();
    }

    @Override
    public void onInitializeClient() {
        if (!"true".equals(System.getProperty("appeng.generateData"))) {
            System.out.println("Applied Energistics 2: Skipping data generation. System property not set.");
            return;
        }

        var outputPath = Paths.get(System.getProperty("appeng.generateData.outputPath"));
        var existingData = System.getProperty("appeng.generateData.existingData").split(";");
        var existingDataPaths = Arrays.stream(existingData).map(Paths::get).toList();

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        new AppEngClientStartup().onInitializeClient();

        try {
            dump(outputPath, existingDataPaths);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

}
