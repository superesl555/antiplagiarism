package org.antiplagiarism.filestorage.exception;

public class DuplicateFileException extends RuntimeException {
    public DuplicateFileException(String filename) {
        super("Файл «" + filename + "» уже существует (плагиат) ");
    }
}
