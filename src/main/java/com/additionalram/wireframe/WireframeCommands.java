package com.additionalram.wireframe;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Wireframe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class WireframeCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event){
        event.getDispatcher().register(
            Commands.literal("wireframe")

                .then(Commands.literal("toggle")
                    .executes(context -> {
                        WireframeState.isEnabled = !WireframeState.isEnabled;
                        sendFeedback(context.getSource(), WireframeState.isEnabled ? "Wireframe enabled" : "Wireframe disabled");
                        return 1;
                    })
                )

                .then(Commands.literal("blind")
                    .executes(context -> {
                        WireframeState.blind = !WireframeState.blind;
                        sendFeedback(context.getSource(), WireframeState.blind ? "Blindness enabled" : "Blindness disabled");
                        return 1;
                    })
                )

                .then(Commands.literal("renderEntities")
                    .executes(context -> {
                        WireframeState.renderEntities = !WireframeState.renderEntities;
                        sendFeedback(context.getSource(), WireframeState.renderEntities ? "Entities enabled" : "Entities disabled");
                        return 1;
                    })
                )

                .then(Commands.literal("renderBlocks")
                    .executes(context -> {
                        WireframeState.renderBlocks = !WireframeState.renderBlocks;
                        sendFeedback(context.getSource(), WireframeState.renderBlocks ? "Blocks enabled" : "Blocks disabled");
                        return 1;
                    })
                )

                .then(Commands.literal("setBlocksColor")
                    .then(Commands.argument("hex", StringArgumentType.greedyString())
                        .executes(context -> {
                            String input = StringArgumentType.getString(context, "hex");
                            if (input.matches("^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
                                writeHexToState(input, false);
                                sendFeedback(context.getSource(), "Block color updated");
                                return 1;
                            } else {
                                sendFeedback(context.getSource(), "Invalid color! Use hex (e.g. #FFFFFF)");
                                return 0;
                            }
                        })
                    )
                )

                .then(Commands.literal("setEntitiesColor")
                    .then(Commands.argument("hex", StringArgumentType.greedyString())
                        .executes(context -> {
                            String input = StringArgumentType.getString(context, "hex");
                            if (input.matches("^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
                                writeHexToState(input, true);
                                sendFeedback(context.getSource(), "Entities color updated");
                                return 1;
                            } else {
                                sendFeedback(context.getSource(), "Invalid color! Use hex (e.g. #FFFFFF)");
                                return 0;
                            }
                        })
                    )
                )

                .then(Commands.literal("setRenderDistance")
                    .then(Commands.argument("distance", FloatArgumentType.floatArg(0.5f, 8.0f))
                        .executes(context -> {
                            float dist = FloatArgumentType.getFloat(context, "distance");
                            WireframeState.renderDistance = dist;
                            sendFeedback(context.getSource(), "Render distance set to " + dist);
                            return 1;
                        })
                    )
                )

                .then(Commands.literal("setMovementEffectsIntensity")
                    .then(Commands.argument("intensity", FloatArgumentType.floatArg(0f, 2.0f))
                        .executes(context -> {
                            float intensity = FloatArgumentType.getFloat(context, "intensity");
                            WireframeState.movementEffectsIntensity = intensity;
                            sendFeedback(context.getSource(), "Movement effects intensity set to " + intensity);
                            return 1;
                        })
                    )
                )

                .then(Commands.literal("setFadeDistance")
                    .then(Commands.argument("distance", FloatArgumentType.floatArg(0.5f, 8.0f))
                        .executes(context -> {
                            float dist = FloatArgumentType.getFloat(context, "distance");
                            WireframeState.fadeStartDistance = dist;
                            sendFeedback(context.getSource(), "Fade start distance set to " + dist);
                            return 1;
                        })
                    )
                )
        );
    }

    private static void sendFeedback(CommandSourceStack source, String message) {
        source.sendSuccess(new TextComponent("[Wireframe] " + message), false);
    }

    private static void writeHexToState(String givenHexCode, Boolean entities) {
        // strip #
        String hex = givenHexCode.startsWith("#") ? givenHexCode.substring(1) : givenHexCode;

        // short hex support
        if (hex.length() == 3) {
            char r = hex.charAt(0);
            char g = hex.charAt(1);
            char b = hex.charAt(2);
            hex = "" + r + r + g + g + b + b;
        }

        float red = Integer.parseInt(hex.substring(0, 2), 16) / 255.0f;
        float green = Integer.parseInt(hex.substring(2, 4), 16) / 255.0f;
        float blue = Integer.parseInt(hex.substring(4, 6), 16) / 255.0f;

        if (entities) {
            WireframeState.entitiesR = red;
            WireframeState.entitiesG = green;
            WireframeState.entitiesB = blue;
        } else {
            WireframeState.blocksR = red;
            WireframeState.blocksG = green;
            WireframeState.blocksB = blue;
        }
    }
}
