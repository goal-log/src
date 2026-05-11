import { useState, useEffect, useCallback } from 'react'
import { getDailyPlans, deleteDailyPlan } from '../api/dailyPlans'
import { getGoals } from '../api/goals'
import DateNavigator from '../components/DateNavigator'
import GoalFilter from '../components/GoalFilter'
import DailyPlanCard from '../components/DailyPlanCard'
import DailyPlanForm from '../components/DailyPlanForm'
import './DailyPlanPage.css'

function toISODate(date) {
  return date.toISOString().split('T')[0]
}

export default function DailyPlanPage() {
  const [selectedDate, setSelectedDate] = useState(new Date())
  const [goals, setGoals] = useState([])
  const [plans, setPlans] = useState([])
  const [selectedGoalId, setSelectedGoalId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    getGoals().then(setGoals).catch(() => {})
  }, [])

  const fetchPlans = useCallback(() => {
    setLoading(true)
    setError(null)
    getDailyPlans({ date: toISODate(selectedDate) })
      .then(setPlans)
      .catch(() => setError('데이터를 불러오지 못했습니다. 다시 시도해주세요.'))
      .finally(() => setLoading(false))
  }, [selectedDate])

  useEffect(() => {
    fetchPlans()
  }, [fetchPlans])

  function handleDateChange(newDate) {
    setSelectedDate(newDate)
    setSelectedGoalId(null)
  }

  function handlePlanCreated(plan) {
    setPlans((prev) => [...prev, plan])
    setShowForm(false)
  }

  function handleDeletePlan(planId) {
    deleteDailyPlan(planId)
      .then(() => setPlans((prev) => prev.filter((p) => p.id !== planId)))
      .catch(() => {})
  }

  const filteredPlans = selectedGoalId
    ? plans.filter((p) => p.goalId === selectedGoalId)
    : plans

  return (
    <div className="page">
      <header className="page-header">
        <h1 className="page-title">하루 계획</h1>
      </header>

      <DateNavigator date={selectedDate} onChange={handleDateChange} />

      <div className="page-content">
        <div className="toolbar">
          <GoalFilter goals={goals} selectedGoalId={selectedGoalId} onChange={setSelectedGoalId} />
          <button className="add-plan-btn" onClick={() => setShowForm(true)}>
            + 계획 추가
          </button>
        </div>

        {loading && <p className="page-status">불러오는 중...</p>}

        {error && (
          <div className="page-error">
            <p>{error}</p>
            <button className="retry-btn" onClick={fetchPlans}>다시 시도</button>
          </div>
        )}

        {!loading && !error && filteredPlans.length === 0 && (
          <div className="empty-state">
            <p>{selectedGoalId ? '해당 목표에 연결된 계획이 없습니다.' : '등록된 계획이 없습니다.'}</p>
            {!selectedGoalId && (
              <button className="add-plan-btn-empty" onClick={() => setShowForm(true)}>
                + 첫 계획 추가하기
              </button>
            )}
          </div>
        )}

        <div className="plan-list">
          {filteredPlans.map((plan) => (
            <DailyPlanCard key={plan.id} plan={plan} onDelete={handleDeletePlan} />
          ))}
        </div>
      </div>

      {showForm && (
        <DailyPlanForm
          date={toISODate(selectedDate)}
          onCreated={handlePlanCreated}
          onClose={() => setShowForm(false)}
        />
      )}
    </div>
  )
}
