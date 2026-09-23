package teacommontea.dashboard;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class RealtimeChannel {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(20);
    private static final long HEARTBEAT_MS = 25_000;

    private final String url;
    private final String apiKey;
    private final String topic;
    private final Consumer<Map<String, String>> onFrame;
    private final Consumer<String> onProblem;
    private final Runnable onJoined;

    private final AtomicInteger ref = new AtomicInteger(1);
    private final Object sendLock = new Object();
    private CompletableFuture<WebSocket> sendChain = CompletableFuture.completedFuture(null);
    private final StringBuilder partial = new StringBuilder();

    private volatile WebSocket socket;
    private volatile boolean closed;
    private volatile teacommontea.util.sched.TaskHandle heartbeat;

    public RealtimeChannel(String supabaseUrl, String apiKey, String channel,
                           Consumer<Map<String, String>> onFrame,
                           Runnable onJoined,
                           Consumer<String> onProblem) {
        this.url = supabaseUrl.replaceFirst("^http", "ws")
                + "/realtime/v1/websocket?apikey=" + apiKey + "&vsn=1.0.0";
        this.apiKey = apiKey;
        this.topic = "realtime:" + channel;
        this.onFrame = onFrame;
        this.onJoined = onJoined;
        this.onProblem = onProblem;
    }

    public void open() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        client.newWebSocketBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .buildAsync(URI.create(url), new Listener())
                .whenComplete((ws, err) -> {
                    if (err != null) {
                        onProblem.accept("could not reach Supabase Realtime");
                        return;
                    }
                    socket = ws;
                    join();
                });
    }

    private void join() {
        send(topic, "phx_join", "{\"config\":{\"broadcast\":{\"self\":false}}}");
        heartbeat = teacommontea.util.sched.Sched.executeAsyncRepeating(() -> {
            if (!closed) {
                send("phoenix", "heartbeat", "{}");
            }
        }, HEARTBEAT_MS, HEARTBEAT_MS);
    }

    public void broadcast(String message, String signature) {
        String payload = "{\"type\":\"broadcast\",\"event\":\"msg\",\"payload\":"
                + Json.object("msg", message, "signature", signature) + "}";
        send(topic, "broadcast", payload);
    }

    private void send(String channelTopic, String event, String payloadJson) {
        WebSocket ws = socket;
        if (ws == null || closed) {
            return;
        }
        String frame = "{\"topic\":" + Json.escape(channelTopic)
                + ",\"event\":" + Json.escape(event)
                + ",\"payload\":" + payloadJson
                + ",\"ref\":\"" + ref.getAndIncrement() + "\"}";
        synchronized (sendLock) {
            sendChain = sendChain
                    .exceptionally(t -> null)
                    .thenCompose(ignored -> ws.sendText(frame, true));
        }
    }

    public void close() {
        closed = true;
        teacommontea.util.sched.TaskHandle h = heartbeat;
        heartbeat = null;
        if (h != null) {
            try {
                h.cancel();
            } catch (Exception ignored) {
            }
        }
        WebSocket ws = socket;
        socket = null;
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "done");
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isOpen() {
        return socket != null && !closed;
    }

    private void handle(String text) {
        Map<String, String> fields = Json.flat(text);
        String event = fields.get("event");
        if (event == null) {
            return;
        }

        if ("phx_reply".equals(event)) {
            if ("ok".equals(fields.get("status"))) {
                onJoined.run();
            } else if (fields.containsKey("status")) {
                onProblem.accept("the channel refused the connection");
            }
            return;
        }

        if ("broadcast".equals(event)) {
            onFrame.accept(fields);
        }
    }

    private final class Listener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket ws) {
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            partial.append(data);
            if (last) {
                String text = partial.toString();
                partial.setLength(0);
                try {
                    handle(text);
                } catch (Exception ignored) {
                }
            }
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int status, String reason) {
            if (!closed) {
                closed = true;
                onProblem.accept("the channel closed");
            }
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            if (!closed) {
                closed = true;
                onProblem.accept("the channel failed");
            }
        }
    }
}
