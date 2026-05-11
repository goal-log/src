import './DateNavigator.css'

function toISODate(date) {
  return date.toISOString().split('T')[0]
}

function isToday(date) {
  return toISODate(date) === toISODate(new Date())
}

function formatDisplay(date) {
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  })
}

function addDays(date, delta) {
  const d = new Date(date)
  d.setDate(d.getDate() + delta)
  return d
}

export default function DateNavigator({ date, onChange }) {
  return (
    <div className="date-navigator">
      <button className="nav-btn" onClick={() => onChange(addDays(date, -1))}>‹</button>
      <span className="date-label">
        {formatDisplay(date)}
        {isToday(date) && <span className="today-badge">오늘</span>}
      </span>
      <button className="nav-btn" onClick={() => onChange(addDays(date, 1))}>›</button>
    </div>
  )
}
