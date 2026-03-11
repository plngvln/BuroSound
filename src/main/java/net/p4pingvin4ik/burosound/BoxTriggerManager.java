package net.p4pingvin4ik.burosound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.p4pingvin4ik.burosound.Burosound.LOGGER;

public class BoxTriggerManager {
    public static final List<SoundBox> activeBoxes = new ArrayList<>();
    public static final Map<Identifier, Identifier> nextSoundMap = new HashMap<>();

    private static BoxMusicInstance activeMusicInstance = null;
    private static Identifier activeSoundId = null;
    private static Identifier pendingSoundId = null;
    private static int transitionTimer = 0;
    private static SoundBox lastTriggeredBox = null;
    private static Identifier lastDimension = null;

    public static void stopAll(MinecraftClient client) {
        if (activeMusicInstance != null) {
            client.getSoundManager().stop(activeMusicInstance);
            activeMusicInstance = null;
        }
        activeSoundId = null;
        pendingSoundId = null;
        transitionTimer = 0;
        LOGGER.info("All music stopped via command/dimension change.");
    }

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;

        Identifier currentDim = player.getEntityWorld().getRegistryKey().getValue();

        if (lastDimension != null && !lastDimension.equals(currentDim)) {
            stopAll(client);
        }
        lastDimension = currentDim;

        if (activeMusicInstance != null && transitionTimer == 0) {
            if (!client.getSoundManager().isPlaying(activeMusicInstance)) {

                Identifier nextSound = nextSoundMap.get(activeSoundId);

                if (nextSound != null) {
                    activeMusicInstance = new BoxMusicInstance(nextSound);
                    activeSoundId = nextSound;
                    client.getSoundManager().play(activeMusicInstance);
                }
                else if (lastTriggeredBox != null && lastTriggeredBox.playWhileInside) {
                    activeMusicInstance = new BoxMusicInstance(activeSoundId);
                    client.getSoundManager().play(activeMusicInstance);
                }
                else {
                    activeMusicInstance = null;
                    activeSoundId = null;
                }
            }
        }

        SoundBox currentBox = null;
        for (SoundBox box : activeBoxes) {
            if (box.isPlayerInside(player.getX(), player.getY(), player.getZ(), currentDim)) {
                currentBox = box;
                break;
            }
        }

        if (currentBox != lastTriggeredBox) {
            if (lastTriggeredBox != null && lastTriggeredBox.playWhileInside && currentBox == null) {
                if (activeMusicInstance != null) activeMusicInstance.fadeOut();
                activeSoundId = null;
                pendingSoundId = null;
                transitionTimer = 0;
            }

            if (currentBox != null) {
                if (currentBox.isExit) {
                    if (activeMusicInstance != null) activeMusicInstance.fadeOut();
                    pendingSoundId = null;
                    activeSoundId = null;
                    transitionTimer = 0;
                } else {
                    Identifier targetSound = currentBox.soundId;
                    Identifier currentDesiredSound = (pendingSoundId != null) ? pendingSoundId : activeSoundId;

                    if (!targetSound.equals(currentDesiredSound)) {
                        if (activeMusicInstance != null && !activeMusicInstance.isDone()) {
                            activeMusicInstance.fadeOut();
                            pendingSoundId = targetSound;
                            transitionTimer = 20;
                        } else {
                            activeMusicInstance = new BoxMusicInstance(targetSound);
                            activeSoundId = targetSound;
                            client.getSoundManager().play(activeMusicInstance);
                        }
                    }
                }
            }
            lastTriggeredBox = currentBox;
        }

        if (transitionTimer > 0) {
            transitionTimer--;
            if (transitionTimer == 0 && pendingSoundId != null) {
                activeMusicInstance = new BoxMusicInstance(pendingSoundId);
                activeSoundId = pendingSoundId;
                client.getSoundManager().play(activeMusicInstance);
                pendingSoundId = null;
            }
        }
    }
}