import { useState } from 'react';
import { createPlan } from '../api/dailyPlans';
import { createTask } from '../api/tasks';
import './DailyPlanForm.css';

export default function DailyPlanForm({ date, goals, onCreated, onClose, prefilledGoalId }) {
  const [goalId, setGoalId] = useState(prefilledGoalId || '');
  const [taskInput, setTaskInput] = useState('');
  const [tasks, setTasks] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  function handleAddTask(e) {
    e.preventDefault();
    const title = taskInput.trim();
    if (!title) return;
    setTasks((prev) => [...prev, title]);
    setTaskInput('');
  }

  function handleRemoveTask(idx) {
    setTasks((prev) => prev.filter((_, i) => i !== idx));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitError(null);
    setSubmitting(true);
    try {
      const body = { date };
      if (goalId) body.longTermGoalId = Number(goalId);
      const plan = await createPlan(body);
      const createdTasks = await Promise.all(tasks.map((title) => createTask(plan.id, title)));
      onCreated({ ...plan, tasks: [...(plan.tasks || []), ...createdTasks] });
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
            연결할 목표 <span className="optional">(선택)</span>
            <select
              value={goalId}
              onChange={(e) => setGoalId(e.target.value)}
              className="form-select"
              disabled={!!prefilledGoalId}
            >
              <option value="">목표 없이 계획하기</option>
              {goals.map((g) => (
                <option key={g.id} value={g.id}>{g.title}</option>
              ))}
            </select>
          </label>

          <div className="form-label">
            할 일 <span className="optional">(선택)</span>
            <div className="task-input-row">
              <input
                type="text"
                className="form-input"
                placeholder="할 일을 입력하세요"
                value={taskInput}
                onChange={(e) => setTaskInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleAddTask(e); } }}
              />
              <button type="button" className="task-add-inline-btn" onClick={handleAddTask}>추가</button>
            </div>

            {tasks.length > 0 && (
              <ul className="task-preview-list">
                {tasks.map((t, i) => (
                  <li key={i} className="task-preview-item">
                    <span className="task-preview-check">☐</span>
                    <span className="task-preview-title">{t}</span>
                    <button type="button" className="task-preview-remove" onClick={() => handleRemoveTask(i)}>✕</button>
                  </li>
                ))}
              </ul>
            )}
          </div>

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