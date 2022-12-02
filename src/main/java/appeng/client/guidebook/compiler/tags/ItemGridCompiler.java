package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.block.LytItemGrid;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class ItemGridCompiler extends BlockTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var itemGrid = new LytItemGrid();

        // We expect children to only contain ItemIcon elements
        for (var childNode : el.children()) {
            if (childNode instanceof MdxJsxElementFields jsxChild && "ItemIcon".equals(jsxChild.name())) {
                var item = MdxAttrs.getRequiredItem(compiler, parent, jsxChild, "id");
                if (item != null) {
                    itemGrid.addItem(item);
                }

                continue;
            }
            parent.appendError(compiler, "Unsupported child-element in ItemGrid", childNode);
        }

        parent.append(itemGrid);
    }
}
