package com.olive.controller;

import com.olive.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;

    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // Allow ADMIN, MANAGER, BA, TEAMMEMBER to upload documents (for tasks)
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'BA', 'TEAMMEMBER')")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Received file upload request for file: {}", file.getOriginalFilename());
        String fileName = fileStorageService.storeFile(file);

        // Construct file download URI
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(fileName)
                .toUriString();

        logger.info("File '{}' uploaded successfully. Download URI: {}", fileName, fileDownloadUri);
        return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "fileDownloadUri", fileDownloadUri,
                "fileType", file.getContentType(),
                "size", String.valueOf(file.getSize())
        ));
    }

    // Allow ADMIN, MANAGER, BA, TEAMMEMBER to download documents (for tasks)
    @GetMapping("/download/{fileName:.+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'BA', 'TEAMMEMBER', 'HR')") // HR might need to download documents associated with tasks for auditing/review
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        logger.info("Received file download request for file: {}", fileName);
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.warn("Could not determine file type for {}. Defaulting to octet-stream.", fileName);
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        logger.info("Serving file '{}' with Content-Type: {}", fileName, contentType);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
