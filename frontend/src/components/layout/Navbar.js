import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutUser();
    navigate('/login');
  };

  const navLinks = () => {
    if (!user) return null;
    if (user.role === 'CUSTOMER') return (
      <>
        <Link to="/businesses">Find a Queue</Link>
        <Link to="/my-token">My Token</Link>
      </>
    );
    if (user.role === 'STAFF') return <Link to="/staff">Staff Dashboard</Link>;
    if (user.role === 'ADMIN') return (
      <>
        <Link to="/admin">Businesses</Link>
        <Link to="/admin/overview">Overview</Link>
      </>
    );
  };

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">
          <span className="brand-mark">Q</span>
          <span className="brand-name">ueueLess</span>
        </Link>

        <div className="navbar-links">
          {navLinks()}
        </div>

        <div className="navbar-actions">
          {user ? (
            <>
              <span className="navbar-user">{user.name}</span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>Sign out</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">Sign in</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Get started</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
