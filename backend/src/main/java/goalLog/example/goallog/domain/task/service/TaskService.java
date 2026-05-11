package goalLog.example.goallog.domain.task.service;

import goalLog.example.goallog.domain.task.dto.TaskResponse;
import goalLog.example.goallog.domain.task.entity.Task;
import goalLog.example.goallog.domain.task.repository.TaskRepository;
import goalLog.example.goallog.global.exception.CustomException;
import goalLog.example.goallog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    // 완료 상태 토글
    @Transactional
    public TaskResponse toggle(String email, Long taskId) {
        Task task = getTask(email, taskId);
        task.toggleCompleted();
        return new TaskResponse(task);
    }

    // 태스크 삭제
    @Transactional
    public void delete(String email, Long taskId) {
        Task task = getTask(email, taskId);
        taskRepository.delete(task);
    }

    // 태스크 조회 + 본인 소유 확인
    private Task getTask(String email, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        if (!task.getDailyPlan().getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return task;
    }
}