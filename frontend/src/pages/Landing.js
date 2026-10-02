import React from 'react';
import { Link } from 'react-router-dom';
import './Landing.css';

const QUEUE_PREVIEW = [
  { token: 22, status: 'waiting' },
  { token: 23, status: 'waiting' },
  { token: 24, status: 'waiting' },
  { token: 25, status: 'waiting' },
  { token: 26, status: 'waiting' },
];

const CATEGORIES = ['Clinics', 'Salons', 'Diagnostic Centers', 'Service Centers', 'Government Offices', 'Restaurants'];

export default function Landing() {
  return (
    <div className="landing">

      {/* Hero */}
      <section className="hero">
        <div className="hero-inner">
          <div className="hero-text">
            <p className="hero-eyebrow">Virtual Queue Management</p>
            <h1 className="hero-heading">
              Queue less.<br />Do more.
            </h1>
            <p className="hero-sub">
              Join service queues digitally and know exactly when it's your turn.
              No waiting rooms. No guessing.
            </p>
            <div className="hero-actions">
              <Link to="/businesses" className="btn btn-primary btn-lg">Find a Queue</Link>
              <Link to="/register" className="btn btn-outline btn-lg">Manage a Queue</Link>
            </div>
          </div>

          {/* Live queue preview widget */}
          <div className="queue-widget">
            <div className="widget-header">
              <div>
                <p className="widget-business">CITY CARE CLINIC</p>
                <p className="widget-service">General Consultation</p>
              </div>
              <span className="badge badge-open">Open</span>
            </div>
            <div className="widget-serving">
              <span className="widget-label">NOW SERVING</span>
              <span className="widget-token-big">#21</span>
            </div>
            <div className="widget-divider" />
            <div className="widget-your">
              <span className="widget-label">YOUR TOKEN</span>
              <span className="widget-token-yours">#27</span>
            </div>
            <div className="widget-meta">
              <span>5 people ahead</span>
              <span>~30 min estimated wait</span>
            </div>
            <div className="widget-queue-row">
              {QUEUE_PREVIEW.map(t => (
                <div key={t.token} className="widget-chip">#{t.token}</div>
              ))}
              <div className="widget-chip widget-chip-you">You #27</div>
            </div>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section className="how-it-works">
        <div className="page-container">
          <p className="section-label">How it works</p>
          <div className="steps">
            {[
              { n: '01', title: 'Find', desc: 'Browse businesses near you. Filter by category, city, or open status.' },
              { n: '02', title: 'Join', desc: 'Select a service and join the virtual queue in one tap.' },
              { n: '03', title: 'Track', desc: 'See your token number, position, and estimated wait time live.' },
              { n: '04', title: 'Get served', desc: 'Arrive when it\'s your turn. No waiting room required.' },
            ].map(s => (
              <div key={s.n} className="step">
                <span className="step-number">{s.n}</span>
                <h3 className="step-title">{s.title}</h3>
                <p className="step-desc">{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Built for */}
      <section className="built-for">
        <div className="page-container">
          <p className="section-label">Built for real-world waiting</p>
          <div className="category-list">
            {CATEGORIES.map(c => (
              <span key={c} className="category-tag">{c}</span>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="cta-section">
        <div className="page-container">
          <div className="cta-box">
            <h2>Don't wait in line. Own your time.</h2>
            <p>Join QueueLess and take control of your waiting experience.</p>
            <Link to="/register" className="btn btn-primary btn-lg">Get started free</Link>
          </div>
        </div>
      </section>

    </div>
  );
}
