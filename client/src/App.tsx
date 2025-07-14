import React, { useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Main from './Components/Main';
import ChatLayout from './Components/ChatLayout';
import Auth from './Components/Auth';
import { Provider, useSelector } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import {  persistor, RootState } from './redux/store';
import store from './redux/store';
import AuthStatus from './redux/AuthStatus';
import PrivateRoute from './redux/PrivateRoute';
import { Toaster } from 'react-hot-toast';
import Home from './Components/NewUI/Home';
import { useAppSelector } from './redux/hooks';
import hybridChatService from './services/hybridChatService';

const App: React.FC = () => {


  

  return (
    <Provider store={store}>
      <PersistGate loading={null} persistor={persistor}>
        <Router>
          <Toaster />
          <Routes>
            <Route path="/auth" element={<Auth />} />
            <Route path="/login" element={<Auth />} />
            <Route path="/" element={<Auth />} />
            
            {/* Private Route */}
            <Route element={<PrivateRoute />}>
              <Route path="/home" element={<Home />} />
              <Route path="/ChatLayout" element={<ChatLayout />} />
            </Route>
          </Routes>
        </Router>
      </PersistGate>
    </Provider>
  );
};

export default App;
