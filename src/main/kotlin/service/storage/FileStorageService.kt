package com.manticore.service.storage

import com.manticore.exception.StorageException

/**
 * Интерфейс для работы с файловым хранилищем.
 * Определяет базовые операции для загрузки и скачивания файлов.
 *
 * @author Linempy
 * @since 15.05.2026
 */
interface FileStorageService {
    /**
     * Загружает файл в хранилище
     *
     * @param fileBytes массив байтов файла для загрузки
     * @param bucketName имя бакета/контейнера в хранилище
     * @param objectKey уникальный ключ объекта в хранилище
     * @param contentType MIME-тип содержимого файла
     *
     * @throws StorageException при ошибках загрузки файла
     */
    @Throws(StorageException::class)
    fun upload(fileBytes: ByteArray?, bucketName: String?, objectKey: String?, contentType: String?)

    /**
     * Скачивает файл из хранилища
     *
     * @param bucketName имя бакета/контейнера в хранилище
     * @param objectKey уникальный ключ объекта в хранилище
     *
     * @return массив байтов содержимого файла
     * @throws StorageException при ошибках скачивания файла
     */
    @Throws(StorageException::class)
    fun download(bucketName: String?, objectKey: String?): ByteArray?
}