/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 - 2015 AlgorithmX2
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

package appeng.api.parts;


import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;


/**
 * A container to store a collection of {@link ResourceLocation} as models for a part as well as other properties.
 */
public interface IPartModel
{

	/**
	 * A solid {@link IPartModel} indicates that the rendering requires a cable connection, which will also result in
	 * creating an intersection for the cable.
	 *
	 * This should be true for pretty much all parts.
	 *
	 * @return true for a solid part.
	 */
	default boolean requireCableConnection()
	{
		return true;
	}

	/**
	 * A collection of {@link ResourceLocation} used as models for a part.
	 *
	 * @return a collection of models, never null.
	 */
	@Nonnull
	default List<ResourceLocation> getModels()
	{
		return Collections.emptyList();
	}

}
