import { useState, useEffect, useRef } from 'react'
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

function CalendarPopup({ date, onChange, onClose }) {
  const [viewYear, setViewYear] = useState(date.getFullYear())
  const [viewMonth, setViewMonth] = useState(date.getMonth())

  function prevMonth() {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11) }
    else setViewMonth(m => m - 1)
  }
  function nextMonth() {
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0) }
    else setViewMonth(m => m + 1)
  }

  function buildDays() {
    const first = new Date(viewYear, viewMonth, 1)
    const last = new Date(viewYear, viewMonth + 1, 0)
    const days = []
    // 앞 빈칸 (일요일=0 기준)
    for (let i = 0; i < first.getDay(); i++) days.push(null)
    for (let d = 1; d <= last.getDate(); d++) days.push(new Date(viewYear, viewMonth, d))
    return days
  }

  const todayStr = toISODate(new Date())
  const selectedStr = toISODate(date)

  return (
    <div className="cal-popup">
      <div className="cal-header">
        <button className="cal-nav" onClick={prevMonth}>‹</button>
        <span className="cal-month-label">{viewYear}년 {viewMonth + 1}월</span>
        <button className="cal-nav" onClick={nextMonth}>›</button>
      </div>
      <div className="cal-weekdays">
        {['일','월','화','수','목','금','토'].map(d => (
          <span key={d} className="cal-wd">{d}</span>
        ))}
      </div>
      <div className="cal-grid">
        {buildDays().map((d, i) => {
          if (!d) return <span key={`e${i}`} />
          const ds = toISODate(d)
          const isSelected = ds === selectedStr
          const isTodayDay = ds === todayStr
          return (
            <button
              key={ds}
              className={`cal-day${isSelected ? ' selected' : ''}${isTodayDay ? ' today' : ''}`}
              onClick={() => { onChange(d); onClose() }}
            >
              {d.getDate()}
            </button>
          )
        })}
      </div>
      <div className="cal-footer">
        <button className="cal-today-btn" onClick={() => { onChange(new Date()); onClose() }}>
          오늘로 이동
        </button>
      </div>
    </div>
  )
}

export default function DateNavigator({ date, onChange }) {
  const [showCal, setShowCal] = useState(false)
  const ref = useRef(null)

  useEffect(() => {
    if (!showCal) return
    function handleClick(e) {
      if (ref.current && !ref.current.contains(e.target)) setShowCal(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [showCal])

  return (
    <div className="date-navigator-wrap" ref={ref}>
      <div className="date-navigator">
        <button className="nav-btn" onClick={() => onChange(addDays(date, -1))}>‹</button>
        <button className="date-label-btn" onClick={() => setShowCal(v => !v)}>
          <span className="date-label">
            {formatDisplay(date)}
            {isToday(date) && <span className="today-badge">오늘</span>}
          </span>
          <span className="cal-icon">📅</span>
        </button>
        <button className="nav-btn" onClick={() => onChange(addDays(date, 1))}>›</button>
      </div>
      {showCal && (
        <CalendarPopup date={date} onChange={onChange} onClose={() => setShowCal(false)} />
      )}
    </div>
  )
}