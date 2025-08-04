package com.ddm.ddm_backend.service;

import ai.djl.translate.TranslateException;
import com.ddm.ddm_backend.exceptionhandling.exception.LoadingException;
import com.ddm.ddm_backend.exceptionhandling.exception.StorageException;
import com.ddm.ddm_backend.indexmodel.IndexUnit;
import com.ddm.ddm_backend.indexrepository.DummyIndexRepository;
import com.ddm.ddm_backend.model.DummyTable;
import com.ddm.ddm_backend.repository.DummyRepository;
import com.ddm.ddm_backend.util.LocationIqClient;
import com.ddm.ddm_backend.util.VectorizationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private final DummyIndexRepository dummyIndexRepository;

    private final DummyRepository dummyRepository;

    private FileServiceMinio fileService;

    //private final LocationIqClient locationIqClient;
    @Value("${location.api.key}")
    private String apiKey;

    @Transactional
    public String indexDocument(MultipartFile documentFile) {
        var newEntity = new DummyTable();
        var newIndex = new IndexUnit();

        var title = Objects.requireNonNull(documentFile.getOriginalFilename()).split("\\.")[0];
        newIndex.setTitle(title);
        newEntity.setTitle(title);

        var documentContent = extractDocumentContent(documentFile);
        newIndex.setContent(documentContent);
        newEntity.setContent(documentContent);

        var serverFilename = fileService.store(documentFile, UUID.randomUUID().toString());
        newIndex.setServerFilename(serverFilename);
        newEntity.setServerFilename(serverFilename);

        newEntity.setMimeType(detectMimeType(documentFile));
        var savedEntity = dummyRepository.save(newEntity);

        try {
            newIndex.setVectorizedContent(VectorizationUtil.getEmbedding(title));
        } catch (TranslateException e) {
            log.error("Could not calculate vector representation for document with ID: {}",
                savedEntity.getId());
        }
        newIndex.setDatabaseId(savedEntity.getId());
        dummyIndexRepository.save(newIndex);

        return serverFilename;
    }

    private String extractDocumentContent(MultipartFile multipartPdfFile) {
        String documentContent;
        try (var pdfFile = multipartPdfFile.getInputStream()) {
            var pdDocument = PDDocument.load(pdfFile);
            var textStripper = new PDFTextStripper();
            documentContent = textStripper.getText(pdDocument);
            pdDocument.close();
        } catch (IOException e) {
            throw new LoadingException("Error while trying to load PDF file content.");
        }

        return documentContent;
    }

    private String detectMimeType(MultipartFile file) {
        var contentAnalyzer = new Tika();

        String trueMimeType;
        String specifiedMimeType;
        try {
            trueMimeType = contentAnalyzer.detect(file.getBytes());
            specifiedMimeType =
                Files.probeContentType(Path.of(Objects.requireNonNull(file.getOriginalFilename())));
        } catch (IOException e) {
            throw new StorageException("Failed to detect mime type for file.");
        }

        if (!trueMimeType.equals(specifiedMimeType) &&
            !(trueMimeType.contains("zip") && specifiedMimeType.contains("zip"))) {
            throw new StorageException("True mime type is different from specified one, aborting.");
        }

        return trueMimeType;
    }
}