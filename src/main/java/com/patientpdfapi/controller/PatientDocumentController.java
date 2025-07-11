package com.patientpdfapi.controller;

import com.patientpdfapi.model.PatientDocument;
import com.patientpdfapi.repository.PatientDocumentRepository;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class PatientDocumentController {

    private final PatientDocumentRepository repository;

    public PatientDocumentController(PatientDocumentRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("patientId") Long patientId) throws IOException {
        PatientDocument doc = new PatientDocument();
        doc.setPatientId(patientId);
        doc.setFileName(file.getOriginalFilename());
        doc.setUploadDate(LocalDateTime.now());
        doc.setPdfData(file.getBytes());
        repository.save(doc);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/latest/{patientId}")
    @Transactional  // ✅ Required for PostgreSQL Large Object (LOB)
    public ResponseEntity<byte[]> getLatestFile(@PathVariable Long patientId) {
        PatientDocument latest = repository.findTopByPatientIdOrderByUploadDateDesc(patientId);
        if (latest == null) return ResponseEntity.notFound().build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(latest.getFileName()).build());
        return new ResponseEntity<>(latest.getPdfData(), headers, HttpStatus.OK);
    }

    @GetMapping("/all/{patientId}")
    @Transactional  // ✅ Needed because LOB data may be fetched lazily
    public List<PatientDocument> getAllFiles(@PathVariable Long patientId) {
        return repository.findByPatientIdOrderByUploadDateDesc(patientId);
    }

    @GetMapping("/download/{docId}")
    @Transactional  // ✅ Important for safely fetching LOB (pdfData)
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long docId) {
        PatientDocument doc = repository.findById(docId).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(doc.getFileName()).build());
        return new ResponseEntity<>(doc.getPdfData(), headers, HttpStatus.OK);
    }
}
