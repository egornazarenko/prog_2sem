package common;

import model.MusicBand;
import java.io.Serializable;
import java.util.List;

/**
 * Объект ответа от сервера к клиенту.
 * Содержит текстовое сообщение и (опционально) коллекцию объектов.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Статус ответа. */
    public enum Status {
        OK,
        ERROR,
        EXIT
    }

    private final Status status;
    private final String message;
    private final List<MusicBand> collection;

    public Response(Status status, String message) {
        this.status = status;
        this.message = message;
        this.collection = null;
    }

    public Response(Status status, String message, List<MusicBand> collection) {
        this.status = status;
        this.message = message;
        this.collection = collection;
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public List<MusicBand> getCollection() { return collection; }

    @Override
    public String toString() {
        return "Response{status=" + status + ", message='" + message + "'}";
    }
}
