package net.p4pingvin4ik.burosound.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.p4pingvin4ik.burosound.SoundDuckingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    @Inject(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At("HEAD")
    )
    private void onPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        this.process(sound);
    }

    private void process(SoundInstance sound) {
        if (sound != null && sound.getIdentifier() != null) {
            if (sound.getIdentifier().getPath().contains("note_block")) {
                SoundDuckingManager.notifyNoteBlock();
            }
        }
    }
}
