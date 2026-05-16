package com.manticore.exception

class StorageException : RuntimeException {
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}