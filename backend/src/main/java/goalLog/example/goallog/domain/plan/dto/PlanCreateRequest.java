package goalLog.example.goallog.domain.plan.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PlanCreateRequest {

    private LocalDate date;
    private Long longTermGoalId; // nullable - 목표 없이 플랜만 만들 수도 있음
}