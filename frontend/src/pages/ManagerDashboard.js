import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

// Manager controls these. OUT_FOR_DELIVERY and DELIVERED are owned by the delivery person.
const MANAGER_STATUS_TRANSITIONS = ['ACCEPTED', 'PREPARING', 'READY'];

export default function ManagerDashboard() {
  const { user } = useAuth();
  const [tab, setTab] = useState('orders');
  const [orders, setOrders] = useState([]);
  const [menuItems, setMenuItems] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({
    name: '', description: '', price: '', category: '', imageUrl: '', available: true
  });
  const [reportForm, setReportForm] = useState({ reportedUserId: '', reportedUsername: '', relatedOrderId: '', reason: '' });

  const loadOrders = () => api.get('/orders/restaurant').then((r) => setOrders(r.data));
  const loadMenu = () => api.get(`/menu-items/restaurant/${user.restaurantId}`).then((r) => setMenuItems(r.data));
  const loadReports = () => api.get('/fraud-reports/mine').then((r) => setReports(r.data));

  useEffect(() => {
    Promise.all([loadOrders(), loadMenu(), loadReports()]).finally(() => setLoading(false));
  }, []);

  const updateStatus = async (id, status) => {
    try {
      await api.put(`/orders/${id}/status`, { status });
      loadOrders();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const openCreate = () => {
    setForm({ name: '', description: '', price: '', category: '', imageUrl: '', available: true });
    setModal({ mode: 'create' });
  };

  const openEdit = (item) => {
    setForm({
      name: item.name, description: item.description || '', price: item.price,
      category: item.category || '', imageUrl: item.imageUrl || '', available: item.available
    });
    setModal({ mode: 'edit', id: item.id });
  };

  const saveItem = async () => {
    try {
      const payload = { ...form, price: parseFloat(form.price) };
      if (modal.mode === 'create') {
        await api.post(`/menu-items/restaurant/${user.restaurantId}`, payload);
      } else {
        await api.put(`/menu-items/${modal.id}`, payload);
      }
      setModal(null);
      loadMenu();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const deleteItem = async (id) => {
    if (!window.confirm('Delete this menu item?')) return;
    try {
      await api.delete(`/menu-items/${id}`);
      loadMenu();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const openReportFromOrder = (order) => {
    setReportForm({
      reportedUserId: order.userId,
      reportedUsername: order.username,
      relatedOrderId: order.id,
      reason: ''
    });
    setModal({ mode: 'report' });
  };

  const submitReport = async () => {
    if (!reportForm.reason.trim()) {
      alert('Please describe the reason for the report.');
      return;
    }
    try {
      await api.post('/fraud-reports', {
        reportedUserId: reportForm.reportedUserId,
        relatedOrderId: reportForm.relatedOrderId || null,
        reason: reportForm.reason.trim()
      });
      setModal(null);
      loadReports();
      alert('Fraud report filed. An admin will review it.');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to file report');
    }
  };

  if (loading) return <div className="loading">Loading dashboard...</div>;

  const openReportsCount = reports.filter((r) => r.status === 'OPEN').length;

  return (
    <div className="container">
      <h2 className="section-title">Manager Dashboard</h2>
      <div className="tabs">
        <button className={`tab ${tab === 'orders' ? 'active' : ''}`} onClick={() => setTab('orders')}>
          Orders ({orders.filter((o) => o.status === 'PENDING').length} pending)
        </button>
        <button className={`tab ${tab === 'menu' ? 'active' : ''}`} onClick={() => setTab('menu')}>
          Menu Management ({menuItems.length})
        </button>
        <button className={`tab ${tab === 'reports' ? 'active' : ''}`} onClick={() => setTab('reports')}>
          My Fraud Reports ({reports.length}{openReportsCount > 0 ? `, ${openReportsCount} open` : ''})
        </button>
      </div>

      {tab === 'orders' && (
        <>
          {orders.length === 0 ? (
            <div className="empty-state">No orders yet</div>
          ) : (
            orders.map((order) => (
              <div key={order.id} className="cart-summary" style={{ marginBottom: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <div>
                    <h3>Order #{order.id} - {order.customerName || order.username}</h3>
                    <p style={{ color: '#888', fontSize: '0.9rem' }}>{new Date(order.orderDate).toLocaleString()}</p>
                  </div>
                  <span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span>
                </div>

                <table style={{ marginBottom: '10px' }}>
                  <thead><tr><th>Item</th><th>Qty</th><th>Subtotal</th></tr></thead>
                  <tbody>
                    {order.items.map((item) => (
                      <tr key={item.id}>
                        <td>{item.menuItemName}</td>
                        <td>{item.quantity}</td>
                        <td>₹{item.subtotal}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {order.deliveryAddress && <p style={{ fontSize: '0.9rem' }}>📍 {order.deliveryAddress}</p>}
                {order.phone && <p style={{ fontSize: '0.9rem' }}>📞 {order.phone}</p>}
                {order.notes && <p style={{ fontSize: '0.9rem', color: '#888' }}>📝 {order.notes}</p>}

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '10px' }}>
                  <strong style={{ color: '#ff5722', fontSize: '1.1rem' }}>Total: ₹{order.totalAmount}</strong>
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    {order.status === 'PENDING' && (
                      <>
                        <button className="btn btn-success btn-sm" onClick={() => updateStatus(order.id, 'ACCEPTED')}>Accept</button>
                        <button className="btn btn-danger btn-sm" onClick={() => updateStatus(order.id, 'REJECTED')}>Reject</button>
                      </>
                    )}
                    {MANAGER_STATUS_TRANSITIONS.includes(order.status) && (
                      <select
                        value={order.status}
                        onChange={(e) => updateStatus(order.id, e.target.value)}
                        style={{ padding: '6px', borderRadius: '4px', border: '1px solid #ddd' }}
                        title="Once you mark READY, a delivery person can pick it up"
                      >
                        {MANAGER_STATUS_TRANSITIONS.map((s) => (
                          <option key={s} value={s}>{s}</option>
                        ))}
                      </select>
                    )}
                    {order.status === 'OUT_FOR_DELIVERY' && order.deliveryPersonName && (
                      <span style={{ fontSize: '0.85rem', color: '#666' }}>
                        🛵 {order.deliveryPersonName}
                      </span>
                    )}
                    <button
                      className="btn btn-danger btn-sm"
                      title="Report this customer for fraudulent or fake order"
                      onClick={() => openReportFromOrder(order)}
                    >
                      🚩 Report Fraud
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </>
      )}

      {tab === 'menu' && (
        <>
          <button className="btn btn-primary" style={{ marginBottom: '15px' }} onClick={openCreate}>
            + Add Menu Item
          </button>
          {menuItems.length === 0 ? (
            <div className="empty-state">No menu items yet</div>
          ) : (
            <table>
              <thead>
                <tr><th>Name</th><th>Category</th><th>Price</th><th>Available</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {menuItems.map((item) => (
                  <tr key={item.id}>
                    <td>{item.name}</td>
                    <td>{item.category}</td>
                    <td>₹{item.price}</td>
                    <td>{item.available ? '✅' : '❌'}</td>
                    <td>
                      <button className="btn btn-primary btn-sm" onClick={() => openEdit(item)}>Edit</button>{' '}
                      <button className="btn btn-danger btn-sm" onClick={() => deleteItem(item.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {tab === 'reports' && (
        <>
          {reports.length === 0 ? (
            <div className="empty-state">
              You have not filed any fraud reports.
              <p style={{ fontSize: '0.9rem', marginTop: '10px', color: '#888' }}>
                Use the 🚩 Report Fraud button on any order to flag suspicious customers for admin review.
              </p>
            </div>
          ) : (
            <table>
              <thead>
                <tr><th>#</th><th>Reported User</th><th>Order</th><th>Reason</th><th>Status</th><th>Admin Notes</th><th>Filed</th><th>Resolved</th></tr>
              </thead>
              <tbody>
                {reports.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.reportedUsername}</td>
                    <td>{r.relatedOrderId ? `#${r.relatedOrderId}` : '—'}</td>
                    <td style={{ maxWidth: '250px' }}>{r.reason}</td>
                    <td><span className={`status status-${r.status.toLowerCase()}`}>{r.status}</span></td>
                    <td style={{ maxWidth: '200px', fontSize: '0.85rem', color: '#666' }}>{r.adminNotes || '—'}</td>
                    <td style={{ fontSize: '0.85rem' }}>{new Date(r.createdAt).toLocaleString()}</td>
                    <td style={{ fontSize: '0.85rem' }}>{r.resolvedAt ? new Date(r.resolvedAt).toLocaleString() : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {modal && modal.mode !== 'report' && (
        <div className="modal-backdrop" onClick={() => setModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>{modal.mode === 'create' ? 'Add Menu Item' : 'Edit Menu Item'}</h2>
            <div className="form-group">
              <label>Name *</label>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea rows="2" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Price *</label>
              <input type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Category</label>
              <input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} placeholder="e.g. Pizza, Beverages" />
            </div>
            <div className="form-group">
              <label>Image URL</label>
              <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} />
            </div>
            <div className="form-group">
              <label>
                <input type="checkbox" checked={form.available}
                  onChange={(e) => setForm({ ...form, available: e.target.checked })} /> Available
              </label>
            </div>
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-success" onClick={saveItem}>Save</button>
            </div>
          </div>
        </div>
      )}

      {modal && modal.mode === 'report' && (
        <div className="modal-backdrop" onClick={() => setModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>🚩 Report Fraud</h2>
            <p style={{ color: '#666', marginBottom: '15px' }}>
              Reporting <strong>{reportForm.reportedUsername}</strong>
              {reportForm.relatedOrderId && <> for order <strong>#{reportForm.relatedOrderId}</strong></>}.
              The report goes to an admin for review.
            </p>
            <div className="form-group">
              <label>Reason *</label>
              <textarea
                rows="4"
                value={reportForm.reason}
                onChange={(e) => setReportForm({ ...reportForm, reason: e.target.value })}
                placeholder="e.g. Repeated fake orders, abusive behavior, payment fraud..."
                maxLength={1000}
              />
              <small style={{ color: '#888' }}>{reportForm.reason.length} / 1000</small>
            </div>
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-danger" onClick={submitReport}>Submit Report</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
