/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;

/**
 * Allows mod to decode their {@link IPatternDetails} from their item stacks. This is required for custom patterns,
 * otherwise the crafting CPU can't properly persist them. Register a single instance to {@link PatternDetailsHelper}.
 */
public interface IPatternDetailsDecoder {
    boolean isEncodedPattern(ItemStack stack);

    @Nullable
    IPatternDetails decodePattern(AEItemKey what, Level level);

    /**
     * Automated recovery is no longer supported, so implementing it is no longer required
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "1.20.4")
    default IPatternDetails decodePattern(ItemStack what, Level level, boolean tryRecovery) {
        return decodePattern(what, level);
    }

    /**
     * Convenience alternative to {@link #decodePattern(AEItemKey, Level)} that takes an ItemStack.
     */
    @Nullable
    default IPatternDetails decodePattern(ItemStack what, Level level) {
        return decodePattern(AEItemKey.of(what), level);
    }
}
