import React, { useEffect, useState } from 'react';

interface TypingIndicatorProps {
  isTyping: boolean;
  username?: string;
}

const TypingIndicator: React.FC<TypingIndicatorProps> = ({ isTyping, username = "Someone" }) => {
  const [dots, setDots] = useState('');

  useEffect(() => {
    if (!isTyping) {
      setDots('');
      return;
    }

    const interval = setInterval(() => {
      setDots(prev => {
        if (prev === '...') return '';
        return prev + '.';
      });
    }, 500);

    return () => clearInterval(interval);
  }, [isTyping]);

  if (!isTyping) return null;

  return (
    <div className="flex items-center space-x-2 p-2 text-gray-500 text-sm animate-pulse">
      <div className="flex space-x-1">
        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
      </div>
      <span>{username} is typing{dots}</span>
    </div>
  );
};

export default TypingIndicator;
