// src/main/java/org/antiplagiarism/service/FilesStoringService.java
package org.antiplagiarism.filestorage.service;

import org.antiplagiarism.filestorage.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FilesStoringService {
    FileEntity store(MultipartFile file);
    FileEntity getFile(Long id);
    List<FileEntity> getAllFiles();
    void deleteFile(Long id);
}
