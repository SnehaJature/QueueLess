import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getBusinesses } from '../../api/businessApi';
import './BusinessDiscovery.css';

const CATEGORIES = ['', 'CLINIC', 'SALON', 'DIAGNOSTIC_CENTER', 'SERVICE_CENTER', 'GOVERNMENT_SERVICE', 'RESTAURANT'];
const CATEGORY_LABELS = {
  CLINIC: 'Clinic', SALON: 'Salon', DIAGNOSTIC_CENTER: 'Diagnostic Center',
  SERVICE_CENTER: 'Service Center', GOVERNMENT_SERVICE: 'Government Service', RESTAURANT: 'Restaurant',
};

export default function BusinessDiscovery() {
  const [businesses, setBusinesses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ category: '', city: '', open: '' });

  useEffect(() => {
    fetchBusinesses();
  }, [filters]);

  const fetchBusinesses = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filters.category) params.category = filters.category;
      if (filters.city) params.city = filters.city;
      if (filters.open !== '') params.open = filters.open;
      const { data } = await getBusinesses(params);
      setBusinesses(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container" style={{ paddingTop: 28 }}>
      <div className="discovery-layout">

        {/* Filters sidebar */}
        <aside className="filters-panel">
          <p className="filters-title">Filters</p>

          <div className="form-group">
            <label className="form-label">Category</label>
            <select className="form-input form-select"
              value={filters.category}
              onChange={e => setFilters({ ...filters, category: e.target.value })}>
              <option value="">All categories</option>
              {CATEGORIES.filter(Boolean).map(c => (
                <option key={c} value={c}>{CATEGORY_LABELS[c]}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">City</label>
            <input className="form-input" type="text" placeholder="e.g. Pune"
              value={filters.city}
              onChange={e => setFilters({ ...filters, city: e.target.value })} />
          </div>

          <div className="form-group">
            <label className="form-label">Availability</label>
            <select className="form-input form-select"
              value={filters.open}
              onChange={e => setFilters({ ...filters, open: e.target.value })}>
              <option value="">All</option>
              <option value="true">Open now</option>
              <option value="false">Closed</option>
            </select>
          </div>

          <button className="btn btn-ghost btn-sm" style={{ marginTop: 4 }}
            onClick={() => setFilters({ category: '', city: '', open: '' })}>
            Clear filters
          </button>
        </aside>

        {/* Results */}
        <div className="results-panel">
          <div className="results-header">
            <p className="results-count">
              {loading ? 'Loading...' : `${businesses.length} business${businesses.length !== 1 ? 'es' : ''} found`}
            </p>
          </div>

          {loading ? (
            <div className="loading-center"><span className="spinner" /></div>
          ) : businesses.length === 0 ? (
            <div className="empty-state">
              <p>No businesses match your filters.</p>
              <button className="btn btn-outline btn-sm" onClick={() => setFilters({ category: '', city: '', open: '' })}>
                Clear filters
              </button>
            </div>
          ) : (
            <div className="business-list">
              {businesses.map(b => (
                <BusinessRow key={b.id} business={b} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function BusinessRow({ business: b }) {
  return (
    <div className="business-row">
      <div className="business-row-main">
        <div className="business-row-info">
          <div className="business-row-top">
            <span className="business-row-name">{b.name}</span>
            <span className={`badge ${b.open ? 'badge-open' : 'badge-closed'}`}>
              {b.open ? 'Open' : 'Closed'}
            </span>
          </div>
          <p className="business-row-meta">
            {CATEGORY_LABELS[b.category]} &middot; {b.city}
          </p>
          {b.description && <p className="business-row-desc">{b.description}</p>}
        </div>
        <div className="business-row-stats">
          <div className="stat-item">
            <span className="stat-label">Avg. service</span>
            <span className="stat-value">~{b.averageServiceTime} min</span>
          </div>
        </div>
      </div>
      <div className="business-row-action">
        <Link to={`/businesses/${b.id}`} className="btn btn-outline btn-sm">
          View Queue
        </Link>
      </div>
    </div>
  );
}
