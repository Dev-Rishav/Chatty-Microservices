import axios from 'axios';
import { UserDTO } from '../interfaces/types';
import { buildApiUrl, API_CONFIG } from '../config/api';

// Utility function to fetch users by search query
export async function fetchUsersBySearch(query: string, token: string): Promise<UserDTO[]> {
    if (!query || query.trim() === "") {
      return [];
    }
  
    try {
      const response = await axios.get(buildApiUrl(API_CONFIG.ENDPOINTS.USER.SEARCH), {
        params: { query }, // search term as query parameter
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!response.data || !Array.isArray(response.data)) {
        console.error('Unexpected response format:', response.data);
        return [];
      }
      
  
      return response.data as UserDTO[];
    } catch (error) {
      console.error("Failed to fetch users:", error);
      return [];
    }
  }