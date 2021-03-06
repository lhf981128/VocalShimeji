package com.group_finity.mascot;

import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.sound.SoundBuffer;
import com.group_finity.mascot.sound.SoundFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class MascotPopupMenu {
    private static final Logger log = Logger.getLogger(MascotEventHandler.class.getName());
    final Main main;
    final ActionListener restoreMenuListener;
    final ActionListener increaseMenuListener;
    final ActionListener gatherMenuListener;
    final ActionListener oneMenuListener;
    final ActionListener aboutMenuListener;
    final ActionListener closeMenuListener;
    final ActionListener configDialogMenuListener;
    final CheckboxMenuItem voiceMenu;
    final JCheckBoxMenuItem jVoiceMenu;
    final CheckboxMenuItem sfxMenu;
    final JCheckBoxMenuItem jSfxMenu;
    final JMenuItem disposeMenu;
    final JPopupMenu popupMenu;
    volatile Mascot targetmascot;

    MascotPopupMenu(Main obj) {
        this.main = obj;
        restoreMenuListener = new ActionListener() {
            final SoundBuffer voice = main.soundFactory.getSound(main.resourceBundle.getString("sound.cmd_restore_window"));
            final Runnable cmd = new Runnable() {

                final SoundBuffer voiceFalse = main.soundFactory.getSound(main.resourceBundle.getString("sound.response_nothing_reset"));
                final SoundBuffer voiceTrue = main.soundFactory.getSound(main.resourceBundle.getString("sound.response_reset"));

                @Override
                public void run() {
                    if (NativeFactory.getInstance().getEnvironment().restoreIE()) {
                        SoundFactory.invokeAfterSound(voiceTrue, null);
                    } else {
                        SoundFactory.invokeAfterSound(voiceFalse, null);
                    }
                }
            };

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundFactory.invokeAfterSound(voice, cmd);
            }
        };
        increaseMenuListener = new ActionListener() {
            final SoundBuffer sound = main.soundFactory.getSound(main.resourceBundle.getString("sound.cmd_one_more_mascot"));
            final Runnable cmd = new Runnable() {

                @Override
                public void run() {
                    main.manager.createMascot();
                }

            };

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundFactory.invokeAfterSound(sound, cmd);
            }
        };
        gatherMenuListener = new ActionListener() {
            final SoundBuffer sound = main.soundFactory.getSound(main.resourceBundle.getString("sound.cmd_together"));
            final Runnable cmd = new Runnable() {

                @Override
                public void run() {
                    main.gatherAll();
                }
            };

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundFactory.invokeAfterSound(sound, cmd);
            }
        };
        oneMenuListener = new ActionListener() {
            final SoundBuffer sound = main.soundFactory.getSound(main.resourceBundle.getString("sound.cmd_remain_one_mascot"));
            final Runnable cmd = new Runnable() {

                @Override
                public void run() {
                    main.remainOne();
                }
            };

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundFactory.invokeAfterSound(sound, cmd);
            }
        };
        aboutMenuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.showAbout();
            }
        };
        closeMenuListener = new ActionListener() {
            final SoundBuffer sound = main.soundFactory.getSound(main.resourceBundle.getString("sound.cmd_exit_app"));
            final Runnable cmd = new Runnable() {
                @Override
                public void run() {
                    main.exit();
                }
            };

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundFactory.invokeAfterSound(sound, cmd);
            }
        };
        configDialogMenuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.showConfigDlg();
            }
        };

        final ItemListener sfxMenuListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SoundFactory.voiceOn = e.getStateChange() == ItemEvent.SELECTED;
                voiceMenu.setState(SoundFactory.voiceOn);
                jVoiceMenu.setSelected(SoundFactory.voiceOn);
            }
        };
        voiceMenu = new CheckboxMenuItem(main.resourceBundle.getString("menu.checkbox_voice"), SoundFactory.voiceOn);
        voiceMenu.addItemListener(sfxMenuListener);
        jVoiceMenu = new JCheckBoxMenuItem(main.resourceBundle.getString("menu.checkbox_voice"), SoundFactory.voiceOn);
        jVoiceMenu.addItemListener(sfxMenuListener);

        final ItemListener sfxMenuListener2 = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SoundFactory.sfxOn = e.getStateChange() == ItemEvent.SELECTED;
                sfxMenu.setState(SoundFactory.sfxOn);
                jSfxMenu.setSelected(SoundFactory.sfxOn);
            }
        };
        sfxMenu = new CheckboxMenuItem(main.resourceBundle.getString("menu.checkbox_sfx"), SoundFactory.sfxOn);
        sfxMenu.addItemListener(sfxMenuListener2);
        jSfxMenu = new JCheckBoxMenuItem(main.resourceBundle.getString("menu.checkbox_sfx"), SoundFactory.sfxOn);
        jSfxMenu.addItemListener(sfxMenuListener2);
        final ActionListener disposeMenuListener = new ActionListener() {
            final SoundBuffer sound = main.soundFactory.getSound(main.resourceBundle.getString("sound.cmd_dispose_mascot"));

            @Override
            public void actionPerformed(ActionEvent e) {
                final Runnable cmd = new Runnable() {
                    final Mascot mascot = targetmascot;

                    @Override
                    public void run() {
                        mascot.dispose();
                    }
                };
                SoundFactory.invokeAfterSound(sound, cmd);//To change body of implemented methods use File | Settings | File Templates.
            }
        };
        disposeMenu = new JMenuItem(main.resourceBundle.getString("menu.cmd_dispose_mascot"));
        disposeMenu.addActionListener(disposeMenuListener);
        popupMenu = new JPopupMenu();
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                if (null != targetmascot) targetmascot.setAnimating(true);
            }

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                if (null != targetmascot) targetmascot.setAnimating(false);
            }
        });
    }

    public void showPopupMenu(final Mascot mascot, final Component invoker, final int x, final int y) {
        if (null != targetmascot) {
            targetmascot.setAnimating(true);
        }
        targetmascot = mascot;
        popupMenu.show(invoker, x, y);
    }

    public void prepareTrayIcon(final TrayIcon icon) {
        icon.addMouseListener(new MouseListener() {
            final SoundBuffer sound = main.soundFactory.getSound(main.resourceBundle.getString("sound.cmd_one_more_mascot"));
            final Runnable cmd = new Runnable() {

                @Override
                public void run() {
                    main.manager.createMascot();
                }

            };

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    SoundFactory.invokeAfterSound(sound, cmd);
                }
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
        });

    }

    public void prepareMainMenu(PopupMenu mainMenu) {
        final MenuItem increaseMenu = new MenuItem(main.resourceBundle.getString("menu.cmd_one_more_mascot"));//増やす");
        increaseMenu.addActionListener(increaseMenuListener);
        final MenuItem gatherMenu = new MenuItem(main.resourceBundle.getString("menu.cmd_together"));//あつまれ！");
        gatherMenu.addActionListener(gatherMenuListener);
        final MenuItem oneMenu = new MenuItem(main.resourceBundle.getString("menu.cmd_remain_one_mascot"));//一匹だけ残す");
        oneMenu.addActionListener(oneMenuListener);
        final MenuItem restoreMenu = new MenuItem(main.resourceBundle.getString("menu.cmd_restore_window"));//IEを元に戻す");
        restoreMenu.addActionListener(restoreMenuListener);
        final MenuItem configDlgMenu = new MenuItem(main.resourceBundle.getString("menu.cmd_show_config_dlg"));
        configDlgMenu.addActionListener(configDialogMenuListener);
        final MenuItem aboutMenu = new MenuItem(main.resourceBundle.getString("menu.cmd_show_about_win"));//"Toshimeji について");
        aboutMenu.addActionListener(aboutMenuListener);
        final MenuItem closeMenu = new MenuItem(main.resourceBundle.getString("menu.cmd_exit_app"));//ばいばい");
        closeMenu.addActionListener(closeMenuListener);
        mainMenu.add(increaseMenu);
        mainMenu.add(gatherMenu);
        mainMenu.add(oneMenu);
        mainMenu.add(restoreMenu);
        mainMenu.addSeparator();
        mainMenu.add(voiceMenu);
        mainMenu.add(sfxMenu);
        mainMenu.add(configDlgMenu);
        mainMenu.addSeparator();
        mainMenu.add(aboutMenu);
        mainMenu.add(closeMenu);
    }

    public void prepareMainMenu(JPopupMenu mainMenu) {
        final JMenuItem increaseMenu = new JMenuItem(main.resourceBundle.getString("menu.cmd_one_more_mascot"));//増やす");
        increaseMenu.addActionListener(increaseMenuListener);
        final JMenuItem gatherMenu = new JMenuItem(main.resourceBundle.getString("menu.cmd_together"));//あつまれ！");
        gatherMenu.addActionListener(gatherMenuListener);
        final JMenuItem oneMenu = new JMenuItem(main.resourceBundle.getString("menu.cmd_remain_one_mascot"));//一匹だけ残す");
        oneMenu.addActionListener(oneMenuListener);
        final JMenuItem restoreMenu = new JMenuItem(main.resourceBundle.getString("menu.cmd_restore_window"));//IEを元に戻す");
        restoreMenu.addActionListener(restoreMenuListener);
        final JMenuItem aboutMenu = new JMenuItem(main.resourceBundle.getString("menu.cmd_show_about_win"));//"Toshimeji について");
        aboutMenu.addActionListener(aboutMenuListener);
        final JMenuItem closeMenu = new JMenuItem(main.resourceBundle.getString("menu.cmd_exit_app"));//ばいばい");
        closeMenu.addActionListener(closeMenuListener);
        mainMenu.add(increaseMenu);
        mainMenu.add(gatherMenu);
        mainMenu.add(oneMenu);
        mainMenu.add(restoreMenu);
        mainMenu.addSeparator();
        mainMenu.add(jVoiceMenu);
        mainMenu.add(jSfxMenu);
        mainMenu.addSeparator();
        mainMenu.add(aboutMenu);
        mainMenu.add(closeMenu);
    }


    public void prepareMascottMenu(boolean includeMainMenu) {
        popupMenu.add(disposeMenu);
        popupMenu.add(new JSeparator());
        JMenu submenu = new JMenu(main.resourceBundle.getString("menu.cmd_behaviors"));
        popupMenu.add(submenu);
        int count = 16;
        for (String behavior : main.getConfiguration().getBehaviorBuilders().keySet()) {
            if(count--<0) {
                count=15;
                JMenu parentmenu = submenu;
                submenu = new JMenu(main.resourceBundle.getString("menu.cmd_behaviors_submenu"));
                parentmenu.add(submenu);
            };
            submenu.add(new BehaviourMenuItem(behavior));
        }
        if (includeMainMenu) {
            popupMenu.add(new JSeparator());
            prepareMainMenu(popupMenu);
        }
    }

    private class BehaviourMenuItem extends JMenuItem {
        final String behaviour;

        private BehaviourMenuItem(String name) throws HeadlessException {
            super(name);
            behaviour = name;
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    Mascot mascot = targetmascot;
                    if (null != mascot) try {
                        mascot.setBehavior(main.configuration.buildBehavior(behaviour));
                    } catch (BehaviorInstantiationException e) {
                        log.log(Level.SEVERE, "次の行動の初期化に失敗しました", e);
                        mascot.dispose();
                    } catch (CantBeAliveException e) {
                        log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                        mascot.dispose();
                    }
                }
            });
        }

    }
}
