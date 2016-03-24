package swing.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class to make animations easy.
 */

public class AnimatablePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = -3670406422771531891L;

    private Timer timer;
    private Double kTimeInterval = 1/50.0;
    private List<Animator> activeAnimators;
    private List<Animator> pendingAnimators;

    //Animatable properties
    private Animator preferredSizeX = null;
    private Animator preferredSizeY = null;
    private Animator red = null;
    private Animator green = null;
    private Animator blue = null;
    private Animator alpha = null;

    private boolean repaints = true;
    private boolean checkFinishes = true;

    private enum AnimationState {
        FINISHED, RUNNING, LOOPING
    }

    /**
     * Constructs a new AnimatablePanel object.
     */
    public AnimatablePanel() {
        timer = new Timer((int)(1000.0*kTimeInterval), this);
        activeAnimators = new CopyOnWriteArrayList<Animator>();
        pendingAnimators = new CopyOnWriteArrayList<Animator>();
    }

    /**
     * Animates a view to a specified size.
     *
     * @param size the end size of the view.
     * @param duration the duration of the animation.
     */
    public void setPreferredSize(Dimension size, Double duration) {
        setPreferredSize(size, duration, AnimationEase.LINEAR);
    }

    /**
     * Animates a view to a specified size.
     *
     * @param size the end size of the view.
     * @param duration the duration of the animation.
     * @param ease the ease to be used for the animation.
     */
    public void setPreferredSize(Dimension size, Double duration, AnimationEase ease) {
        preferredSizeX = new Animator(getPreferredSize().getWidth(), duration, size.getWidth());
        preferredSizeX.setEase(ease);
        activeAnimators.add(preferredSizeX);
        preferredSizeY = new Animator(getPreferredSize().getHeight(), duration, size.getHeight());
        preferredSizeY.setEase(ease);
        activeAnimators.add(preferredSizeY);

        if (! timer.isRunning()) timer.start();
        animationBegun();
    }

    /**
     * Animates the background Color of a view.
     *
     * @param color the end Color of the view.
     * @param duration the duration of the animation.
     */
    public void setBackground(Color color, Double duration) {
        setBackground(color, duration, AnimationEase.LINEAR);
    }

    /**
     * Animates the background Color of a view.
     *
     * @param color the end Color of the view.
     * @param duration the duration of the animation.
     * @param ease the ease to be used for the animation.
     */
    public void setBackground(Color color, Double duration, AnimationEase ease) {
        Color currentColor = getBackground();
        Double currentRed = ((double)currentColor.getRed())/255;
        Double currentGreen = ((double)currentColor.getGreen())/255;
        Double currentBlue = ((double)currentColor.getBlue())/255;
        Double currentAlpha = ((double)currentColor.getAlpha())/255;

        Double red = ((double)color.getRed())/255;
        Double green = ((double)color.getGreen())/255;
        Double blue = ((double)color.getBlue())/255;
        Double alpha = ((double)color.getAlpha())/255;

        this.red = new Animator(currentRed, duration, red);
        this.red.setEase(ease);
        activeAnimators.add(this.red);
        this.green = new Animator(currentGreen, duration, green);
        this.green.setEase(ease);
        activeAnimators.add(this.green);
        this.blue = new Animator(currentBlue, duration, blue);
        this.blue.setEase(ease);
        activeAnimators.add(this.blue);
        this.alpha = new Animator(currentAlpha, duration, alpha);
        this.alpha.setEase(ease);
        activeAnimators.add(this.alpha);

        if (! timer.isRunning()) timer.start();
        animationBegun();
    }

    /**
     * Updates the animation properties.
     *
     * @param e the ActionEvent containing information about which object sent it.
     */
    public void actionPerformed(ActionEvent e) {
        updateAnimators();
    }

    private void updateAnimators() {
        boolean finished = true;
        boolean noLoops = true;
        final List<Animator> finishedAnimators = new ArrayList<Animator>();
        for (Animator a : activeAnimators) {
            AnimationState f = a.step();
            if (f == AnimationState.FINISHED) finishedAnimators.add(a);
            finished &= (f != AnimationState.RUNNING);
            noLoops &= (f != AnimationState.LOOPING);
        }
        //for (Animator a : finishedAnimators) {
        //    activeAnimators.remove(a);
        //    a = null;
        //}
        if(preferredSizeX != null && preferredSizeY != null) setPreferredSize(new Dimension(preferredSizeX.value().intValue(), preferredSizeY.value().intValue()));
        if(red != null && green != null && blue != null && alpha != null) setBackground(new Color((int) (255 * red.value()), (int) (255 * green.value()), (int) (255 * blue.value()), (int) (255 * alpha.value())));

        if (finished && checkFinishes) {
            animationCompleted();
            if (noLoops) timer.stop();
            checkFinishes = false;
        }

        if (repaints) {
            revalidate();
            repaint();
        }
    }

    public void setRepaints(boolean repaints) {
        this.repaints = repaints;
    }

    /**
     * Cancels all current animations.
     */
    public void cancelAllAnimations() {
        if (timer != null) timer.stop();
        activeAnimators = new CopyOnWriteArrayList<Animator>();
    }

    public boolean removeAnimator(Animator animator) {
        boolean success = false;
        if (activeAnimators.contains(animator)) {
            activeAnimators.remove(animator);
            success = true;
        }
        if (pendingAnimators.contains(animator)) {
            pendingAnimators.remove(animator);
            success = true;
        }
        return success;
    }

    public void pauseAnimations() {
        if (timer != null) timer.stop();
    }

    public void resumeAnimations() {
        if (timer != null && !timer.isRunning()) timer.start();
    }

    /**
     * Called when the first animation has started.
     */
    public void animationBegun() {}

    /**
     * Called when the last animation has finished.
     */
    public void animationCompleted() {}

    /**
     * Returns an Animator object.
     *
     * @param value the start value to be animated.
     * @param target the end value of the animation.
     * @param duration the duration of the animation.
     */
    public Animator createAnimator(Double value, Double target, Double duration, boolean loops) {
        if (!loops) checkFinishes = true;
        Animator animator = new Animator(value, duration, target);
        animator.setLoops(loops);
        activeAnimators.add(animator);
        if (!timer.isRunning()) timer.start();
        animationBegun();
        return animator;
    }

    public Animator createDelayedAnimator(Double value, Double target, Double duration) {
        Animator animator = new Animator(value, duration, target);
        pendingAnimators.add(animator);
        return animator;
    }

    public void start() {
        activeAnimators.addAll(pendingAnimators);
        pendingAnimators.clear();
        if (!timer.isRunning()) timer.start();
    }

    /**
     * An enum containing the different ease types supported.
     */
    public enum AnimationEase {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT
    }

    // A class to iterate values.
    public class Animator {

        private AnimationEase ease = AnimationEase.LINEAR;
        private Double time;
        private Double initial;
        private Double change;
        private Double duration;
        private boolean increasing = true;
        public boolean loops = false;

        /**
         * Constructs a new Animator object.
         *
         * @param value the start value of the animation.
         * @param duration the duration of the animation.
         * @param target the end value of the animation.
         */
        public Animator(Double value, Double duration, Double target) {
            this.time = 0.0;
            this.initial = value;
            this.change = target - value;
            this.duration = duration;

            if (change < 0) increasing = false;
        }

        public void setLoops(boolean loops) {
            this.loops = loops;
        }

        public void setTime(Double time) {
            this.time = time;
        }

        /**
         * Steps through the animation.
         */
        public AnimationState step() {
            time += kTimeInterval;
            if (time >= duration) {
                if (loops) {
                    time = 0.0;
                } else {
                    time = duration;
                    return AnimationState.FINISHED;
                }
            }
            if (loops) return AnimationState.LOOPING;
            return AnimationState.RUNNING;
        }

        /**
         * Returns the current value at this point in the animation.
         *
         * @return the current value at this point in the animation.
         */
        public Double value() {
            if (ease == AnimationEase.EASE_IN) return change * Math.pow(time/duration, 3) + initial;
            if (ease == AnimationEase.EASE_OUT) return change * (Math.pow((time/duration) - 1, 3) + 1) + initial;
            if (ease == AnimationEase.EASE_IN_OUT) {
                if (time / duration < 0.5) return (change / 2) * Math.pow((time * 2)/duration, 3) + initial;
                else return (change / 2) * (Math.pow(((time * 2)/duration) - 2, 3) + 2) + initial;
            }
            return change * (time / duration) + initial;
        }

        /**
         * Sets the ease to be used in the animation.
         *
         * @param ease the AnimationEase to be used in the animation.
         */
        public void setEase(AnimationEase ease) {
            this.ease = ease;
        }

    }

}
