package net.p4pingvin4ik.burosound.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.p4pingvin4ik.burosound.SoundDuckingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
            at = @At("HEAD")
    )
    private void onPlay(SoundInstance sound, CallbackInfo ci) {
        if (sound != null && sound.getId() != null) {
            if (sound.getId().getPath().contains("note_block")) {
                SoundDuckingManager.notifyNoteBlock();
            }
        }
    }
}