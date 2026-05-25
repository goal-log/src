import { useNavigate, useLocation } from 'react-router-dom';
import './Navbar.css';

export default function Navbar() {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  function handleLogout() {
    localStorage.removeItem('token');
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <span className="navbar-logo">GoalLog</span>
      <div className="navbar-tabs">
        <button
          className={`navbar-tab${pathname === '/goals' ? ' active' : ''}`}
          onClick={() => navigate('/goals')}
        >
          🎯 목표
        </button>
        <button
          className={`navbar-tab${pathname === '/daily-plan' ? ' active' : ''}`}
          onClick={() => navigate('/daily-plan')}
        >
          📋 하루 계획
        </button>
      </div>
      <button className="navbar-logout" onClick={handleLogout}>로그아웃</button>
    </nav>
  );
}