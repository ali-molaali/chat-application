import { useEffect, useState, type FormEvent } from "react";
import { Client } from "@stomp/stompjs";

type ChatMessage = {
    eventId: string;
    senderId: number;
    roomId: string;
    text: string;
    sentAt: string;
};

type SendMessageRequest = {
    senderId: number;
    roomId: string;
    text: string;
};

const roomName = "general";

function App() {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [text, setText] = useState<string>("");
    const [isSending, setIsSending] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [connectionStatus, setConnectionStatus] =
        useState<"connecting" | "connected" | "error">("connecting");

    function addMessage(message: ChatMessage) {
        setMessages((currentMessages) => {
            const alreadyExists = currentMessages.some(
                (currentMessage) =>
                    currentMessage.eventId === message.eventId
            );

            return alreadyExists
                ? currentMessages
                : [...currentMessages, message];
        });
    }

    useEffect(() => {
        const client = new Client({
            brokerURL: "ws://localhost:8080/api/ws",

            onConnect: () => {
                setConnectionStatus("connected");

                client.subscribe(`/topic/${roomName}`, (frame) => {
                    const message: ChatMessage = JSON.parse(frame.body);

                    if (message.roomId === roomName) {
                        addMessage(message);
                    }
                });
            },

            onStompError: () => {
                setConnectionStatus("error");
                setError("WebSocket connection failed");
            },

            onWebSocketError: () => {
                setConnectionStatus("error");
                setError("WebSocket connection failed");
            },

            onWebSocketClose: () => {
                setConnectionStatus("error");
            }
        });

        client.activate();

        return () => {
            void client.deactivate();
        };
    }, []);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const trimmedText = text.trim();
        if (!trimmedText || isSending) {
            return;
        }

        setIsSending(true);
        setError(null);

        try {
            const request: SendMessageRequest = {
                senderId: 1,
                roomId: roomName,
                text: trimmedText
            };

            const response = await fetch("/api/messages", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(request)
            });

         if (!response.ok) {
    const errorBody = await response.text();

    throw new Error(
        `Message could not be sent (${response.status}): ${errorBody}`
    );
}

            const message: ChatMessage = await response.json();
            addMessage(message);
            setText("");
        } catch (submitError: unknown) {
            setError(
                submitError instanceof Error
                    ? submitError.message
                    : "Message could not be sent"
            );
        } finally {
            setIsSending(false);
        }
    }

    return (
        <main>
            <h1>Chat Application</h1>
            <p>Room: {roomName}</p>
            <p>Connection: {connectionStatus}</p>

            <form onSubmit={handleSubmit}>
                <input
                    value={text}
                    onChange={(event) => {
                        setText(event.target.value);
                        setError(null);
                    }}
                    disabled={isSending}
                    aria-label="Message"
                />
                <button
                    type="submit"
                    disabled={isSending || !text.trim()}
                >
                    {isSending ? "Sending..." : "Send"}
                </button>
            </form>

            {error && <p role="alert">{error}</p>}

            <ul>
                {messages.map((message) => (
                    <li key={message.eventId}>
                        {message.text}
                    </li>
                ))}
            </ul>
        </main>
    );
}

export default App;
