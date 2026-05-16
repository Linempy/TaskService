package com.manticore.service.storage

import com.manticore.exception.StorageException
import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

/**
 * Реализация сервиса для работы с файловым хранилищем MinIO.
 * Предоставляет методы для загрузки и скачивания файлов в объектное хранилище MinIO.
 *
 * @author Linempy
 * @since 15.05.2026
 */
@Service
class MinioFileStorageService(
    private val client: MinioClient,
) : FileStorageService {

    @Throws(StorageException::class)
    override fun upload(fileBytes: ByteArray?, bucketName: String?, objectKey: String?, contentType: String?) {
        try {
            ByteArrayInputStream(fileBytes).use { fileStream ->
                val putObject = fileBytes?.size?.let {
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .`object`(objectKey)
                        .stream(fileStream, it.toLong(), -1)
                        .contentType(contentType)
                        .build()
                }
                client.putObject(putObject)
            }
        } catch (e: Exception) {
            throw StorageException("Ошибка в загрузке файла в MinIO", e)
        }
    }

    @Throws(StorageException::class)
    override fun download(bucketName: String?, objectKey: String?): ByteArray {
        try {
            val getObject = GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectKey)
                .build()
            client.getObject(getObject).use { stream ->
                return stream.readAllBytes()
            }
        } catch (e: Exception) {
            throw StorageException("Ошибка в скачивании файла из MinIO", e)
        }
    }
}