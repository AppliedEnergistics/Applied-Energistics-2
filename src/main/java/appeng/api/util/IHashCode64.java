package appeng.api.util;

public interface IHashCode64 {
    default long hashCode64() {
        return HashHelper.hashInt32(hashCode());
    }
}
