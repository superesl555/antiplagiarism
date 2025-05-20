package org.antiplagiarism.filestorage.repository;

import org.antiplagiarism.filestorage.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByHash(String hash);
}