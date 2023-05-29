package appeng.client.guidebook.compiler.tags;

import java.util.Set;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.block.LytBox;
import appeng.client.guidebook.document.block.LytHBox;
import appeng.client.guidebook.document.block.LytVBox;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class BoxTagCompiler extends BlockTagCompiler {
    private final BoxFlowDirection direction;

    public BoxTagCompiler(BoxFlowDirection direction) {
        this.direction = direction;
    }

    @Override
    public Set<String> getTagNames() {
        return direction == BoxFlowDirection.ROW ? Set.of("Row") : Set.of("Column");
    }

    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        int gap = MdxAttrs.getInt(compiler, parent, el, "gap", 5);

        LytBox box = switch (this.direction) {
            case ROW -> {
                var hbox = new LytHBox();
                hbox.setGap(gap);
                yield hbox;
            }
            default -> {
                var vbox = new LytVBox();
                vbox.setGap(gap);
                yield vbox;
            }
        };

        compiler.compileBlockContext(el.children(), box);

        parent.append(box);
    }
}
