import React from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '../../redux/store';

const ChatDebugger: React.FC = () => {
  const { list: chats, loading, error } = useSelector((state: RootState) => state.chats);
  
  if (process.env.NODE_ENV === 'production') {
    return null; // Don't render in production
  }
  
  return (
    <div className="fixed bottom-4 right-4 bg-black text-white p-4 rounded-lg text-xs max-w-md z-50">
      <div className="font-bold mb-2">Chat Debug Info</div>
      <div>Chat Count: {chats.length}</div>
      <div>Loading: {loading ? 'Yes' : 'No'}</div>
      <div>Error: {error || 'None'}</div>
      <div className="mt-2 max-h-32 overflow-y-auto">
        <div className="font-semibold">Recent Chats:</div>
        {chats.slice(0, 3).map(chat => (
          <div key={chat.email} className="text-xs">
            {chat.username} - {chat.lastMessage || 'No messages'} 
            {chat.unread && chat.unread > 0 && ` (${chat.unread} unread)`}
          </div>
        ))}
      </div>
    </div>
  );
};

export default ChatDebugger;
