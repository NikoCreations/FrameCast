package dev.nikocreations.framecast;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public final class FrameCastCommands implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommands.literal("framecast")
                                .then(ClientCommands.literal("bright")
                                        .then(ClientCommands.argument(
                                                        "percentage",
                                                        IntegerArgumentType.integer(0, 100))
                                                .executes(context -> {
                                                    int percentage =
                                                            IntegerArgumentType.getInteger(
                                                                    context, "percentage");
                                                    FrameCastRenderer.setBrightness(percentage);
                                                    context.getSource().sendFeedback(
                                                            Component.literal(
                                                                    "FrameCast screen brightness set to "
                                                                            + percentage + "%"));
                                                    return 1;
                                                })
                                        )
                                )
                )
        );
    }
}
