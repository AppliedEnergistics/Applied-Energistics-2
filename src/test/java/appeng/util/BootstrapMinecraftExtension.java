package appeng.util;

import com.google.common.reflect.Reflection;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.core.AppEng;
import appeng.core.AppEngBootstrap;
import appeng.core.CreativeTab;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {

    private static boolean keyTypesInitialized;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        AppEngBootstrap.runEarlyStartup();
        Reflection.initialize(AEItems.class);
        Reflection.initialize(AEBlocks.class);
        Reflection.initialize(AEBlockEntities.class);
        CreativeTab.init();

        if (!keyTypesInitialized) {
            var e = new NewRegistryEvent();
            var supplier = e.create(new RegistryBuilder<AEKeyType>()
                    .setType(AEKeyType.class)
                    .setMaxID(127)
                    .setName(AppEng.makeId("keytypes")));
            ReflectionUtils.invokeMethod(NewRegistryEvent.class.getDeclaredMethod("fill"), e);
            AEKeyTypesInternal.setRegistry(supplier);
            AEKeyTypes.register(AEKeyType.items());
            AEKeyTypes.register(AEKeyType.fluids());
            keyTypesInitialized = true;
        }
    }
}
