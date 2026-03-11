package net.p4pingvin4ik.burosound;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class BoxMusicInstance extends PositionedSoundInstance implements TickableSoundInstance {
    private boolean done = false;
    private boolean isFadingOut = false;
    private float fadeMultiplier = 1.0f;

    public BoxMusicInstance(Identifier id) {
        super(id, SoundCategory.RECORDS, 1.0f, 1.0f, SoundInstance.createRandom(), false, 0, AttenuationType.NONE, 0.0, 0.0, 0.0, true);
    }

    public void fadeOut() {
        this.isFadingOut = true;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void tick() {
        if (this.isFadingOut) {
            this.fadeMultiplier -= 0.05f;

            if (this.fadeMultiplier <= 0.0f) {
                this.fadeMultiplier = 0.0f;
                this.done = true;
            }
        }
        this.volume = this.fadeMultiplier * SoundDuckingManager.getModifier();
    }
}