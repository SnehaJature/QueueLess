import React, { useState, useEffect } from 'react';
import { getBusinesses } from '../../api/businessApi';
import { getQueuesByBusiness, getQueueStats } from '../../api/queueApi';

export default function AdminOverview() {
  const [businesses, setBusinesses] = useState([]);
  const [summary, setSummary] = useState({ total: 0, open: 0, totalWaiting: 0, totalServed: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const { data: bList } = await getBusinesses();
        setBusinesses(bList);

        let totalWaiting = 0, totalServed = 0;
        for (const b of bList) {
          try {
            const { data: queues } = await getQueuesByBusiness(b.id);
            for (const q of queues) {
              if (q.status === 'OPEN') {
                totalWaiting += q.waitingCount || 0;
                try {
                  const { data: s } = await getQueueStats(q.id);
                  totalServed += s.totalServed || 0;
                } catch (_) {}
              }
            }
          } catch (_) {}
        }

        setSummary({
          total: bList.length,
          open: bList.filter(b => b.open).length,
          totalWaiting,
          totalServed,
        });
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <div className="loading-center"><span className="spinner" /></div>;

  return (
    <div className="page-container" style={{ paddingTop: 28, paddingBottom: 48 }}>
      <div className="page-header">
        <h1 className="page-title">System Overview</h1>
        <p className="page-subtitle">Live snapshot across all businesses</p>
      </div>

      <div className="overview-stats">
        {[
          { label: 'Total businesses', value: summary.total },
          { label: 'Open right now',   value: summary.open },
          { label: 'Currently waiting', value: summary.totalWaiting },
          { label: 'Served today',      value: summary.totalServed },
        ].map(s => (
          <div key={s.label} className="card overview-stat-card">
            <p className="overview-stat-value">{s.value}</p>
            <p className="overview-stat-label">{s.label}</p>
          </div>
        ))}
      </div>

      <div className="card" style={{ marginTop: 24 }}>
        <p className="section-mini-heading">All businesses</p>
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Business</th>
                <th>Category</th>
                <th>City</th>
                <th>Status</th>
                <th>Avg. service time</th>
              </tr>
            </thead>
            <tbody>
              {businesses.map(b => (
                <tr key={b.id}>
                  <td style={{ fontWeight: 600 }}>{b.name}</td>
                  <td>{b.category}</td>
                  <td>{b.city}</td>
                  <td><span className={`badge ${b.open ? 'badge-open' : 'badge-closed'}`}>{b.open ? 'Open' : 'Closed'}</span></td>
                  <td>{b.averageServiceTime} min</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
