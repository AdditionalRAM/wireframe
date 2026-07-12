package com.additionalram.wireframe;

import com.mojang.brigadier.arguments.FloatArgumentType;

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
}
