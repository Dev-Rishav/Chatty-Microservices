import { useEffect, useRef, useState, useCallback } from "react";
import {
  MagnifyingGlassIcon,
  PlusCircleIcon,
  SunIcon,
  MoonIcon,
  Cog6ToothIcon,
  CurrencyDollarIcon,
  UserGroupIcon,
  DocumentTextIcon,
} from "@heroicons/react/24/outline";
import bg2 from "../../assets/bg2.webp";
import bg1 from "../../assets/bg1.avif";
import ChatHeader from "./ChatHeader";
import MessageBubble from "./MessageBubble";
import MessageInput from "./MessageInput";
import EmptyState from "./EmptyState";
import SendMoneyModal from "./SendMoneyModal";
import { Chat, Message, Notification } from "../../interfaces/types";
import { useSelector } from "react-redux";
import store, { RootState } from "../../redux/store";
import fetchAllMessages from "../../utility/fetchAllMessages";
import stompService from "../../services/stompService";
import toast from "react-hot-toast";
import uploadFile from "../../utility/uploadFile";
import { useAppDispatch, useAppSelector } from "../../redux/hooks";
import { updateUserPresence, setInitialOnlineUsers } from "../../redux/actions/presenceActions";
import ChatList from "./ChatList";
import Navbar from "./Navbar";
import { addNotification, updateNotification } from "../../redux/actions/notificationActions";
import SideBarHeader from "./SideBarHeader";
import chatStompService from "../../services/chatStompService";
import notificationStompService from "../../services/notificationStompService";
import ChatDebugger from "./ChatDebugger";
import { 
  fetchAllChats, 
  handleNewMessage, 
  handleContactAccepted, 
  resetUnreadCount,
  updateChatOnlineStatus
} from "../../redux/actions/chatActions";
import hybridChatService from "../../services/hybridChatService";

