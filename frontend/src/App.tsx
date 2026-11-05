
import { useState, type FormEvent } from "react";
type ChatMessage = {
    eventId: string;
    senderId: number;
    roomId: string;
    text: string;
    sentAt: string;
};
function App() {
const [messages, setMessages] = useState<ChatMessage[]>([
    {
        eventId: "test-1",
        senderId: 1,
        roomId: "general",
        text: "First test message",
        sentAt: "2026-11-06T10:00:00Z"
    }
]);    const roomName:string = "general";
    const [text, setText] = useState<string>("");

    return (
        <main>
            <h1>Chat Application</h1>
            <p>Room: {roomName}</p>
         <form onSubmit={handleSubmit}>
    <input
        value={text}
        onChange={(event) => setText(event.target.value)}
    />
    <button type="submit">Send</button>
</form>
<ul>
    {messages.map((message) => (
        <li key={message.eventId}>
            {message.text}
        </li>
    ))}
</ul>
        </main>
        
    );

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!text.trim()) {
        return;
    }

    const newMessage: ChatMessage = {
        eventId: crypto.randomUUID(),
        senderId: 1,
        roomId: roomName,
        text: text,
        sentAt: new Date().toISOString()
    };

    setMessages((currentMessages) => [
        ...currentMessages,
        newMessage
    ]);

    setText("");
}
}

export default App;