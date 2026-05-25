import React, { useEffect, useState } from 'react';
import api from '../services/api';

export default function MyOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    api.get('/orders/my-orders')
      .then((res) => setOrders(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this order?')) return;
    try {
      await api.post(`/orders/${id}/cancel`);
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel');
    }
  };

  if (loading) return <div className="loading">Loading orders...</div>;

  return (
    <div className="container">
      <h2 className="section-title">My Orders</h2>
      {orders.length === 0 ? (
        <div className="empty-state">You haven't placed any orders yet</div>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="cart-summary" style={{ marginBottom: '15px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
              <div>
                <h3>Order #{order.id}</h3>
                <p style={{ color: '#888', fontSize: '0.9rem' }}>
                  {order.restaurantName} · {new Date(order.orderDate).toLocaleString()}
                </p>
              </div>
              <span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span>
            </div>

            <table style={{ marginBottom: '10px' }}>
              <thead>
                <tr><th>Item</th><th>Qty</th><th>Price</th><th>Subtotal</th></tr>
              </thead>
              <tbody>
                {order.items.map((item) => (
                  <tr key={item.id}>
                    <td>{item.menuItemName}</td>
                    <td>{item.quantity}</td>
                    <td>₹{item.price}</td>
                    <td>₹{item.subtotal}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                {order.deliveryAddress && <p style={{ fontSize: '0.9rem' }}>📍 {order.deliveryAddress}</p>}
                {order.phone && <p style={{ fontSize: '0.9rem' }}>📞 {order.phone}</p>}
                {order.notes && <p style={{ fontSize: '0.9rem', color: '#888' }}>📝 {order.notes}</p>}
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: '#ff5722' }}>
                  Total: ₹{order.totalAmount}
                </div>
                {order.status === 'PENDING' && (
                  <button className="btn btn-danger btn-sm" style={{ marginTop: '8px' }} onClick={() => handleCancel(order.id)}>
                    Cancel Order
                  </button>
                )}
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
