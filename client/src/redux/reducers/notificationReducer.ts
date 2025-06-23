import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { Notification } from "../../interfaces/types";
import { ADD_NOTIFICATION, CLEAR_NOTIFICATIONS, MARK_AS_READ, SET_NOTIFICATIONS } from "../actions/notificationActionTypes";



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

