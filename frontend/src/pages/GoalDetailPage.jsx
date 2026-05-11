import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getGoal, getGoalProgress } from '../api/goals';
import { MOCK_GOALS, MOCK_PROGRESS } from '../api/mock';
import './GoalDetailPage.css';

const MOCK_MODE = true;

const PRIORITY_LABEL = { HIGH: '높음', MEDIUM: '보통', LOW: '낮음' };
const STATUS_LABEL   = { IN_PROGRESS: '진행중', COMPLETED: '완료', PAUSED: '중단' };
const BIG_CIRCUMFERENCE = 2 * Math.PI * 40; // r=40

export default function GoalDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [goal, setGoal] = useState(null);
  const [progress, setProgress] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (MOCK_MODE) {
      const found = MOCK_GOALS.find(g => g.id === Number(id));
      if (!found) { navigate('/goals'); return; }
      setGoal(found);
      setProgress(MOCK_PROGRESS[Number(id)] ?? { totalTasks: 0, completedTasks: 0, progressPercent: 0 });
      setLoading(false);
      return;
    }
    async function fetchData() {
      try {
        const [goalRes, progressRes] = await Promise.all([getGoal(id), getGoalProgress(id)]);
        setGoal(goalRes.data.data);
        setProgress(progressRes.data.data);
      } catch {
        navigate('/goals');
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [id, navigate]);

  if (loading) return <div className="loading">불러오는 중...</div>;
  if (!goal) return null;

  const pct = progress?.progressPercent ?? 0;
  const offset = BIG_CIRCUMFERENCE - (pct / 100) * BIG_CIRCUMFERENCE;
  const progCls = goal.status === 'COMPLETED' ? 'completed' : goal.status === 'PAUSED' ? 'paused' : '';

  return (
    <div className="detail-layout">
      <button className="back-btn" onClick={() => navigate('/goals')}>← 목표 목록</button>

      {/* 메타 카드 */}
      <div className="detail-meta-card">
        <div className="detail-meta-top">
          <div className="detail-badges">
            {goal.category && <span className="badge-category">{goal.category}</span>}
            {goal.priority && (
              <span className={`badge-priority ${goal.priority}`}>{PRIORITY_LABEL[goal.priority]}</span>
            )}
          </div>
          {goal.status && (
            <span className={`badge-status ${goal.status}`}>{STATUS_LABEL[goal.status]}</span>
          )}
        </div>
        <h1>{goal.title}</h1>
        {goal.description && <p className="detail-description">{goal.description}</p>}
        {goal.deadline && (
          <p className="detail-deadline">📅 마감일: {goal.deadline}</p>
        )}
      </div>

      {/* 진행률 카드 */}
      {progress && (
        <div className="detail-progress-card">
          <h2>진행률</h2>
          <div className="progress-main">
            {/* 큰 링 */}
            <div className="big-ring-wrap">
              <svg width="100" height="100" viewBox="0 0 100 100">
                <circle className="big-ring-bg" cx="50" cy="50" r="40" />
                <circle
                  className={`big-ring-fill ${progCls}`}
                  cx="50" cy="50" r="40"
                  strokeDasharray={BIG_CIRCUMFERENCE}
                  strokeDashoffset={offset}
                />
              </svg>
              <div className="big-ring-label">
                {pct}%<span>달성</span>
              </div>
            </div>

            <div className="progress-detail">
              <div className="progress-bar-track">
                <div className={`progress-bar-fill ${progCls}`} style={{ width: `${pct}%` }} />
              </div>
              <div className="progress-numbers">
                <div className="prog-num">
                  <span className="num">{progress.completedTasks}</span>
                  <span className="lbl">완료 태스크</span>
                </div>
                <div className="prog-num">
                  <span className="num">{progress.totalTasks - progress.completedTasks}</span>
                  <span className="lbl">남은 태스크</span>
                </div>
                <div className="prog-num">
                  <span className="num">{progress.totalTasks}</span>
                  <span className="lbl">전체 태스크</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
