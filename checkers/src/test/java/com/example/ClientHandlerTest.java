package com.example.server;

import com.example.model.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientHandlerTest {

    @Test
    void sendWritesToSocket() throws Exception {
        // mock serwera i socketu
        Server serverMock = mock(Server.class);
        Socket socketMock = mock(Socket.class);

        // przechwytujemy output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos, true);

        // handler
        ClientHandler handler = new ClientHandler(serverMock, socketMock);

        // ustawiamy out ręcznie
        handler.setOut(pw);

        // wysyłamy wiadomość
        handler.send("HELLO");

        // sprawdzamy wynik
        assertEquals("HELLO\n", baos.toString());
    }

    @Test
    void setPlayerStoresPlayer() {
        Server serverMock = mock(Server.class);
        Socket socketMock = mock(Socket.class);

        ClientHandler handler = new ClientHandler(serverMock, socketMock);

        Player p = new Player("id1", "Ala", null);
        handler.setPlayer(p);

        assertEquals(p, handler.getPlayer());
    }

}
