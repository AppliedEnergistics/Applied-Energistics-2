package appeng.integration.modules.igtooltip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.BaseClassRegistration;
import appeng.api.parts.IPartHost;

public class BaseClassRegistrationImpl implements BaseClassRegistration {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClassRegistrationImpl.class);

    private final List<BaseClass> baseClasses = new ArrayList<>();
    private final Set<BaseClass> partHostClasses = new HashSet<>();

    @Override
    public void addBaseBlockEntity(Class<? extends BlockEntity> blockEntityClass, Class<? extends Block> blockClass) {
        var defaultClass = new BaseClass(blockEntityClass, blockClass);

        // If any superclass is already in the list, don't add it
        for (var registeredClass : baseClasses) {
            if (registeredClass.isSuperclassOf(defaultClass)) {
                LOGGER.info("Not registering {}, because superclass {} is already registered.",
                        defaultClass, registeredClass);
                return;
            }
        }

        // Remove any subclasses of this class
        baseClasses.removeIf(otherClass -> {
            if (defaultClass.isSuperclassOf(otherClass)) {
                LOGGER.info("Replacing default server-data registration for {} with superclass {}.",
                        defaultClass, otherClass);
                return true;
            }
            return false;
        });

        baseClasses.add(defaultClass);
    }

    @Override
    public <T extends BlockEntity & IPartHost> void addPartHost(Class<T> blockEntityClass,
            Class<? extends Block> blockClass) {
        partHostClasses.add(new BaseClass(blockEntityClass, blockClass));
    }

    public List<BaseClass> getBaseClasses() {
        return baseClasses;
    }

    public Set<BaseClass> getPartHostClasses() {
        return partHostClasses;
    }

    public record BaseClass(Class<? extends BlockEntity> blockEntity, Class<? extends Block> block) {
        public boolean isSuperclassOf(BaseClass other) {
            return blockEntity.isAssignableFrom(other.blockEntity);
        }
    }
}
