package com.keithlamond.tasktracker.repository;

import com.keithlamond.tasktracker.entity.Priority;
import com.keithlamond.tasktracker.entity.Status;
import com.keithlamond.tasktracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(Status status);

    List<Task> findByPriority(Priority priority);

    List<Task> findByUserId(Long userId);

    List<Task> findByStatusAndPriority(Status status, Priority priority);

    List<Task> findByUserIdAndStatus(Long userId, Status status);

    List<Task> findByUserIdAndPriority(Long userId, Priority priority);

    List<Task> findByUserIdAndStatusAndPriority(Long userId, Status status, Priority priority);

    List<Task> findAllByOrderByPriorityAsc();

    List<Task> findAllByOrderByPriorityDesc();
}
