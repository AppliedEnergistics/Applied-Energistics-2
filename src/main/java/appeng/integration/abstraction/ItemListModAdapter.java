package appeng.integration.abstraction;

public interface ItemListModAdapter {

    boolean isEnabled();

    String getShortName();

    default String getSearchText() {
        return "";
    }

    default void setSearchText(String text) {
    }

    default boolean hasSearchFocus() {
        return false;
    }

    static ItemListModAdapter none() {
        return new ItemListModAdapter() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public String getShortName() {
                return "REI/EMI";
            }
        };
    }

}
