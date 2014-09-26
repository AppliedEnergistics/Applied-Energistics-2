/*
 * Copyright (c) 2013 Andrew Crocker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package invtweaks.api;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface IItemTree {
    public void registerOre(String category, String name, String oreName, int order);

    boolean matches(List<IItemTreeItem> items, String keyword);

    boolean isKeywordValid(String keyword);

    Collection<IItemTreeCategory> getAllCategories();

    IItemTreeCategory getRootCategory();

    IItemTreeCategory getCategory(String keyword);

    boolean isItemUnknown(String id, int damage);

    List<IItemTreeItem> getItems(String id, int damage);

    List<IItemTreeItem> getItems(String name);

    IItemTreeItem getRandomItem(Random r);

    boolean containsItem(String name);

    boolean containsCategory(String name);

    void setRootCategory(IItemTreeCategory category);

    IItemTreeCategory addCategory(String parentCategory, String newCategory) throws NullPointerException;

    void addCategory(String parentCategory, IItemTreeCategory newCategory) throws NullPointerException;

    IItemTreeItem addItem(String parentCategory, String name, String id, int damage, int order)
            throws NullPointerException;

    void addItem(String parentCategory, IItemTreeItem newItem) throws NullPointerException;

    int getKeywordDepth(String keyword);

    int getKeywordOrder(String keyword);
}
