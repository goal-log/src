import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import ProgressBar from '../ProgressBar'

describe('Sprint 4 — ProgressBar 컴포넌트', () => {
  it('done/total 비율을 정수 % 로 렌더링한다', () => {
    render(<ProgressBar done={1} total={4} />)
    expect(screen.getByText('25%')).toBeInTheDocument()
  })

  it('100% 완료 시 complete 클래스를 부여한다', () => {
    const { container } = render(<ProgressBar done={3} total={3} />)
    expect(screen.getByText('100%')).toHaveClass('complete')
    expect(container.querySelector('.progress-bar-fill')).toHaveClass('complete')
  })

  it('total=0 일 때 0% 로 안전하게 처리한다', () => {
    render(<ProgressBar done={0} total={0} />)
    expect(screen.getByText('0%')).toBeInTheDocument()
  })

  it('33.3% 같은 소수는 반올림한다 (1/3 → 33%)', () => {
    render(<ProgressBar done={1} total={3} />)
    expect(screen.getByText('33%')).toBeInTheDocument()
  })
})
