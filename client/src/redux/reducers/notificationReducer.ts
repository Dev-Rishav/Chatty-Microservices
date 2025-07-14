import { Notification } from "../../interfaces/types";
import { ADD_NOTIFICATION, CLEAR_NOTIFICATIONS, MARK_AS_READ, SET_NOTIFICATIONS, REMOVE_NOTIFICATION, UPDATE_NOTIFICATION } from "../actions/notificationActionTypes";



interface NotificationState {
  list: Notification[];
}

const initialState: NotificationState = {
  list: [],
};


const notificationReducer = (state = initialState, action: any): NotificationState => {
  switch (action.type) {
    case ADD_NOTIFICATION:
      return {
        ...state,
        list: [action.payload, ...state.list],
      };

    case UPDATE_NOTIFICATION:
      return {
        ...state,
        list: state.list.map((notification) =>
          notification.id === action.payload.id ? action.payload : notification
        ),
      };

    case REMOVE_NOTIFICATION:
      return {
        ...state,
        list: state.list.filter((notification) => notification.id !== action.payload),
      };

    case MARK_AS_READ:
      return {
        ...state,
        list: state.list.map((notification) =>
        notification.id === action.payload ? { ...notification, read: true } : notification
        ),
      };

    case CLEAR_NOTIFICATIONS:
      return {
        ...state,
        list: [],
      };

    case SET_NOTIFICATIONS:
      // console.log("reducer not", action.payload); // Log for debugging
      return {
        ...state,
        list: action.payload,
      };
    default:
      return state;
  }
};

export default notificationReducer;

