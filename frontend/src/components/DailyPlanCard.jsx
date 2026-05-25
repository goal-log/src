import { useState, useEffect } from 'react';
import { createTask, toggleTask, deleteTask } from '../api/tasks';
import ProgressBar from './ProgressBar';
import TaskItem from './TaskItem';
import './DailyPlanCard.css';

export default function DailyPlanCard({ plan, goalTitle, onDelete }) {
  const [tasks, setTasks] = useState(plan?.tasks || []);
  const [newTitle, setNewTitle] = useState('');
  const [adding, setAdding] = useState(false);
  const [taskError, setTaskError] = useState('');

  useEffect(() => {
    setTasks(plan?.tasks || []);
  }, [plan?.id]);

  const pendingTasks = tasks.filter((t) => !t.completed);
  const doneTasks = tasks.filter((t) => t.completed);
  const isAllDone = tasks.length > 0 && doneTasks.length === tasks.length;

  async function handleToggle(taskId) {
    try {
      const updated = await toggleTask(taskId);
      setTasks((prev) =>
        prev.map((t) => (t.id === taskId ? { ...t, completed: updated.completed } : t))
      );
    } catch {
      setTaskError('상태 변경에 실패했습니다.');
    }
  }

  async function handleDeleteTask(taskId) {
    try {
      await deleteTask(taskId);
      setTasks((prev) => prev.filter((t) => t.id !== taskId));
    } catch {
      setTaskError('삭제에 실패했습니다.');
    }
  }

  async function handleAddTask(e) {
    e.preventDefault();
    const title = newTitle.trim();
    if (!title) return;
    setAdding(true);
    setTaskError('');
    try {
      const task = await createTask(plan.id, title);
      setTasks((prev) => [...prev, task]);
      setNewTitle('');
    } catch {
      setTaskError('추가에 실패했습니다.');
    } finally {
      setAdding(false);
    }
  }

  return (
    <div className={`plan-card${isAllDone ? ' all-done' : ''}`}>
      <div className="plan-card-header">
        {goalTitle
          ? <span className="goal-badge">🎯 {goalTitle}</span>
          : <span className="no-goal-badge">오늘의 할 일</span>
        }
        <div className="header-right">
          {isAllDone && <span className="done-badge">✓ 완료</span>}
          <button className="plan-delete-btn" onClick={onDelete}>삭제</button>
        </div>
      </div>

      <ProgressBar done={doneTasks.length} total={tasks.length} />

      <div className="task-section">
        {tasks.length === 0 && (
          <p className="task-msg">아래에서 할 일을 추가해보세요.</p>
        )}
        {pendingTasks.map((t) => (
          <TaskItem key={t.id} task={t} onComplete={handleToggle} onDelete={handleDeleteTask} />
        ))}
        {pendingTasks.length > 0 && doneTasks.length > 0 && (
          <div className="task-divider" />
        )}
        {doneTasks.map((t) => (
          <TaskItem key={t.id} task={t} onComplete={handleToggle} onDelete={handleDeleteTask} />
        ))}
      </div>

      {taskError && <p className="task-msg error">{taskError}</p>}

      <form className="task-add-form" onSubmit={handleAddTask}>
        <input
          type="text"
          placeholder="할 일 추가..."
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          className="task-add-input"
        />
        <button type="submit" className="task-add-btn" disabled={adding || !newTitle.trim()}>
          추가
        </button>
      </form>
    </div>
  );
}