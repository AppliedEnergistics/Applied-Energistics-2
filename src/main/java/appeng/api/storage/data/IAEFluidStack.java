/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

package appeng.api.storage.data;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

import appeng.util.fluid.AEFluidStack;

/**
 * An alternate version of FluidStack for AE to keep tabs on things easier, and to support larger storage. stackSizes of
 * getFluidStack will be capped.
 *
 * You may hold on to these if you want, just make sure you let go of them when your not using them.
 *
 * Don't Implement.
 *
 * Construct with IAppEngApi.instance().storage().getStorageChannel( IFluidStorageChannel.class).createStack( FluidStack
 * )
 */
public interface IAEFluidStack extends IAEStack {
    static IAEFluidStack of(FluidVariant fluid, long amount) {
        return AEFluidStack.of(fluid, amount);
    }

    static IAEFluidStack of(ResourceAmount<FluidVariant> fluidStack) {
        return AEFluidStack.of(fluidStack.resource(), fluidStack.amount());
    }

    /**
     * create a AE Fluid clone.
     *
     * @return the copy.
     */
    IAEFluidStack copy();

    /**
     * quick way to get access to the Forge Fluid Definition.
     *
     * @return fluid definition
     */
    FluidVariant getFluid();

    default ResourceAmount<FluidVariant> getFluidStack() {
        return new ResourceAmount<>(getFluid(), getStackSize());
    }
}
