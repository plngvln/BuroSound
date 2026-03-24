package net.p4pingvin4ik.burosound.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
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
            if (world == null || !world.isClientSide() || player == null || hitResult == null) {
                return InteractionResult.PASS;
            }

            var pos = hitResult.getBlockPos();
            if (pos == null) {
                return InteractionResult.PASS;
            }

            Identifier currentDim = world.dimension().identifier();
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock());

            Minecraft client = Minecraft.getInstance();
            BoxTriggerManager.handleBlockInteraction(client, currentDim, pos, blockId);

            return InteractionResult.PASS;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SoundJsonReader.readSoundsConfig();
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath("burosound", "sounds_reloader");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                LOGGER.info("Reloading Burosound config due to resource reload (F3+T)");
                SoundJsonReader.readSoundsConfig();
            }
        });
    }
}
