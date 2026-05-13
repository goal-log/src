import { useState } from 'react';
import { createPlan } from '../api/dailyPlans';
import './DailyPlanForm.css';

export default function DailyPlanForm({ date, goals, onCreated, onClose, prefilledGoalId }) {
  const [goalId, setGoalId] = useState(prefilledGoalId || (goals.length > 0 ? goals[0].id : ''));
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitError(null);
    setSubmitting(true);
    try {
      const body = { date };
      if (!goalId) {
        setSubmitError('목표를 선택해주세요');
        setSubmitting(false);
        return;
      }
      body.longTermGoalId = Number(goalId);
      const plan = await createPlan(body);
      onCreated(plan);
    } catch (err) {
      setSubmitError(err.response?.data?.message || '저장에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>오늘의 계획 만들기</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="plan-form">
          <label className="form-label">
            연결할 목표
            <select
              value={goalId}
              onChange={(e) => setGoalId(e.target.value)}
              className="form-select"
              disabled={!!prefilledGoalId}
            >
              <option value="" disabled>목표를 선택해주세요</option>
              {goals.map((g) => (
                <option key={g.id} value={g.id}>{g.title}</option>
              ))}
            </select>
          </label>

          {submitError && <p className="submit-error">{submitError}</p>}

          <div className="form-actions">
            <button type="button" className="btn-cancel" onClick={onClose}>취소</button>
            <button type="submit" className="btn-submit" disabled={submitting}>
              {submitting ? '만드는 중...' : '계획 만들기'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
