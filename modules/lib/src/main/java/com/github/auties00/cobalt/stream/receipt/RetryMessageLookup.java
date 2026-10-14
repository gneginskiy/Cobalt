package com.github.auties00.cobalt.stream.receipt;

import com.github.auties00.cobalt.store.linked.LinkedWhatsAppChatStore;
import com.github.auties00.cobalt.wire.core.jid.Jid;
import com.github.auties00.cobalt.wire.linked.message.LinkedMessageInfo;

/**
 * Finds the original outbound message a retry receipt asks to re-send (frosto).
 *
 * <p>The receipt names the requester ({@code from}, or {@code participant} for a broadcast); the
 * message is looked up in that chat first, as before. A requester addressed by its LID while the
 * message was stored under its phone number (or the other way round) is not found that way when
 * the store does not know the pair yet - WA Web solves it with the LID/PN dual key of
 * {@code getActualChatInfo}, which Cobalt does not keep. Then the message is looked up by its id in
 * every chat: a re-send must not be refused only because the requester changed its address.
 */
public final class RetryMessageLookup {
    private RetryMessageLookup() {
    }

    /**
     * @return the original message, or {@code null} when the store does not have it
     */
    public static LinkedMessageInfo find(LinkedWhatsAppChatStore chats, Jid provider, Jid participant, String id) {
        if (id == null) {
            return null;
        }
        var direct = provider == null ? null : chats.findMessageById(provider, id)
                .map(LinkedMessageInfo.class::cast)
                .orElse(null);
        if (direct != null) {
            return direct;
        }
        if (participant != null && provider != null && provider.hasBroadcastServer() && !provider.isStatusBroadcastAccount()) {
            return chats.findMessageById(participant.toUserJid(), id)
                    .map(LinkedMessageInfo.class::cast)
                    .orElse(null);
        }
        return null;
    }
}
