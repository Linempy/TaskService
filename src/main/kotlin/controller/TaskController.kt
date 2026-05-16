package com.manticore.controller

import com.manticore.dto.TaskCreateDto
import com.manticore.dto.TaskFilterDto
import com.manticore.dto.TaskStatusHistoryViewDto
import com.manticore.dto.TaskViewDto
import com.manticore.service.TaskService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST контроллер для управления задачами.
 *
 * Предоставляет API для создания, получения, обработки и отмены задач.
 * Все эндпоинты используют префикс `/api/v1/tasks`.
 *
 * @author Linempy
 * @since 15.05.2026
 */
@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val taskService: TaskService
) {

    /**
     * Создаёт задачу на основе переданных данных и ставит её в очередь на обработку.
     * Задача создаётся со статусом `PENDING`.
     *
     * @param request DTO с данными для создания задачи (название, тип, приоритет, ключ файла)
     * @return DTO созданной задачи с HTTP статусом 201 (Created)
     *
     * @throws IllegalArgumentException если файл с указанным `fileKey` не найден в хранилище
     * @throws jakarta.validation.ConstraintViolationException если валидация DTO не пройдена
     */
    @PostMapping
    fun createTask(@Valid @RequestBody request: TaskCreateDto): ResponseEntity<TaskViewDto> {
        val task = taskService.createTask(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(task)
    }

    /**
     * Запускает обработку задачи. Переводит задачу со статусами `FAILED` или `CANCELLED` в статус `PENDING`
     * и отправляет на повторную обработку.
     *
     * @param id идентификатор задачи
     * @return DTO задачи с обновлённым статусом и HTTP статусом 202 (Accepted)
     *
     * @throws NoSuchElementException если задача с указанным ID не найдена
     * @throws IllegalStateException если задача не может быть перезапущена (статус не `FAILED` или `CANCELLED`)
     */
    @PostMapping("/{id}/process")
    fun startProcessing(@PathVariable id: Long): ResponseEntity<TaskViewDto> {
        val task = taskService.startProcessing(id)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(task)
    }

    /**
     * История переходов статусов задачи (от старых записей к новым).
     *
     * @param id идентификатор задачи
     */
    @GetMapping("/{id}/history")
    fun getTaskStatusHistory(@PathVariable id: Long): ResponseEntity<List<TaskStatusHistoryViewDto>> {
        return ResponseEntity.ok(taskService.getTaskStatusHistory(id))
    }

    /**
     * Возвращает задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return DTO задачи с HTTP статусом 200 (OK)
     *
     * @throws NoSuchElementException если задача с указанным ID не найдена
     */
    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): ResponseEntity<TaskViewDto> {
        return ResponseEntity.ok(taskService.getTask(id))
    }

    /**
     * Возвращает список задач с фильтрацией и пагинацией. Поддерживает фильтрацию по статусу, типу задачи
     * и диапазону времени создания (`createdFrom` / `createdTo`).
     * Результат возвращается постранично с размером страницы 20 по умолчанию.
     *
     * @param filterDto параметры фильтрации (status, taskType, createdFrom, createdTo)
     * @param pageable параметры пагинации (страница, размер, сортировка)
     * @return Страница с DTO задач и HTTP статусом 200 (OK)
     */
    @GetMapping
    fun getTasks(
        @ModelAttribute filterDto: TaskFilterDto,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<TaskViewDto>> {
        return ResponseEntity.ok(taskService.getTasks(filterDto, pageable))
    }

    /**
     * Отменяет задачу со статусами `PENDING` или `PROCESSING`.
     * Отменённая задача не будет обработана, а если находилась в обработке — будет прервана.
     *
     * @param id идентификатор задачи
     * @return DTO отменённой задачи с HTTP статусом 202 (Accepted)
     *
     * @throws NoSuchElementException если задача с указанным ID не найдена
     * @throws IllegalStateException если задача не может быть отменена (статус не `PENDING` или `PROCESSING`)
     */
    @DeleteMapping("/{id}")
    fun cancelTask(@PathVariable id: Long): ResponseEntity<TaskViewDto> {
        val task = taskService.cancelTask(id)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(task)
    }
}