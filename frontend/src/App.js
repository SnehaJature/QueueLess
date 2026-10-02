import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Navbar from './components/layout/Navbar';

import Landing           from './pages/Landing';
import Login             from './pages/auth/Login';
import Register          from './pages/auth/Register';
import BusinessDiscovery from './pages/customer/BusinessDiscovery';
import BusinessDetail    from './pages/customer/BusinessDetail';
import MyToken           from './pages/customer/MyToken';
import StaffDashboard    from './pages/staff/StaffDashboard';
import AdminBusinesses   from './pages/admin/AdminBusinesses';
import AdminOverview     from './pages/admin/AdminOverview';

import './styles/global.css';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          {/* Public */}
          <Route path="/"         element={<Landing />} />
          <Route path="/login"    element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/businesses"    element={<BusinessDiscovery />} />
          <Route path="/businesses/:id" element={<BusinessDetail />} />

          {/* Customer */}
          <Route path="/my-token" element={
            <ProtectedRoute roles={['CUSTOMER']}>
              <MyToken />
            </ProtectedRoute>
          } />

          {/* Staff */}
          <Route path="/staff" element={
            <ProtectedRoute roles={['STAFF', 'ADMIN']}>
              <StaffDashboard />
            </ProtectedRoute>
          } />

          {/* Admin */}
          <Route path="/admin" element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminBusinesses />
            </ProtectedRoute>
          } />
          <Route path="/admin/overview" element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminOverview />
            </ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
