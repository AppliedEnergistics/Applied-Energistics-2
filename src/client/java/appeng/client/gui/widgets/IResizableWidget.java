package appeng.client.gui.widgets;

import appeng.client.Point;

public interface IResizableWidget {

    void move(Point pos);

    void resize(int width, int height);

}
