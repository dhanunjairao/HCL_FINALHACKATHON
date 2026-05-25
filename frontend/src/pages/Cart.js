import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export default function Cart() {
  const {
    items, restaurantId, restaurantName, total,
    updateQuantity, removeFromCart, clearCart, refresh
  } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    deliveryAddress: '',
    phone: '',
    notes: ''
  });

  const handlePlaceOrder = async () => {
    setError('');
    setPlacing(true);
    try {
      // Server reads items from the persistent cart, so we only send delivery info
      const payload = {
        deliveryAddress: form.deliveryAddress,
        phone: form.phone,
        notes: form.notes
      };
      await api.post('/orders', payload);
      await refresh();
      alert('Order placed successfully!');
      navigate('/my-orders');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order');
    } finally {
      setPlacing(false);
    }
  };

  if (items.length === 0) {
    return (
      <div className="container">
        <h2 className="section-title">Your Cart</h2>
        <div className="empty-state">
          Your cart is empty<br />
          <button className="btn btn-primary" style={{ marginTop: '15px' }} onClick={() => navigate('/')}>
            Browse Restaurants
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h2 className="section-title">Your Cart</h2>
      <p style={{ marginBottom: '15px', color: '#666' }}>
        Ordering from <strong>{restaurantName}</strong>
      </p>

      {items.map((item) => (
        <div key={item.id} className="cart-item">
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            {item.menuItemImageUrl && (
              <img src={item.menuItemImageUrl} alt={item.menuItemName}
                style={{ width: '60px', height: '60px', objectFit: 'cover', borderRadius: '4px' }} />
            )}
            <div>
              <strong>{item.menuItemName}</strong>
              <p style={{ color: '#888', fontSize: '0.9rem' }}>Rs. {item.price} each</p>
            </div>
          </div>
          <div className="cart-controls">
            <button className="qty-btn" onClick={() => updateQuantity(item.id, item.quantity - 1)}>-</button>
            <span>{item.quantity}</span>
            <button className="qty-btn" onClick={() => updateQuantity(item.id, item.quantity + 1)}>+</button>
            <span style={{ minWidth: '90px', textAlign: 'right' }}>
              <strong>Rs. {parseFloat(item.subtotal).toFixed(2)}</strong>
            </span>
            <button className="btn btn-danger btn-sm" onClick={() => removeFromCart(item.id)}>Remove</button>
          </div>
        </div>
      ))}

      <div className="cart-summary">
        <h3 style={{ marginBottom: '15px' }}>Delivery Details</h3>
        <div className="form-group">
          <label>Delivery Address</label>
          <textarea rows="2" placeholder={user?.address || 'Enter delivery address'}
            value={form.deliveryAddress}
            onChange={(e) => setForm({ ...form, deliveryAddress: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Phone</label>
          <input placeholder="Contact number" value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Notes (optional)</label>
          <textarea rows="2" placeholder="Any special instructions"
            value={form.notes}
            onChange={(e) => setForm({ ...form, notes: e.target.value })} />
        </div>

        <hr style={{ margin: '20px 0', border: 'none', borderTop: '2px dashed #eee' }} />
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.3rem', marginBottom: '15px' }}>
          <strong>Total</strong>
          <strong style={{ color: '#ff5722' }}>Rs. {total.toFixed(2)}</strong>
        </div>

        <button className="btn btn-success btn-block" disabled={placing} onClick={handlePlaceOrder}>
          {placing ? 'Placing order...' : 'Place Order'}
        </button>
        <button className="btn btn-secondary btn-block" style={{ marginTop: '10px' }} onClick={clearCart}>
          Clear Cart
        </button>
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
}
