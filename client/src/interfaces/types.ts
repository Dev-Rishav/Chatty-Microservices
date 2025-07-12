export interface ReceiverObj {
    receiverProfileImg?: string;
    receiverUsername: string;
    receiverId:string
  }
  

export interface Message {
  id: number;
  from: string;
  to:string,
  content: string;
  timestamp: any; 
  fileUrl?: string;
  reactions?: Record<string, number>;
  encrypted?: boolean;
}

export interface Chat {
  id: number | null; // Allow null for contacts without message history
  email:string
  username: string;
  lastMessage: string;
  timestamp: string;
  unread?: number;
  isGroup?: boolean;
  online?: boolean;
  profilePic?: string;
}

export interface UserDTO{
  user_id:number;
  email:string;
  username:string
  profilePic?:string
}

export interface Notification {
  id: string;
  message: string;
  isRead: boolean;
  createdAt: any;
  type?: 'contact_request' | 'general' | 'message';
  requestId?: string;
  senderEmail?: string;
  senderUsername?: string;
}



export interface ContactRequestDTO{
  senderEmail:string
  receiverEmail:String
}