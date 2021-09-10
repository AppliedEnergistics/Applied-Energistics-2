package appeng.core.api;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.crafting.IPatternDetailsHelper;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.crafting.pattern.PatternDetailsAdapter;

public class ApiPatternDetails implements IPatternDetailsHelper {
    private final List<IPatternDetailsDecoder> decoders = new CopyOnWriteArrayList<>();

    public ApiPatternDetails() {
        // Register support for our own stacks.
        // TODO: get rid of this hack when we get rid of the overlap in ApiCrafting!
        ApiCrafting crafting = new ApiCrafting();
        registerDecoder(new IPatternDetailsDecoder() {
            @Override
            public boolean isEncodedPattern(ItemStack stack) {
                return crafting.isEncodedPattern(stack);
            }

            @Nullable
            @Override
            public IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery) {
                return PatternDetailsAdapter.adapt(crafting.decodePattern(stack, level, autoRecovery));
            }
        });
    }

    @Override
    public void registerDecoder(IPatternDetailsDecoder decoder) {
        Objects.requireNonNull(decoder);
        decoders.add(decoder);
    }

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        for (var decoder : decoders) {
            if (decoder.isEncodedPattern(stack)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery) {
        for (var decoder : decoders) {
            var decoded = decoder.decodePattern(stack, level, autoRecovery);
            if (decoded != null) {
                return decoded;
            }
        }
        return null;
    }
}
