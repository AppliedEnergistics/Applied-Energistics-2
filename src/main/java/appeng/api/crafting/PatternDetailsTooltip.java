package appeng.api.crafting;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.GuiText;
import appeng.util.AECodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Properties shown in the tooltip of an encoded pattern. Used for both valid and invalid encoded patterns. For invalid
 * patterns, only partial information might be given.
 */
public class PatternDetailsTooltip {

    public static final StreamCodec<RegistryFriendlyByteBuf, PatternDetailsTooltip> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_STREAM_CODEC, PatternDetailsTooltip::getOutputMethod,
            Property.STREAM_CODEC.apply(ByteBufCodecs.list()), PatternDetailsTooltip::getProperties,
            GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()), PatternDetailsTooltip::getInputs,
            GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()), PatternDetailsTooltip::getOutputs,
            PatternDetailsTooltip::new
    );

    /**
     * The text to use when the pattern uses Vanilla crafting as its method of producing the item. Usually reserved for
     * patterns used in molecular assemblers.
     */
    public static final Component OUTPUT_TEXT_CRAFTS = GuiText.Crafts.text();

    /**
     * The text to use when the pattern uses some other form of processing to produce the output.
     */
    public static final Component OUTPUT_TEXT_PRODUCES = GuiText.Produces.text();

    private Component outputMethod;

    private final List<Property> additionalProperties = new ArrayList<>();

    private final List<GenericStack> inputs = new ArrayList<>();

    private final List<GenericStack> outputs = new ArrayList<>();

    /**
     * @param outputMethod The method of producing the outputs of this pattern ({@link #OUTPUT_TEXT_CRAFTS} or
     *                     {@link #OUTPUT_TEXT_PRODUCES}). Usually this will depend on the type of pattern and not so
     *                     much the individual NBT data.
     */
    public PatternDetailsTooltip(Component outputMethod) {
        setOutputMethod(outputMethod);
    }

    private PatternDetailsTooltip(Component outputMethod,
                                  List<Property> additionalProperties,
                                  List<GenericStack> inputs,
                                  List<GenericStack> outputs) {
        setOutputMethod(outputMethod);
        this.additionalProperties.addAll(additionalProperties);
        this.inputs.addAll(inputs);
        this.outputs.addAll(outputs);
    }

    /**
     * @param outputMethod The method of producing the outputs of this pattern ({@link #OUTPUT_TEXT_CRAFTS} or
     *                     {@link #OUTPUT_TEXT_PRODUCES}). Usually this will depend on the type of pattern and not so
     *                     much the individual NBT data.
     */
    public void setOutputMethod(Component outputMethod) {
        this.outputMethod = Objects.requireNonNull(outputMethod, "outputMethod");
    }

    public List<Property> getProperties() {
        return additionalProperties;
    }

    public List<GenericStack> getInputs() {
        return inputs;
    }

    public List<GenericStack> getOutputs() {
        return outputs;
    }

    public void addInput(AEKey what, long amount) {
        inputs.add(new GenericStack(what, amount));
    }

    public void addInput(GenericStack stack) {
        inputs.add(new GenericStack(stack.what(), stack.amount()));
    }

    public void addOutput(AEKey what, long amount) {
        outputs.add(new GenericStack(what, amount));
    }

    public void addOutput(GenericStack stack) {
        outputs.add(new GenericStack(stack.what(), stack.amount()));
    }

    public void addProperty(Component name, Component value) {
        this.additionalProperties.add(new Property(name, value));
    }

    public void addProperty(Component description) {
        this.additionalProperties.add(new Property(description, null));
    }

    public void addInputsAndOutputs(IPatternDetails details) {
        for (var input : details.getInputs()) {
            if (input == null) {
                continue;
            }

            addInput(input.getPossibleInputs()[0].what(),
                    input.getPossibleInputs()[0].amount() * input.getMultiplier());
        }

        for (var output : details.getOutputs()) {
            if (output == null) {
                continue;
            }

            addOutput(output.what(), output.amount());
        }
    }

    /**
     * An additional property to display on the patterns tooltip, such as whether substitutions of items was enabled or
     * not.
     */
    public record Property(Component name, @Nullable Component value) {
        public static StreamCodec<RegistryFriendlyByteBuf, Property> STREAM_CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC, Property::name,
                ComponentSerialization.STREAM_CODEC.apply(AECodecs::nullable), Property::value,
                Property::new
        );
    }

    public Component getOutputMethod() {
        return outputMethod;
    }
}
