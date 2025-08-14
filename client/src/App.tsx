import { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client/dist/sockjs";
import Stomp from "stompjs";

const SOCKET_URL = "http://localhost:8080/ws";

function App() {
  const [messages, setMessages] = useState<{ taskId: string; result: number; status: string }[]>([]);
  const [connectionStatus, setConnectionStatus] = useState<"disconnected" | "connecting" | "connected" | "error">("disconnected");

  const stompClientRef = useRef<Stomp.Client | null>(null);

  useEffect(() => {
    initializeWebSocketConnection();

    return () => {
      // if (stompClientRef.current) {
      //   stompClientRef.current.disconnect();
      // }
    };
  }, []);

  useEffect(() => {
    console.log("Messages state changed! Re-rendering UI.");
  }, [messages]);

  const initializeWebSocketConnection = () => {
    setConnectionStatus("connecting");

    let socket = new SockJS(SOCKET_URL);
    const stompClient = Stomp.over(socket);
    stompClientRef.current = stompClient;

    stompClient.connect(
      {},
      onConnected,
      (error: string | Stomp.Frame) => onError(error.toString())
    );
  };

  const onConnected = () => {
    console.log("Connected to WebSocket");
    setConnectionStatus("connected");

    if (!stompClientRef.current) return;

    stompClientRef.current.subscribe("/topic/updates", (message) => {
      console.log("Message received from subscription:", message.body);
      onMessageReceived(message);
    });
  };

  const onMessageReceived = (message: Stomp.Message) => {
    const update = JSON.parse(message.body);
    console.log("Received:", update);

    setMessages((prevMessages) => {
      const newMessages = [...prevMessages, update];
      console.log("Updated Messages State:", newMessages); // 🛠 Debugging
      return newMessages;
    });
  };

  const onError = (error: string) => {
    console.error("WebSocket error:", error);
    setConnectionStatus("error");
  };

  return (
    <div>
      <h2>WebSocket Messages</h2>
      {messages.length > 0 ? (
        <ul>
          {messages.map((msg, index) => (
            <li key={index}>
              <strong>Task ID:</strong> {msg.taskId} | <strong>Area:</strong> {msg.result} | <strong>Status:</strong> {msg.status}
            </li>
          ))}
        </ul>
      ) : (
        <p style={{ color: "red", fontWeight: "bold" }}>No messages received yet.</p>
      )}
    </div>
  );
}

export default App;
