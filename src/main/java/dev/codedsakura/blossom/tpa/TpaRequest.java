package dev.codedsakura.blossom.tpa;

import dev.codedsakura.blossom.lib.text.CommandTextBuilder;
import dev.codedsakura.blossom.lib.text.TextUtils;
import net.minecraft.server.level.ServerPlayer;

import java.util.Timer;
import java.util.TimerTask;

class TpaRequest {
    public static final String TRANSLATION_KEY_TPA_TO = "blossom.tpa.to";
    public static final String TRANSLATION_KEY_TPA_HERE = "blossom.tpa.here";
    final ServerPlayer teleportWho;
    final ServerPlayer teleportTo;
    final ServerPlayer initiator;
    final ServerPlayer receiver;
    final boolean tpaHere;

    private Timer timer;

    TpaRequest(ServerPlayer initiator, ServerPlayer receiver, boolean tpaHere) {
        this.initiator = initiator;
        this.receiver = receiver;
        this.tpaHere = tpaHere;
        if (tpaHere) {
            this.teleportWho = receiver;
            this.teleportTo = initiator;
        } else {
            this.teleportWho = initiator;
            this.teleportTo = receiver;
        }
    }

    boolean similarTo(TpaRequest other) {
        return (this.teleportWho.equals(other.teleportWho) && this.teleportTo.equals(other.teleportTo)) ||
                (this.teleportWho.equals(other.teleportTo) && this.teleportTo.equals(other.teleportWho));
    }

    void startTimeout(Runnable onTimeout) {
        String translationKeyPrefix = tpaHere ? TRANSLATION_KEY_TPA_HERE : TRANSLATION_KEY_TPA_TO;
        timer = new Timer();
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    BlossomTpa.LOGGER.info("{} timed out", TpaRequest.this);
                    initiator.displayClientMessage(TextUtils.fTranslation(translationKeyPrefix + ".timeout.initiator", TextUtils.Type.ERROR, toArgs()), false);
                    receiver.displayClientMessage(TextUtils.fTranslation(translationKeyPrefix + ".timeout.receiver", TextUtils.Type.ERROR, toArgs()), false);
                    onTimeout.run();
                }
            },
            BlossomTpa.CONFIG.timeout * 1000L
        );
        initiator.displayClientMessage(TextUtils.translation(translationKeyPrefix + ".start.initiator", toArgs()), false);
        receiver.displayClientMessage(TextUtils.translation(translationKeyPrefix + ".start.receiver", toArgs()), false);
    }

    void cancelTimeout() {
        timer.cancel();
    }

    Object[] toArgs() {
        return new Object[]{
                BlossomTpa.CONFIG.timeout,
                initiator,
                receiver,
                new CommandTextBuilder("/tpacancel")
                        .setCommandRun("/tpacancel " + receiver.getGameProfile().name())
                        .setHoverShowRun(),
                new CommandTextBuilder("/tpaaccept")
                        .setCommandRun("/tpaaccept " + initiator.getGameProfile().name())
                        .setHoverShowRun(),
                new CommandTextBuilder("/tpadeny")
                        .setCommandRun("/tpadeny " + initiator.getGameProfile().name())
                        .setHoverShowRun(),
        };
    }

    @Override
    public String toString() {
        return "TpaRequest[" +
            "teleportWho=" + teleportWho.getStringUUID() + ", " +
            "teleportTo=" + teleportTo.getStringUUID() + ", " +
            "initiator=" + initiator.getStringUUID() + ", " +
            "receiver=" + receiver.getStringUUID() + ']';
    }
}
