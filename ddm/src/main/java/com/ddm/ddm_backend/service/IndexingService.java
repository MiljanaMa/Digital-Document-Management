package com.ddm.ddm_backend.service;

import ai.djl.translate.TranslateException;
import com.ddm.ddm_backend.dto.ParsedDocDTO;
import com.ddm.ddm_backend.exceptionhandling.exception.LoadingException;
import com.ddm.ddm_backend.exceptionhandling.exception.StorageException;
import com.ddm.ddm_backend.indexmodel.DummyIndex;
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
import org.apache.tika.language.detect.LanguageDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private final DummyIndexRepository dummyIndexRepository;

    private final DummyRepository dummyRepository;
    private final LanguageDetector languageDetector;
    private final FileServiceMinio fileService;
    private final Map<String, DummyIndex> tempParsedDocuments = new ConcurrentHashMap<>();

    private final LocationIqClient locationIqClient;
    @Value("${location.api.key}")
    private String apiKey;

    @Transactional
    public ParsedDocDTO addDocumentFile(MultipartFile documentFile) {
        var newEntity = new DummyTable();
        var newIndex = new DummyIndex();

        var title = Objects.requireNonNull(documentFile.getOriginalFilename()).split("\\.")[0];
        newIndex.setTitle(title);
        newEntity.setTitle(title);
        System.out.println(title);

        var documentContent = extractDocumentContent(documentFile);
        newIndex.setContent(documentContent);
        newEntity.setContent(documentContent);

        if (detectLanguage(documentContent).equals("SR")) {
            newIndex.setContent(documentContent);
            System.out.println("Serbian detected");
        }

        Map<String, String> fields = parseFields(documentContent);

        newIndex.setEmployeeFullName(fields.get("employeeFullName"));
        newIndex.setSecurityOrganizationName(fields.get("organization"));
        newIndex.setAffectedOrganizationName(fields.get("affectedOrganization"));
        newIndex.setIncidentSeverity(fields.get("incidentSeverity"));
        newIndex.setAffectedOrganizationAddress(fields.get("address"));

        String docId = UUID.randomUUID().toString();
        var serverFilename = this.fileService.store(documentFile, docId);
        newIndex.setServerFilename(serverFilename);
        tempParsedDocuments.put(docId, newIndex);

        var response = new ParsedDocDTO();
        response.setAffectedOrganizationName(newIndex.getAffectedOrganizationName());
        response.setAffectedOrganizationAddress(newIndex.getAffectedOrganizationAddress());
        response.setEmployeeFullName(newIndex.getEmployeeFullName());
        response.setSecurityOrganizationName(newIndex.getSecurityOrganizationName());
        response.setIncidentSeverity(newIndex.getIncidentSeverity());
        response.setDocumentId(docId);
        response.setTitle(title);
        return response;
        /*var serverFilename = fileService.store(documentFile, UUID.randomUUID().toString());
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

        return serverFilename;*/
    }
    @Transactional
    public String indexDocument(ParsedDocDTO dto) throws Exception {
        var newEntity = new DummyTable();
        var index = tempParsedDocuments.get(dto.getDocumentId());
        InputStream stream = fileService.loadAsResource(index.getServerFilename());
        byte[] content = stream.readAllBytes();

        MultipartFile documentFile = new MockMultipartFile(
                "file",
                index.getTitle()+".pdf",
                "application/pdf",
                content
        );

        newEntity.setTitle(index.getTitle());
        var serverFilename = dto.getDocumentId()+".pdf";
        newEntity.setServerFilename(serverFilename);
        index.setServerFilename(serverFilename);

        newEntity.setMimeType(detectMimeType(documentFile));
        var savedEntity = dummyRepository.save(newEntity);

        try {
            index.setVectorizedContent(VectorizationUtil.getEmbedding(extractDocumentContent(documentFile)));
        } catch (TranslateException e) {
            log.error("Could not calculate vector representation for document with ID: {}",
                    savedEntity.getId());
        }

        index.setDatabaseId(savedEntity.getId());
        index.setAffectedOrganizationName(dto.getAffectedOrganizationName());
        index.setAffectedOrganizationAddress(dto.getAffectedOrganizationAddress());
        System.out.println(dto.getAffectedOrganizationAddress());
        var location = locationIqClient.forwardGeolocation(
                apiKey, dto.getAffectedOrganizationAddress(), "json").get(0);
        index.setLocation(new GeoPoint(Double.parseDouble(location.getLat()), Double.parseDouble(location.getLon())));
        index.setEmployeeFullName(dto.getEmployeeFullName());
        index.setSecurityOrganizationName(dto.getSecurityOrganizationName());
        index.setIncidentSeverity(dto.getIncidentSeverity());

        dummyIndexRepository.save(index);
        log.info("STATISTIC-LOG Indexed file {} with employee_name:{} affected_organization:{} address:{}", index.getTitle(), index.getEmployeeFullName(), index.getAffectedOrganizationName(), index.getAffectedOrganizationAddress());
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
    private Map<String, String> parseFields(String documentContent) {
        Map<String, String> fields = new HashMap<>();

        Pattern employeePattern = Pattern.compile("Naziv Zaposlenog:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = employeePattern.matcher(documentContent);
        if (m.find()) {
            fields.put("employeeFullName", m.group(1).trim());
        }

        Pattern orgPattern = Pattern.compile("Bezbednosna Organizacija:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        m = orgPattern.matcher(documentContent);
        if (m.find()) {
            fields.put("organization", m.group(1).trim());
        }

        Pattern affectedOrgPattern = Pattern.compile("Pogodjena Organizacija:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        m = affectedOrgPattern.matcher(documentContent);
        if (m.find()) {
            fields.put("affectedOrganization", m.group(1).trim());
        }

        Pattern severityPattern = Pattern.compile("Ozbiljnost Incidenta:\\s*(niska|srednja|visoka|kriticna)", Pattern.CASE_INSENSITIVE);
        m = severityPattern.matcher(documentContent);
        if (m.find()) {
            fields.put("incidentSeverity", m.group(1).toLowerCase());
        }

        Pattern addressPattern = Pattern.compile("Adresa Pogodjene Organizacije:\\s*(.*)", Pattern.CASE_INSENSITIVE);
        m = addressPattern.matcher(documentContent);
        if (m.find()) {
            fields.put("address", m.group(1).trim());
        }

        return fields;
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
    private String detectLanguage(String text) {
        var detectedLanguage = languageDetector.detect(text).getLanguage().toUpperCase();
        if (detectedLanguage.equals("HR")) {
            detectedLanguage = "SR";
        }

        return detectedLanguage;
    }
    public void deleteDocument(String id) {
        tempParsedDocuments.remove(id);
        this.fileService.delete(id+".pdf");

    }
}