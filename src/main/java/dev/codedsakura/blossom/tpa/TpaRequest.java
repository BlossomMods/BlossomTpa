package dev.codedsakura.blossom.tpa;

import dev.codedsakura.blossom.lib.TextUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

class TpaRequest {
    public static final String TRANSLATION_KEY_TPA_TO = "blossom.tpa.to";
    public static final String TRANSLATION_KEY_TPA_HERE = "blossom.tpa.here";
    final ServerPlayerEntity teleportWho;
    final ServerPlayerEntity teleportTo;
    final ServerPlayerEntity initiator;
    final ServerPlayerEntity receiver;
    private final boolean tpaHere;

    private Timer timer;

    TpaRequest(ServerPlayerEntity teleportWho, ServerPlayerEntity teleportTo, boolean tpaHere) {
        this.teleportWho = teleportWho;
        this.teleportTo = teleportTo;
        this.tpaHere = tpaHere;
        if (tpaHere) {
            this.initiator = teleportTo;
            this.receiver = teleportWho;
        } else {
            this.initiator = teleportWho;
            this.receiver = teleportTo;
        }
    }

    boolean similarTo(TpaRequest other) {
        return (this.teleportWho.equals(other.teleportWho) && this.teleportTo.equals(other.teleportTo)) ||
                (this.teleportWho.equals(other.teleportTo) && this.teleportTo.equals(other.teleportWho));
    }

    void startTimeout(Runnable onTimeout) {
        Object[] args = {
            BlossomTpa.CONFIG.timeout,
            initiator,
            receiver
        };
        String translationKeyPrefix = tpaHere ? TRANSLATION_KEY_TPA_HERE : TRANSLATION_KEY_TPA_TO;
        timer = new Timer();
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    BlossomTpa.LOGGER.info("{} timed out", this);
                    initiator.sendMessage(TextUtils.fTranslation(translationKeyPrefix + ".timeout.initiator", TextUtils.Type.ERROR, args), false);
                    receiver.sendMessage(TextUtils.fTranslation(translationKeyPrefix + ".timeout.receiver", TextUtils.Type.ERROR, args), false);
                    onTimeout.run();
                }
            },
            BlossomTpa.CONFIG.timeout * 1000L
        );
        initiator.sendMessage(TextUtils.translation(translationKeyPrefix + ".start.initiator", args), false);
        receiver.sendMessage(TextUtils.translation(translationKeyPrefix + ".start.receiver", args), false);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TpaRequest) obj;
        return Objects.equals(this.teleportWho, that.teleportWho) &&
            Objects.equals(this.teleportTo, that.teleportTo) &&
            Objects.equals(this.initiator, that.initiator) &&
            Objects.equals(this.receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teleportWho, teleportTo, initiator, receiver);
    }

    @Override
    public String toString() {
        return "TpaRequest[" +
            "teleportWho=" + teleportWho.getUuidAsString() + ", " +
            "teleportTo=" + teleportTo.getUuidAsString() + ", " +
            "initiator=" + initiator.getUuidAsString() + ", " +
            "receiver=" + receiver.getUuidAsString() + ']';
    }
}
