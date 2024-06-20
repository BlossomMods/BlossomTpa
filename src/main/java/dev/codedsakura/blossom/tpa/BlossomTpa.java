package dev.codedsakura.blossom.tpa;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.codedsakura.blossom.lib.mod.BlossomMod;
import dev.codedsakura.blossom.lib.permissions.Permissions;
import dev.codedsakura.blossom.lib.polyfill.PlaySound;
import dev.codedsakura.blossom.lib.teleport.TeleportUtils;
import dev.codedsakura.blossom.lib.text.TextUtils;
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

public class BlossomTpa extends BlossomMod<BlossomTpaConfig> implements ModInitializer {
    private final ArrayList<TpaRequest> activeTpas = new ArrayList<>();
    private static BlossomTpa self;

    public BlossomTpa() {
        super(BlossomTpaConfig.class);
        self = this;
    }

    @Override
    public String getName() {
        return "BlossomTpa";
    }

    public static Logger getLogger() {
        return self.logger;
    }

    public static BlossomTpaConfig getConfig() {
        return self.config;
    }

    @Override
    public void onInitialize() {
        register();

        addCommand(literal("tpa")
                .requires(Permissions.require("blossom.tpa.command.tpa", true))
            .then(argument("target", EntityArgumentType.player())
                .executes(this::runTpaTo)));

        addCommand(literal("tpahere")
                .requires(Permissions.require("blossom.tpa.command.tpahere", true))
            .then(argument("target", EntityArgumentType.player())
                .executes(this::runTpaHere)));

        addCommand(literal(config.commandOverrides.accept)
                .requires(Permissions.require("blossom.tpa.command.accept", true))
            .executes(this::acceptTpaAuto)
            .then(argument("target", EntityArgumentType.player())
                .executes(this::acceptTpaTarget)));

        addCommand(literal(config.commandOverrides.deny)
                .requires(Permissions.require("blossom.tpa.command.deny", true))
            .executes(this::denyTpaAuto)
            .then(argument("target", EntityArgumentType.player())
                .executes(this::denyTpaTarget)));

        addCommand(literal(config.commandOverrides.cancel)
                .requires(Permissions.require("blossom.tpa.command.cancel", true))
            .executes(this::cancelTpaAuto)
            .then(argument("target", EntityArgumentType.player())
                .executes(this::cancelTpaTarget)));
    }


    private int runTpa(CommandContext<ServerCommandSource> ctx, boolean tpaHere) throws CommandSyntaxException {
        ServerPlayerEntity initiator = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "target");

