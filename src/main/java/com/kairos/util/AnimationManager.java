package com.kairos.util;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationManager {

    public static TranslateTransition slideIn(Node node, Duration duration, Side direction) {
        TranslateTransition transition = new TranslateTransition(duration, node);
        if (direction == Side.LEFT) {
            transition.setFromX(-40);
            transition.setToX(0);
        } else if (direction == Side.RIGHT) {
            transition.setFromX(40);
            transition.setToX(0);
        } else if (direction == Side.TOP) {
            transition.setFromY(-40);
            transition.setToY(0);
        } else if (direction == Side.BOTTOM) {
            transition.setFromY(40);
            transition.setToY(0);
        }
        return transition;
    }

    public static TranslateTransition slideOut(Node node, Duration duration, Side direction) {
        TranslateTransition transition = new TranslateTransition(duration, node);
        if (direction == Side.LEFT) {
            transition.setToX(-40);
        } else if (direction == Side.RIGHT) {
            transition.setToX(40);
        } else if (direction == Side.TOP) {
            transition.setToY(-40);
        } else if (direction == Side.BOTTOM) {
            transition.setToY(40);
        }
        return transition;
    }

    public static FadeTransition fadeIn(Node node, Duration duration) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        return ft;
    }

    public static FadeTransition fadeOut(Node node, Duration duration) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        return ft;
    }

    public static ScaleTransition pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setByX(0.05);
        st.setByY(0.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        return st;
    }

    public static ScaleTransition popIn(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setFromX(0);
        st.setFromY(0);
        st.setToX(1);
        st.setToY(1);
        return st;
    }
}
