package vocalshimeji.theme.sakata_gintoki;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.logging.Level;

final class AboutScreen extends JWindow {

    public AboutScreen() {
        JLabel imageLabel = new JLabel();
        imageLabel.setIcon(new ImageIcon(AboutScreen.class.getResource("res/about.png")));
        setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);
        MouseListener closeListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                final int[][] hotArea = {};
                final String[] href = {};
                final int x = e.getX();
                final int y = e.getY();
                final int areaSize = 16;
                for (int i = 0; i < hotArea.length; i++) {
                    int dx = x - hotArea[i][0], dy = y - hotArea[i][1];
                    if (dx >= 0 && dx <= areaSize && dy >= 0 && dy <= areaSize) {
                        try {
                            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + href[i]);
                        } catch (IOException e1) {
                            VocalShimeji.log.log(Level.INFO, "跳转网址失败", e1);
                        }
                    }
                }
                setVisible(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
        addMouseListener(closeListener);
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
    }

}
