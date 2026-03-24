package net.p4pingvin4ik.burosound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

public class BoxMusicInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private boolean stopped = false;
    private boolean isFadingOut = false;
    private float fadeMultiplier = 1.0f;
    private final boolean ignoreNoteBlocks;

    public BoxMusicInstance(Identifier id, boolean ignoreNoteBlocks) {
        super(id, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.ignoreNoteBlocks = ignoreNoteBlocks;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.looping = false;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
    }

    public void fadeOut() {
        this.isFadingOut = true;
    }

    public boolean isDone() {
        return this.stopped;
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void tick() {
        if (this.isFadingOut) {
            this.fadeMultiplier -= 0.05f;

            if (this.fadeMultiplier <= 0.0f) {
                this.fadeMultiplier = 0.0f;
                this.stopped = true;
            }
        }
        this.volume = SoundDuckingManager.calculateVolume(this.fadeMultiplier, this.ignoreNoteBlocks);
    }
}
