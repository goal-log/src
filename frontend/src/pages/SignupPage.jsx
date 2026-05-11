import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { signup } from '../api/auth';
<<<<<<< HEAD
import './LoginPage.css'; /* 동일한 auth 스타일 재사용 */
=======
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb

export default function SignupPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
<<<<<<< HEAD
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
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
        <h1>회원가입</h1>
        <p className="subtitle">GoalLog에 오신 것을 환영합니다</p>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>이메일</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="example@email.com"
              required
            />
          </div>
          <div className="form-group">
            <label>비밀번호</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="6자 이상 입력"
              minLength={6}
              required
            />
          </div>
          <button className="btn-primary" type="submit" disabled={loading}>
            {loading ? '처리 중...' : '회원가입'}
          </button>
          {error && <p className="error-msg">{error}</p>}
        </form>
        <p className="auth-footer">
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
=======
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (password !== confirm) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }
    try {
      await signup(email, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.');
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>GoalLog</h1>
        <p style={styles.subtitle}>새 계정을 만들어보세요</p>
        <form onSubmit={handleSubmit} style={styles.form}>
          <input
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={styles.input}
            required
          />
          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={styles.input}
            required
          />
          <input
            type="password"
            placeholder="비밀번호 확인"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            style={styles.input}
            required
          />
          {error && <p style={styles.error}>{error}</p>}
          <button type="submit" style={styles.button}>회원가입</button>
        </form>
        <p style={styles.link}>
          이미 계정이 있으신가요? <Link to="/">로그인</Link>
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb
        </p>
      </div>
    </div>
  );
}
<<<<<<< HEAD
=======

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f0f4f8',
  },
  card: {
    backgroundColor: '#fff',
    padding: '2.5rem',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
    width: '100%',
    maxWidth: '400px',
    textAlign: 'center',
  },
  title: {
    fontSize: '2rem',
    fontWeight: 'bold',
    color: '#4f46e5',
    marginBottom: '0.25rem',
    marginTop: '-0.5rem',
  },
  subtitle: {
    color: '#6b7280',
    marginBottom: '1.5rem',
    fontSize: '0.9rem',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.75rem',
  },
  input: {
    padding: '0.75rem 1rem',
    borderRadius: '8px',
    border: '1px solid #d1d5db',
    fontSize: '1rem',
    outline: 'none',
  },
  button: {
    padding: '0.75rem',
    backgroundColor: '#4f46e5',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '1rem',
    cursor: 'pointer',
    marginTop: '0.25rem',
  },
  error: {
    color: '#ef4444',
    fontSize: '0.875rem',
    margin: 0,
  },
  link: {
    marginTop: '1.25rem',
    color: '#6b7280',
    fontSize: '0.9rem',
  },
};
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb
