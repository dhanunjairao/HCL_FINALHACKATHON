import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const data = await login(form.username, form.password);
      if (data.role === 'ADMIN') navigate('/admin');
      else if (data.role === 'MANAGER') navigate('/manager');
      else if (data.role === 'DELIVERY') navigate('/delivery');
      else navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    }
  };

  return (
    <div className="auth-container">
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Username</label>
          <input type="text" value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })} required />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input type="password" value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })} required />
        </div>
        <button type="submit" className="btn btn-primary btn-block">Login</button>
        {error && <p className="error">{error}</p>}
      </form>
      <p style={{ marginTop: '15px', textAlign: 'center' }}>
        No account? <Link to="/register" className="link">Register</Link>
      </p>
      <p style={{ marginTop: '15px', textAlign: 'center', fontSize: '0.85rem', color: '#777' }}>
        Customers and Restaurant Owners can register here.<br />
        Admin accounts are created only by an existing admin.
      </p>
    </div>
  );
}
