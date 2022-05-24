package dev.codedsakura.blossom.tpa;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.codedsakura.blossom.lib.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.Logger;

import java.util.ArrayList;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlossomTpa implements ModInitializer {
    static BlossomTpaConfig CONFIG = BlossomConfig.load(BlossomTpaConfig.class, "BlossomTpa.json");
    public static final Logger LOGGER = CustomLogger.createLogger("BlossomTpa");
    private final ArrayList<TpaRequest> activeTpas = new ArrayList<>();

    @Override
    public void onInitialize() {
         BlossomLib.addCommand(literal("tpa")
             .requires(Permissions.require("blossom.tpa.tpa", true))
             .then(argument("target", EntityArgumentType.player())
                 .executes(this::runTpa)));
    }

    private int runTpa(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity teleportWho = ctx.getSource().getPlayer();
        ServerPlayerEntity teleportTo = EntityArgumentType.getPlayer(ctx, "target");

        if (teleportWho.equals(teleportTo)) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.to-self");
            return Command.SINGLE_SUCCESS;
        }

        final TpaRequest tpaRequest = new TpaRequest(teleportWho, teleportTo, false);
        if (activeTpas.stream().anyMatch(tpaRequest::similarTo)) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.similar", teleportTo);
            return Command.SINGLE_SUCCESS;
        }

        tpaRequest.startTimeout(() -> activeTpas.remove(tpaRequest));
        activeTpas.add(tpaRequest);

        return Command.SINGLE_SUCCESS;
    }
}
