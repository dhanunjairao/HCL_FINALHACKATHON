import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Restaurants() {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showClosed, setShowClosed] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/restaurants')
      .then((res) => setRestaurants(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const handleClick = (id, openNow) => {
    if (!openNow) { alert('This restaurant is currently closed.'); return; }
    if (!user) { navigate('/login'); return; }
    if (user.role !== 'USER') { alert('Only customers can browse menus to order'); return; }
    navigate(`/restaurants/${id}/menu`);
  };

  // Default view shows only restaurants that are open right now.
  // Customers can opt in to "show closed too" via the checkbox below.
  const filtered = restaurants
    .filter((r) => showClosed || r.openNow)
    .filter((r) =>
      r.name.toLowerCase().includes(search.toLowerCase()) ||
      (r.cuisineType || '').toLowerCase().includes(search.toLowerCase())
    );

  // "09:00:00" → "9:00 AM"
  const formatTime = (t) => {
    if (!t) return null;
    const [h, m] = t.split(':');
    const hh = parseInt(h, 10);
    const period = hh >= 12 ? 'PM' : 'AM';
    const display = hh === 0 ? 12 : hh > 12 ? hh - 12 : hh;
    return `${display}:${m} ${period}`;
  };

  if (loading) return <div className="loading">Loading restaurants...</div>;

  return (
    <div className="container">
      <h2 className="section-title">Restaurants</h2>
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px', flexWrap: 'wrap', marginBottom: '20px' }}>
        <div className="form-group" style={{ flex: '1 1 300px', maxWidth: '400px', margin: 0 }}>
          <input
            placeholder="Search by name or cuisine..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.9rem', color: '#555' }}>
          <input
            type="checkbox"
            checked={showClosed}
            onChange={(e) => setShowClosed(e.target.checked)}
          />
          Show closed restaurants
        </label>
      </div>
      {filtered.length === 0 ? (
        <div className="empty-state">
          {showClosed ? 'No restaurants found' : 'No restaurants are open right now. Toggle "Show closed restaurants" to see all listings.'}
        </div>
      ) : (
        <div className="grid">
          {filtered.map((r) => (
            <div
              key={r.id}
              className="card"
              onClick={() => handleClick(r.id, r.openNow)}
              style={{ position: 'relative', opacity: r.openNow ? 1 : 0.6, cursor: r.openNow ? 'pointer' : 'not-allowed' }}
            >
              <img src={r.imageUrl || 'https://via.placeholder.com/280x180?text=Restaurant'} alt={r.name} />
              <span
                style={{
                  position: 'absolute', top: 10, right: 10,
                  padding: '4px 10px', borderRadius: '12px',
                  fontSize: '0.75rem', fontWeight: 600,
                  background: r.openNow ? '#4caf50' : '#9e9e9e',
                  color: '#fff'
                }}
              >
                {r.openNow ? 'Open now' : 'Closed'}
              </span>
              <div className="card-body">
                <h3>{r.name}</h3>
                <p><strong>{r.cuisineType}</strong></p>
                <p>{r.description}</p>
                <p style={{ fontSize: '0.85rem', color: '#888' }}>📍 {r.address}</p>
                <p style={{
                  fontSize: '0.85rem',
                  color: r.openNow ? '#2e7d32' : '#777',
                  fontWeight: 500,
                  marginTop: '6px'
                }}>
                  🕒 {(() => {
                    const open = formatTime(r.openTime);
                    const close = formatTime(r.closeTime);
                    if (!open && !close) return 'Open 24 hours';
                    if (r.openNow) {
                      return close
                        ? <>Open now — until <strong>{close}</strong> <span style={{ color: '#aaa', fontWeight: 400 }}>({open || '—'} – {close})</span></>
                        : <>Open now</>;
                    }
                    return open
                      ? <>Closed — opens at <strong>{open}</strong> <span style={{ color: '#aaa', fontWeight: 400 }}>({open} – {close || '—'})</span></>
                      : <>Closed</>;
                  })()}
                </p>
                <p style={{ marginTop: '8px', fontSize: '0.85rem' }}>{r.menuItemCount} items</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
