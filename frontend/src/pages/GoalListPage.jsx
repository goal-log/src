import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { getGoals, createGoal, updateGoal, deleteGoal } from '../api/goals';
import { MOCK_GOALS, MOCK_PROGRESS, CATEGORIES, PRIORITIES, STATUSES } from '../api/mock';
import './GoalListPage.css';

const MOCK_MODE = false;

const EMPTY_FORM = {
  title: '', description: '', deadline: '',
  category: '학습', priority: 'MEDIUM', status: 'IN_PROGRESS',
};

const PRIORITY_LABEL = { HIGH: '높음', MEDIUM: '보통', LOW: '낮음' };
const STATUS_LABEL   = { IN_PROGRESS: '진행중', COMPLETED: '완료', PAUSED: '중단' };
const SORT_OPTIONS   = [
  { value: 'createdAt', label: '최신순' },
  { value: 'deadline',  label: '마감일순' },
  { value: 'priority',  label: '우선순위순' },
  { value: 'progress',  label: '진행률순' },
];
const PRIORITY_ORDER = { HIGH: 0, MEDIUM: 1, LOW: 2 };

function getDday(deadline) {
  if (!deadline) return null;
  const diff = Math.ceil((new Date(deadline) - new Date()) / 86400000);
  return diff;
}

function DdayBadge({ deadline }) {
  const d = getDday(deadline);
  if (d === null) return <span className="dday no-date">마감일 없음</span>;
  if (d < 0)  return <span className="dday done">완료됨</span>;
  if (d === 0) return <span className="dday urgent">D-Day</span>;
  if (d <= 7)  return <span className="dday urgent">D-{d}</span>;
  if (d <= 30) return <span className="dday soon">D-{d}</span>;
  return <span className="dday normal">D-{d}</span>;
}

const CIRCUMFERENCE = 2 * Math.PI * 20; // r=20

function ProgressRing({ percent, status }) {
  const offset = CIRCUMFERENCE - (percent / 100) * CIRCUMFERENCE;
  const cls = status === 'COMPLETED' ? 'completed' : status === 'PAUSED' ? 'paused' : '';
  return (
    <div className="ring-wrap">
      <svg width="52" height="52" viewBox="0 0 52 52">
        <circle className="ring-bg" cx="26" cy="26" r="20" />
        <circle
          className={`ring-fill ${cls}`}
          cx="26" cy="26" r="20"
          strokeDasharray={CIRCUMFERENCE}
          strokeDashoffset={offset}
        />
      </svg>
      <span className="ring-label">{percent}%</span>
    </div>
  );
}

