package goalLog.example.goallog.domain.goal.controller;

import goalLog.example.goallog.domain.goal.dto.*;
import goalLog.example.goallog.domain.goal.service.GoalService;
import goalLog.example.goallog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ApiResponse<GoalResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody GoalCreateRequest request) {
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
}