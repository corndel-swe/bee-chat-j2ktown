import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final Map<String, WsContext> userSessions = new HashMap<>();

    public static void main(String[] args) {

        Javalin app = Javalin.create(javalinConfig -> {
            // Modifying the WebSocketServletFactory to set the socket timeout to 120 seconds
            javalinConfig.jetty.modifyWebSocketServletFactory(jettyWebSocketServletFactory ->
                    jettyWebSocketServletFactory.setIdleTimeout(Duration.ofSeconds(120))
            );
        });


        app.ws("/", wsConfig -> {

            wsConfig.onConnect((connectContext) -> {
                String sessionId = connectContext.sessionId();
                userSessions.put(sessionId, connectContext);
                System.out.println("Connected: " + sessionId);
            });

            wsConfig.onMessage((messageContext) -> {
                String json = messageContext.message();
                ObjectMapper objectMapper = new ObjectMapper();


                Message message = objectMapper.readValue(json, Message.class);
                String recipientId = message.recipientId;


                String messageJson = objectMapper.writeValueAsString(message);

                if (!recipientId.isEmpty()){
                    for (Map.Entry<String, WsContext> entry : userSessions.entrySet()) {
                        WsContext recipientContext = entry.getValue();
                        recipientContext.send(messageJson);  // Send JSON string
                    }
                } else {
                    WsContext recipientContext = userSessions.get(recipientId);
                    if (recipientContext != null){
                        recipientContext.send(messageJson);
                        messageContext.send(messageJson);
                    } else {
                        System.out.println("Recipient Id not found");
                    }
                }

                System.out.println("Message: " + messageContext.sessionId());

            });

            wsConfig.onClose((closeContext) -> {
                System.out.println("Closed: " + closeContext.sessionId());
            });

            wsConfig.onError((errorContext) -> {
                System.out.println("Error: " + errorContext.sessionId());
            });

        });


        app.start(5001);
    }




}