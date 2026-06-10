package client;

import common.Request;
import common.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Управляет TCP-соединением с сервером.
 * На клиенте используются потоки ввода-вывода (InputStream/OutputStream).
 *
 * Протокол: 4 байта (int, big-endian) — длина данных, затем сами данные.
 */
public class ServerConnection {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final String host;
    private final int port;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Устанавливает соединение с сервером.
     *
     * @return true если успешно
     */
    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Отправляет запрос на сервер.
     *
     * @param request запрос
     * @return true если успешно
     */
    public boolean sendRequest(Request request) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(request);
                oos.flush();
            }
            byte[] data = baos.toByteArray();
            out.writeInt(data.length);
            out.write(data);
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Получает ответ от сервера.
     *
     * @return объект Response или null при ошибке
     */
    public Response receiveResponse() {
        try {
            int length = in.readInt();
            if (length <= 0 || length > 10 * 1024 * 1024) return null;
            byte[] data = new byte[length];
            in.readFully(data);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                return (Response) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Проверяет, установлено ли соединение.
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Закрывает соединение.
     */
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
