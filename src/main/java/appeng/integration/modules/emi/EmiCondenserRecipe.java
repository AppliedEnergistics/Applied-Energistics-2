package appeng.integration.modules.emi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Splitter;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import appeng.api.config.CondenserOutput;
import appeng.api.implementations.items.IStorageComponent;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.client.gui.Icon;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;

/**
 * Virtual recipe used to represent the types of {@link CondenserOutput} that actually produce can output.
 */
class EmiCondenserRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("condenser",
            EmiStack.of(AEBlocks.CONDENSER), EmiText.CATEGORY_CONDENSER);
    private final CondenserOutput type;
    private final EmiIngredient viableStorageComponents;
    private final EmiStack output;

    public EmiCondenserRecipe(CondenserOutput type) {
        super(CATEGORY, getRecipeId(type), 96, 48);
        this.type = type;
        this.output = EmiStack.of(getOutput(type));
        this.outputs.add(this.output);
        this.viableStorageComponents = getViableStorageComponents(type);
        this.catalysts.add(this.viableStorageComponents);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        var background = AppEng.makeId("textures/guis/condenser.png");
        widgets.addTexture(background, 0, 0, 96, 48, 48, 25);

        widgets.add(new SpriteWidget(Icon.BACKGROUND_TRASH, 3, 27, 16, 16));
        widgets.add(new SpriteWidget(Icon.TOOLBAR_BUTTON_BACKGROUND, 79, 28, 16, 16));
        widgets.addDrawable(79, 28, 16, 16, (draw, mouseX, mouseY, delta) -> {
            draw.blitSprite(Icon.TOOLBAR_BUTTON_BACKGROUND, 0, 0, 16, 16);
        });

        widgets.addAnimatedTexture(background, 72, 0, 6, 18, 176, 0,
                2000, false, true, false);

        if (type == CondenserOutput.MATTER_BALLS) {
            widgets.add(new SpriteWidget(Icon.CONDENSER_OUTPUT_MATTER_BALL, 79, 27, 16, 16));
        } else if (type == CondenserOutput.SINGULARITY) {
            widgets.add(new SpriteWidget(Icon.CONDENSER_OUTPUT_SINGULARITY, 79, 27, 16, 16));
        }
        widgets.addTooltipText(getTooltip(type), 80, 28, 16, 16);

        widgets.addSlot(output, 56, 26).drawBack(false);
        widgets.addSlot(viableStorageComponents, 52, 0).drawBack(false);

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