export default function GoalListPage() {
  const navigate = useNavigate();
  const [goals, setGoals] = useState([]);
  const [progressMap, setProgressMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState(EMPTY_FORM);
  const [filterCategory, setFilterCategory] = useState('전체');
  const [filterStatus, setFilterStatus] = useState('전체');
  const [sortBy, setSortBy] = useState('createdAt');

  useEffect(() => {
    if (MOCK_MODE) {
      setGoals(MOCK_GOALS);
      setProgressMap(MOCK_PROGRESS);
      setLoading(false);
      return;
    }
    fetchGoals();
  }, []);

  async function fetchGoals() {
    try {
      const data = await getGoals();
      setGoals(Array.isArray(data) ? data : []);
    } catch {
      navigate('/login');
    } finally {
      setLoading(false);
    }
  }

  // 통계
  const stats = useMemo(() => {
    const total = goals.length;
    const completed = goals.filter(g => g.status === 'COMPLETED').length;
    const inProgress = goals.filter(g => g.status === 'IN_PROGRESS').length;
    const avgProgress = total === 0 ? 0 :
      Math.round(Object.values(progressMap).reduce((s, p) => s + p.progressPercent, 0) / total);
    return { total, completed, inProgress, avgProgress };
  }, [goals, progressMap]);

  // 필터 + 정렬
  const filtered = useMemo(() => {
    let list = [...goals];
    if (filterCategory !== '전체') list = list.filter(g => g.category === filterCategory);
    if (filterStatus !== '전체') list = list.filter(g => g.status === filterStatus);
    list.sort((a, b) => {
      if (sortBy === 'deadline') {
        if (!a.deadline) return 1;
        if (!b.deadline) return -1;
        return new Date(a.deadline) - new Date(b.deadline);
      }
      if (sortBy === 'priority') return PRIORITY_ORDER[a.priority] - PRIORITY_ORDER[b.priority];
      if (sortBy === 'progress') {
        const pa = progressMap[a.id]?.progressPercent ?? 0;
        const pb = progressMap[b.id]?.progressPercent ?? 0;
        return pb - pa;
      }
      return new Date(b.createdAt) - new Date(a.createdAt);
    });
    return list;
  }, [goals, filterCategory, filterStatus, sortBy, progressMap]);

  async function handleCreate(e) {
    e.preventDefault();
    setFormError('');
    if (!form.title.trim()) { setFormError('제목을 입력해주세요.'); return; }
    try {
      const newGoal = await createGoal(form);
      setGoals([...goals, newGoal]);
      setProgressMap({ ...progressMap, [newGoal.id]: { totalTasks: 0, completedTasks: 0, progressPercent: 0 } });
      setForm(EMPTY_FORM);
      setShowForm(false);
    } catch {
      setFormError('목표 저장에 실패했습니다.');
    }
  }

  function startEdit(goal) {
    setEditingId(goal.id);
    setEditForm({
      title: goal.title, description: goal.description || '',
      deadline: goal.deadline || '', category: goal.category || '학습',
      priority: goal.priority || 'MEDIUM', status: goal.status || 'IN_PROGRESS',
    });
  }

  async function handleUpdate(e, id) {
    e.preventDefault();
    if (!editForm.title.trim()) return;
    try {
      const updated = await updateGoal(id, editForm);
      setGoals(goals.map(g => g.id === id ? updated : g));
      setEditingId(null);
    } catch {
      setFormError('목표 수정에 실패했습니다.');
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('목표를 삭제할까요?')) return;
    try {
      await deleteGoal(id);
      setGoals(goals.filter(g => g.id !== id));
    } catch {
      alert('목표 삭제에 실패했습니다.');
    }
  }

  if (loading) return <div className="loading">불러오는 중...</div>;

  return (
    <div className="page-layout">

      {/* 헤더 */}
      <div className="page-header">
        <h1>🎯 장기 목표</h1>
        <div className="header-actions">
          <button className="btn-outline" onClick={() => { localStorage.removeItem('token'); navigate('/login'); }}>
            로그아웃
          </button>
          <button className="btn-add" onClick={() => { setShowForm(true); setFormError(''); }}>
            + 목표 추가
          </button>
        </div>
      </div>

      {/* 통계 */}
      <div className="stats-row">
        <div className="stat-card accent-blue">
          <span className="stat-label">전체 목표</span>
          <span className="stat-value">{stats.total}</span>
          <span className="stat-sub">개</span>
        </div>
        <div className="stat-card accent-green">
          <span className="stat-label">완료</span>
          <span className="stat-value">{stats.completed}</span>
          <span className="stat-sub">개</span>
        </div>
        <div className="stat-card accent-orange">
          <span className="stat-label">진행중</span>
          <span className="stat-value">{stats.inProgress}</span>
          <span className="stat-sub">개</span>
        </div>
        <div className="stat-card accent-red">
          <span className="stat-label">평균 진행률</span>
          <span className="stat-value">{stats.avgProgress}%</span>
          <span className="stat-sub">전체 평균</span>
        </div>
      </div>

      {/* 목표 생성 폼 */}
      {showForm && (
        <div className="goal-form-card">
          <h2>새 목표 만들기</h2>
          <form onSubmit={handleCreate}>
            <div className="form-grid">
              <div className="form-group full">
                <label>제목 *</label>
                <input
                  type="text"
                  value={form.title}
                  onChange={e => setForm({ ...form, title: e.target.value })}
                  placeholder="달성하고 싶은 목표를 입력하세요"
                />
              </div>
              <div className="form-group full">
                <label>설명</label>
                <textarea
                  rows={2}
                  value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                  placeholder="목표에 대한 구체적인 설명 (선택)"
                />
              </div>
              <div className="form-group">
                <label>카테고리</label>
                <select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}>
                  {CATEGORIES.filter(c => c !== '전체').map(c => <option key={c}>{c}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>마감일</label>
                <input type="date" value={form.deadline} onChange={e => setForm({ ...form, deadline: e.target.value })} />
              </div>
              <div className="form-group">
                <label>우선순위</label>
                <select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
                  {PRIORITIES.map(p => <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>상태</label>
                <select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                  {STATUSES.map(s => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
                </select>
              </div>
            </div>
            {formError && <p className="error-msg">{formError}</p>}
            <div className="form-actions">
              <button type="button" className="btn-cancel" onClick={() => { setShowForm(false); setForm(EMPTY_FORM); }}>취소</button>
              <button type="submit" className="btn-submit">저장</button>
            </div>
          </form>
        </div>
      )}

      {/* 툴바 */}
      <div className="toolbar">
        <div className="filter-tabs">
          {['전체', ...CATEGORIES.filter(c => c !== '전체')].map(c => (
            <button
              key={c}
              className={`filter-tab ${filterCategory === c ? 'active' : ''}`}
              onClick={() => setFilterCategory(c)}
            >{c}</button>
          ))}
        </div>
        <div className="toolbar-right">
          <select
            className="sort-select"
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
          >
            <option value="전체">전체 상태</option>
            {STATUSES.map(s => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
          </select>
          <select
            className="sort-select"
            value={sortBy}
            onChange={e => setSortBy(e.target.value)}
          >
            {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
      </div>

      {/* 목표 카드 그리드 */}
      <div className="goal-grid">
        {filtered.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon">🎯</div>
            <p>조건에 맞는 목표가 없어요</p>
          </div>
        )}

        {filtered.map(goal => {
          const prog = progressMap[goal.id] ?? { totalTasks: 0, completedTasks: 0, progressPercent: 0 };
          const progCls = goal.status === 'COMPLETED' ? 'completed' : goal.status === 'PAUSED' ? 'paused' : '';

          return editingId === goal.id ? (
            /* 인라인 편집 */
            <div className="goal-card" key={goal.id}>
              <form className="goal-edit-form" onSubmit={e => handleUpdate(e, goal.id)}>
                <input
                  type="text"
                  value={editForm.title}
                  onChange={e => setEditForm({ ...editForm, title: e.target.value })}
                  placeholder="제목"
                  required
                />
                <textarea
                  rows={2}
                  value={editForm.description}
                  onChange={e => setEditForm({ ...editForm, description: e.target.value })}
                  placeholder="설명"
                />
                <div className="edit-row">
                  <select value={editForm.category} onChange={e => setEditForm({ ...editForm, category: e.target.value })}>
                    {CATEGORIES.filter(c => c !== '전체').map(c => <option key={c}>{c}</option>)}
                  </select>
                  <input type="date" value={editForm.deadline} onChange={e => setEditForm({ ...editForm, deadline: e.target.value })} />
                </div>
                <div className="edit-row">
                  <select value={editForm.priority} onChange={e => setEditForm({ ...editForm, priority: e.target.value })}>
                    {PRIORITIES.map(p => <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>)}
                  </select>
                  <select value={editForm.status} onChange={e => setEditForm({ ...editForm, status: e.target.value })}>
                    {STATUSES.map(s => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
                  </select>
                </div>
                <div className="form-actions">
                  <button type="button" className="btn-cancel" onClick={() => setEditingId(null)}>취소</button>
                  <button type="submit" className="btn-submit">저장</button>
                </div>
              </form>
            </div>
          ) : (
            /* 일반 카드 */
            <div className={`goal-card status-${goal.status.toLowerCase()}`} key={goal.id}>
              <div className="card-top">
                <div className="card-badges">
                  <span className="badge-category">{goal.category}</span>
                  <span className={`badge-priority ${goal.priority}`}>{PRIORITY_LABEL[goal.priority]}</span>
                </div>
                <span className={`badge-status ${goal.status}`}>{STATUS_LABEL[goal.status]}</span>
              </div>

              <div className="card-body" onClick={() => navigate(`/goals/${goal.id}`)}>
                <h3>{goal.title}</h3>
                {goal.description && <p className="description">{goal.description}</p>}
              </div>

              <div className="card-progress">
                <ProgressRing percent={prog.progressPercent} status={goal.status} />
                <div className="progress-info">
                  <p className="progress-pct">{prog.progressPercent}% 달성</p>
                  <div className="progress-bar-track">
                    <div className={`progress-bar-fill ${progCls}`} style={{ width: `${prog.progressPercent}%` }} />
                  </div>
                  <p className="task-count">{prog.completedTasks} / {prog.totalTasks} 태스크</p>
                </div>
              </div>

              <div className="card-footer">
                <DdayBadge deadline={goal.deadline} />
                <div className="card-actions">
                  <button className="btn-icon edit" onClick={() => startEdit(goal)}>수정</button>
                  <button className="btn-icon delete" onClick={() => handleDelete(goal.id)}>삭제</button>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
