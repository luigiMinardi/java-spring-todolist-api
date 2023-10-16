package com.lugmin.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lugmin.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || taskModel.getEndAt().isBefore(taskModel.getStartAt())) {
            return ResponseEntity.status(400).body("You can't create a task in the past nor set it to end before the starting time.");
        } else {
            var task = this.taskRepository.save(taskModel);

            return ResponseEntity.status(200).body(task);
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<TaskModel>> list(HttpServletRequest request) {
        var tasks = this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
        return ResponseEntity.status(200).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        
        var task = this.taskRepository.findById(id).orElse(null);

        Utils.copyNonNullProperties(taskModel, task);

        var taskUpdated = this.taskRepository.save(task);

        return ResponseEntity.status(200).body(taskUpdated);
    }
}
