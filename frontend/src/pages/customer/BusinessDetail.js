import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getBusinessById, getServices } from '../../api/businessApi';
import { getQueuesByBusiness, joinQueue, getAlternatives } from '../../api/queueApi';
import { useAuth } from '../../context/AuthContext';
import './BusinessDetail.css';

const CATEGORY_LABELS = {
  CLINIC: 'Clinic', SALON: 'Salon', DIAGNOSTIC_CENTER: 'Diagnostic Center',
  SERVICE_CENTER: 'Service Center', GOVERNMENT_SERVICE: 'Government Service', RESTAURANT: 'Restaurant',
};

export default function BusinessDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [business, setBusiness] = useState(null);
  const [services, setServices] = useState([]);
  const [queues, setQueues] = useState([]);
  const [alternatives, setAlternatives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [joining, setJoining] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const [bRes, sRes, qRes] = await Promise.all([
          getBusinessById(id),
          getServices(id),
          getQueuesByBusiness(id),
        ]);
        setBusiness(bRes.data);
        setServices(sRes.data);
        setQueues(qRes.data);

        const openQueue = qRes.data.find(q => q.status === 'OPEN');
        if (openQueue) {
          try {
            const altRes = await getAlternatives(openQueue.id);
            setAlternatives(altRes.data);
          } catch (_) {}
        }
      } catch (e) {
        setError('Failed to load business details.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handleJoin = async (queueId) => {
    if (!user) { navigate('/login'); return; }
    setJoining(true);
    setError('');
    try {
      const { data } = await joinQueue(queueId);
      navigate(`/my-token`, { state: { token: data } });
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to join queue.');
    } finally {
      setJoining(false);
    }
  };

  if (loading) return <div className="loading-center"><span className="spinner" /></div>;
  if (!business) return <div className="page-container"><p>Business not found.</p></div>;

  const openQueue = queues.find(q => q.status === 'OPEN');

  return (
    <div className="page-container" style={{ paddingTop: 28, paddingBottom: 48 }}>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="detail-layout">
        <div className="detail-main">
          {/* Business header */}
          <div className="detail-header card">
            <div className="detail-header-top">
              <div>
                <h1 className="detail-name">{business.name}</h1>
                <p className="detail-meta">
                  {CATEGORY_LABELS[business.category]} &middot; {business.address}, {business.city}
                  {business.phone && ` · ${business.phone}`}
                </p>
                {business.description && <p className="detail-desc">{business.description}</p>}
              </div>
              <span className={`badge ${business.open ? 'badge-open' : 'badge-closed'}`}>
                {business.open ? 'Open' : 'Closed'}
              </span>
            </div>
          </div>

          {/* Services */}
          <div className="card" style={{ marginTop: 16 }}>
            <p className="section-heading">Services offered</p>
            {services.length === 0 ? (
              <p className="text-muted">No services listed.</p>
            ) : (
              <div className="services-list">
                {services.map(s => (
                  <div key={s.id} className="service-row">
                    <div>
                      <p className="service-name">{s.name}</p>
                      {s.description && <p className="service-desc">{s.description}</p>}
                    </div>
                    <span className="service-time">~{s.estimatedMinutes} min</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Queue sidebar */}
        <aside className="detail-sidebar">
          {openQueue ? (
            <div className="queue-info-card card">
              <p className="section-heading">Current Queue</p>
              <div className="queue-stat-row">
                <div className="queue-stat">
                  <span className="queue-stat-label">Now serving</span>
                  <span className="queue-stat-value">#{openQueue.currentTokenNumber}</span>
                </div>
                <div className="queue-stat">
                  <span className="queue-stat-label">Waiting</span>
                  <span className="queue-stat-value">{openQueue.waitingCount}</span>
                </div>
              </div>
              <div className="queue-wait-estimate">
                <span className="queue-stat-label">Estimated wait</span>
                <span className="queue-wait-time">
                  ~{openQueue.waitingCount * openQueue.averageServiceTime} min
                </span>
              </div>
              {user?.role === 'CUSTOMER' && (
                <button className="btn btn-primary" style={{ width: '100%', marginTop: 16 }}
                  onClick={() => handleJoin(openQueue.id)} disabled={joining}>
                  {joining ? <span className="spinner" /> : 'Join Queue'}
                </button>
              )}
              {!user && (
                <button className="btn btn-outline" style={{ width: '100%', marginTop: 16 }}
                  onClick={() => navigate('/login')}>
                  Sign in to join
                </button>
              )}
            </div>
          ) : (
            <div className="card">
              <p className="section-heading">Queue</p>
              <p className="text-muted">No active queue at this time.</p>
            </div>
          )}

          {/* Alternatives */}
          {alternatives.length > 0 && (
            <div className="card" style={{ marginTop: 16 }}>
              <p className="section-heading">Shorter queues nearby</p>
              {alternatives.map(alt => (
                <div key={alt.queueId} className="alt-row">
                  <div>
                    <p className="alt-name">{alt.businessName}</p>
                    <p className="alt-meta">{alt.city} · {alt.waitingCount} waiting · ~{alt.estimatedWaitMinutes} min</p>
                  </div>
                  <button className="btn btn-outline btn-sm"
                    onClick={() => navigate(`/businesses/${alt.businessId}`)}>
                    View
                  </button>
                </div>
              ))}
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}
