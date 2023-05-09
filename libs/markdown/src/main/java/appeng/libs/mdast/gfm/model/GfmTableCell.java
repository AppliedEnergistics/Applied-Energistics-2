package appeng.libs.mdast.gfm.model;

import appeng.libs.mdast.model.MdAstAnyContent;
import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstPhrasingContent;
import appeng.libs.unist.UnistNode;

public class GfmTableCell extends MdAstParent<MdAstPhrasingContent> implements MdAstAnyContent {
    public GfmTableCell() {
        super("tableCell");
    }

    @Override
    protected Class<MdAstPhrasingContent> childClass() {
        return MdAstPhrasingContent.class;
    }
}
