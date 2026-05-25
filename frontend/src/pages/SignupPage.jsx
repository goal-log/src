import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { signup } from '../api/auth';
import './SignupPage.css';

export default function SignupPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (password !== confirm) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }
    setLoading(true);
    try {
      await signup(email, password);
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-logo">GoalLog</div>
        <p className="auth-subtitle">목표를 향한 첫 걸음을 시작하세요</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="input-group">
            <label className="input-label">이메일</label>
            <input
              type="email"
              placeholder="example@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="auth-input"
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">비밀번호</label>
            <input
              type="password"
              placeholder="8자 이상 입력해주세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="auth-input"
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">비밀번호 확인</label>
            <input
              type="password"
              placeholder="비밀번호를 다시 입력해주세요"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              className={`auth-input${confirm && password !== confirm ? ' input-error' : ''}`}
              required
            />
            {confirm && password !== confirm && (
              <p className="input-hint error">비밀번호가 일치하지 않습니다.</p>
            )}
            {confirm && password === confirm && (
              <p className="input-hint success">비밀번호가 일치합니다.</p>
            )}
          </div>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? '처리 중...' : '회원가입'}
          </button>
        </form>

        <p className="auth-footer">
          이미 계정이 있으신가요? <Link to="/login" className="auth-link">로그인</Link>
        </p>
      </div>
    </div>
  );
}