import React, { useEffect, useState } from 'react';
import api from '../services/api';

export default function AdminDashboard() {
  const [tab, setTab] = useState('restaurants');
  const [restaurants, setRestaurants] = useState([]);
  const [pending, setPending] = useState([]);
  const [orders, setOrders] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({
    name: '', description: '', cuisineType: '', address: '', phone: '', imageUrl: ''
  });
  const [resolveForm, setResolveForm] = useState({ id: null, status: 'RESOLVED', adminNotes: '' });

  const loadRestaurants = () => api.get('/restaurants/all').then((r) => setRestaurants(r.data));
  const loadPending = () => api.get('/restaurants/pending').then((r) => setPending(r.data));
  const loadOrders = () => api.get('/orders/all').then((r) => setOrders(r.data));
  const loadReports = () => api.get('/fraud-reports').then((r) => setReports(r.data));

  useEffect(() => {
    Promise.all([loadRestaurants(), loadPending(), loadOrders(), loadReports()]).finally(() => setLoading(false));
  }, []);

  const openEdit = (r) => {
    setForm({
      name: r.name, description: r.description || '', cuisineType: r.cuisineType || '',
      address: r.address || '', phone: r.phone || '', imageUrl: r.imageUrl || ''
    });
    setModal({ mode: 'edit', id: r.id });
  };

  const saveRestaurant = async () => {
    try {
      await api.put(`/restaurants/${modal.id}`, form);
      setModal(null);
      loadRestaurants();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const deleteRestaurant = async (id) => {
    if (!window.confirm('Delete this restaurant? This will also delete its menu.')) return;
    try {
      await api.delete(`/restaurants/${id}`);
      loadRestaurants();
      loadPending();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const approve = async (id) => {
    if (!window.confirm('Approve this restaurant? The manager will be able to log in.')) return;
    try {
      await api.put(`/restaurants/${id}/verify`, { status: 'APPROVED' });
      loadRestaurants();
      loadPending();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const reject = async (id) => {
    const reason = window.prompt('Reason for rejection?');
    if (reason === null) return;
    try {
      await api.put(`/restaurants/${id}/verify`, { status: 'REJECTED', rejectionReason: reason });
      loadRestaurants();
      loadPending();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const openResolveReport = (report, status) => {
    setResolveForm({ id: report.id, status, adminNotes: '' });
    setModal({ mode: 'resolveReport' });
  };

  const submitResolveReport = async () => {
    try {
      await api.put(`/fraud-reports/${resolveForm.id}/status`, {
        status: resolveForm.status,
        adminNotes: resolveForm.adminNotes || null
      });
      setModal(null);
      loadReports();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const deleteReport = async (id) => {
    if (!window.confirm('Delete this fraud report? This cannot be undone.')) return;
    try {
      await api.delete(`/fraud-reports/${id}`);
      loadReports();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  const disableReportedUser = async (userId) => {
    if (!window.confirm('Disable this user account? They will no longer be able to log in.')) return;
    try {
      await api.put(`/users/${userId}/status`, { enabled: false });
      alert('User disabled.');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed');
    }
  };

  if (loading) return <div className="loading">Loading dashboard...</div>;

  const totalRevenue = orders
    .filter((o) => o.status === 'DELIVERED')
    .reduce((s, o) => s + parseFloat(o.totalAmount), 0);
  const pendingOrdersCount = orders.filter((o) => o.status === 'PENDING').length;
  const openReportsCount = reports.filter((r) => r.status === 'OPEN').length;

  return (
    <div className="container">
      <h2 className="section-title">Admin Dashboard</h2>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginBottom: '20px' }}>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Total Restaurants</p>
          <h2 style={{ color: '#ff5722' }}>{restaurants.length}</h2>
        </div>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Pending Approvals</p>
          <h2 style={{ color: '#ff9800' }}>{pending.length}</h2>
        </div>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Total Orders</p>
          <h2 style={{ color: '#ff5722' }}>{orders.length}</h2>
        </div>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Pending Orders</p>
          <h2 style={{ color: '#ff5722' }}>{pendingOrdersCount}</h2>
        </div>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Revenue (Delivered)</p>
          <h2 style={{ color: '#4caf50' }}>₹{totalRevenue.toFixed(2)}</h2>
        </div>
        <div className="cart-summary" style={{ marginTop: 0, textAlign: 'center' }}>
          <p style={{ color: '#666' }}>Open Fraud Reports</p>
          <h2 style={{ color: openReportsCount > 0 ? '#d32f2f' : '#888' }}>{openReportsCount}</h2>
        </div>
      </div>

      <div className="tabs">
        <button className={`tab ${tab === 'restaurants' ? 'active' : ''}`} onClick={() => setTab('restaurants')}>
          Restaurants
        </button>
        <button className={`tab ${tab === 'pending' ? 'active' : ''}`} onClick={() => setTab('pending')}>
          Pending Approvals {pending.length > 0 && <span style={{ background: '#ff9800', color: '#fff', borderRadius: '10px', padding: '0 8px', marginLeft: '6px', fontSize: '0.8rem' }}>{pending.length}</span>}
        </button>
        <button className={`tab ${tab === 'orders' ? 'active' : ''}`} onClick={() => setTab('orders')}>
          All Orders
        </button>
        <button className={`tab ${tab === 'reports' ? 'active' : ''}`} onClick={() => setTab('reports')}>
          Fraud Reports {openReportsCount > 0 && <span style={{ background: '#d32f2f', color: '#fff', borderRadius: '10px', padding: '0 8px', marginLeft: '6px', fontSize: '0.8rem' }}>{openReportsCount}</span>}
        </button>
      </div>

      {tab === 'restaurants' && (
        <>
          {restaurants.length === 0 ? (
            <div className="empty-state">No restaurants yet. Restaurants are created when a manager registers and is approved.</div>
          ) : (
            <table>
              <thead>
                <tr><th>Name</th><th>Cuisine</th><th>Address</th><th>Phone</th><th>Status</th><th>Items</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {restaurants.map((r) => (
                  <tr key={r.id}>
                    <td>{r.name}</td>
                    <td>{r.cuisineType}</td>
                    <td>{r.address}</td>
                    <td>{r.phone}</td>
                    <td><span className={`status status-${(r.verificationStatus || '').toLowerCase()}`}>{r.verificationStatus}</span></td>
                    <td>{r.menuItemCount}</td>
                    <td>
                      <button className="btn btn-primary btn-sm" onClick={() => openEdit(r)}>Edit</button>{' '}
                      <button className="btn btn-danger btn-sm" onClick={() => deleteRestaurant(r.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {tab === 'pending' && (
        <>
          {pending.length === 0 ? (
            <div className="empty-state">No restaurants are awaiting approval.</div>
          ) : (
            <table>
              <thead>
                <tr><th>Restaurant</th><th>Proprietor</th><th>Food License</th><th>Address</th><th>Phone</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {pending.map((r) => (
                  <tr key={r.id}>
                    <td><strong>{r.name}</strong><br /><small style={{ color: '#666' }}>{r.cuisineType}</small></td>
                    <td>{r.proprietorName}</td>
                    <td style={{ fontFamily: 'monospace' }}>{r.foodLicense}</td>
                    <td>{r.address}</td>
                    <td>{r.phone}</td>
                    <td>
                      <button className="btn btn-success btn-sm" onClick={() => approve(r.id)}>Approve</button>{' '}
                      <button className="btn btn-danger btn-sm" onClick={() => reject(r.id)}>Reject</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {tab === 'orders' && (
        <>
          {orders.length === 0 ? (
            <div className="empty-state">No orders yet</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>#</th><th>Customer</th><th>Restaurant</th><th>Items</th>
                  <th>Total</th><th>Status</th><th>Date</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td>{order.id}</td>
                    <td>{order.customerName || order.username}</td>
                    <td>{order.restaurantName}</td>
                    <td>{order.items.length}</td>
                    <td>₹{order.totalAmount}</td>
                    <td><span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span></td>
                    <td style={{ fontSize: '0.85rem' }}>{new Date(order.orderDate).toLocaleString()}</td>
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
            <div className="empty-state">No fraud reports filed.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>#</th><th>Reported User</th><th>Filed By</th><th>Restaurant</th>
                  <th>Order</th><th>Reason</th><th>Status</th><th>Admin Notes</th><th>Filed</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {reports.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td><strong>{r.reportedUsername}</strong></td>
                    <td>{r.reportingManagerUsername}</td>
                    <td>{r.restaurantName || '—'}</td>
                    <td>{r.relatedOrderId ? `#${r.relatedOrderId}` : '—'}</td>
                    <td style={{ maxWidth: '250px' }}>{r.reason}</td>
                    <td><span className={`status status-${r.status.toLowerCase()}`}>{r.status}</span></td>
                    <td style={{ maxWidth: '180px', fontSize: '0.85rem', color: '#666' }}>{r.adminNotes || '—'}</td>
                    <td style={{ fontSize: '0.85rem' }}>{new Date(r.createdAt).toLocaleString()}</td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                        {r.status === 'OPEN' && (
                          <>
                            <button className="btn btn-success btn-sm" onClick={() => openResolveReport(r, 'RESOLVED')}>Resolve</button>
                            <button className="btn btn-secondary btn-sm" onClick={() => openResolveReport(r, 'DISMISSED')}>Dismiss</button>
                            <button className="btn btn-danger btn-sm" onClick={() => disableReportedUser(r.reportedUserId)}>Disable User</button>
                          </>
                        )}
                        <button className="btn btn-danger btn-sm" onClick={() => deleteReport(r.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {modal && modal.mode === 'resolveReport' && (
        <div className="modal-backdrop" onClick={() => setModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>{resolveForm.status === 'RESOLVED' ? 'Resolve' : 'Dismiss'} Fraud Report</h2>
            <p style={{ color: '#666', marginBottom: '15px' }}>
              {resolveForm.status === 'RESOLVED'
                ? 'Mark this report as confirmed and resolved. Optionally add notes for the record.'
                : 'Mark this report as dismissed (not actionable). Optionally explain why.'}
            </p>
            <div className="form-group">
              <label>Admin Notes (optional)</label>
              <textarea
                rows="3"
                value={resolveForm.adminNotes}
                onChange={(e) => setResolveForm({ ...resolveForm, adminNotes: e.target.value })}
                maxLength={1000}
              />
            </div>
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button
                className={resolveForm.status === 'RESOLVED' ? 'btn btn-success' : 'btn btn-primary'}
                onClick={submitResolveReport}
              >
                Confirm {resolveForm.status === 'RESOLVED' ? 'Resolve' : 'Dismiss'}
              </button>
            </div>
          </div>
        </div>
      )}

      {modal && modal.mode !== 'resolveReport' && (
        <div className="modal-backdrop" onClick={() => setModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Edit Restaurant</h2>
            <div className="form-group">
              <label>Name *</label>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Cuisine Type</label>
              <input value={form.cuisineType} onChange={(e) => setForm({ ...form, cuisineType: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea rows="2" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Address</label>
              <input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Phone</label>
              <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Image URL</label>
              <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} />
            </div>
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-success" onClick={saveRestaurant}>Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
