import './TaskItem.css'

export default function TaskItem({ task, onComplete, onDelete }) {
  return (
    <div className={`task-item${task.completed ? ' done' : ''}`}>
      <button
        className="task-check"
        onClick={() => onComplete(task.id)}
        title={task.completed ? '완료 취소' : '완료 처리'}
      >
        {task.completed ? '✓' : ''}
      </button>
      <span className="task-title">{task.title}</span>
      <button className="task-delete" onClick={() => onDelete(task.id)} title="삭제">
        ✕
      </button>
    </div>
  )
}