        if (initiator.equals(receiver)) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.to-self");
            return Command.SINGLE_SUCCESS;
        }

        if (Permissions.check(receiver, "blossom.tpa.disallowed", false)) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.disallowed", receiver);
            return Command.SINGLE_SUCCESS;
        }

        final TpaRequest tpaRequest = new TpaRequest(initiator, receiver, tpaHere);


        boolean forced = false;

        if (tpaHere) {
            if (Permissions.check(receiver, "blossom.tpa.accept.always.tpahere", false) ||
                    Permissions.check(initiator, "blossom.tpa.accept.force.tpahere", false)) {
                forced = true;
            }
        } else if (Permissions.check(receiver, "blossom.tpa.accept.always.tpa", false) ||
                Permissions.check(initiator, "blossom.tpa.accept.force.tpa", false)) {
            forced = true;
        }

        if (forced) {
            TeleportUtils.teleport(
                    config.teleportation,
                    BlossomTpa.class,
                    tpaRequest.teleportWho,
                    () -> new TeleportUtils.TeleportDestination(tpaRequest.teleportTo)
            );
            return Command.SINGLE_SUCCESS;
        }


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


    private enum ResolveState {ACCEPT, DENY, CANCEL}

    private int resolveTpa(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity receiver, ServerPlayerEntity initiator, ResolveState resolveState) {
        Optional<TpaRequest> tpaRequestOptional = activeTpas.stream()
            .filter(request -> request.receiver.equals(receiver) && request.initiator.equals(initiator))
            .findFirst();

        if (tpaRequestOptional.isEmpty()) {
            if (resolveState == ResolveState.CANCEL) {
                TextUtils.sendErr(ctx, "blossom.tpa.fail.cancel.none-to", receiver);
            } else {
                TextUtils.sendErr(ctx, "blossom.tpa.fail.none-from", initiator);
            }
            return Command.SINGLE_SUCCESS;
        }

        TpaRequest tpaRequest = tpaRequestOptional.get();
        if (resolveState == ResolveState.ACCEPT) {
            TeleportUtils.teleport(
                    config.teleportation,
                BlossomTpa.class,
                tpaRequest.teleportWho,
                () -> new TeleportUtils.TeleportDestination(tpaRequest.teleportTo)
            );
        }

        String localeRoot = "blossom.tpa." +
            (tpaRequest.tpaHere ? "here." : "to.") +
            resolveState.toString().toLowerCase();

        tpaRequest.initiator.sendMessage(TextUtils.translation(localeRoot + ".initiator", tpaRequest.toArgs()), false);
        tpaRequest.receiver.sendMessage(TextUtils.translation(localeRoot + ".receiver", tpaRequest.toArgs()), false);

        switch (resolveState) {
            case ACCEPT -> PlaySound.maybePlayToPlayer(tpaRequest.initiator, config.acceptInitiatorSound);
            case DENY -> PlaySound.maybePlayToPlayer(tpaRequest.initiator, config.denyInitiatorSound);
            case CANCEL -> PlaySound.maybePlayToPlayer(tpaRequest.receiver, config.cancelRecipientSound);
        }

        tpaRequest.cancelTimeout();
        activeTpas.remove(tpaRequest);
        return Command.SINGLE_SUCCESS;
    }

    private int resolveTpaAuto(CommandContext<ServerCommandSource> ctx, ResolveState resolveState) {
        List<TpaRequest> candidates;
        if (resolveState == ResolveState.CANCEL) {
            ServerPlayerEntity initiator = ctx.getSource().getPlayer();
            candidates = activeTpas.stream()
                .filter(request -> request.initiator.equals(initiator))
                .toList();
        } else {
            ServerPlayerEntity receiver = ctx.getSource().getPlayer();
            candidates = activeTpas.stream()
                .filter(request -> request.receiver.equals(receiver))
                .toList();
        }

        if (candidates.size() > 1) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.multiple");
            return Command.SINGLE_SUCCESS;
        }

        if (candidates.isEmpty()) {
            TextUtils.sendErr(ctx, "blossom.tpa.fail.none");
            return Command.SINGLE_SUCCESS;
        }

        return resolveTpa(ctx, candidates.get(0).receiver, candidates.get(0).initiator, resolveState);
    }


    private int acceptTpaAuto(CommandContext<ServerCommandSource> ctx) {
        return resolveTpaAuto(ctx, ResolveState.ACCEPT);
    }

    private int acceptTpaTarget(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity receiver = ctx.getSource().getPlayer();
        ServerPlayerEntity initiator = EntityArgumentType.getPlayer(ctx, "target");
        return resolveTpa(ctx, receiver, initiator, ResolveState.ACCEPT);
    }


    private int denyTpaAuto(CommandContext<ServerCommandSource> ctx) {
        return resolveTpaAuto(ctx, ResolveState.DENY);
    }

    private int denyTpaTarget(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity receiver = ctx.getSource().getPlayer();
        ServerPlayerEntity initiator = EntityArgumentType.getPlayer(ctx, "target");
        return resolveTpa(ctx, receiver, initiator, ResolveState.DENY);
    }


    private int cancelTpaAuto(CommandContext<ServerCommandSource> ctx) {
        return resolveTpaAuto(ctx, ResolveState.CANCEL);
    }

    private int cancelTpaTarget(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "target");
        ServerPlayerEntity initiator = ctx.getSource().getPlayer();
        return resolveTpa(ctx, receiver, initiator, ResolveState.CANCEL);
    }
}
