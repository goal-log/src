package goalLog.example.goallog.domain.plan.controller;

import goalLog.example.goallog.domain.plan.dto.PlanCreateRequest;
import goalLog.example.goallog.domain.plan.dto.PlanResponse;
import goalLog.example.goallog.domain.plan.service.PlanService;
import goalLog.example.goallog.domain.task.dto.TaskCreateRequest;
import goalLog.example.goallog.domain.task.dto.TaskResponse;
import goalLog.example.goallog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public ApiResponse<PlanResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PlanCreateRequest request) {
        return ApiResponse.success(planService.create(userDetails.getUsername(), request));
    }

    @GetMapping
    public ApiResponse<PlanResponse> getByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(planService.getByDate(userDetails.getUsername(), date));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanResponse> getOne(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success(planService.getById(userDetails.getUsername(), id));
    }

    @GetMapping("/all")
    public ApiResponse<List<PlanResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(planService.getAll(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        planService.delete(userDetails.getUsername(), id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{planId}/tasks")
    public ApiResponse<TaskResponse> addTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long planId,
            @RequestBody TaskCreateRequest request) {
        return ApiResponse.success(planService.addTask(userDetails.getUsername(), planId, request));
    }

    // 플랜의 태스크 목록 조회
    @GetMapping("/{planId}/tasks")
    public ApiResponse<List<TaskResponse>> getTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long planId) {
        return ApiResponse.success(planService.getTasks(userDetails.getUsername(), planId));
    }

}