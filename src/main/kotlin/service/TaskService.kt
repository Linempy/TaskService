package com.manticore.service

import com.manticore.dto.TaskCreateDto
import com.manticore.dto.TaskFilterDto
import com.manticore.dto.TaskStatusHistoryViewDto
import com.manticore.dto.TaskViewDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.manticore.exception.StorageException

/**
 * Сервис для управления задачами.
 *
 * Предоставляет бизнес-логику для создания, получения, обработки и отмены задач.
 *
 * @author Linempy
 * @since 15.05.2026
 */
interface TaskService {

    /**
     * Создаёт новую задачу. Проверяет существование файла в хранилище, создаёт задачу со статусом `PENDING`
     * и сохраняет её в базу данных.
     *
     * @param createDto DTO с данными для создания задачи (название, тип, приоритет, ключ файла)
     * @return DTO созданной задачи, или `null` если создание не удалось
     *
     * @throws IllegalArgumentException если файл с указанным `fileKey` не найден в хранилище
     * @throws StorageException если произошла ошибка при проверке файла в хранилище
     */
    fun createTask(createDto: TaskCreateDto): TaskViewDto?

    /**
     * Запускает повторную обработку задачи. Переводит задачу со статусами `FAILED` или `CANCELLED` в статус `PENDING`,
     * сбрасывает результат и ошибку, затем отправляет задачу на обработку.
     *
     * @param id идентификатор задачи
     * @return DTO задачи после обновления статуса, или `null` если задача не найдена
     *
     * @throws NoSuchElementException если задача с указанным ID не найдена
     * @throws IllegalStateException если задача не может быть перезапущена (статус не `FAILED` или `CANCELLED`)
     */
    fun startProcessing(id: Long): TaskViewDto?

    /**
     * Возвращает задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return DTO задачи, или `null` если задача не найдена
     *
     * @throws NoSuchElementException если задача с указанным ID не найдена
     */
    fun getTask(id: Long): TaskViewDto?

    /**
     * Возвращает список задач с фильтрацией и пагинацией. Фильтры: статус, тип задачи, интервал по `createdAt`.
     * Результат возвращается постранично.
     *
     * @param filterDto параметры фильтрации (status, taskType, createdFrom, createdTo)
     * @param pageable параметры пагинации (страница, размер, сортировка)
     * @return Страница с DTO задач, никогда не возвращает `null`
     */
    fun getTasks(
        filterDto: TaskFilterDto,
        pageable: Pageable
    ): Page<TaskViewDto>

    /**
     * Отменяет задачу со статусами `PENDING` или `PROCESSING`.
     * Отменённая задача не будет обработана, а если находилась в обработке - будет прервана.
     *
     * @param id идентификатор задачи
     * @return DTO отменённой задачи
     *
     * @throws NoSuchElementException если задача с указанным ID не найдена
     * @throws IllegalStateException если задача не может быть отменена (статус не `PENDING` или `PROCESSING`)
     */
    fun cancelTask(id: Long): TaskViewDto

    /**
     * История смены статусов задачи (хронологический порядок).
     */
    fun getTaskStatusHistory(taskId: Long): List<TaskStatusHistoryViewDto>
}