package com.atomicbunker.reservation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/seats/{showtimeId}")
public class SeatWebSocket {

    private static final Logger LOG = Logger.getLogger(SeatWebSocket.class);

    private static final Map<String, Set<Session>> showtimeSessions = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @OnOpen
    public void onOpen(Session session, @PathParam("showtimeId") String showtimeId) {
        showtimeSessions.computeIfAbsent(showtimeId, k -> ConcurrentHashMap.newKeySet()).add(session);
        LOG.infof("WebSocket connected for showtime %s, session %s", showtimeId, session.getId());
    }

    @OnClose
    public void onClose(Session session, @PathParam("showtimeId") String showtimeId) {
        Set<Session> sessions = showtimeSessions.get(showtimeId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                showtimeSessions.remove(showtimeId);
            }
        }
        LOG.infof("WebSocket disconnected for showtime %s, session %s", showtimeId, session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("showtimeId") String showtimeId) {
        LOG.errorf(throwable, "WebSocket error for showtime %s, session %s", showtimeId, session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("showtimeId") String showtimeId) {
        LOG.debugf("Received message for showtime %s: %s", showtimeId, message);
        // Client messages are not expected; server is the single source of truth
        // Seat updates should only come from the server-side business logic
    }

    public void broadcastSeatUpdate(String showtimeId, Object seatUpdate) {
        try {
            String json = objectMapper.writeValueAsString(seatUpdate);
            broadcastToShowtime(showtimeId, json);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to serialize seat update for showtime %s", showtimeId);
        }
    }

    private void broadcastToShowtime(String showtimeId, String message) {
        Set<Session> sessions = showtimeSessions.get(showtimeId);
        if (sessions != null) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            }
        }
    }
}
