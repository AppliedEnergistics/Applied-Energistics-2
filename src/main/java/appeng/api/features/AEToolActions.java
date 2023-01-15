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

package appeng.api.features;

import org.jetbrains.annotations.ApiStatus;

import net.minecraftforge.common.ToolAction;

/**
 * Tool actions defined by AE.
 *
 * @deprecated Check for the standard {@code #forge:tools/wrench} tag instead.
 */
@ApiStatus.ScheduledForRemoval(inVersion = "1.19")
@Deprecated(forRemoval = true)
public final class AEToolActions {
    private AEToolActions() {
    }

    /**
     * An action that is triggered by right-clicking a supported block or part, which will disassemble that block or
     * part into its item form.
     */
    public static final ToolAction WRENCH_DISASSEMBLE = ToolAction.get("wrench_disassemble");

    /**
     * An action that is triggered by shift-right-clicking a supported block or part, which will rotate that part.
     */
    public static final ToolAction WRENCH_ROTATE = ToolAction.get("wrench_rotate");
}
