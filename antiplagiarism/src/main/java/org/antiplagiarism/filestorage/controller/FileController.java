package org.antiplagiarism.filestorage.controller;

import org.antiplagiarism.fileanalysis.AnalysisResult;
import org.antiplagiarism.fileanalysis.TextAnalyzer;
import org.antiplagiarism.filestorage.entity.FileEntity;
import org.antiplagiarism.filestorage.exception.DuplicateFileException;
import org.antiplagiarism.filestorage.service.FilesStoringService;
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
    public ResponseEntity<Map<String,Object>> upload(@RequestParam("file") MultipartFile file) {

        FileEntity saved = service.store(file);
        AnalysisResult ar = TextAnalyzer.analyze(saved.getData());

        return ResponseEntity.created(URI.create("/files/" + saved.getId()))
                .body(Map.of(
                        "fileId",    saved.getId(),
                        "filename",  saved.getFilename(),
                        "analysis",  ar
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        FileEntity f = service.getFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"%s\"".formatted(f.getFilename()))
                .body(f.getData());
    }

    @ExceptionHandler(DuplicateFileException.class)
    public ResponseEntity<Map<String,String>> duplicate(DuplicateFileException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
