package vn.vlu.svjob.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

final class FileResponses {

    private FileResponses() {
    }

    /** Trả file PDF để trình duyệt mở xem trực tiếp. */
    static ResponseEntity<Resource> pdf(Resource file, String downloadName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + downloadName + "\"")
                .body(file);
    }
}
