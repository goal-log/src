package goalLog.example.goallog.domain.task.controller;

import goalLog.example.goallog.domain.task.dto.TaskResponse;
import goalLog.example.goallog.domain.task.service.TaskService;
import goalLog.example.goallog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// [GRASP: Controller] - 시스템 이벤트(HTTP 요청)를 수신하여 TaskService에 위임한다.
// [GRASP: Don't Talk to Strangers] - Repository, Entity를 직접 참조하지 않고 TaskService에만 의존한다.
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