package appeng.libs.mdast.gfm.model;


import appeng.libs.mdast.model.MdAstAnyContent;
import appeng.libs.mdast.model.MdAstParent;

public class GfmTableRow extends MdAstParent<GfmTableCell> implements MdAstAnyContent {
    public GfmTableRow() {
        super("tableRow");
    }

    @Override
    protected Class<GfmTableCell> childClass() {
        return GfmTableCell.class;
    }
}
