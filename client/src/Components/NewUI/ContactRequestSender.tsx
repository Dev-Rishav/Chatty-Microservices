import React, { useState } from 'react';
import { useAppSelector } from '../../redux/hooks';
import { RootState } from '../../redux/store';
import notificationStompService from '../../services/notificationStompService';
import toast from 'react-hot-toast';
import { ContactRequestDTO } from '../../interfaces/types';

const ContactRequestSender: React.FC = () => {
  const [recipientEmail, setRecipientEmail] = useState('');
  const { userDTO } = useAppSelector((state: RootState) => state.auth);

  const handleSendRequest = () => {
    if (!recipientEmail.trim()) {
      toast.error('Please enter a recipient email');
      return;
    }

    if (!userDTO?.email) {
      toast.error('User not authenticated');
      return;
    }

    if (recipientEmail === userDTO.email) {
      toast.error('Cannot send contact request to yourself');
      return;
    }

    const contactRequest: ContactRequestDTO = {
      senderEmail: userDTO.email,
      receiverEmail: recipientEmail,
    };

    notificationStompService.sendContactRequest(contactRequest);
    toast.success(`Contact request sent to ${recipientEmail}! 📤`);
    setRecipientEmail('');
  };

  return (
    <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 shadow-paper">
      <h3 className="font-playfair text-lg text-amber-900 mb-3">
        Send Contact Request
      </h3>
      <div className="flex space-x-2">
        <input
          type="email"
          value={recipientEmail}
          onChange={(e) => setRecipientEmail(e.target.value)}
          placeholder="Enter recipient's email"
          className="flex-1 px-3 py-2 border border-amber-300 rounded-md focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-transparent"
        />
        <button
          onClick={handleSendRequest}
          className="bg-amber-600 hover:bg-amber-700 text-white px-4 py-2 rounded-md font-medium transition-colors"
        >
          Send Request
        </button>
      </div>
    </div>
  );
};

export default ContactRequestSender;
