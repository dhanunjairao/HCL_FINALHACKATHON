import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';

const CartContext = createContext();
export const useCart = () => useContext(CartContext);

export function CartProvider({ children }) {
  const { user } = useAuth();
  const [cart, setCart] = useState({
    id: null,
    items: [],
    restaurantId: null,
    restaurantName: '',
    totalAmount: 0,
    itemCount: 0,
  });
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (!user || user.role !== 'USER') {
      setCart({ id: null, items: [], restaurantId: null, restaurantName: '', totalAmount: 0, itemCount: 0 });
      return;
    }
    setLoading(true);
    try {
      const { data } = await api.get('/cart');
      setCart({
        id: data.id,
        items: data.items || [],
        restaurantId: data.restaurantId,
        restaurantName: data.restaurantName || '',
        totalAmount: data.totalAmount || 0,
        itemCount: data.itemCount || 0,
      });
    } catch (err) {
      console.error('Failed to load cart', err);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const addToCart = async (menuItemId, quantity = 1) => {
    try {
      const { data } = await api.post('/cart/items', { menuItemId, quantity });
      setCart({
        id: data.id,
        items: data.items || [],
        restaurantId: data.restaurantId,
        restaurantName: data.restaurantName || '',
        totalAmount: data.totalAmount || 0,
        itemCount: data.itemCount || 0,
      });
      return { ok: true };
    } catch (err) {
      return { ok: false, message: err.response?.data?.message || 'Failed to add item' };
    }
  };

  const updateQuantity = async (cartItemId, quantity) => {
    try {
      const { data } = await api.put(`/cart/items/${cartItemId}`, { quantity: Math.max(quantity, 1) });
      if (quantity <= 0) {
        await removeFromCart(cartItemId);
        return;
      }
      setCart({
        id: data.id,
        items: data.items || [],
        restaurantId: data.restaurantId,
        restaurantName: data.restaurantName || '',
        totalAmount: data.totalAmount || 0,
        itemCount: data.itemCount || 0,
      });
    } catch (err) {
      console.error(err);
    }
  };

  const removeFromCart = async (cartItemId) => {
    try {
      const { data } = await api.delete(`/cart/items/${cartItemId}`);
      setCart({
        id: data.id,
        items: data.items || [],
        restaurantId: data.restaurantId,
        restaurantName: data.restaurantName || '',
        totalAmount: data.totalAmount || 0,
        itemCount: data.itemCount || 0,
      });
    } catch (err) {
      console.error(err);
    }
  };

  const clearCart = async () => {
    try {
      await api.delete('/cart');
      setCart({ id: null, items: [], restaurantId: null, restaurantName: '', totalAmount: 0, itemCount: 0 });
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <CartContext.Provider
      value={{
        items: cart.items,
        restaurantId: cart.restaurantId,
        restaurantName: cart.restaurantName,
        total: parseFloat(cart.totalAmount) || 0,
        count: cart.itemCount || 0,
        loading,
        addToCart,
        updateQuantity,
        removeFromCart,
        clearCart,
        refresh,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}
