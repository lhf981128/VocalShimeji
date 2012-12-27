package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.image.ImagePair;
import com.group_finity.mascot.sound.Sound;

import java.awt.*;

public class Pose {
    private final ImagePair image;

    private final int dx;

    private final int dy;

    private final int duration;

    private final Sound sfx;

    //    public Pose(final ImagePair image) {
    //        this(image, 0, 0, 1, null);
    //    }
    //
    //    public Pose(final ImagePair image, final int duration) {
    //        this(image, 0, 0, duration, null);
    //    }

    public Pose(final ImagePair image, final int dx, final int dy, final int duration, final Sound sfx) {

        this.image = image;
        this.dx = dx;
        this.dy = dy;
        this.duration = duration;
        this.sfx = sfx;
    }

    @Override
    public String toString() {
        return "姿勢(" + getImage() + "," + getDx() + "," + getDy() + "," + getDuration() + ")";
    }

    public void next(final Mascot mascot) {

        mascot.setAnchor(new Point(mascot.getAnchor().x + (mascot.isLookRight() ? -getDx() : getDx()), mascot.getAnchor().y + getDy()));
        mascot.setImage(getImage().getImage(mascot.isLookRight()));

        if (null != sfx) {
            mascot.sfxController.sound(sfx);
        }

    }

    public int getDuration() {
        return this.duration;
    }

    public ImagePair getImage() {
        return this.image;
    }

    public int getDx() {
        return this.dx;
    }

    public int getDy() {
        return this.dy;
    }
}
