import axios from 'axios';
import { toast } from 'react-hot-toast';
import {
  LOGIN_REQUEST,
  LOGIN_SUCCESS,
  LOGIN_FAILURE,
  SIGNUP_REQUEST,
  SIGNUP_SUCCESS,
  SIGNUP_FAILURE,
  LOGOUT,
} from './authActionTypes';
import { AppDispatch, persistor } from '../store';
import { setInitialOnlineUsers, updateUserPresence } from './presenceActions';
import stompService from '../../services/stompService';
import {UserDTO} from '../../interfaces/types'
import { buildApiUrl, API_CONFIG } from '../../config/api';

interface Credentials {
  email: string;
  password: string;
}


interface LoginResponse {
  userDTO: UserDTO;
  token: string;
}

export const loginUser = (credentials: Credentials) => async (dispatch: AppDispatch) => {
  dispatch({ type: LOGIN_REQUEST });
  try {
    const response = await axios.post<LoginResponse>(buildApiUrl(API_CONFIG.ENDPOINTS.AUTH.LOGIN), credentials);
    const { userDTO, token } = response.data;

    toast.success(`Welcome back, ${userDTO.username}!`);
    localStorage.setItem('authToken', token);
    localStorage.setItem('userDTO', JSON.stringify(userDTO));

    dispatch({
      type: LOGIN_SUCCESS,
      payload: { userDTO, token },
    });

    const res = await axios.get<string[]>(
      buildApiUrl(API_CONFIG.ENDPOINTS.PRESENCE.ONLINE),
      { headers: { Authorization: `Bearer ${token}` } }
    );

    //  Convert Set<String> → Record<string, boolean>
    const onlineUsersMap: Record<string, boolean> = {};
    res.data.forEach((email: string) => {
      onlineUsersMap[email] = true;
    });

    dispatch(setInitialOnlineUsers(onlineUsersMap)); // Store in Redux


    

  } catch (error: any) {
    dispatch({
      type: LOGIN_FAILURE,
      payload: error.response?.data?.message || 'Login failed',
    });
    toast.error('Invalid Credentials / Account does not exist');
    throw error;
  }
};

export const registerUser = (credentials: Credentials) => async (dispatch: AppDispatch) => {
  dispatch({ type: SIGNUP_REQUEST });
  try {
    const res=await axios.post(buildApiUrl(API_CONFIG.ENDPOINTS.AUTH.REGISTER), credentials);

    if(res.data?.status === "error"){
      toast.error(res.data.message)
      return
    }

    toast(res.data?.message);
    

    dispatch({
      type: SIGNUP_SUCCESS,
      payload: { message: 'Registration successful' },
    });
  } catch (error: any) {
    dispatch({
      type: SIGNUP_FAILURE,
      payload: error.response?.data?.message || 'Registration failed',
    });
    toast.error('Registration failed. Please try again.');
    throw error;
  }
};

export const logoutUser = () => async (dispatch: AppDispatch) => {
  console.log("Logout function called!");

  const token = localStorage.getItem('authToken');

  stompService.disconnect();

  try {
    if (token) {
      const res = await axios.post(
        buildApiUrl(API_CONFIG.ENDPOINTS.AUTH.LOGOUT),
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );
      toast.success(res.data);
    }
  } catch (error) {
    console.error("Logout failed:", error);
    toast.error("Logout failed!");
  }

  await persistor.purge();
  localStorage.removeItem('authToken');
  localStorage.removeItem('userDTO');
  dispatch({ type: LOGOUT });
};

