package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.rest.notifications.AttachedClient;
import org.jboss.pnc.rest.notifications.OutputConverter;

import javax.websocket.Session;
import java.io.IOException;

public class SessionBasedAttachedClient implements AttachedClient {

    private final Session session;
    private final OutputConverter outputConverter;

    public SessionBasedAttachedClient(Session session, OutputConverter outputConverter) {
        this.session = session;
        this.outputConverter = outputConverter;
    }

    @Override
    public boolean isEnabled() {
        return session.isOpen();
    }

    @Override
    public void sendMessage(Object messageBody) throws IOException {
        session.getBasicRemote().sendText(outputConverter.apply(messageBody));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SessionBasedAttachedClient that = (SessionBasedAttachedClient) o;

        if (outputConverter != null ? !outputConverter.equals(that.outputConverter) : that.outputConverter != null)
            return false;
        if (session != null ? !session.equals(that.session) : that.session != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = session != null ? session.hashCode() : 0;
        result = 31 * result + (outputConverter != null ? outputConverter.hashCode() : 0);
        return result;
    }
}
