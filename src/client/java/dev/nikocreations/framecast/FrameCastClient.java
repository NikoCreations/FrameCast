package dev.nikocreations.framecast;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FrameCastClient implements ClientModInitializer {
    public static final String MOD_ID = "framecast";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    private static BlockPos pendingFirst;
    private static Direction pendingSide;

    @Override
    public void onInitializeClient() {
        FrameCastConfig.load();

        ClientLifecycleEvents.CLIENT_STARTED.register(
                client -> FrameCastMedia.reload()
        );

        ClientLifecycleEvents.CLIENT_STOPPING.register(
                client -> FrameCastMedia.stop()
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> FrameCastMedia.tick()
        );

        FrameCastRenderer.register();

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!player.getItemInHand(hand).is(Items.STICK)) {
                return InteractionResult.PASS;
            }


            Direction side = hit.getDirection();

            if (side.getAxis() == Direction.Axis.Y) {
                player.sendSystemMessage(
                        Component.literal(
                                "FrameCast: select a vertical wall, not a floor or ceiling."
                        )
                );

                return InteractionResult.FAIL;
            }

            BlockPos hitPos = hit.getBlockPos();

            BlockPos clicked = new BlockPos(
                    hitPos.getX(),
                    hitPos.getY(),
                    hitPos.getZ()
            );

            if (pendingFirst == null) {
                pendingFirst = clicked;
                pendingSide = side;

                player.sendSystemMessage(
                        Component.literal(
                                "FrameCast: first corner selected. Click the opposite corner."
                        )
                );

                return InteractionResult.FAIL;
            }

            if (side != pendingSide
                    || !sameWallPlane(pendingFirst, clicked, pendingSide)) {
                pendingFirst = null;
                pendingSide = null;

                player.sendSystemMessage(
                        Component.literal(
                                "FrameCast: invalid second corner. Start again on the same wall face."
                        )
                );

                return InteractionResult.FAIL;
            }

            String dimension =
                    world.dimension().toString();

            FrameCastConfig.data.screen =
                    new FrameCastConfig.Screen(
                            pendingFirst,
                            clicked,
                            pendingSide,
                            dimension
                    );

            FrameCastConfig.save();

            pendingFirst = null;
            pendingSide = null;

            player.sendSystemMessage(
                    Component.literal(
                            "FrameCast: display created and coordinates saved."
                    )
            );

            return InteractionResult.FAIL;
        });

        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> dispatcher.register(
                        ClientCommands.literal("framecast")
                                .then(ClientCommands.literal("reload")
                                        .executes(context -> {
                                            FrameCastConfig.load();
                                            FrameCastMedia.reload();

                                            context.getSource().sendFeedback(
                                                    Component.literal(
                                                            "FrameCast: configuration and media reloaded."
                                                    )
                                            );

                                            return 1;
                                        }))
                                .then(ClientCommands.literal("clear")
                                        .executes(context -> {
                                            FrameCastMedia.stop();
                                            FrameCastConfig.clearScreen();

                                            pendingFirst = null;
                                            pendingSide = null;

                                            context.getSource().sendFeedback(
                                                    Component.literal(
                                                            "FrameCast: display removed."
                                                    )
                                            );

                                            return 1;
                                        }))
                )
        );

        LOGGER.info("FrameCast initialized.");
    }

    private static boolean sameWallPlane(
            BlockPos first,
            BlockPos second,
            Direction side
    ) {
        if (side.getAxis() == Direction.Axis.Z) {
            return first.getZ() == second.getZ();
        }

        return first.getX() == second.getX();
    }
}
