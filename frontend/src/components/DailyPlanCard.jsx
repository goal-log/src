import { useState } from 'react'
import { createTask, toggleTask, deleteTask } from '../api/tasks'
import ProgressBar from './ProgressBar'
import TaskItem from './TaskItem'
import './DailyPlanCard.css'

export default function DailyPlanCard({ plan, onDelete }) {
  const [tasks, setTasks] = useState(plan.tasks ?? [])
  const [newTitle, setNewTitle] = useState('')
  const [adding, setAdding] = useState(false)
  const [taskError, setTaskError] = useState(null)

  const pendingTasks = tasks.filter((t) => !t.completed)
  const doneTasks = tasks.filter((t) => t.completed)
  const isAllDone = tasks.length > 0 && doneTasks.length === tasks.length

  async function handleToggle(taskId) {
    try {
      const updated = await toggleTask(taskId)
      setTasks((prev) => prev.map((t) => t.id === taskId ? { ...t, completed: updated.completed } : t))
    } catch {
      setTaskError('상태 변경에 실패했습니다.')
    }
  }

  async function handleDeleteTask(taskId) {
    try {
      await deleteTask(taskId)
      setTasks((prev) => prev.filter((t) => t.id !== taskId))
    } catch {
      setTaskError('삭제에 실패했습니다.')
    }
  }

  async function handleAddTask(e) {
    e.preventDefault()
    const title = newTitle.trim()
    if (!title) return
    setAdding(true)
    setTaskError(null)
    try {
      const task = await createTask(plan.id, title)
      setTasks((prev) => [...prev, task])
      setNewTitle('')
    } catch {
      setTaskError('Task 추가에 실패했습니다.')
    } finally {
      setAdding(false)
    }
  }

  return (
    <div className={`plan-card${isAllDone ? ' all-done' : ''}`}>
      <div className="plan-card-header">
        <span className="plan-date">{plan.date}</span>
        <div className="header-right">
          {isAllDone && <span className="done-badge">완료</span>}
          <button className="plan-delete-btn" onClick={onDelete}>삭제</button>
        </div>
      </div>

      <ProgressBar done={doneTasks.length} total={tasks.length} />

      <div className="task-section">
        {taskError && <p className="task-msg error">{taskError}</p>}
        {tasks.length === 0 && (
          <p className="task-msg">등록된 Task가 없습니다.</p>
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

      <form className="task-add-form" onSubmit={handleAddTask}>
        <input
          type="text"
          placeholder="Task 추가..."
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          className="task-add-input"
        />
        <button type="submit" className="task-add-btn" disabled={adding || !newTitle.trim()}>
          추가
        </button>
      </form>
    </div>
  )
}