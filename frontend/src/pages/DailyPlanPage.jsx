import { useState, useEffect, useCallback } from 'react'
import { getPlanByDate, createPlan, deletePlan } from '../api/dailyPlans'
import DateNavigator from '../components/DateNavigator'
import DailyPlanCard from '../components/DailyPlanCard'
import './DailyPlanPage.css'

function toISODate(date) {
  return date.toISOString().split('T')[0]
}

export default function DailyPlanPage() {
  const [selectedDate, setSelectedDate] = useState(new Date())
  const [plan, setPlan] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetchPlan = useCallback(() => {
    setLoading(true)
    setError(null)
    getPlanByDate(toISODate(selectedDate))
      .then(setPlan)
      .catch((err) => {
        if (err.status === 404) {
          setPlan(null)
        } else {
          setError('데이터를 불러오지 못했습니다.')
        }
      })
      .finally(() => setLoading(false))
  }, [selectedDate])

  useEffect(() => {
    fetchPlan()
  }, [fetchPlan])

  function handleDateChange(newDate) {
    setSelectedDate(newDate)
  }

  async function handleStart() {
    try {
      const newPlan = await createPlan(toISODate(selectedDate))
      setPlan(newPlan)
    } catch {
      setError('계획 생성에 실패했습니다.')
    }
  }

  async function handleDelete() {
    if (!plan) return
    try {
      await deletePlan(plan.id)
      setPlan(null)
    } catch {
      setError('삭제에 실패했습니다.')
    }
  }

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

        {!loading && !error && !plan && (
          <div className="empty-state">
            <p>등록된 계획이 없습니다.</p>
            <button className="add-plan-btn-empty" onClick={handleStart}>
              + 오늘의 계획 시작하기
            </button>
          </div>
        )}

        {!loading && !error && plan && (
          <DailyPlanCard plan={plan} onDelete={handleDelete} />
        )}
      </div>
    </div>
  )
}