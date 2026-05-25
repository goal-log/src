import { useState, useEffect, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getPlanByDate, deletePlan } from '../api/dailyPlans';
import { getGoals } from '../api/goals';
import Navbar from '../components/Navbar';
import DateNavigator from '../components/DateNavigator';
import DailyPlanCard from '../components/DailyPlanCard';
import DailyPlanForm from '../components/DailyPlanForm';
import './DailyPlanPage.css';

function toISODate(date) {
  return date.toISOString().split('T')[0];
}

export default function DailyPlanPage() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const prefilledGoalId = state?.prefilledGoalId;
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [plans, setPlans] = useState([]);
  const [goals, setGoals] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    getGoals()
      .then((res) => setGoals(res.data ?? []))
      .catch(() => navigate('/login'));
  }, []);

  const fetchPlans = useCallback(() => {
    setLoading(true);
    setError(null);
    getPlanByDate(toISODate(selectedDate))
      .then((data) => setPlans(data))
      .catch(() => setError('데이터를 불러오지 못했습니다. 다시 시도해주세요.'))
      .finally(() => setLoading(false));
  }, [selectedDate]);

  useEffect(() => {
    fetchPlans();
  }, [fetchPlans]);

  function handlePlanCreated(newPlan) {
    setPlans((prev) => [...prev, newPlan]);
    setShowForm(false);
  }

  function handleDeletePlan(planId) {
    if (!window.confirm('이 계획을 삭제할까요?')) return;
    deletePlan(planId)
      .then(() => setPlans((prev) => prev.filter((p) => p.id !== planId)))
      .catch(() => {});
  }

  function getGoalTitle(plan) {
    if (!plan?.longTermGoalId) return null;
    return goals.find((g) => g.id === plan.longTermGoalId)?.title ?? null;
  }

  return (
    <div className="app-layout">
      <Navbar />
      <div className="page">
        {prefilledGoalId && (
          <button className="back-btn" onClick={() => navigate(-1)}>← 뒤로가기</button>
        )}

        <DateNavigator date={selectedDate} onChange={setSelectedDate} />

        <div className="page-content">
          {loading && <p className="page-status">불러오는 중...</p>}

          {error && (
            <div className="page-error">
              <p>{error}</p>
              <button className="retry-btn" onClick={fetchPlans}>다시 시도</button>
            </div>
          )}

          {!loading && !error && plans.length === 0 && (
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <p className="empty-text">이 날의 계획이 없어요.</p>
              <p className="empty-sub">목표를 향한 오늘의 할 일을 만들어보세요.</p>
              <button className="add-plan-btn-empty" onClick={() => setShowForm(true)}>
                + 계획 만들기
              </button>
            </div>
          )}

          {!loading && !error && plans.map((plan) => (
            <DailyPlanCard
              key={plan.id}
              plan={plan}
              goalTitle={getGoalTitle(plan)}
              onDelete={() => handleDeletePlan(plan.id)}
            />
          ))}

          {!loading && !error && plans.length > 0 && (
            <button className="add-more-btn" onClick={() => setShowForm(true)}>
              + 다른 목표의 계획 추가
            </button>
          )}
        </div>

        {showForm && (
          <DailyPlanForm
            date={toISODate(selectedDate)}
            goals={goals}
            onCreated={handlePlanCreated}
            onClose={() => setShowForm(false)}
            prefilledGoalId={prefilledGoalId}
          />
        )}
      </div>
    </div>
  );
}