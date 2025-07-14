import { BellIcon, UserCircleIcon } from "@heroicons/react/24/outline";
import { useAppDispatch, useAppSelector } from "../../redux/hooks";
import { RootState } from "../../redux/store";
import { useEffect, useRef, useState } from "react";
import toast from "react-hot-toast";
import {
  addNotification,
  fetchNotificationHistory,
  acceptContactRequest,
  rejectContactRequest,
} from "../../redux/actions/notificationActions";
import notificationStompService from "../../services/notificationStompService";
import { logoutUser } from "../../redux/actions/authActions";

const Navbar: React.FC = () => {
  const { userDTO, token } = useAppSelector((state: RootState) => state.auth);
  const [showNotifications, setShowNotifications] = useState(false);
  const notificationRef = useRef<HTMLDivElement>(null);
  const dispatch = useAppDispatch();
  const notificationArray = useAppSelector(
    (state: RootState) => state.notifications.list
  );
  const notificationCount = notificationArray.length;

  useEffect(() => {
    dispatch(fetchNotificationHistory());
  }, [dispatch]);

  const handleAcceptRequest = (requestId: string, notificationId: string, event?: React.MouseEvent) => {
    event?.preventDefault();
    event?.stopPropagation();
    console.log("🤝 Accepting contact request:", { requestId, notificationId });
    dispatch(acceptContactRequest(requestId, notificationId));
    toast.success("Contact request accepted! 🤝");
    // Remove the page reload - let the notification system handle updates
  };

  const handleRejectRequest = (requestId: string, notificationId: string, event?: React.MouseEvent) => {
    event?.preventDefault();
    event?.stopPropagation();
    console.log("❌ Rejecting contact request:", { requestId, notificationId });
    dispatch(rejectContactRequest(requestId, notificationId));
    toast.success("Contact request rejected");
  };

  const isContactRequest = (message: string) => {
    return message.includes("contact request") && 
           !message.includes("accepted") && 
           !message.includes("declined") &&
           message.includes("received");
  };

  // Extract request ID from notification message for contact requests
  const extractRequestInfo = (notification: any) => {
    return {
      requestId: notification.requestId?.toString() || notification.id, // Use the actual contact request ID
      senderEmail: notification.senderEmail || '',
      senderUsername: notification.senderUsername || 'Someone'
    };
  };

  //handle logout
  const handleLogout = () => {
    dispatch(logoutUser());
    // localStorage.removeItem("userDTO");
    // localStorage.removeItem("authToken");
    // toast.success("Logged out successfully");
  };

  // Close notifications when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target as Node)
      ) {
        setShowNotifications(false);
      }
    };

    if (showNotifications) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [showNotifications]);

  return (
    <nav className="bg-gradient-to-b from-amber-50 to-transparent px-6 py-4 backdrop-blur-sm relative z-40">
      <div className="flex justify-between items-center max-w-7xl mx-auto">
        {/* Logo */}
        <div className="flex items-center space-x-3">
          <span className="text-2xl font-playfair text-amber-900">Chatty</span>
          <span className="text-amber-700 text-xl">✉️</span>
        </div>

        {/* Right Section */}
        <div className="flex items-center space-x-4">
          {/* Notifications */}
          <div className="relative" ref={notificationRef}>
            <button
              type="button"
              className="p-2 hover:bg-amber-100/50 rounded-full transition-colors relative"
              onClick={() => setShowNotifications(!showNotifications)}
            >
              <BellIcon className="w-6 h-6 text-amber-700" />
              {notificationCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-amber-600 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center">
                  {notificationCount}
                </span>
              )}
            </button>

            {showNotifications && (
              <div className="absolute right-0 mt-2 w-96 bg-amber-50 border border-amber-200 rounded-lg shadow-paper z-50">
                <div className="p-3 font-playfair text-lg text-amber-900 border-b border-amber-200">
                  Notifications
                </div>
                <div className="max-h-96 overflow-y-auto">
                  {notificationArray.length > 0 ? (
                    notificationArray.map((notification) => {
                      const requestInfo = extractRequestInfo(notification);
                      const isContactReq = isContactRequest(notification.message);
                      
                      // Debug logging
                      if (isContactReq) {
                        console.log("🔍 Contact request notification:", {
                          notification,
                          requestInfo,
                          isContactReq
                        });
                      }
                      
                      return (
                        <div
                          key={notification.id}
                          className={`p-4 text-base font-crimson transition-colors ${
                            !notification.isRead
                              ? "bg-amber-100/50"
                              : "bg-transparent"
                          } hover:bg-amber-100 border-b border-amber-100 last:border-b-0`}
                        >
                          <div className="flex items-start space-x-3">
                            {!notification.isRead && (
                              <div className="w-3 h-3 bg-amber-600 rounded-full flex-shrink-0 mt-1" />
                            )}
                            <div className="flex-1">
                              <span
                                className={
                                  !notification.isRead
                                    ? "text-amber-900"
                                    : "text-amber-700/80"
                                }
                              >
                                {notification.message}
                              </span>
                              
                              {/* Contact Request Action Buttons */}
                              {isContactReq && (
                                <div className="flex space-x-2 mt-3">
                                  <button
                                    type="button"
                                    onClick={(e) => handleAcceptRequest(requestInfo.requestId, notification.id, e)}
                                    className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
                                  >
                                    Accept
                                  </button>
                                  <button
                                    type="button"
                                    onClick={(e) => handleRejectRequest(requestInfo.requestId, notification.id, e)}
                                    className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
                                  >
                                    Reject
                                  </button>
                                </div>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="p-4 text-center text-amber-700/80 text-base font-crimson">
                      No new messages
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* User Profile + Logout */}
          <div className="flex items-center space-x-3">
            <button 
              type="button"
              className="hover:bg-amber-100/50 rounded-full transition-colors"
            >
              {userDTO?.profilePic ? (
                <img
                  src={userDTO.profilePic}
                  alt="Profile"
                  className="w-9 h-9 rounded-full border-2 border-amber-200"
                />
              ) : (
                <div className="w-9 h-9 rounded-full bg-amber-100 border-2 border-amber-200 flex items-center justify-center">
                  <UserCircleIcon className="w-6 h-6 text-amber-700" />
                </div>
              )}
            </button>
            <span className="font-crimson text-amber-900 text-base hidden md:block">
              {userDTO?.username}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              className="ml-2 bg-amber-100 hover:bg-amber-200 text-amber-900 text-sm px-3 py-1 rounded-lg font-semibold font-crimson transition"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
