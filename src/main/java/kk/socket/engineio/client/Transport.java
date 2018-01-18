package kk.socket.engineio.client;


import java.util.Map;

import kk.socket.emitter.Emitter;
import kk.socket.engineio.parser.Packet;
import kk.socket.engineio.parser.Parser;
import kk.socket.thread.EventThread;
import kk.socket.utf8.UTF8Exception;
import okhttp3.Call;
import okhttp3.WebSocket;

public abstract class Transport extends Emitter {

    protected enum ReadyState {
        OPENING, OPEN, CLOSED, PAUSED;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public static final String EVENT_OPEN = "open";
    public static final String EVENT_CLOSE = "close";
    public static final String EVENT_PACKET = "packet";
    public static final String EVENT_DRAIN = "drain";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_REQUEST_HEADERS = "requestHeaders";
    public static final String EVENT_RESPONSE_HEADERS = "responseHeaders";

    public boolean writable;
    public String name;
    public Map<String, String> query;

    protected boolean secure;
    protected boolean timestampRequests;
    protected int port;
    protected String path;
    protected String hostname;
    protected String timestampParam;
    protected Socket socket;
    protected ReadyState readyState;
    protected WebSocket.Factory webSocketFactory;
    protected Call.Factory callFactory;

    public Transport(Options opts) {
        this.path = opts.path;
        this.hostname = opts.hostname;
        this.port = opts.port;
        this.secure = opts.secure;
        this.query = opts.query;
        this.timestampParam = opts.timestampParam;
        this.timestampRequests = opts.timestampRequests;
        this.socket = opts.socket;
        this.webSocketFactory = opts.webSocketFactory;
        this.callFactory = opts.callFactory;
    }

    protected Transport onError(String msg, Exception desc) {
        // TODO: handle error
        Exception err = new EngineIOException(msg, desc);
        this.emit(EVENT_ERROR, err);
        return this;
    }

    public Transport open() {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                if (Transport.this.readyState == ReadyState.CLOSED || Transport.this.readyState == null) {
                    Transport.this.readyState = ReadyState.OPENING;
                    Transport.this.doOpen();
                }
            }
        });
        return this;
    }

    public Transport close() {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                if (Transport.this.readyState == ReadyState.OPENING || Transport.this.readyState == ReadyState.OPEN) {
                    Transport.this.doClose();
                    Transport.this.onClose();
                }
            }
        });
        return this;
    }

    public void send(final Packet[] packets) {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                if (Transport.this.readyState == ReadyState.OPEN) {
                    try {
                        Transport.this.write(packets);
                    } catch (UTF8Exception err) {
                        throw new RuntimeException(err);
                    }
                } else {
                    throw new RuntimeException("Transport not open");
                }
            }
        });
    }

    protected void onOpen() {
        this.readyState = ReadyState.OPEN;
        this.writable = true;
        this.emit(EVENT_OPEN);
    }

    protected void onData(String data) {
        this.onPacket(Parser.decodePacket(data));
    }

    protected void onData(byte[] data) {
        this.onPacket(Parser.decodePacket(data));
    }

    protected void onPacket(Packet packet) {
        this.emit(EVENT_PACKET, packet);
    }

    protected void onClose() {
        this.readyState = ReadyState.CLOSED;
        this.emit(EVENT_CLOSE);
    }

    abstract protected void write(Packet[] packets) throws UTF8Exception;

    abstract protected void doOpen();

    abstract protected void doClose();


    public static class Options {

        public String hostname;
        public String path;
        public String timestampParam;
        public boolean secure;
        public boolean timestampRequests;
        public int port = -1;
        public int policyPort = -1;
        public Map<String, String> query;
        protected Socket socket;
        public WebSocket.Factory webSocketFactory;
        public Call.Factory callFactory;
    }
}
