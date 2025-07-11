package com.patientpdfapi.repository;

import com.patientpdfapi.model.PatientDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Long> {
    List<PatientDocument> findByPatientIdOrderByUploadDateDesc(Long patientId);
    PatientDocument findTopByPatientIdOrderByUploadDateDesc(Long patientId);
}
