<<<<<<< HEAD
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import GoalListPage from './pages/GoalListPage';
import GoalDetailPage from './pages/GoalDetailPage';

// MOCK_MODE=true 이면 로그인 없이 모든 페이지 접근 가능
const MOCK_MODE = true;

function PrivateRoute({ children }) {
  if (MOCK_MODE) return children;
  return localStorage.getItem('token') ? children : <Navigate to="/login" />;
}
=======
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import DailyPlanPage from './pages/DailyPlanPage';
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
<<<<<<< HEAD
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/goals" element={<PrivateRoute><GoalListPage /></PrivateRoute>} />
        <Route path="/goals/:id" element={<PrivateRoute><GoalDetailPage /></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/goals" />} />
=======
        <Route path="/" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/daily-plan" element={<DailyPlanPage />} />
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb
      </Routes>
    </BrowserRouter>
  );
}
