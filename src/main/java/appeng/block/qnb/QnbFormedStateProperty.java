package appeng.block.qnb;


import net.minecraftforge.common.property.IUnlistedProperty;


public class QnbFormedStateProperty implements IUnlistedProperty<QnbFormedState> {

    @Override
    public String getName() {
        return "qnb_formed";
    }

    @Override
    public boolean isValid(QnbFormedState value) {
        return value != null;
    }

    @Override
    public Class<QnbFormedState> getType() {
        return QnbFormedState.class;
    }

    @Override
    public String valueToString(QnbFormedState value) {
        return null;
    }
}
