package appeng.client.guidebook.scene;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.tags.BlockTagCompiler;
import appeng.client.guidebook.compiler.tags.MdxAttrs;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class ItemImageTagCompiler extends BlockTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var item = MdxAttrs.getRequiredItem(compiler, parent, el, "id");
        var scale = MdxAttrs.getFloat(compiler, parent, el, "scale", 1.0f);

        if (item != null) {
            var itemImage = new LytItemImage();
            itemImage.setItem(item.getDefaultInstance());
            itemImage.setScale(scale);
            parent.append(itemImage);
        }
    }
}
