import { useState, useEffect } from 'react'
import { getGoals } from '../api/goals'
import { createDailyPlan } from '../api/dailyPlans'
import './DailyPlanForm.css'

export default function DailyPlanForm({ date, onCreated, onClose }) {
  const [goals, setGoals] = useState([])
  const [goalId, setGoalId] = useState('')
  const [title, setTitle] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState(null)

  useEffect(() => {
    getGoals().then(setGoals).catch(() => {})
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitError(null)
    setSubmitting(true)
    try {
      const body = { title: title.trim(), date }
      if (goalId) body.goalId = Number(goalId)
      const plan = await createDailyPlan(body)
      const selectedGoal = goals.find((g) => g.id === Number(goalId))
      onCreated({ ...plan, goalTitle: selectedGoal?.title ?? '' })
    } catch (err) {
      setSubmitError(err.message || '저장에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>오늘의 계획 추가</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="plan-form">
          <label className="form-label">
            연결할 목표 <span className="optional">(선택)</span>
            <select
              value={goalId}
              onChange={(e) => setGoalId(e.target.value)}
              className="form-select"
            >
              <option value="">목표 없음</option>
              {goals.map((g) => (
                <option key={g.id} value={g.id}>{g.title}</option>
              ))}
            </select>
          </label>

          <label className="form-label">
            계획 제목
            <input
              type="text"
              placeholder="예: 단어 100개 암기"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="form-input"
              required
            />
          </label>

          {submitError && <p className="submit-error">{submitError}</p>}

          <div className="form-actions">
            <button type="button" className="btn-cancel" onClick={onClose}>취소</button>
            <button type="submit" className="btn-submit" disabled={submitting}>
              {submitting ? '저장 중...' : '저장'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
