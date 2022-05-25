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
import java.util.List;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlossomTpa implements ModInitializer {
    static BlossomTpaConfig CONFIG = BlossomConfig.load(BlossomTpaConfig.class, "BlossomTpa.json");
    public static final Logger LOGGER = CustomLogger.createLogger("BlossomTpa");
    private final ArrayList<TpaRequest> activeTpas = new ArrayList<>();

    @Override
    public void onInitialize() {
        BlossomLib.addCommand(literal("tpa")
            .requires(Permissions.require("blossom.tpa", true))
            .then(argument("target", EntityArgumentType.player())
                .executes(this::runTpaTo)));

        BlossomLib.addCommand(literal("tpahere")
            .requires(Permissions.require("blossom.tpa.here", true))
            .then(argument("target", EntityArgumentType.player())
                .executes(this::runTpaHere)));

        BlossomLib.addCommand(literal("tpaaccept")
            .requires(Permissions.require("blossom.tpa", true))
            .executes(this::acceptTpaAuto)
            .then(argument("target", EntityArgumentType.player())
                .executes(this::acceptTpaTarget)));
    }


    private int runTpa(CommandContext<ServerCommandSource> ctx, boolean tpaHere) throws CommandSyntaxException {
        ServerPlayerEntity initiator = ctx.getSource().getPlayer();
        ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "target");

        if (initiator.equals(receiver)) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.to-self");
            return Command.SINGLE_SUCCESS;
        }

        final TpaRequest tpaRequest = new TpaRequest(initiator, receiver, tpaHere);
        if (activeTpas.stream().anyMatch(tpaRequest::similarTo)) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.similar", receiver);
            return Command.SINGLE_SUCCESS;
        }

        tpaRequest.startTimeout(() -> activeTpas.remove(tpaRequest));
        activeTpas.add(tpaRequest);

        return Command.SINGLE_SUCCESS;
    }

    private int runTpaTo(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return runTpa(ctx, false);
    }

    private int runTpaHere(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return runTpa(ctx, true);
    }


    private int acceptTpa(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity initiator) {
        Optional<TpaRequest> tpaRequestOptional = activeTpas.stream()
            .filter(request -> request.initiator.equals(initiator))
            .findFirst();

        if (tpaRequestOptional.isEmpty()) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.none-from", initiator);
            return Command.SINGLE_SUCCESS;
        }

        TpaRequest tpaRequest = tpaRequestOptional.get();
        TeleportUtils.teleport(
            CONFIG.teleportation,
            CONFIG.standStill,
            CONFIG.cooldown,
            BlossomTpa.class,
            tpaRequest.teleportWho,
            () -> new TeleportUtils.TeleportDestination(tpaRequest.teleportTo)
        );

        if (tpaRequest.tpaHere) {
            tpaRequest.initiator.sendMessage(TextUtils.translation("blossom.tpa.here.accept.initiator", tpaRequest.toArgs()), false);
            tpaRequest.receiver.sendMessage(TextUtils.translation("blossom.tpa.here.accept.receiver", tpaRequest.toArgs()), false);
        } else {
            tpaRequest.initiator.sendMessage(TextUtils.translation("blossom.tpa.to.accept.initiator", tpaRequest.toArgs()), false);
            tpaRequest.receiver.sendMessage(TextUtils.translation("blossom.tpa.to.accept.receiver", tpaRequest.toArgs()), false);
        }

        tpaRequest.cancelTimeout();
        activeTpas.remove(tpaRequest);
        return Command.SINGLE_SUCCESS;
    }

    private int acceptTpaAuto(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity receiver = ctx.getSource().getPlayer();
        List<TpaRequest> candidates = activeTpas.stream()
            .filter(request -> request.receiver.equals(receiver))
            .toList();

        if (candidates.size() > 1) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.multiple");
            return Command.SINGLE_SUCCESS;
        }

        if (candidates.size() < 1) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.none");
            return Command.SINGLE_SUCCESS;
        }

        return acceptTpa(ctx, candidates.get(0).initiator);
    }

    private int acceptTpaTarget(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity initiator = EntityArgumentType.getPlayer(ctx, "target");
        return acceptTpa(ctx, initiator);
    }
}
