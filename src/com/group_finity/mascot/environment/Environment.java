package com.group_finity.mascot.environment;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Environment {

    protected abstract Area getWorkArea();

    public abstract Area getActiveIE();

    public abstract void moveActiveIE(final Point point);

    public abstract boolean restoreIE();

    private static Rectangle screenRect = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());

    private static Map<String, Rectangle> screenRects = new HashMap<String, Rectangle>();

    static {

        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //noinspection InfiniteLoopStatement
                    for (; ; ) {
                        updateScreenRect();
                        Thread.sleep(5000);
                    }
                } catch (final InterruptedException ignored) {
                }
            }

        };
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private static void updateScreenRect() {

        Rectangle virtualBounds = new Rectangle();

        Map<String, Rectangle> screenRects = new HashMap<String, Rectangle>();

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gs = ge.getScreenDevices();

        for (int j = gs.length - 1; j >= 0; j--) {
            final GraphicsDevice gd = gs[j];
            screenRects.put(gd.getIDstring(), gd.getDefaultConfiguration().getBounds());
            virtualBounds = virtualBounds.union(gd.getDefaultConfiguration().getBounds());
        }

        Environment.screenRects = screenRects;

        screenRect = virtualBounds;
    }

    /**
     * 画面の領域を取得する. この領域はディスプレイの左上から右下まですべてを含む.
     *
     * @return 画面の領域.
     */
    private static Rectangle getScreenRect() {
        return screenRect;
    }

    /**
     * カーソルの座標を取得する.
     *
     * @return カーソルの座標.
     */
    private static Point getCursorPos() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    public ComplexArea complexScreen = new ComplexArea();

    public Area screen = new Area();

    public Location cursor = new Location();

    protected Environment() {
        tick();
    }

    public void tick() {
        this.screen.set(Environment.getScreenRect());
        this.complexScreen.set(screenRects);
        this.cursor.set(Environment.getCursorPos());
    }

    public Area getScreen() {
        return screen;
    }

    public Collection<Area> getScreens() {
        return complexScreen.getAreas();
    }

    public ComplexArea getComplexScreen() {
        return complexScreen;
    }

    public Location getCursor() {
        return cursor;
    }

    public boolean isScreenTopBottom(final Point location) {


        int count = 0;

        for (Area area : getScreens()) {
            if (area.getTopBorder().isOn(location)) {
                ++count;
            }
            if (area.getBottomBorder().isOn(location)) {
                ++count;
            }
        }


        if (count == 0) {
            if (getWorkArea().getTopBorder().isOn(location)) {
                return true;
            }
            if (getWorkArea().getBottomBorder().isOn(location)) {
                return true;
            }
        }

        return count == 1;
    }

    public boolean isScreenLeftRight(final Point location) {


        int count = 0;

        for (Area area : getScreens()) {
            if (area.getLeftBorder().isOn(location)) {
                ++count;
            }
            if (area.getRightBorder().isOn(location)) {
                ++count;
            }
        }

        if (count == 0) {
            if (getWorkArea().getLeftBorder().isOn(location)) {
                return true;
            }
            if (getWorkArea().getRightBorder().isOn(location)) {
                return true;
            }
        }

        return count == 1;
    }

}
