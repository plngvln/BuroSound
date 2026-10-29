package net.p4pingvin4ik.burosound;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Random;

public class BoxRenderManager {
    private static boolean isRendering = false;

    public static boolean toggle() {
        isRendering = !isRendering;
        return isRendering;
    }

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(BoxRenderManager::renderBoxes);
    }

    private static void renderBoxes(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();

        if (!isRendering || BoxTriggerManager.activeBoxes.isEmpty() || client.level == null) return;

        PoseStack poseStack = context.poseStack();
        Camera camera = client.gameRenderer.mainCamera();
        Vec3 camPos = camera.position();

        Identifier currentDim = client.level.dimension().identifier();
        Font font = client.font;

        for (SoundBox soundBox : BoxTriggerManager.activeBoxes) {
            if (!soundBox.dimension.equals(currentDim)) continue;

            int color;
            if (soundBox.isExit) {
                color = 0xFFFF0000;
            } else {
                int hash = soundBox.soundIdName.hashCode();
                Random random = new Random(hash);
                int r = 100 + random.nextInt(155);
                int g = 100 + random.nextInt(155);
                int b = 100 + random.nextInt(155);
                color = (255 << 24) | (r << 16) | (g << 8) | b;
            }

            poseStack.pushPose();

            AABB box = new AABB(
                    soundBox.minX, soundBox.minY, soundBox.minZ,
                    soundBox.maxX, soundBox.maxY, soundBox.maxZ
            );

            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
            context.submitNodeCollector().submitShapeOutline(
                    poseStack, Shapes.create(box), RenderTypes.lines(), color, 2.0f, false
            );
            poseStack.popPose();

            poseStack.pushPose();
            double centerX = (soundBox.minX + soundBox.maxX) / 2.0;
            double centerY = (soundBox.minY + soundBox.maxY) / 2.0;
            double centerZ = (soundBox.minZ + soundBox.maxZ) / 2.0;

            poseStack.translate(
                    (float) (centerX - camPos.x),
                    (float) (centerY - camPos.y),
                    (float) (centerZ - camPos.z)
            );

            poseStack.rotate(camera.rotation());

            float scale = 0.025f;
            poseStack.scale(scale, -scale, scale);

            String text = soundBox.soundIdName;
            float renderX = -font.width(text) * 0.5f;

            context.submitNodeCollector().submitText(
                    poseStack, renderX, 0, Component.literal(text).getVisualOrderText(), false,
                    Font.DisplayMode.NORMAL, 0xF000F0, 0xFFFFFFFF, 0, 0
            );
            poseStack.popPose();
        }

        for (BlockTrigger trigger : BoxTriggerManager.blockTriggers) {
            if (!trigger.box.dimension.equals(currentDim)) continue;

            int color = 0xFF00FFFF;

            poseStack.pushPose();

            AABB box = new AABB(
                    trigger.x, trigger.y, trigger.z,
                    trigger.x + 1, trigger.y + 1, trigger.z + 1
            );

            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
            context.submitNodeCollector().submitShapeOutline(
                    poseStack, Shapes.create(box), RenderTypes.lines(), color, 2.0f, false
            );
            poseStack.popPose();

            poseStack.pushPose();
            double centerX = trigger.x + 0.5;
            double centerY = trigger.y + 0.5;
            double centerZ = trigger.z + 0.5;

            poseStack.translate(
                    (float) (centerX - camPos.x),
                    (float) (centerY - camPos.y),
                    (float) (centerZ - camPos.z)
            );

            poseStack.rotate(camera.rotation());

            float scale = 0.025f;
            poseStack.scale(scale, -scale, scale);

            String text = trigger.box.soundIdName;
            float renderX = -font.width(text) * 0.5f;

            context.submitNodeCollector().submitText(
                    poseStack, renderX, 0, Component.literal(text).getVisualOrderText(), false,
                    Font.DisplayMode.SEE_THROUGH, 0xF000F0, 0xFFFFFFFF, 0, 0
            );
            poseStack.popPose();
        }
    }
}
