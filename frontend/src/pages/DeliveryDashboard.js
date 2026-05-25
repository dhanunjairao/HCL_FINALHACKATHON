import React, { useEffect, useState } from 'react';
import api from '../services/api';

export default function DeliveryDashboard() {
  const [tab, setTab] = useState('available');
  const [available, setAvailable] = useState([]);
  const [mine, setMine] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadAvailable = () => api.get('/orders/available-deliveries').then((r) => setAvailable(r.data));
  const loadMine = () => api.get('/orders/my-deliveries').then((r) => setMine(r.data));

  const refresh = () => Promise.all([loadAvailable(), loadMine()]);

  useEffect(() => {
    refresh().finally(() => setLoading(false));
  }, []);

  const claim = async (id) => {
    if (!window.confirm('Claim this delivery? It will be assigned to you.')) return;
    try {
      await api.post(`/orders/${id}/claim-delivery`);
      await refresh();
      setTab('active');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to claim — another driver may have got it first.');
      await refresh();
    }
  };

  const markDelivered = async (id) => {
    if (!window.confirm('Mark this order as DELIVERED?')) return;
    try {
      await api.post(`/orders/${id}/mark-delivered`);
      await refresh();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  if (loading) return <div className="loading">Loading dashboard...</div>;

  const active = mine.filter((o) => o.status === 'OUT_FOR_DELIVERY');
  const history = mine.filter((o) => o.status !== 'OUT_FOR_DELIVERY');

  return (
    <div className="container">
      <h2 className="section-title">Delivery Dashboard</h2>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginBottom: '20px' }}>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Available Pickups</p>
          <h2 style={{ color: available.length > 0 ? '#ff5722' : '#888' }}>{available.length}</h2>
        </div>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Active Deliveries</p>
          <h2 style={{ color: '#ff9800' }}>{active.length}</h2>
        </div>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Completed</p>
          <h2 style={{ color: '#4caf50' }}>{history.filter((o) => o.status === 'DELIVERED').length}</h2>
        </div>
      </div>

      <div className="tabs">
        <button className={`tab ${tab === 'available' ? 'active' : ''}`} onClick={() => setTab('available')}>
          Available ({available.length})
        </button>
        <button className={`tab ${tab === 'active' ? 'active' : ''}`} onClick={() => setTab('active')}>
          My Active Deliveries ({active.length})
        </button>
        <button className={`tab ${tab === 'history' ? 'active' : ''}`} onClick={() => setTab('history')}>
          History ({history.length})
        </button>
        <button className="btn btn-secondary btn-sm" style={{ marginLeft: 'auto' }} onClick={refresh}>
          🔄 Refresh
        </button>
      </div>

      {tab === 'available' && (
        <>
          {available.length === 0 ? (
            <div className="empty-state">
              No orders ready for pickup right now.
              <p style={{ fontSize: '0.9rem', marginTop: '10px', color: '#888' }}>
                Orders show up here once a restaurant marks them as READY.
              </p>
            </div>
          ) : (
            available.map((order) => (
              <div key={order.id} className="cart-summary" style={{ marginBottom: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <div>
                    <h3>Order #{order.id} — {order.restaurantName}</h3>
                    <p style={{ color: '#888', fontSize: '0.9rem' }}>
                      Placed {new Date(order.orderDate).toLocaleString()}
                    </p>
                  </div>
                  <span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span>
                </div>

                <div style={{ fontSize: '0.9rem', marginBottom: '10px' }}>
                  <p>🍽 {order.items.length} item{order.items.length !== 1 ? 's' : ''} — ₹{order.totalAmount}</p>
                  <p>👤 Customer: {order.customerName || order.username}</p>
                  {order.deliveryAddress && <p>📍 {order.deliveryAddress}</p>}
                  {order.phone && <p>📞 {order.phone}</p>}
                  <p>💰 Payment: {order.paymentMethod === 'CASH_ON_DELIVERY' ? 'Cash on Delivery' : order.paymentMethod}</p>
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button className="btn btn-success" onClick={() => claim(order.id)}>
                    🛵 Accept & Pick Up
                  </button>
                </div>
              </div>
            ))
          )}
        </>
      )}

      {tab === 'active' && (
        <>
          {active.length === 0 ? (
            <div className="empty-state">No active deliveries. Claim one from the Available tab.</div>
          ) : (
            active.map((order) => (
              <div key={order.id} className="cart-summary" style={{ marginBottom: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <div>
                    <h3>Order #{order.id} — {order.restaurantName}</h3>
                  </div>
                  <span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span>
                </div>

                <div style={{ fontSize: '0.9rem', marginBottom: '10px' }}>
                  <p>👤 Customer: {order.customerName || order.username}</p>
                  {order.deliveryAddress && <p>📍 <strong>{order.deliveryAddress}</strong></p>}
                  {order.phone && <p>📞 <a href={`tel:${order.phone}`}>{order.phone}</a></p>}
                  {order.notes && <p>📝 {order.notes}</p>}
                  <p>💰 Payment: {order.paymentMethod === 'CASH_ON_DELIVERY' ? `Collect ₹${order.totalAmount} cash` : order.paymentMethod}</p>
                </div>

                <table style={{ marginBottom: '10px' }}>
                  <thead><tr><th>Item</th><th>Qty</th></tr></thead>
                  <tbody>
                    {order.items.map((item) => (
                      <tr key={item.id}>
                        <td>{item.menuItemName}</td>
                        <td>{item.quantity}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button className="btn btn-success" onClick={() => markDelivered(order.id)}>
                    ✅ Mark Delivered
                  </button>
                </div>
              </div>
            ))
          )}
        </>
      )}

      {tab === 'history' && (
        <>
          {history.length === 0 ? (
            <div className="empty-state">No delivery history yet.</div>
          ) : (
            <table>
              <thead>
                <tr><th>#</th><th>Restaurant</th><th>Customer</th><th>Total</th><th>Status</th><th>Date</th></tr>
              </thead>
              <tbody>
                {history.map((order) => (
                  <tr key={order.id}>
                    <td>{order.id}</td>
                    <td>{order.restaurantName}</td>
                    <td>{order.customerName || order.username}</td>
                    <td>₹{order.totalAmount}</td>
                    <td><span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span></td>
                    <td style={{ fontSize: '0.85rem' }}>{new Date(order.updatedAt || order.orderDate).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  );
}
