import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import GoalListPage from './pages/GoalListPage';
import GoalDetailPage from './pages/GoalDetailPage';
import DailyPlanPage from './pages/DailyPlanPage';

function PrivateRoute({ children }) {
  return localStorage.getItem('token') ? children : <Navigate to="/login" />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/goals" element={<PrivateRoute><GoalListPage /></PrivateRoute>} />
        <Route path="/goals/:id" element={<PrivateRoute><GoalDetailPage /></PrivateRoute>} />
        <Route path="/daily-plan" element={<PrivateRoute><DailyPlanPage /></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}