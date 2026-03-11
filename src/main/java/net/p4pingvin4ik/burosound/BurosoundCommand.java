package net.p4pingvin4ik.burosound;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class BurosoundCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("burosound")
                    .then(ClientCommandManager.literal("boxes")
                            .executes(context -> {
                                boolean newState = BoxRenderManager.toggle();
                                context.getSource().sendFeedback(Text.literal("Boxes visibility: " + newState));
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("stop")
                            .executes(context -> {
                                BoxTriggerManager.stopAll(context.getSource().getClient());
                                context.getSource().sendFeedback(Text.literal("All music stopped and next queue cleared."));
                                return 1;
                            })
                    )
            );
        });
    }
}