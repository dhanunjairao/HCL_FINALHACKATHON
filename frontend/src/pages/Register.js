import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const EMPTY_FORM = {
  username: '',
  email: '',
  password: '',
  fullName: '',
  phone: '',
  address: '',
  // manager-only
  proprietorName: '',
  restaurantName: '',
  foodLicense: '',
  restaurantAddress: '',
  cuisineType: '',
  restaurantPhone: '',
  restaurantDescription: '',
  restaurantImageUrl: '',
  openTime: '',
  closeTime: '',
  latitude: '',
  longitude: ''
};

export default function Register() {
  const [role, setRole] = useState('USER');
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const buildPayload = () => {
    const base = {
      username: form.username,
      email: form.email,
      password: form.password,
      fullName: form.fullName,
      phone: form.phone,
      address: form.address,
      role
    };
    if (role !== 'MANAGER') return base;
    return {
      ...base,
      proprietorName: form.proprietorName,
      restaurantName: form.restaurantName,
      foodLicense: form.foodLicense,
      restaurantAddress: form.restaurantAddress,
      cuisineType: form.cuisineType || null,
      restaurantPhone: form.restaurantPhone || null,
      restaurantDescription: form.restaurantDescription || null,
      restaurantImageUrl: form.restaurantImageUrl || null,
      openTime: form.openTime || null,
      closeTime: form.closeTime || null,
      latitude: form.latitude ? Number(form.latitude) : null,
      longitude: form.longitude ? Number(form.longitude) : null
    };
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const result = await register(buildPayload());
      if (role === 'MANAGER') {
        setSuccess(result?.message || 'Registration submitted. Awaiting admin approval.');
      } else {
        navigate('/');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  if (success) {
    return (
      <div className="auth-container">
        <h2>Registration Submitted</h2>
        <div style={{ background: '#fff3e0', border: '1px solid #ffb74d', borderRadius: '8px', padding: '15px', marginBottom: '15px' }}>
          <p style={{ margin: 0, color: '#e65100' }}>{success}</p>
        </div>
        <p style={{ marginTop: '10px', color: '#555' }}>
          Once an admin approves your restaurant you will be able to log in.
        </p>
        <button className="btn btn-primary btn-block" onClick={() => navigate('/login')}>Go to Login</button>
      </div>
    );
  }

  return (
    <div className="auth-container">
      <h2>Register</h2>

      <div className="form-group">
        <label>Register as</label>
        <select value={role} onChange={(e) => setRole(e.target.value)}>
          <option value="USER">Customer</option>
          <option value="MANAGER">Restaurant Owner (Manager)</option>
          <option value="DELIVERY">Delivery Person</option>
        </select>
        <small style={{ color: '#777' }}>
          {role === 'MANAGER' && 'Restaurant registrations are reviewed by an admin before login is enabled.'}
          {role === 'USER' && 'Customers can browse, order, and track deliveries.'}
          {role === 'DELIVERY' && 'Delivery people pick up ready orders and deliver them. No approval needed.'}
        </small>
      </div>

      <form onSubmit={handleSubmit}>
        <h3 style={{ marginTop: '15px', color: '#ff5722' }}>Account</h3>
        <div className="form-group">
          <label>Username *</label>
          <input value={form.username} onChange={set('username')} required minLength={3} />
        </div>
        <div className="form-group">
          <label>Email *</label>
          <input type="email" value={form.email} onChange={set('email')} required />
        </div>
        <div className="form-group">
          <label>Password *</label>
          <input type="password" value={form.password} onChange={set('password')} required minLength={6} />
        </div>
        <div className="form-group">
          <label>Full Name</label>
          <input value={form.fullName} onChange={set('fullName')} />
        </div>
        <div className="form-group">
          <label>Phone</label>
          <input value={form.phone} onChange={set('phone')} />
        </div>
        <div className="form-group">
          <label>{role === 'MANAGER' ? 'Personal Address' : 'Address'}</label>
          <textarea value={form.address} onChange={set('address')} rows="2" />
        </div>

        {role === 'MANAGER' && (
          <>
            <h3 style={{ marginTop: '20px', color: '#ff5722' }}>Restaurant Details</h3>
            <div className="form-group">
              <label>Proprietor Name *</label>
              <input value={form.proprietorName} onChange={set('proprietorName')} required />
            </div>
            <div className="form-group">
              <label>Restaurant Name *</label>
              <input value={form.restaurantName} onChange={set('restaurantName')} required />
            </div>
            <div className="form-group">
              <label>Food License Number *</label>
              <input value={form.foodLicense} onChange={set('foodLicense')} required placeholder="e.g. FSSAI-12345678901234" />
            </div>
            <div className="form-group">
              <label>Restaurant Address *</label>
              <textarea value={form.restaurantAddress} onChange={set('restaurantAddress')} rows="2" required />
            </div>
            <div className="form-group">
              <label>Cuisine Type</label>
              <input value={form.cuisineType} onChange={set('cuisineType')} placeholder="Italian, Indian, etc." />
            </div>
            <div className="form-group">
              <label>Restaurant Phone</label>
              <input value={form.restaurantPhone} onChange={set('restaurantPhone')} />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea value={form.restaurantDescription} onChange={set('restaurantDescription')} rows="2" />
            </div>
            <div className="form-group">
              <label>Image URL</label>
              <input value={form.restaurantImageUrl} onChange={set('restaurantImageUrl')} />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div className="form-group">
                <label>Open Time</label>
                <input type="time" value={form.openTime} onChange={set('openTime')} />
              </div>
              <div className="form-group">
                <label>Close Time</label>
                <input type="time" value={form.closeTime} onChange={set('closeTime')} />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div className="form-group">
                <label>Latitude</label>
                <input type="number" step="any" value={form.latitude} onChange={set('latitude')} />
              </div>
              <div className="form-group">
                <label>Longitude</label>
                <input type="number" step="any" value={form.longitude} onChange={set('longitude')} />
              </div>
            </div>
          </>
        )}

        <button type="submit" className="btn btn-primary btn-block" style={{ marginTop: '15px' }}>
          {role === 'MANAGER' ? 'Submit for Approval' : 'Register'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>
      <p style={{ marginTop: '15px', textAlign: 'center' }}>
        Already have an account? <Link to="/login" className="link">Login</Link>
      </p>
    </div>
  );
}
