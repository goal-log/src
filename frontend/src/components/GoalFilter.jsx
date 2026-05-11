import './GoalFilter.css'

export default function GoalFilter({ goals, selectedGoalId, onChange }) {
  return (
    <div className="goal-filter">
      <button
        className={`filter-tab${selectedGoalId === null ? ' active' : ''}`}
        onClick={() => onChange(null)}
      >
        전체
      </button>
      {goals.map((g) => (
        <button
          key={g.id}
          className={`filter-tab${selectedGoalId === g.id ? ' active' : ''}`}
          onClick={() => onChange(g.id)}
        >
          {g.title}
        </button>
      ))}
    </div>
  )
}
