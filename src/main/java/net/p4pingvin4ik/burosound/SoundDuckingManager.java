package net.p4pingvin4ik.burosound;

import net.minecraft.util.math.MathHelper;

public class SoundDuckingManager {
    private static float currentVolumeModifier = 1.0f;
    private static long lastNoteBlockTime = 0;

    public static void tick() {
        long now = System.currentTimeMillis();

        float targetModifier = (now - lastNoteBlockTime < 1000) ? 0.01f : 1.0f;

        float transitionSpeed;

        if (targetModifier < currentVolumeModifier) {
            transitionSpeed = 0.3f;
        } else {
            transitionSpeed = 0.02f;
        }
        currentVolumeModifier = MathHelper.lerp(transitionSpeed, currentVolumeModifier, targetModifier);
    }

    public static void notifyNoteBlock() {
        lastNoteBlockTime = System.currentTimeMillis();
    }

    public static float getModifier() {
        return currentVolumeModifier;
    }
}