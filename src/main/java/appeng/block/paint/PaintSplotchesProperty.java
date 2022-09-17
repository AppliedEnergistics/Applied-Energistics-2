package appeng.block.paint;


import net.minecraftforge.common.property.IUnlistedProperty;


class PaintSplotchesProperty implements IUnlistedProperty<PaintSplotches> {

    @Override
    public String getName() {
        return "paint_splots";
    }

    @Override
    public boolean isValid(PaintSplotches value) {
        return value != null;
    }

    @Override
    public Class<PaintSplotches> getType() {
        return PaintSplotches.class;
    }

    @Override
    public String valueToString(PaintSplotches value) {
        return null;
    }
}
