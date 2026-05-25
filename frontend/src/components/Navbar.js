import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { count } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <h1 onClick={() => navigate('/')}>FoodOrder</h1>
      <div className="nav-links">
        {!user && (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
        {user && user.role === 'USER' && (
          <>
            <Link to="/">Restaurants</Link>
            <Link to="/cart">Cart {count > 0 && <span className="cart-badge">{count}</span>}</Link>
            <Link to="/my-orders">My Orders</Link>
            <span>{user.username} <span className="role-badge">USER</span></span>
            <button onClick={handleLogout}>Logout</button>
          </>
        )}
        {user && user.role === 'MANAGER' && (
          <>
            <Link to="/manager">Dashboard</Link>
            <span>{user.username} <span className="role-badge">MANAGER</span></span>
            <button onClick={handleLogout}>Logout</button>
          </>
        )}
        {user && user.role === 'ADMIN' && (
          <>
            <Link to="/admin">Dashboard</Link>
            <span>{user.username} <span className="role-badge">ADMIN</span></span>
            <button onClick={handleLogout}>Logout</button>
          </>
        )}
        {user && user.role === 'DELIVERY' && (
          <>
            <Link to="/delivery">Dashboard</Link>
            <span>{user.username} <span className="role-badge">DELIVERY</span></span>
            <button onClick={handleLogout}>Logout</button>
          </>
        )}
      </div>
    </nav>
  );
}
