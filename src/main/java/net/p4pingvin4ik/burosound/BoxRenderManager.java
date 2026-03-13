package net.p4pingvin4ik.burosound;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Random;

public class BoxRenderManager {
    private static boolean isRendering = false;

    public static boolean toggle() {
        isRendering = !isRendering;
        return isRendering;
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!isRendering || BoxTriggerManager.activeBoxes.isEmpty() || context.world() == null) return;

            Identifier currentDim = context.world().getRegistryKey().getValue();
            MatrixStack matrices = context.matrixStack();
            Vec3d camPos = context.camera().getPos();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            for (SoundBox soundBox : BoxTriggerManager.activeBoxes) {
                if (!soundBox.dimension.equals(currentDim)) continue;

                float r, g, b;
                if (soundBox.isExit) {
                    r = 1.0f; g = 0.0f; b = 0.0f;
                } else {
                    int hash = soundBox.soundIdName.hashCode();
                    Random random = new Random(hash);
                    r = 0.4f + random.nextFloat() * 0.6f;
                    g = 0.4f + random.nextFloat() * 0.6f;
                    b = 0.4f + random.nextFloat() * 0.6f;
                }

                matrices.push();
                VertexConsumer lineConsumer = context.consumers().getBuffer(RenderLayer.getLines());

                Box box = new Box(
                        soundBox.minX, soundBox.minY, soundBox.minZ,
                        soundBox.maxX, soundBox.maxY, soundBox.maxZ
                ).offset(-camPos.x, -camPos.y, -camPos.z);

                WorldRenderer.drawBox(
                        matrices, lineConsumer,
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ,
                        r, g, b, 1.0f
                );
                matrices.pop();

                matrices.push();

                double centerX = (soundBox.minX + soundBox.maxX) / 2.0;
                double centerY = (soundBox.minY + soundBox.maxY) / 2.0;
                double centerZ = (soundBox.minZ + soundBox.maxZ) / 2.0;

                float tx = (float) (centerX - camPos.x);
                float ty = (float) (centerY - camPos.y);
                float tz = (float) (centerZ - camPos.z);

                matrices.translate(tx, ty, tz);

                matrices.multiply(context.camera().getRotation());

                float scale = 0.025f;
                matrices.scale(scale, -scale, scale);

                Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();
                String text = soundBox.soundIdName;
                float renderX = -textRenderer.getWidth(text) * 0.5f;

                textRenderer.draw(
                        text,
                        renderX,
                        0,
                        0xFFFFFFFF,
                        false,
                        modelViewMatrix,
                        context.consumers(),
                        TextRenderer.TextLayerType.NORMAL,
                        0,
                        0xF000F0
                );
                matrices.pop();
            }

            for (BlockTrigger trigger : BoxTriggerManager.blockTriggers) {
                if (!trigger.box.dimension.equals(currentDim)) continue;

                matrices.push();

                VertexConsumer lineConsumer = context.consumers().getBuffer(RenderLayer.getLines());

                Box box = new Box(
                        trigger.x, trigger.y, trigger.z,
                        trigger.x + 1, trigger.y + 1, trigger.z + 1
                ).offset(-camPos.x, -camPos.y, -camPos.z);

                WorldRenderer.drawBox(
                        matrices,
                        lineConsumer,
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ,
                        0.0f, 1.0f, 1.0f, 1.0f
                );
                matrices.pop();

                matrices.push();
                double centerX = trigger.x + 0.5;
                double centerY = trigger.y + 0.5;
                double centerZ = trigger.z + 0.5;

                matrices.translate(
                        (float)(centerX - camPos.x),
                        (float)(centerY - camPos.y),
                        (float)(centerZ - camPos.z)
                );

                matrices.multiply(context.camera().getRotation());

                float scale = 0.025f;
                matrices.scale(scale, -scale, scale);

                Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();
                String text = trigger.box.soundIdName;
                float renderX = -textRenderer.getWidth(text) * 0.5f;

                textRenderer.draw(
                        text,
                        renderX,
                        0,
                        0xFFFFFFFF,
                        false,
                        modelViewMatrix,
                        context.consumers(),
                        TextRenderer.TextLayerType.NORMAL,
                        0,
                        0xF000F0
                );
                matrices.pop();
            }
        });
    }
}