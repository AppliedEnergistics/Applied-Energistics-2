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

package appeng.api.features;


import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.ISubItemResolver;


public interface IRecipeHandlerRegistry
{

	/**
	 * Add a new Recipe Handler to the parser.
	 *
	 * MUST BE CALLED IN PRE-INIT
	 *
	 * @param name    name of crafthandler
	 * @param handler class of crafthandler
	 */
	void addNewCraftHandler( String name, Class<? extends ICraftHandler> handler );

	/**
	 * Add a new resolver to the parser.
	 *
	 * MUST BE CALLED IN PRE-INIT
	 *
	 * @param sir sub item resolver
	 */
	void addNewSubItemResolver( ISubItemResolver sir );

	/**
	 * @param name name of crafting handler
	 *
	 * @return A recipe handler by name, returns null on failure.
	 */
	ICraftHandler getCraftHandlerFor( String name );

	/**
	 * @return a new recipe handler, which can be used to parse, and read recipe files.
	 */
	IRecipeHandler createNewRecipehandler();

	/**
	 * resolve sub items by name.
	 *
	 * @param nameSpace namespace of item
	 * @param itemName  full name of item
	 *
	 * @return ResolverResult or ResolverResultSet
	 */
	Object resolveItem( String nameSpace, String itemName );
}
