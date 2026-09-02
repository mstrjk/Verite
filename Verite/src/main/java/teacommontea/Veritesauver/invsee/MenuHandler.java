package teacommontea.veritesauver.invsee;


public interface MenuHandler {

    void clickedEntry(Object menu);

    void beforeClick(Object menu);

    void afterClick(Object menu);

    boolean tracks();

    Object quickMove(Object menu, Object nmsPlayer, int rawIndex);

    void removed(Object menu, Object nmsPlayer);

    Object bukkitView(Object menu);
}
