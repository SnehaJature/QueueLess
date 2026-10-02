import React, { useState, useEffect, useCallback } from 'react';
import { getQueuesByBusiness, callNext, completeToken, skipToken, getWaitingTokens, getQueueStats, openQueue, pauseQueue, closeQueue } from '../../api/queueApi';
import { getBusinesses } from '../../api/businessApi';
import './StaffDashboard.css';

export default function StaffDashboard() {
  const [businesses, setBusinesses] = useState([]);
  const [selectedQueue, setSelectedQueue] = useState(null);
  const [queues, setQueues] = useState([]);
  const [waitingTokens, setWaitingTokens] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState('');
  const [error, setError] = useState('');
  const [selectedBusiness, setSelectedBusiness] = useState('');

  useEffect(() => {
    getBusinesses({ open: true }).then(r => {
      setBusinesses(r.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const loadQueue = useCallback(async (queueId) => {
    try {
      const [wRes, sRes] = await Promise.all([
        getWaitingTokens(queueId),
        getQueueStats(queueId),
      ]);
      setWaitingTokens(wRes.data);
      setStats(sRes.data);
    } catch (e) {
      setError('Failed to load queue data.');
    }
  }, []);

  const handleSelectBusiness = async (businessId) => {
    setSelectedBusiness(businessId);
    setSelectedQueue(null);
    setWaitingTokens([]);
    setStats(null);
    if (!businessId) return;
    try {
      const { data } = await getQueuesByBusiness(businessId);
      setQueues(data);
      const open = data.find(q => q.status === 'OPEN');
      if (open) { setSelectedQueue(open); loadQueue(open.id); }
    } catch (e) { setError('Failed to load queues.'); }
  };

  const handleSelectQueue = (queue) => {
    setSelectedQueue(queue);
    loadQueue(queue.id);
  };

  const doAction = async (action, id) => {
    setActionLoading(action + id);
    setError('');
    try {
      if (action === 'next')     await callNext(selectedQueue.id);
      if (action === 'complete') await completeToken(id);
      if (action === 'skip')     await skipToken(id);
      if (action === 'open')     await openQueue(id);
      if (action === 'pause')    await pauseQueue(id);
      if (action === 'close')    await closeQueue(id);

      // Refresh queue state
      const [qRes, wRes, sRes] = await Promise.all([
        getQueuesByBusiness(selectedBusiness),
        getWaitingTokens(selectedQueue.id),
        getQueueStats(selectedQueue.id),
      ]);
      setQueues(qRes.data);
      const updated = qRes.data.find(q => q.id === selectedQueue.id);
      if (updated) setSelectedQueue(updated);
      setWaitingTokens(wRes.data);
      setStats(sRes.data);
    } catch (e) {
      setError(e.response?.data?.message || 'Action failed.');
    } finally {
      setActionLoading('');
    }
  };

  if (loading) return <div className="loading-center"><span className="spinner" /></div>;

  return (
    <div className="page-container" style={{ paddingTop: 28, paddingBottom: 48 }}>
      <div className="page-header">
        <h1 className="page-title">Staff Dashboard</h1>
        <p className="page-subtitle">Manage your queue in real time</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Business + Queue selector */}
      <div className="staff-selectors">
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label className="form-label">Select business</label>
          <select className="form-input form-select" value={selectedBusiness}
            onChange={e => handleSelectBusiness(e.target.value)}>
            <option value="">— choose a business —</option>
            {businesses.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
          </select>
        </div>
        {queues.length > 1 && (
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Select queue</label>
            <select className="form-input form-select" value={selectedQueue?.id || ''}
              onChange={e => handleSelectQueue(queues.find(q => q.id === e.target.value))}>
              {queues.map(q => <option key={q.id} value={q.id}>{q.queueName}</option>)}
            </select>
          </div>
        )}
      </div>

      {selectedQueue && (
        <div className="staff-layout">
          {/* Left: controls */}
          <div className="staff-controls">
            <div className="card">
              <div className="staff-queue-header">
                <div>
                  <p className="staff-queue-name">{selectedQueue.queueName}</p>
                  <p className="staff-business-name">{selectedQueue.businessName}</p>
                </div>
                <span className={`badge badge-${selectedQueue.status.toLowerCase()}`}>
                  {selectedQueue.status}
                </span>
              </div>

              <div className="serving-display">
                <p className="serving-label">NOW SERVING</p>
                <p className="serving-number">#{selectedQueue.currentTokenNumber}</p>
              </div>

              <div className="staff-action-buttons">
                <button className="btn btn-primary"
                  onClick={() => doAction('next', selectedQueue.id)}
                  disabled={!!actionLoading || selectedQueue.status !== 'OPEN'}>
                  {actionLoading === 'next' + selectedQueue.id ? <span className="spinner" /> : 'Call Next'}
                </button>

                {selectedQueue.status === 'OPEN' && (
                  <button className="btn btn-outline"
                    onClick={() => doAction('pause', selectedQueue.id)}
                    disabled={!!actionLoading}>
                    Pause Queue
                  </button>
                )}
                {selectedQueue.status === 'PAUSED' && (
                  <button className="btn btn-success"
                    onClick={() => doAction('open', selectedQueue.id)}
                    disabled={!!actionLoading}>
                    Resume Queue
                  </button>
                )}
                {selectedQueue.status !== 'CLOSED' && (
                  <button className="btn btn-danger"
                    onClick={() => doAction('close', selectedQueue.id)}
                    disabled={!!actionLoading}>
                    Close Queue
                  </button>
                )}
                {selectedQueue.status === 'CLOSED' && (
                  <button className="btn btn-success"
                    onClick={() => doAction('open', selectedQueue.id)}
                    disabled={!!actionLoading}>
                    Open Queue
                  </button>
                )}
              </div>
            </div>

            {/* Stats */}
            {stats && (
              <div className="card" style={{ marginTop: 16 }}>
                <p className="section-mini-heading">Today's summary</p>
                <div className="stats-grid">
                  <div className="stat-box">
                    <span className="stat-box-value">{stats.totalServed}</span>
                    <span className="stat-box-label">Served</span>
                  </div>
                  <div className="stat-box">
                    <span className="stat-box-value">{stats.totalSkipped}</span>
                    <span className="stat-box-label">Skipped</span>
                  </div>
                  <div className="stat-box">
                    <span className="stat-box-value">{stats.currentlyWaiting}</span>
                    <span className="stat-box-label">Waiting</span>
                  </div>
                  <div className="stat-box">
                    <span className="stat-box-value">{stats.averageServiceTime}m</span>
                    <span className="stat-box-label">Avg. time</span>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Right: waiting list */}
          <div className="staff-queue-list">
            <div className="card">
              <p className="section-mini-heading">
                Waiting — {waitingTokens.length} customer{waitingTokens.length !== 1 ? 's' : ''}
              </p>
              {waitingTokens.length === 0 ? (
                <p className="empty-queue-msg">No customers waiting.</p>
              ) : (
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Token</th>
                        <th>Joined</th>
                        <th>Waiting</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {waitingTokens.map(t => (
                        <tr key={t.tokenId}>
                          <td><strong>#{t.tokenNumber}</strong></td>
                          <td>{new Date(t.joinedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                          <td>{getWaitingDuration(t.joinedAt)}</td>
                          <td>
                            <div style={{ display: 'flex', gap: 6 }}>
                              <button className="btn btn-success btn-sm"
                                onClick={() => doAction('complete', t.tokenId)}
                                disabled={!!actionLoading}>
                                Done
                              </button>
                              <button className="btn btn-outline btn-sm"
                                onClick={() => doAction('skip', t.tokenId)}
                                disabled={!!actionLoading}>
                                Skip
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {!selectedQueue && selectedBusiness && (
        <div className="empty-state" style={{ marginTop: 40 }}>
          <p>No active queue found for this business.</p>
        </div>
      )}
    </div>
  );
}

function getWaitingDuration(joinedAt) {
  const diff = Math.floor((Date.now() - new Date(joinedAt)) / 60000);
  if (diff < 1) return '< 1 min';
  return `${diff} min`;
}
