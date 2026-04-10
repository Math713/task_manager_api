package com.matheus.task_manager_api.controller;

import com.matheus.task_manager_api.dto.TaskRequest;
import com.matheus.task_manager_api.dto.TaskResponse;
import com.matheus.task_manager_api.dto.TaskUpdateRequest;
import com.matheus.task_manager_api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
@Tag(name = "Task", description = "Tasks Endpoints")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest taskRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(taskRequest));
    }

    @Operation(summary = "Get all tasks")
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getAll(pageable));
    }

    @Operation(summary = "Get task by id")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @Operation(summary = "Update task by id")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateById(@PathVariable Long id,
                                                   @Valid @RequestBody TaskUpdateRequest updateRequest) {

        return ResponseEntity.ok(taskService.update(id, updateRequest));
    }

    @Operation(summary = "Delete task by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}