import './ProgressBar.css'

export default function ProgressBar({ done, total }) {
  const percent = total > 0 ? Math.round((done / total) * 100) : 0
  const complete = percent === 100

  return (
    <div className="progress-bar-wrap">
      <div className="progress-bar-track">
        <div
          className={`progress-bar-fill${complete ? ' complete' : ''}`}
          style={{ width: `${percent}%` }}
        />
      </div>
      <span className={`progress-label${complete ? ' complete' : ''}`}>
        {percent}%
      </span>
    </div>
  )
}
