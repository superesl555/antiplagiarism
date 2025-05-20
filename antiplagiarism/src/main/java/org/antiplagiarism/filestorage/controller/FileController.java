// src/main/java/org/antiplagiarism/filestorage/controller/FileController.java
package org.antiplagiarism.filestorage.controller;

import org.antiplagiarism.filestorage.entity.FileEntity;
import org.antiplagiarism.filestorage.exception.DuplicateFileException;
import org.antiplagiarism.filestorage.service.FilesStoringService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FilesStoringService service;

    public FileController(FilesStoringService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FileEntity> upload(@RequestParam("file") MultipartFile file) {
        FileEntity saved = service.store(file);
        URI location = URI.create("/files/" + saved.getId());
        return ResponseEntity
                .created(location)
                .body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
        FileEntity file = service.getFile(id);
        ByteArrayResource resource = new ByteArrayResource(file.getData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(resource);
    }

    @ExceptionHandler(DuplicateFileException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateFileException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
