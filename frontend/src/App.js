import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Restaurants from './pages/Restaurants';
import Menu from './pages/Menu';
import Cart from './pages/Cart';
import MyOrders from './pages/MyOrders';
import ManagerDashboard from './pages/ManagerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import DeliveryDashboard from './pages/DeliveryDashboard';

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Restaurants />;
  if (user.role === 'ADMIN') return <Navigate to="/admin" replace />;
  if (user.role === 'MANAGER') return <Navigate to="/manager" replace />;
  if (user.role === 'DELIVERY') return <Navigate to="/delivery" replace />;
  return <Restaurants />;
}

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <Navbar />
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<HomeRedirect />} />
            <Route path="/restaurants/:id/menu" element={
              <ProtectedRoute roles={['USER']}><Menu /></ProtectedRoute>
            } />
            <Route path="/cart" element={
              <ProtectedRoute roles={['USER']}><Cart /></ProtectedRoute>
            } />
            <Route path="/my-orders" element={
              <ProtectedRoute roles={['USER']}><MyOrders /></ProtectedRoute>
            } />
            <Route path="/manager" element={
              <ProtectedRoute roles={['MANAGER']}><ManagerDashboard /></ProtectedRoute>
            } />
            <Route path="/admin" element={
              <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
            } />
            <Route path="/delivery" element={
              <ProtectedRoute roles={['DELIVERY']}><DeliveryDashboard /></ProtectedRoute>
            } />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}
