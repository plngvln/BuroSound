package net.p4pingvin4ik.burosound;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
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
            MinecraftClient client = MinecraftClient.getInstance();

            if (!isRendering || BoxTriggerManager.activeBoxes.isEmpty() || client.world == null) return;

            MatrixStack matrices = context.matrices();
            if (matrices == null) return;

            Camera camera = client.gameRenderer.getCamera();
            Vec3d camPos = camera.getCameraPos();

            Identifier currentDim = client.world.getRegistryKey().getValue();
            TextRenderer textRenderer = client.textRenderer;

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

                matrices.push();

                VertexConsumer lineConsumer = context.consumers().getBuffer(RenderLayer.getLines());

                Box box = new Box(
                        soundBox.minX, soundBox.minY, soundBox.minZ,
                        soundBox.maxX, soundBox.maxY, soundBox.maxZ
                );

                VertexRendering.drawOutline(
                        matrices,
                        lineConsumer,
                        VoxelShapes.cuboid(box),
                        -camPos.x, -camPos.y, -camPos.z,
                        color
                );
                matrices.pop();

                matrices.push();
                double centerX = (soundBox.minX + soundBox.maxX) / 2.0;
                double centerY = (soundBox.minY + soundBox.maxY) / 2.0;
                double centerZ = (soundBox.minZ + soundBox.maxZ) / 2.0;

                matrices.translate(
                        (float)(centerX - camPos.x),
                        (float)(centerY - camPos.y),
                        (float)(centerZ - camPos.z)
                );

                matrices.multiply(camera.getRotation());

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
        });
    }
}