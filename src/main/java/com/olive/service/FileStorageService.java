package com.olive.service;

import com.olive.config.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        logger.info("File storage location initialized to: {}", this.fileStorageLocation);

        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Created upload directory: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create the upload directory: {}", this.fileStorageLocation, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create the upload directory.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex).toLowerCase(); // .doc, .docx, .pdf
        }

        // Validate file type
        if (!(".pdf".equals(fileExtension) || ".doc".equals(fileExtension) || ".docx".equals(fileExtension))) {
            logger.warn("Invalid file type attempted for upload: {}. Only .pdf, .doc, .docx are allowed.", fileExtension);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only PDF and Word documents (.doc, .docx) are allowed.");
        }

        // Generate a unique file name to prevent collisions
        String fileName = UUID.randomUUID().toString() + fileExtension;
        logger.info("Storing file: Original='{}', Generated='{}'", originalFileName, fileName);

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                logger.warn("Attempted to store file with invalid path sequence: {}", fileName);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File stored successfully to: {}", targetLocation);
            return fileName; // Return the generated unique filename
        } catch (IOException ex) {
            logger.error("Could not store file {}. Please try again!", fileName, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        logger.info("Loading file as resource: {}", fileName);
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                logger.debug("File resource '{}' found.", fileName);
                return resource;
            } else {
                logger.warn("File not found: {}", fileName);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            logger.error("Error loading file '{}': {}", fileName, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        logger.info("Attempting to delete file: {}", fileName);
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("File '{}' deleted successfully.", fileName);
            } else {
                logger.warn("File '{}' not found for deletion.", fileName);
            }
        } catch (IOException ex) {
            logger.error("Could not delete file '{}': {}", fileName, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete file " + fileName + ". Please try again!", ex);
        }
    }
}
