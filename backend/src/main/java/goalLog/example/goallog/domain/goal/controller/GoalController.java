package goalLog.example.goallog.domain.goal.controller;

import goalLog.example.goallog.domain.goal.dto.*;
import goalLog.example.goallog.domain.plan.dto.PlanResponse;
import goalLog.example.goallog.domain.goal.service.GoalService;
import goalLog.example.goallog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [GRASP: Controller] - 시스템 이벤트(HTTP 요청)를 수신하여 GoalService에 위임한다.
// [GRASP: Don't Talk to Strangers] - Repository, Entity를 직접 참조하지 않고 GoalService에만 의존한다.
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ApiResponse<GoalResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody GoalCreateRequest request) {
        // [GRASP: Don't Talk to Strangers] - Service 계층에만 메시지를 전달한다.
        return ApiResponse.success(goalService.create(userDetails.getUsername(), request));
    }

    @GetMapping
    public ApiResponse<List<GoalResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(goalService.getAll(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ApiResponse<GoalResponse> getOne(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success(goalService.getOne(userDetails.getUsername(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<GoalResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody GoalUpdateRequest request) {
        return ApiResponse.success(goalService.update(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        goalService.delete(userDetails.getUsername(), id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<GoalProgressResponse> getProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success(goalService.getProgress(userDetails.getUsername(), id));
    }

    @GetMapping("/{id}/plans")
    public ApiResponse<List<PlanResponse>> getPlans(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success(goalService.getPlans(userDetails.getUsername(), id));
    }
}