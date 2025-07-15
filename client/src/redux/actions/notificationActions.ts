import axios from "axios";
import { Notification } from "../../interfaces/types";
import { ADD_NOTIFICATION, CLEAR_NOTIFICATIONS,MARK_AS_READ,SET_NOTIFICATIONS, REMOVE_NOTIFICATION, UPDATE_NOTIFICATION } from "./notificationActionTypes";
import notificationStompService from "../../services/notificationStompService";
import { buildApiUrl, API_CONFIG } from '../../config/api';


// Action Creators
export const addNotification = (notification: Notification) => ({
  type: ADD_NOTIFICATION,
  payload: notification,
});

export const updateNotification = (notification: Notification) => ({
  type: UPDATE_NOTIFICATION,
  payload: notification,
});

export const markAsRead = (notificationId: any) => ({
  type: MARK_AS_READ,
  payload: notificationId,
});

export const clearNotifications = () => ({
  type: CLEAR_NOTIFICATIONS,
});

export const removeNotification = (notificationId: string) => ({
  type: REMOVE_NOTIFICATION,
  payload: notificationId,
});

export const setNotifications = (notifications: Notification[]) => ({
  type: SET_NOTIFICATIONS,
  payload: notifications,
});

// Thunk: Accept Contact Request
export const acceptContactRequest = (requestId: string, notificationId: string) => {
  return async (dispatch: any) => {
    try {
      notificationStompService.acceptContactRequest(requestId);
      // Don't remove the notification - let the backend send an updated one
      console.log("✅ Contact request acceptance sent to backend");
    } catch (error) {
      console.error("❌ Error accepting contact request:", error);
    }
  };
};

// Thunk: Reject Contact Request
export const rejectContactRequest = (requestId: string, notificationId: string) => {
  return async (dispatch: any) => {
    try {
      notificationStompService.rejectContactRequest(requestId);
      // Don't remove the notification - let it stay visible as rejected
      console.log("✅ Contact request rejection sent to backend");
    } catch (error) {
      console.error("❌ Error rejecting contact request:", error);
    }
  };
};

// Thunk: Fetch Notification History
export const fetchNotificationHistory = () => {
  return async (dispatch: any, getState: any) => {
    try {
      const token = getState().auth.token; 
      const response = await axios.get(buildApiUrl(API_CONFIG.ENDPOINTS.NOTIFICATION.ALL), {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status !== 200) {
        throw new Error("Failed to fetch notifications");
      }

      const data:Notification[] =  response.data;
      console.log("notifications",data);
      
      dispatch(setNotifications(data));
    } catch (error) {
      console.error("❌ Error fetching notifications:", error);
    }
  };
};
