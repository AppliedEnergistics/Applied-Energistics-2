package appeng.util;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.fluids.FluidType;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {

    private static boolean keyTypesInitialized;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
//        SharedConstants.tryDetectVersion();
//        Bootstrap.bootStrap();
//        AppEngBootstrap.runEarlyStartup();
//        Reflection.initialize(AEItems.class);
//        Reflection.initialize(AEBlocks.class);
//        Reflection.initialize(AEBlockEntities.class);
//
//        var configDir = Files.createTempDirectory("ae2config");
//        try {
//            if (AEConfig.instance() == null) {
//                AEConfig.load(configDir);
//            }
//        } finally {
//            MoreFiles.deleteRecursively(configDir, RecursiveDeleteOption.ALLOW_INSECURE);
//        }

        if (!keyTypesInitialized) {
//            try {
//                InitBlocks.init(ForgeRegistries.BLOCKS);
//                InitItems.init(ForgeRegistries.ITEMS);
//            } catch (Throwable e) {
//                throw new RuntimeException(e);
//            }

//            mockForgeFluidTypes();

//            var e = new NewRegistryEvent();
//            var supplier = e.create(new RegistryBuilder<AEKeyType>()
//                    .setMaxID(127)
//                    .setName(AppEng.makeId("keytypes")));
//            var m = NewRegistryEvent.class.getDeclaredMethod("fill");
//            m.setAccessible(true);
//            m.invoke(e);
//            AEKeyTypesInternal.setRegistry(supplier);
//            AEKeyTypes.register(AEKeyType.items());
//            AEKeyTypes.register(AEKeyType.fluids());
            keyTypesInitialized = true;
        }

    }

    private void mockForgeFluidTypes() {
        // Otherwise constructing ANY FluidKey will crash
        var mocked = Mockito.mockStatic(CommonHooks.class, Mockito.CALLS_REAL_METHODS);
        var props = FluidType.Properties.create();
        props.descriptionId("fluid");
        mocked.when(() -> CommonHooks.getVanillaFluidType(any()))
                .thenReturn(new FluidType(props));
    }
}
