package com.ddm.ddm_backend.controller;
import com.ddm.ddm_backend.dto.DummyDocumentFileResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ddm.ddm_backend.dto.ParsedDocDTO;
import com.ddm.ddm_backend.service.IndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class IndexController {
    private final IndexingService indexingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ParsedDocDTO addDocumentFile(@RequestPart("file") MultipartFile documentFile) {
        var parsedDocDTO = indexingService.addDocumentFile(documentFile);
        return parsedDocDTO;
    }
    @PostMapping("/save")
    public DummyDocumentFileResponseDTO confirmIndexedFile(@RequestBody ParsedDocDTO dto) {
        try {
            var serverFilename = indexingService.indexDocument(dto);
            return new DummyDocumentFileResponseDTO(serverFilename);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @DeleteMapping("/cancel/{tempId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineIndexedFile(@PathVariable("tempId") String id) {
        indexingService.deleteDocument(id);
    }
}