const HomePage: React.FC = () => {
  const [selectedChatId, setSelectedChatId] = useState<number | null>(null);
  const [selectedChatEmail, setSelectedChatEmail] = useState<string | null>(null);
  const [messageInput, setMessageInput] = useState("");
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [showSendMoney, setShowSendMoney] = useState(false);
  const { token, userDTO } = useSelector((state: RootState) => state.auth);
  const { list: allChats, loading: chatsLoading, error: chatsError } = useSelector((state: RootState) => state.chats);
  const [selectedChat, setSelectedChat] = useState<Chat | null>(null);
  const [currentMessages, setCurrentMessages] = useState<Message[]>([]);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const onlineUsers = useSelector(
    (state: RootState) => state.presence.onlineUsers
  );
  const onlineUsersArray = Object.keys(onlineUsers).filter(
    (email) => onlineUsers[email]
  );
  const dispatch = useAppDispatch();

  if (token === null || token === undefined) {
    window.location.href = "/login";
    return null;
  }


  // Initialize hybrid chat service when component mounts
  useEffect(() => {
    if (token) {
      hybridChatService.initialize(token);
      
      // Set up global event listeners for background updates
      hybridChatService.onPresenceUpdate((users) => {
        console.log("👥 Presence update received:", users);
        
        // Convert array of online users to Record<string, boolean> format
        const onlineUsersMap: Record<string, boolean> = {};
        if (users && Array.isArray(users) && users.length > 0) {
          users.forEach((email) => {
            onlineUsersMap[email] = true;
          });
          console.log("📊 Converted to presence map:", onlineUsersMap);
        } else {
          console.log("📊 No online users or empty array received");
        }
        
        // Update the main presence state (this fixes the UI display issue)
        dispatch(setInitialOnlineUsers(onlineUsersMap));
        
        // Also update individual chat online status in Redux for chat list
        if (users && Array.isArray(users)) {
          // First, mark all current chats as offline (reset)
          const currentChats = store.getState().chats.list;
          currentChats.forEach((chat) => {
            const isOnline = users.includes(chat.email);
            dispatch(updateChatOnlineStatus(chat.email, isOnline));
          });
        }
        
        console.log("✅ Presence state updated in Redux");
      });

      hybridChatService.onChatListUpdate((chats) => {
        console.log("💬 Chat list update:", chats);
        // Update chat list in global state
      });

      hybridChatService.onMessageUpdate((message) => {
        console.log("📨 Message update:", message);
        // Handle message updates (notifications, etc.)
        dispatch(handleNewMessage(message, message.to));
      });
    }

    // Cleanup on unmount
    return () => {
      hybridChatService.disconnect();
    };
  }, [token]);





  // Presence updates are now handled via SSE in hybridChatService (Kafka-driven)
  // No need for separate WebSocket presence subscriptions

  //function to fetch messages between current and selected chat user
  const fetchMessages = useCallback(async (user: string) => {
    try {
      const res: Message[] = await fetchAllMessages(token, user);
      if (res && res.length > 0) {
        setCurrentMessages(res);
        console.log("teh current messages are", res);
      }
    } catch (error) {
      console.error("Error fetching messages:", error);
    }
  }, [token]);

  //fetch message on selected chat
  useEffect(() => {
    if (allChats && selectedChatEmail) {
      const chat = allChats.find((c) => c.email === selectedChatEmail);
      if (chat) {
        setSelectedChat(chat);
        // Reset unread count when chat is selected
        dispatch(resetUnreadCount(chat.email));
        
        // Start hybrid chat session when a chat is selected
        hybridChatService.startChatSession(chat.email, (message) => {
          console.log("📨 Real-time message received:", message);
          // Update current messages if from the currently selected chat
          if (message.from === chat.email) {
            setCurrentMessages((prevMessages) => {
              return [...(prevMessages || []), message];
            });
          }
        });
        
        if (chat.id) {
          // Only fetch messages if there's a chat ID (existing conversation)
          fetchMessages(chat.email);
        } else {
          // New contact without message history
          setCurrentMessages([]);
        }
      }
    }
  }, [selectedChatEmail, dispatch]);

  // End chat session when switching chats or unmounting
  useEffect(() => {
    return () => {
      if (selectedChat) {
        hybridChatService.endChatSession();
      }
    };
  }, [selectedChat]);

  useEffect(() => {
    if (token) {
      dispatch(fetchAllChats(token));
    }
  }, [token, dispatch]);

  //send message function
  const handleSendMessage = async () => {
    if ((!messageInput.trim() && !selectedFile) || !selectedChat || !userDTO)
      return;

    const pushMessageToUI = (fileUrl: string | null = null) => {
      const messageToSend: Message = {
        id: Date.now(),
        content: messageInput,
        from: userDTO.email,
        to: selectedChat.email,
        timestamp: new Date().toISOString(),
        fileUrl: fileUrl || undefined,
      };

      setCurrentMessages((prev) => [...prev, messageToSend]);
      console.log("Message to send:", messageToSend);
      
      // Update Redux chat list with new message
      dispatch(handleNewMessage(messageToSend, userDTO.email));
      
      setMessageInput("");
      setSelectedFile(null);
    };

    try {
      if (selectedFile) {
        // Upload file first, then send message with file URL
        const data = await uploadFile(selectedFile, token);
        await hybridChatService.sendMessage(messageInput || "", data.url);
        pushMessageToUI(data.url);
      } else {
        // Send text message only
        await hybridChatService.sendMessage(messageInput);
        pushMessageToUI();
      }
      
      console.log("✅ Message sent successfully via hybrid service");
    } catch (error) {
      console.error("❌ Failed to send message:", error);
      
      // Fallback to old method if hybrid service fails
      const payload: any = {
        to: selectedChat.email,
        from: userDTO.email,
        content: messageInput || "",
      };

      if (selectedFile) {
        try {
          const data = await uploadFile(selectedFile, token);
          payload.fileUrl = data.url;
          chatStompService.send("/app/private-message", payload);
          pushMessageToUI(data.url);
        } catch (error) {
          console.error("File upload failed:", error);
        }
      } else {
        chatStompService.send("/app/private-message", payload);
        pushMessageToUI();
      }
    }
  };

  //event listeners for new messages
  useEffect(() => {
    if (!chatStompService.isConnected()) {
      console.warn(
        "⚠️ Chat WebSocket is not connected. Cannot subscribe to messages."
      );
      return;
    }

    chatStompService.subscribe("/user/queue/messages", (payload: any) => {
      const message: Message = {
        id: Date.now(), // Temporary ID until the backend sends a real one
        content: payload.content,
        from: payload.from,
        to: payload.to,
        timestamp: new Date().toISOString(),
        fileUrl: payload.fileUrl ? payload.fileUrl : null,
        reactions: payload.reactions ? payload.reactions : {},
        encrypted: payload.encrypted ? payload.encrypted : false,
      };
      
      console.log("Message received:", message);
      
      // Update Redux chat list with new message
      if (userDTO) {
        dispatch(handleNewMessage(message, userDTO.email));
      }
      
      // Only update current messages if from the currently selected chat
      if (payload.from === selectedChat?.email) {
        setCurrentMessages((prevMessages) => {
          return [...(prevMessages || []), message];
        });
      }
      
      toast.success("💬 Got message:", payload.content);
    });

    return () => {
      chatStompService.unsubscribe("/user/queue/messages");
    };
  }, [selectedChat, dispatch, userDTO]);

  //* fetch notifications
  useEffect(() => {
    if (!token) return;

    console.log("🔌 Setting up notification WebSocket connection...");
    
    notificationStompService.connect(token, () => {
      console.log("✅ Notification WebSocket connected successfully");
      // subscribe to notifications topic
      setTimeout(() => {
        console.log("📡 Subscribing to /user/queue/notifications...");
        notificationStompService.subscribe(
          "/user/queue/notifications",
          (payload) => {
            console.log("🔔 Received notification payload:", payload);

            const notification: Notification = {
              id: payload.id || Date.now().toString(),
              message: payload.message || "New Notification",
              isRead: payload.read || false,
              createdAt: payload.createdAt || new Date().toISOString(),
              type: payload.message?.includes("contact request") ? 'contact_request' : 'general',
              requestId: payload.requestId || payload.id,
              senderEmail: payload.senderEmail,
              senderUsername: payload.senderUsername,
            };

            console.log("📝 Processing notification:", notification);
            
            // Check if this is an update to an existing notification or a new one
            const existingNotifications = store.getState().notifications.list;
            const existingNotification = existingNotifications.find(n => 
              n.requestId === notification.requestId && 
              n.type === 'contact_request'
            );
            
            // Check if this notification is related to contact changes
            const isContactAccepted = notification.message.includes("accepted your contact request") || 
                                    notification.message.includes("You have accepted the contact request");
            
            if (existingNotification && notification.message.includes("accepted")) {
              // This is an acceptance update - update the existing notification
              const updatedNotification = {
                ...existingNotification,
                message: notification.message,
                type: 'general' as const, // Remove contact request type so buttons disappear
                isRead: false
              };
              console.log("🔄 Updating existing notification for acceptance");
              dispatch(updateNotification(updatedNotification));
              
              // 🚀 Add contact to chat list when accepted
              if (notification.senderEmail && notification.senderUsername) {
                console.log("➕ Adding accepted contact to chat list");
                dispatch(handleContactAccepted(notification.senderEmail, notification.senderUsername));
              }
              
            } else {
              // This is a new notification
              console.log("➕ Adding new notification");
              dispatch(addNotification(notification));
              
              // 🚀 Handle contact acceptance from new notifications
              if (isContactAccepted && notification.senderEmail && notification.senderUsername) {
                console.log("➕ Adding new accepted contact to chat list");
                dispatch(handleContactAccepted(notification.senderEmail, notification.senderUsername));
              }
            }
            
            toast.success(`🔔 ${payload.message}`);
            console.log("✅ Notification processed and dispatched to Redux store");
          }
        );
      }, 300); // Delay to ensure connection is ready
    });

    return () => {
      console.log("🧹 Cleaning up notification subscription...");
      notificationStompService.unsubscribe("/user/queue/notifications");
    };
  }, [token, dispatch]);


  
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(scrollToBottom, [currentMessages]);

  //handle file upload
  const handleFileSelect = (file: File) => {
    setSelectedFile(file);
  };

  // console.log("curent user", userDTO);

  return (
    <>
      <Navbar />
      <div
        className="min-h-screen bg-[#f5f1e8]"
        style={{ backgroundImage: `url(${bg2})` }}
      >
        <div className="flex h-screen max-w-7xl mx-auto">
          {/* Left Sidebar */}
          <div className="w-96 pr-4 py-6 flex flex-col">
            <div className="paper-container p-6 rounded-sm flex-1 flex flex-col shadow-paper">
              {/* Left Top */}
              <SideBarHeader
                isDarkMode={isDarkMode}
                setIsDarkMode={setIsDarkMode}
              />

              {/* Left Mid */}
              <ChatList
                allChats={allChats}
                selectedChatId={selectedChatId}
                setSelectedChatId={setSelectedChatId}
                selectedChatEmail={selectedChatEmail}
                setSelectedChatEmail={setSelectedChatEmail}
                onlineUsersArray={onlineUsersArray}
              />

              {/*Left Bottom */}
              <div className="mt-4 paper-container font-crimson text-xl p-4 rounded-sm shadow-paper">
                <button className="paper-menu-item">
                  <Cog6ToothIcon className="w-6 h-6 mr-3 text-amber-700" />
                  <span className="text-amber-900">Settings</span>
                </button>
                <button
                  className="paper-menu-item"
                  onClick={() => setShowSendMoney(true)}
                >
                  <CurrencyDollarIcon className="w-6 h-6 mr-3 text-green-700" />
                  <span className="text-amber-900">Send Money</span>
                </button>
                <button className="paper-menu-item">
                  <UserGroupIcon className="w-6 h-6 mr-3 text-stone-600" />
                  <span className="text-amber-900">New Group</span>
                </button>
              </div>
            </div>
          </div>

          {/* Main Chat Area */}
          <div className="flex-1 flex flex-col py-6 pl-4">
            {selectedChat ? (
              <div className="paper-container h-full flex flex-col rounded-sm shadow-paper">
                <ChatHeader chat={selectedChat} />
                <div
                  className="flex-1 overflow-y-auto p-6 space-y-6 bg-[#faf8f3] bg-cover bg-center"
                  style={{ backgroundImage: `url(${bg1})` }}
                >
                  {currentMessages.map((message) => (
                    <MessageBubble key={message.id} message={message} />
                  ))}
                  <div ref={messagesEndRef} />
                </div>
                <MessageInput
                  value={messageInput}
                  onChange={setMessageInput}
                  onSend={handleSendMessage}
                  onFileSelect={handleFileSelect}
                />
              </div>
            ) : (
              <EmptyState onNewChat={() => setSelectedChatId(1)} />
            )}
            <SendMoneyModal
              show={showSendMoney}
              onClose={() => setShowSendMoney(false)}
            />
          </div>
        </div>
      </div>
      {/* <ChatDebugger /> */}
    </>
  );
};

export default HomePage;
