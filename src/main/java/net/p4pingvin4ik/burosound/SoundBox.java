package net.p4pingvin4ik.burosound;

import net.minecraft.util.Identifier;

public class SoundBox {
    public final String soundIdName;
    public final Identifier soundId;
    public final Identifier dimension;
    public final boolean isExit;
    public final boolean playWhileInside;

    public final double minX, minY, minZ;
    public final double maxX, maxY, maxZ;

    public SoundBox(String soundName, double x1, double y1, double z1, double x2, double y2, double z2, Identifier dimension, boolean isExit,boolean playWhileInside) {
        this.soundIdName = soundName;
        this.soundId = Identifier.of("minecraft", soundName);
        this.dimension = dimension;
        this.isExit = isExit;
        this.playWhileInside = playWhileInside;

        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean isPlayerInside(double px, double py, double pz, Identifier currentDim) {
        if (!this.dimension.equals(currentDim)) return false;

        return px >= minX && px <= maxX &&
                py >= minY && py <= maxY &&
                pz >= minZ && pz <= maxZ;
    }
}