import React, { useState, useEffect } from 'react';
import { getBusinesses, createBusiness, updateBusiness, deleteBusiness, openBusiness, closeBusiness } from '../../api/businessApi';
import { getQueuesByBusiness, createQueue, openQueue, closeQueue, pauseQueue } from '../../api/queueApi';
import './AdminBusinesses.css';

const CATEGORIES = ['CLINIC','SALON','DIAGNOSTIC_CENTER','SERVICE_CENTER','GOVERNMENT_SERVICE','RESTAURANT'];
const CATEGORY_LABELS = {
  CLINIC:'Clinic', SALON:'Salon', DIAGNOSTIC_CENTER:'Diagnostic Center',
  SERVICE_CENTER:'Service Center', GOVERNMENT_SERVICE:'Government Service', RESTAURANT:'Restaurant',
};
const EMPTY_FORM = { name:'', category:'CLINIC', description:'', address:'', city:'', phone:'', averageServiceTime:15 };

export default function AdminBusinesses() {
  const [businesses, setBusinesses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  // Queue state per business
  const [queuesMap, setQueuesMap] = useState({});         // businessId -> queues[]
  const [expandedBiz, setExpandedBiz] = useState(null);  // which business row is expanded
  const [queueForm, setQueueForm] = useState({ queueName:'Main Queue', averageServiceTime:15 });
  const [showQueueForm, setShowQueueForm] = useState(null); // businessId
  const [queueSaving, setQueueSaving] = useState(false);

  const load = async () => {
    try {
      const { data } = await getBusinesses();
      setBusinesses(data);
    } catch (e) {
      setError('Failed to load businesses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const loadQueues = async (businessId) => {
    try {
      const { data } = await getQueuesByBusiness(businessId);
      setQueuesMap(prev => ({ ...prev, [businessId]: data }));
    } catch (e) {
      setQueuesMap(prev => ({ ...prev, [businessId]: [] }));
    }
  };

  const handleExpandBiz = (businessId) => {
    if (expandedBiz === businessId) {
      setExpandedBiz(null);
    } else {
      setExpandedBiz(businessId);
      loadQueues(businessId);
    }
  };

  // ── Business CRUD ──────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      if (editingId) {
        await updateBusiness(editingId, form);
      } else {
        const { data: biz } = await createBusiness(form);
        // Auto-create a default queue for new business
        await createQueue({
          businessId: biz.id,
          queueName: 'Main Queue',
          averageServiceTime: form.averageServiceTime,
        });
      }
      setShowForm(false); setEditingId(null); setForm(EMPTY_FORM);
      load();
    } catch (e) {
      setError(e.response?.data?.message || 'Save failed.');
    } finally { setSaving(false); }
  };

  const handleEdit = (b) => {
    setEditingId(b.id);
    setForm({ name:b.name, category:b.category, description:b.description||'', address:b.address, city:b.city, phone:b.phone||'', averageServiceTime:b.averageServiceTime });
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this business? All its queues will also be removed.')) return;
    try { await deleteBusiness(id); load(); }
    catch (e) { setError('Delete failed.'); }
  };

  const handleToggleOpen = async (b) => {
    try {
      b.open ? await closeBusiness(b.id) : await openBusiness(b.id);
      load();
    } catch (e) { setError('Failed to update business status.'); }
  };

  // ── Queue CRUD ─────────────────────────────────────────────
  const handleCreateQueue = async (businessId) => {
    setQueueSaving(true);
    try {
      await createQueue({ businessId, queueName: queueForm.queueName, averageServiceTime: queueForm.averageServiceTime });
      setShowQueueForm(null);
      setQueueForm({ queueName:'Main Queue', averageServiceTime:15 });
      loadQueues(businessId);
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to create queue.');
    } finally { setQueueSaving(false); }
  };

  const handleQueueAction = async (action, queueId, businessId) => {
    try {
      if (action === 'open')  await openQueue(queueId);
      if (action === 'pause') await pauseQueue(queueId);
      if (action === 'close') await closeQueue(queueId);
      loadQueues(businessId);
    } catch (e) {
      setError(e.response?.data?.message || 'Queue action failed.');
    }
  };

  if (loading) return <div className="loading-center"><span className="spinner" /></div>;

  return (
    <div className="page-container" style={{ paddingTop: 28, paddingBottom: 48 }}>
      <div className="page-header" style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div>
          <h1 className="page-title">Businesses</h1>
          <p className="page-subtitle">Manage businesses and their queues</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setShowForm(true); setEditingId(null); setForm(EMPTY_FORM); }}>
          + Add Business
        </button>
      </div>

      {error && <div className="alert alert-error" onClick={() => setError('')}>{error}</div>}

      {/* Business Form */}
      {showForm && (
        <div className="card" style={{ marginBottom: 24 }}>
          <p className="section-mini-heading">{editingId ? 'Edit Business' : 'New Business'}</p>
          <form onSubmit={handleSubmit} className="admin-form">
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Business name</label>
                <input className="form-input" value={form.name} onChange={e => setForm({...form, name:e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Category</label>
                <select className="form-input form-select" value={form.category} onChange={e => setForm({...form, category:e.target.value})}>
                  {CATEGORIES.map(c => <option key={c} value={c}>{CATEGORY_LABELS[c]}</option>)}
                </select>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Address</label>
                <input className="form-input" value={form.address} onChange={e => setForm({...form, address:e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">City</label>
                <input className="form-input" value={form.city} onChange={e => setForm({...form, city:e.target.value})} required />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Phone</label>
                <input className="form-input" value={form.phone} onChange={e => setForm({...form, phone:e.target.value})} />
              </div>
              <div className="form-group">
                <label className="form-label">Avg. service time (min)</label>
                <input className="form-input" type="number" min="1" value={form.averageServiceTime}
                  onChange={e => setForm({...form, averageServiceTime:parseInt(e.target.value)})} required />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <input className="form-input" value={form.description} onChange={e => setForm({...form, description:e.target.value})} />
            </div>
            <div style={{ display:'flex', gap:10 }}>
              <button className="btn btn-primary" type="submit" disabled={saving}>
                {saving ? <span className="spinner" /> : (editingId ? 'Save changes' : 'Create business')}
              </button>
              <button className="btn btn-ghost" type="button" onClick={() => { setShowForm(false); setEditingId(null); }}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Business Table */}
      <div className="card">
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Business</th>
                <th>Category</th>
                <th>City</th>
                <th>Status</th>
                <th>Avg. time</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {businesses.length === 0 && (
                <tr><td colSpan={6} style={{ textAlign:'center', color:'var(--text-muted)', padding:32 }}>No businesses yet. Click "+ Add Business" to create one.</td></tr>
              )}
              {businesses.map(b => (
                <React.Fragment key={b.id}>
                  {/* Business row */}
                  <tr>
                    <td>
                      <p style={{ fontWeight:600 }}>{b.name}</p>
                      <p style={{ fontSize:'0.8125rem', color:'var(--text-muted)' }}>{b.address}</p>
                    </td>
                    <td>{CATEGORY_LABELS[b.category]}</td>
                    <td>{b.city}</td>
                    <td><span className={`badge ${b.open ? 'badge-open' : 'badge-closed'}`}>{b.open ? 'Open' : 'Closed'}</span></td>
                    <td>{b.averageServiceTime} min</td>
                    <td>
                      <div style={{ display:'flex', gap:6, flexWrap:'wrap' }}>
                        <button className="btn btn-outline btn-sm" onClick={() => handleEdit(b)}>Edit</button>
                        <button className={`btn btn-sm ${b.open ? 'btn-outline' : 'btn-success'}`} onClick={() => handleToggleOpen(b)}>
                          {b.open ? 'Close' : 'Open'}
                        </button>
                        <button className="btn btn-sm btn-queue" onClick={() => handleExpandBiz(b.id)}>
                          {expandedBiz === b.id ? 'Hide Queues ▲' : 'Queues ▼'}
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(b.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>

                  {/* Expanded queue section */}
                  {expandedBiz === b.id && (
                    <tr>
                      <td colSpan={6} style={{ padding:0, background:'var(--bg-subtle)' }}>
                        <div className="queue-expand-panel">
                          <div className="queue-expand-header">
                            <p className="queue-expand-title">Queues for {b.name}</p>
                            <button className="btn btn-primary btn-sm"
                              onClick={() => setShowQueueForm(showQueueForm === b.id ? null : b.id)}>
                              + Create Queue
                            </button>
                          </div>

                          {/* Create queue form */}
                          {showQueueForm === b.id && (
                            <div className="queue-create-form">
                              <div className="form-row">
                                <div className="form-group" style={{ marginBottom:0 }}>
                                  <label className="form-label">Queue name</label>
                                  <input className="form-input" value={queueForm.queueName}
                                    onChange={e => setQueueForm({...queueForm, queueName:e.target.value})} />
                                </div>
                                <div className="form-group" style={{ marginBottom:0 }}>
                                  <label className="form-label">Avg. service time (min)</label>
                                  <input className="form-input" type="number" min="1" value={queueForm.averageServiceTime}
                                    onChange={e => setQueueForm({...queueForm, averageServiceTime:parseInt(e.target.value)})} />
                                </div>
                              </div>
                              <div style={{ display:'flex', gap:8, marginTop:12 }}>
                                <button className="btn btn-primary btn-sm" onClick={() => handleCreateQueue(b.id)} disabled={queueSaving}>
                                  {queueSaving ? <span className="spinner" /> : 'Create Queue'}
                                </button>
                                <button className="btn btn-ghost btn-sm" onClick={() => setShowQueueForm(null)}>Cancel</button>
                              </div>
                            </div>
                          )}

                          {/* Queue list */}
                          {!queuesMap[b.id] ? (
                            <p className="queue-loading">Loading queues...</p>
                          ) : queuesMap[b.id].length === 0 ? (
                            <div className="queue-empty">
                              <p>No queues yet for this business.</p>
                              <p style={{ fontSize:'0.8125rem', color:'var(--text-muted)', marginTop:4 }}>
                                Click "+ Create Queue" to add one. Staff can then open it and customers can join.
                              </p>
                            </div>
                          ) : (
                            <table className="queue-inner-table">
                              <thead>
                                <tr>
                                  <th>Queue Name</th>
                                  <th>Status</th>
                                  <th>Avg. Time</th>
                                  <th>Waiting</th>
                                  <th>Actions</th>
                                </tr>
                              </thead>
                              <tbody>
                                {queuesMap[b.id].map(q => (
                                  <tr key={q.id}>
                                    <td style={{ fontWeight:600 }}>{q.queueName}</td>
                                    <td><span className={`badge badge-${q.status.toLowerCase()}`}>{q.status}</span></td>
                                    <td>{q.averageServiceTime} min</td>
                                    <td>{q.waitingCount ?? 0}</td>
                                    <td>
                                      <div style={{ display:'flex', gap:6 }}>
                                        {q.status === 'CLOSED' && (
                                          <button className="btn btn-success btn-sm" onClick={() => handleQueueAction('open', q.id, b.id)}>Open</button>
                                        )}
                                        {q.status === 'OPEN' && (
                                          <button className="btn btn-outline btn-sm" onClick={() => handleQueueAction('pause', q.id, b.id)}>Pause</button>
                                        )}
                                        {q.status === 'PAUSED' && (
                                          <button className="btn btn-success btn-sm" onClick={() => handleQueueAction('open', q.id, b.id)}>Resume</button>
                                        )}
                                        {q.status !== 'CLOSED' && (
                                          <button className="btn btn-danger btn-sm" onClick={() => handleQueueAction('close', q.id, b.id)}>Close</button>
                                        )}
                                      </div>
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          )}
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
