import React, { useState, useEffect, useCallback } from 'react';
import { useLocation } from 'react-router-dom';
import { getMyToken, cancelToken } from '../../api/queueApi';
import './MyToken.css';

const STATUS_MESSAGES = {
  WAITING:   { label: 'Waiting',      hint: "You are in the queue. We'll let you know when it's your turn." },
  CALLED:    { label: 'Called',       hint: "You've been called! Please proceed to the counter." },
  SERVING:   { label: 'Being served', hint: 'You are currently being served.' },
  COMPLETED: { label: 'Completed',    hint: 'Your visit is complete. Thank you!' },
  SKIPPED:   { label: 'Skipped',      hint: 'Your token was skipped. Please check with the staff.' },
  CANCELLED: { label: 'Cancelled',    hint: 'You have left the queue.' },
};

export default function MyToken() {
  const location = useLocation();
  const [token, setToken] = useState(location.state?.token || null);
  const [loading, setLoading] = useState(!location.state?.token);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState(false);

  const fetchToken = useCallback(async () => {
    try {
      const { data } = await getMyToken();
      setToken(data);
    } catch (e) {
      if (e.response?.status !== 404) setError('Could not load your token.');
      else setToken(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!location.state?.token) fetchToken();
    const interval = setInterval(fetchToken, 15000);
    return () => clearInterval(interval);
  }, [fetchToken, location.state]);

  const handleCancel = async () => {
    if (!window.confirm('Leave the queue?')) return;
    setCancelling(true);
    try {
      await cancelToken(token.tokenId);
      fetchToken();
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to leave queue.');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <div className="loading-center"><span className="spinner" /></div>;

  if (!token) return (
    <div className="page-container" style={{ paddingTop: 48 }}>
      <div className="no-token-state">
        <p className="no-token-title">No active token</p>
        <p className="no-token-sub">You are not currently in any queue.</p>
      </div>
    </div>
  );

  const statusInfo = STATUS_MESSAGES[token.status] || { label: token.status, hint: '' };
  const isActive = ['WAITING', 'CALLED', 'SERVING'].includes(token.status);
  const isCalled = token.status === 'CALLED' || token.status === 'SERVING';

  return (
    <div className="page-container" style={{ paddingTop: 32, paddingBottom: 48 }}>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="token-page">
        <div className="token-business-header">
          <p className="token-business-name">{token.businessName}</p>
          <p className="token-queue-name">{token.queueName}</p>
        </div>

        <div className={`token-card ${isCalled ? 'token-card-called' : ''}`}>
          <div className="token-main">
            <p className="token-label">YOUR TOKEN</p>
            <p className="token-number">#{token.tokenNumber}</p>
          </div>

          <div className="token-divider" />

          <div className="token-serving-row">
            <div className="token-serving-item">
              <p className="token-serving-label">NOW SERVING</p>
              <p className="token-serving-number">#{token.currentServingToken}</p>
            </div>
            <div className="token-serving-item">
              <p className="token-serving-label">AHEAD OF YOU</p>
              <p className="token-serving-number">{token.peopleAhead}</p>
            </div>
            <div className="token-serving-item">
              <p className="token-serving-label">EST. WAIT</p>
              <p className="token-serving-number">{token.estimatedWaitMinutes} min</p>
            </div>
          </div>

          <div className="token-status-row">
            <span className={`badge badge-${token.status.toLowerCase()}`}>{statusInfo.label}</span>
            <p className="token-status-hint">{statusInfo.hint}</p>
          </div>
        </div>

        <div className="token-meta-row">
          {token.joinedAt && (
            <div className="token-meta-item">
              <span className="token-meta-label">Joined at</span>
              <span className="token-meta-value">
                {new Date(token.joinedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          )}
          {token.calledAt && (
            <div className="token-meta-item">
              <span className="token-meta-label">Called at</span>
              <span className="token-meta-value">
                {new Date(token.calledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          )}
          {token.completedAt && (
            <div className="token-meta-item">
              <span className="token-meta-label">Completed at</span>
              <span className="token-meta-value">
                {new Date(token.completedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          )}
        </div>

        {isActive && (
          <div style={{ textAlign: 'center', marginTop: 24 }}>
            <button className="btn btn-outline btn-sm" onClick={handleCancel} disabled={cancelling}>
              {cancelling ? <span className="spinner" /> : 'Leave queue'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
