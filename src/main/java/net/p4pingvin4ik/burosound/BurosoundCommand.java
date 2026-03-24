package net.p4pingvin4ik.burosound;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public class BurosoundCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("burosound")
                    .then(ClientCommands.literal("boxes")
                            .executes(context -> {
                                boolean newState = BoxRenderManager.toggle();
                                context.getSource().sendFeedback(Component.literal("Boxes visibility: " + newState));
                                return 1;
                            })
                    )
                    .then(ClientCommands.literal("stop")
                            .executes(context -> {
                                BoxTriggerManager.stopAll(context.getSource().getClient());
                                context.getSource().sendFeedback(Component.literal("All music stopped and next queue cleared."));
                                return 1;
                            })
                    )
            );
        });
    }
}