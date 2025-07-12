import { Chat } from "../../interfaces/types";
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
} from "../actions/chatActionTypes";

interface ChatState {
  list: Chat[];
  loading: boolean;
  error: string | null;
}

const initialState: ChatState = {
  list: [],
  loading: false,
  error: null,
};

const chatReducer = (state = initialState, action: any): ChatState => {
  switch (action.type) {
    case SET_CHATS:
      return {
        ...state,
        list: action.payload,
        loading: false,
        error: null,
      };

    case ADD_CHAT:
      // Check if chat already exists
      const existingChatIndex = state.list.findIndex(chat => chat.email === action.payload.email);
      if (existingChatIndex >= 0) {
        return state; // Don't add duplicate
      }
      return {
        ...state,
        list: [action.payload, ...state.list], // Add to beginning of list
      };

    case UPDATE_CHAT_LAST_MESSAGE:
      return {
        ...state,
        list: state.list.map(chat => {
          if (chat.email === action.payload.chatEmail) {
            return {
              ...chat,
              lastMessage: action.payload.message.content || (action.payload.message.fileUrl ? "📎 File" : ""),
              timestamp: action.payload.message.timestamp,
              // If chat had no ID before (new contact), keep it as null
              // Backend will handle creating chat ID when first message is sent
            };
          }
          return chat;
        }),
      };

    case UPDATE_CHAT_ONLINE_STATUS:
      return {
        ...state,
        list: state.list.map(chat => {
          if (chat.email === action.payload.chatEmail) {
            return {
              ...chat,
              online: action.payload.isOnline,
            };
          }
          return chat;
        }),
      };

    case INCREMENT_UNREAD_COUNT:
      return {
        ...state,
        list: state.list.map(chat => {
          if (chat.email === action.payload) {
            return {
              ...chat,
              unread: (chat.unread || 0) + 1,
            };
          }
          return chat;
        }),
      };

    case RESET_UNREAD_COUNT:
      return {
        ...state,
        list: state.list.map(chat => {
          if (chat.email === action.payload) {
            return {
              ...chat,
              unread: 0,
            };
          }
          return chat;
        }),
      };

    case SET_CHATS_LOADING:
      return {
        ...state,
        loading: action.payload,
      };

    case SET_CHATS_ERROR:
      return {
        ...state,
        error: action.payload,
        loading: false,
      };

    case CLEAR_CHATS:
      return {
        ...state,
        list: [],
        loading: false,
        error: null,
      };

    default:
      return state;
  }
};

export default chatReducer;
