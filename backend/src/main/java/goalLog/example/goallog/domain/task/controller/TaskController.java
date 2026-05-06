package goalLog.example.goallog.domain.task.controller;

import goalLog.example.goallog.domain.task.dto.TaskResponse;
import goalLog.example.goallog.domain.task.service.TaskService;
import goalLog.example.goallog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // 완료 상태 토글
    @PatchMapping("/{id}/toggle")
    public ApiResponse<TaskResponse> toggle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success(taskService.toggle(userDetails.getUsername(), id));
    }

    // 태스크 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        taskService.delete(userDetails.getUsername(), id);
        return ApiResponse.success(null);
    }
}