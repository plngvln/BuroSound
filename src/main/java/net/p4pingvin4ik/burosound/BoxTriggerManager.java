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
    private static boolean pendingIgnoreNoteBlocks = false;
    private static int transitionTimer = 0;
    private static SoundBox lastTriggeredBox = null;
    private static Identifier lastDimension = null;
    private static final Map<SoundBox, OverlapContext> overlapContexts = new HashMap<>();

    private static class OverlapContext {
        BoxMusicInstance instance;
        Identifier currentSoundId;

        OverlapContext(BoxMusicInstance instance, Identifier currentSoundId) {
            this.instance = instance;
            this.currentSoundId = currentSoundId;
        }
    }

    public static void stopAll(MinecraftClient client) {
        if (activeMusicInstance != null) {
            client.getSoundManager().stop(activeMusicInstance);
            activeMusicInstance = null;
        }
        if (!overlapContexts.isEmpty()) {
            for (OverlapContext ctx : overlapContexts.values()) {
                client.getSoundManager().stop(ctx.instance);
            }
            overlapContexts.clear();
        }
        activeSoundId = null;
        pendingSoundId = null;
        transitionTimer = 0;
        LOGGER.info("All music stopped.");
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
                    activeMusicInstance = new BoxMusicInstance(nextSound, lastTriggeredBox != null && lastTriggeredBox.ignoreNoteBlocks);
                    activeSoundId = nextSound;
                    client.getSoundManager().play(activeMusicInstance);
                } else if (lastTriggeredBox != null && lastTriggeredBox.playWhileInside) {
                    activeMusicInstance = new BoxMusicInstance(activeSoundId, lastTriggeredBox.ignoreNoteBlocks);
                    client.getSoundManager().play(activeMusicInstance);
                } else {
                    activeMusicInstance = null;
                    activeSoundId = null;
                }
            }
        }

        SoundBox exitBox = null;
        SoundBox baseBox = null;
        List<SoundBox> overlapBoxesInRange = new ArrayList<>();

        for (SoundBox box : activeBoxes) {
            if (!box.isPlayerInside(player.getX(), player.getY(), player.getZ(), currentDim)) continue;

            if (box.isExit) {
                exitBox = box;
                break;
            }

            if (box.allowOverlap) {
                overlapBoxesInRange.add(box);
                continue;
            }

            if (baseBox == null) baseBox = box;
        }

        if (exitBox != null) {
            baseBox = exitBox;
        }

        // Handle overlap boxes independently: next / playWhileInside without touching base music.
        for (SoundBox overlapBox : overlapBoxesInRange) {
            OverlapContext ctx = overlapContexts.get(overlapBox);

            if (ctx == null) {
                Identifier startId = overlapBox.soundId;
                BoxMusicInstance overlapSound = new BoxMusicInstance(startId, overlapBox.ignoreNoteBlocks);
                client.getSoundManager().play(overlapSound);
                overlapContexts.put(overlapBox, new OverlapContext(overlapSound, startId));
            } else {
                if (!client.getSoundManager().isPlaying(ctx.instance)) {
                    Identifier next = nextSoundMap.get(ctx.currentSoundId);

                    if (next != null) {
                        BoxMusicInstance nextInstance = new BoxMusicInstance(next, overlapBox.ignoreNoteBlocks);
                        client.getSoundManager().play(nextInstance);
                        ctx.instance = nextInstance;
                        ctx.currentSoundId = next;
                    } else if (overlapBox.playWhileInside) {
                        Identifier restartId = overlapBox.soundId;
                        BoxMusicInstance restartInstance = new BoxMusicInstance(restartId, overlapBox.ignoreNoteBlocks);
                        client.getSoundManager().play(restartInstance);
                        ctx.instance = restartInstance;
                        ctx.currentSoundId = restartId;
                    } else {
                        overlapContexts.remove(overlapBox);
                    }
                }
            }
        }

        if (!overlapContexts.isEmpty()) {
            List<SoundBox> toRemove = new ArrayList<>();
            for (Map.Entry<SoundBox, OverlapContext> entry : overlapContexts.entrySet()) {
                SoundBox box = entry.getKey();
                if (!overlapBoxesInRange.contains(box)) {
                    OverlapContext ctx = entry.getValue();
                    if (box.playWhileInside && client.getSoundManager().isPlaying(ctx.instance)) {
                        ctx.instance.fadeOut();
                    }
                    toRemove.add(box);
                }
            }
            for (SoundBox box : toRemove) {
                overlapContexts.remove(box);
            }
        }

        if (baseBox != lastTriggeredBox) {
            if (lastTriggeredBox != null && lastTriggeredBox.playWhileInside && baseBox == null) {
                if (activeMusicInstance != null) activeMusicInstance.fadeOut();
                activeSoundId = null;
                pendingSoundId = null;
                transitionTimer = 0;
            }

            if (baseBox != null) {
                if (baseBox.isExit) {
                    if (activeMusicInstance != null) activeMusicInstance.fadeOut();
                    if (!overlapContexts.isEmpty()) {
                        for (OverlapContext ctx : overlapContexts.values()) {
                            ctx.instance.fadeOut();
                        }
                        overlapContexts.clear();
                    }
                    pendingSoundId = null;
                    activeSoundId = null;
                    transitionTimer = 0;
                } else {
                    Identifier targetSound = baseBox.soundId;
                    Identifier currentDesiredSound = (pendingSoundId != null) ? pendingSoundId : activeSoundId;

                    if (!targetSound.equals(currentDesiredSound)) {
                        if (activeMusicInstance != null && !activeMusicInstance.isDone()) {
                            activeMusicInstance.fadeOut();
                            pendingSoundId = targetSound;
                            pendingIgnoreNoteBlocks = baseBox.ignoreNoteBlocks;
                            transitionTimer = 20;
                        } else {
                            activeMusicInstance = new BoxMusicInstance(targetSound, baseBox.ignoreNoteBlocks);
                            activeSoundId = targetSound;
                            client.getSoundManager().play(activeMusicInstance);
                        }
                    }
                }
            }
            lastTriggeredBox = baseBox;
        }

        if (transitionTimer > 0) {
            transitionTimer--;
            if (transitionTimer == 0 && pendingSoundId != null) {
                activeMusicInstance = new BoxMusicInstance(pendingSoundId, pendingIgnoreNoteBlocks);
                activeSoundId = pendingSoundId;
                client.getSoundManager().play(activeMusicInstance);
                pendingSoundId = null;
            }
        }
    }
}