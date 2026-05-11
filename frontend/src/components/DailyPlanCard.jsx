import { useState, useEffect } from 'react'
import { getTasks, createTask, completeTask, deleteTask } from '../api/tasks'
import ProgressBar from './ProgressBar'
import TaskItem from './TaskItem'
import './DailyPlanCard.css'

export default function DailyPlanCard({ plan, onDelete }) {
  const [tasks, setTasks] = useState([])
  const [tasksLoaded, setTasksLoaded] = useState(false)
  const [newTitle, setNewTitle] = useState('')
  const [adding, setAdding] = useState(false)
  const [taskError, setTaskError] = useState(null)

  useEffect(() => {
    getTasks({ dailyPlanId: plan.id })
      .then((data) => {
        setTasks(data)
        setTasksLoaded(true)
      })
      .catch(() => setTaskError('Task를 불러오지 못했습니다.'))
  }, [plan.id])

  const pendingTasks = tasks.filter((t) => !t.completed)
  const doneTasks = tasks.filter((t) => t.completed)

  // plan.taskCount / completedCount 로 초기 프로그레스 표시, 로드 후 실제 값으로 전환
  const progressDone = tasksLoaded ? doneTasks.length : (plan.completedCount ?? 0)
  const progressTotal = tasksLoaded ? tasks.length : (plan.taskCount ?? 0)
  const isAllDone = tasksLoaded && tasks.length > 0 && doneTasks.length === tasks.length

  async function handleComplete(taskId) {
    try {
      await completeTask(taskId)
      setTasks((prev) => prev.map((t) => t.id === taskId ? { ...t, completed: true } : t))
    } catch {
      // 서버 오류 시 UI 유지
    }
  }

  async function handleDeleteTask(taskId) {
    try {
      await deleteTask(taskId)
      setTasks((prev) => prev.filter((t) => t.id !== taskId))
    } catch {
      // 서버 오류 시 UI 유지
    }
  }

  async function handleAddTask(e) {
    e.preventDefault()
    const title = newTitle.trim()
    if (!title) return
    setAdding(true)
    try {
      const task = await createTask({ dailyPlanId: plan.id, title })
      setTasks((prev) => [...prev, task])
      setNewTitle('')
    } catch {
      // 서버 오류 시 UI 유지
    } finally {
      setAdding(false)
    }
  }

  return (
    <div className={`plan-card${isAllDone ? ' all-done' : ''}`}>
      <div className="plan-card-header">
        {plan.goalTitle && <span className="goal-badge">{plan.goalTitle}</span>}
        <div className="header-right">
          {isAllDone && <span className="done-badge">완료</span>}
          <button className="plan-delete-btn" onClick={() => onDelete(plan.id)}>
            삭제
          </button>
        </div>
      </div>

      <h3 className="plan-title">{plan.title}</h3>

      <ProgressBar done={progressDone} total={progressTotal} />

      <div className="task-section">
        {taskError && <p className="task-msg error">{taskError}</p>}
        {tasksLoaded && !taskError && tasks.length === 0 && (
          <p className="task-msg">등록된 Task가 없습니다.</p>
        )}
        {pendingTasks.map((t) => (
          <TaskItem key={t.id} task={t} onComplete={handleComplete} onDelete={handleDeleteTask} />
        ))}
        {pendingTasks.length > 0 && doneTasks.length > 0 && (
          <div className="task-divider" />
        )}
        {doneTasks.map((t) => (
          <TaskItem key={t.id} task={t} onComplete={handleComplete} onDelete={handleDeleteTask} />
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
