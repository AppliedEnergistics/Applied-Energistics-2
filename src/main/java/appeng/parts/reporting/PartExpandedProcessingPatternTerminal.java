package appeng.parts.reporting;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import static appeng.helpers.PatternHelper.PROCESSING_INPUT_LIMIT;
import static appeng.helpers.PatternHelper.PROCESSING_OUTPUT_LIMIT;


public class PartExpandedProcessingPatternTerminal extends AbstractPartEncoder {
    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/expanded_processing_pattern_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/expanded_processing_pattern_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    @Reflected
    public PartExpandedProcessingPatternTerminal(final ItemStack is) {
        super(is);
        this.crafting = new AppEngInternalInventory(this, PROCESSING_INPUT_LIMIT);
        this.output = new AppEngInternalInventory(this, PROCESSING_OUTPUT_LIMIT);
        this.pattern = new AppEngInternalInventory(this, 2);
    }

    @Override
    public GuiBridge getGuiBridge() {
        return GuiBridge.GUI_EXPANDED_PROCESSING_PATTERN_TERMINAL;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}

