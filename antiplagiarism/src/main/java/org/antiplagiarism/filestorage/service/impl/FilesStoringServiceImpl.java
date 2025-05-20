// src/main/java/org/antiplagiarism/filestorage/service/impl/FilesStoringServiceImpl.java
package org.antiplagiarism.filestorage.service.impl;

import org.antiplagiarism.filestorage.entity.FileEntity;
import org.antiplagiarism.filestorage.exception.DuplicateFileException;
import org.antiplagiarism.filestorage.repository.FileRepository;
import org.antiplagiarism.filestorage.service.FilesStoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;  // <-- добавлен import

@Service
@Transactional
public class FilesStoringServiceImpl implements FilesStoringService {

    private final FileRepository fileRepository;

    public FilesStoringServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public FileEntity store(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            String hash = sha256(content);

            // Проверяем дубликат
            fileRepository.findByHash(hash)
                    .ifPresent(f -> {
                        throw new DuplicateFileException(file.getOriginalFilename());
                    });

            FileEntity entity = new FileEntity(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    content,
                    LocalDateTime.now()
            );
            entity.setHash(hash);
            return fileRepository.save(entity);

        } catch (IOException e) {
            throw new RuntimeException("Не удалось считать содержимое файла", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileEntity getFile(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Файл с id=" + id + " не найден"));
    }

    @Override
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    @Override
    public void deleteFile(Long id) {
        if (!fileRepository.existsById(id)) {
            throw new RuntimeException("Файл с id=" + id + " не найден");
        }
        fileRepository.deleteById(id);
    }

    // === утилита для SHA-256 ===
    private static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 алгоритм не найден", e);
        }
    }
}
