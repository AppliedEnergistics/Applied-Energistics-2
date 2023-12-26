package appeng.integration.modules.emi;

import appeng.api.config.CondenserOutput;
import appeng.api.implementations.items.IStorageComponent;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;
import com.google.common.base.Splitter;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Virtual recipe used to represent the types of {@link CondenserOutput} that actually produce can output.
 */
public class EmiCondenserRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("condenser", EmiStack.of(AEBlocks.CONDENSER), EmiText.CATEGORY_CONDENSER);
    private final CondenserOutput type;
    private final EmiIngredient viableStorageComponents;
    private final EmiStack output;

    public EmiCondenserRecipe(CondenserOutput type) {
        super(CATEGORY, getRecipeId(type), 94, 48);
        this.type = type;
        this.output = EmiStack.of(getOutput(type));
        this.outputs.add(this.output);
        this.viableStorageComponents = getViableStorageComponents(type);
        this.catalysts.add(this.viableStorageComponents);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        var background = AppEng.makeId("textures/guis/condenser.png");
        widgets.addTexture(background, 0, 0, 94, 48, 50, 25);

        var statesLocation = AppEng.makeId("textures/guis/states.png");
        widgets.addTexture(statesLocation, 2, 28, 14, 14, 241, 81);
        widgets.addTexture(statesLocation, 78, 28, 16, 16, 240, 240);

        widgets.addAnimatedTexture(background, 70, 0, 6, 18, 178, 25,
                2000, false, true, false
        );

        if (type == CondenserOutput.MATTER_BALLS) {
            widgets.addTexture(statesLocation, 78, 28, 14, 14, 16, 112);
        } else if (type == CondenserOutput.SINGULARITY) {
            widgets.addTexture(statesLocation, 78, 28, 14, 14, 32, 112);
        }
        widgets.addTooltipText(getTooltip(type), 78, 28, 16, 16);

        widgets.addSlot(output, 54, 26).drawBack(false);
        widgets.addSlot(viableStorageComponents, 50, 0).drawBack(false);

    }

    private static ItemStack getOutput(CondenserOutput recipe) {
        return switch (recipe) {
            case MATTER_BALLS -> AEItems.MATTER_BALL.stack();
            case SINGULARITY -> AEItems.SINGULARITY.stack();
            default -> ItemStack.EMPTY;
        };
    }

    private EmiIngredient getViableStorageComponents(CondenserOutput condenserOutput) {
        List<EmiStack> viableComponents = new ArrayList<>();
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_1K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_4K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_16K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_64K);
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_256K);
        return EmiIngredient.of(viableComponents);
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<EmiStack> viableComponents,
                                    ItemLike item) {
        IStorageComponent comp = (IStorageComponent) item.asItem();
        int storage = comp.getBytes(item.asItem().getDefaultInstance()) * CondenserBlockEntity.BYTE_MULTIPLIER;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(EmiStack.of(item));
        }
    }

    private static ResourceLocation getRecipeId(CondenserOutput type) {
        return AppEng.makeId(type.name().toLowerCase(Locale.ROOT));
    }

    private List<Component> getTooltip(CondenserOutput type) {
        String key;
        switch (type) {
            case MATTER_BALLS:
                key = ButtonToolTips.MatterBalls.getTranslationKey();
                break;
            case SINGULARITY:
                key = ButtonToolTips.Singularity.getTranslationKey();
                break;
            default:
                return Collections.emptyList();
        }

        return Splitter.on("\n")
                .splitToList(Component.translatable(key, type.requiredPower).getString())
                .stream()
                .<Component>map(Component::literal)
                .toList();
    }

}
