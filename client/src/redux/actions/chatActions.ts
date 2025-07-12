import axios from "axios";
import { Chat, Message } from "../../interfaces/types";
import { 
  SET_CHATS, 
  ADD_CHAT, 
  UPDATE_CHAT_LAST_MESSAGE, 
  UPDATE_CHAT_ONLINE_STATUS,
  SET_CHATS_LOADING,
  SET_CHATS_ERROR,
  CLEAR_CHATS,
  INCREMENT_UNREAD_COUNT,
  RESET_UNREAD_COUNT
} from "./chatActionTypes";

// Action Creators
export const setChats = (chats: Chat[]) => ({
  type: SET_CHATS,
  payload: chats,
});

export const addChat = (chat: Chat) => ({
  type: ADD_CHAT,
  payload: chat,
});

export const updateChatLastMessage = (chatEmail: string, message: Message) => ({
  type: UPDATE_CHAT_LAST_MESSAGE,
  payload: { chatEmail, message },
});

export const updateChatOnlineStatus = (chatEmail: string, isOnline: boolean) => ({
  type: UPDATE_CHAT_ONLINE_STATUS,
  payload: { chatEmail, isOnline },
});

export const setChatsLoading = (loading: boolean) => ({
  type: SET_CHATS_LOADING,
  payload: loading,
});

export const setChatsError = (error: string | null) => ({
  type: SET_CHATS_ERROR,
  payload: error,
});

export const clearChats = () => ({
  type: CLEAR_CHATS,
});

export const incrementUnreadCount = (chatEmail: string) => ({
  type: INCREMENT_UNREAD_COUNT,
  payload: chatEmail,
});

export const resetUnreadCount = (chatEmail: string) => ({
  type: RESET_UNREAD_COUNT,
  payload: chatEmail,
});

// Thunk: Fetch All Chats
export const fetchAllChats = (token: string) => {
  return async (dispatch: any) => {
    try {
      dispatch(setChatsLoading(true));
      dispatch(setChatsError(null));
      
      const apiUrl = `http://localhost:8081/chat/allChats`;
      const response = await axios.get(apiUrl, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      
      dispatch(setChats(response.data));
      dispatch(setChatsLoading(false));
    } catch (error) {
      console.error("Error fetching chats:", error);
      dispatch(setChatsError("Failed to fetch chats"));
      dispatch(setChatsLoading(false));
    }
  };
};

// Thunk: Add New Contact to Chat List
export const addNewContactToChats = (contactEmail: string, contactUsername: string) => {
  return async (dispatch: any) => {
    const newChat: Chat = {
      id: null, // No chat history yet
      email: contactEmail,
      username: contactUsername,
      lastMessage: "", // No messages yet
      timestamp: new Date().toISOString(),
      unread: 0,
      isGroup: false,
      online: false,
      profilePic: undefined,
    };
    
    dispatch(addChat(newChat));
  };
};

// Thunk: Handle New Message (update last message and timestamp)
export const handleNewMessage = (message: Message, currentUserEmail: string) => {
  return async (dispatch: any) => {
    // Determine which chat to update based on message direction
    const chatEmail = message.from === currentUserEmail ? message.to : message.from;
    
    dispatch(updateChatLastMessage(chatEmail, message));
    
    // If message is from someone else, increment unread count
    if (message.from !== currentUserEmail) {
      dispatch(incrementUnreadCount(chatEmail));
    }
  };
};

// Thunk: Handle Contact Acceptance (add to chat list)
export const handleContactAccepted = (contactEmail: string, contactUsername: string) => {
  return async (dispatch: any, getState: any) => {
    const state = getState();
    const chatsList = state.chats.list;
    
    // Check if contact already exists in chat list
    const existingChat = chatsList.find((chat: Chat) => chat.email === contactEmail);
    
    if (!existingChat) {
      dispatch(addNewContactToChats(contactEmail, contactUsername));
    }
  };
};
