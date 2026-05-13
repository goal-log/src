import { useState, useEffect, useCallback } from 'react';
import { getPlanByDate, createPlan, deletePlan } from '../api/dailyPlans';
import { getGoals } from '../api/goals';
import DateNavigator from '../components/DateNavigator';
import DailyPlanCard from '../components/DailyPlanCard';
import DailyPlanForm from '../components/DailyPlanForm';
import './DailyPlanPage.css';

function toISODate(date) {
  return date.toISOString().split('T')[0];
}

export default function DailyPlanPage() {
  const [selectedDate, setSelectedDate] = useState(new Date());
  // undefined = 로딩 중, null = 해당 날짜에 plan 없음, object = plan 존재
  const [plan, setPlan] = useState(undefined);
  const [goals, setGoals] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    getGoals()
      .then((res) => setGoals(res.data.data ?? []))
      .catch(() => {});
  }, []);

  const fetchPlan = useCallback(() => {
    setLoading(true);
    setError(null);
    setPlan(undefined);
    getPlanByDate(toISODate(selectedDate))
      .then(setPlan)
      .catch(() => setError('데이터를 불러오지 못했습니다. 다시 시도해주세요.'))
      .finally(() => setLoading(false));
  }, [selectedDate]);

  useEffect(() => {
    fetchPlan();
  }, [fetchPlan]);

  function handleDateChange(newDate) {
    setSelectedDate(newDate);
  }

  function handlePlanCreated(newPlan) {
    setPlan(newPlan);
    setShowForm(false);
  }

  function handleDeletePlan() {
    deletePlan(plan.id)
      .then(() => setPlan(null))
      .catch(() => {});
  }

  const goalTitle = plan?.longTermGoalId
    ? (goals.find((g) => g.id === plan.longTermGoalId)?.title ?? null)
    : null;

  return (
    <div className="page">
      <header className="page-header">
        <h1 className="page-title">하루 계획</h1>
      </header>

      <DateNavigator date={selectedDate} onChange={handleDateChange} />

      <div className="page-content">
        {loading && <p className="page-status">불러오는 중...</p>}

        {error && (
          <div className="page-error">
            <p>{error}</p>
            <button className="retry-btn" onClick={fetchPlan}>다시 시도</button>
          </div>
        )}

        {!loading && !error && plan === null && (
          <div className="empty-state">
            <p>이 날의 계획이 없습니다.</p>
            <button className="add-plan-btn-empty" onClick={() => setShowForm(true)}>
              + 계획 만들기
            </button>
          </div>
        )}

        {!loading && !error && plan && (
          <DailyPlanCard
            plan={plan}
            goalTitle={goalTitle}
            onDelete={handleDeletePlan}
          />
        )}
      </div>

      {showForm && (
        <DailyPlanForm
          date={toISODate(selectedDate)}
          goals={goals}
          onCreated={handlePlanCreated}
          onClose={() => setShowForm(false)}
        />
      )}
    </div>
  );
}
