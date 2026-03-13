package net.p4pingvin4ik.burosound.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.p4pingvin4ik.burosound.*;

import static net.p4pingvin4ik.burosound.Burosound.LOGGER;

public class BurosoundClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            BoxTriggerManager.tick(client);
            SoundDuckingManager.tick();
        });

        BurosoundCommand.register();
        BoxRenderManager.register();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world == null || !world.isClient() || player == null || hitResult == null) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            if (pos == null) {
                return ActionResult.PASS;
            }

            Identifier currentDim = world.getRegistryKey().getValue();
            Identifier blockId = Registries.BLOCK.getId(world.getBlockState(pos).getBlock());

            MinecraftClient client = MinecraftClient.getInstance();
            BoxTriggerManager.handleBlockInteraction(client, currentDim, pos, blockId);

            return ActionResult.PASS;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SoundJsonReader.readSoundsConfig();
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of("burosound", "sounds_reloader");
            }

            @Override
            public void reload(ResourceManager manager) {
                LOGGER.info("Reloading Burosound config due to resource reload (F3+T)");
                SoundJsonReader.readSoundsConfig();
            }
        });
    }
}