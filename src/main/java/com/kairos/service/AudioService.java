package com.kairos.service;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Service to handle audio playbacks within the application.
 */
public class AudioService {

    /**
     * Plays the default notification sound asynchronously.
     */
    public static void playNotification() {
        try {
            URL url = AudioService.class.getResource("/com/kairos/notify.wav");
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else {
                System.err.println("[AudioService] notify.wav not found in classpath.");
                // Fallback to basic system beep if file is missing
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception e) {
            System.err.println("[AudioService] Error playing notification: " + e.getMessage());
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
}
