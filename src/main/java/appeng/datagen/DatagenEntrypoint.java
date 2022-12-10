package appeng.datagen;

import java.nio.file.Paths;
import java.util.Arrays;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class DatagenEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var existingData = System.getProperty("appeng.datagen.existingData").split(";");
        var existingDataPaths = Arrays.stream(existingData).map(Paths::get).toList();
        AE2DataGenerators.onGatherData(generator, new ExistingFileHelper(existingDataPaths, true),
                generator.createPack());
    }
}
