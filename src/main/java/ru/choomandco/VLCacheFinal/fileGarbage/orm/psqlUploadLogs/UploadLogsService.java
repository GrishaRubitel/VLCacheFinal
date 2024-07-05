package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUploadLogs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Optional;

@Service
public class UploadLogsService {

    private static final String IN_PROGRESS = "in_progress";
    private static final String COMPLETED = "completed";
    private static final String FAILED = "in_progress";

    @Autowired
    private UploadLogsRepository uploadLogsRepository;

    public UploadLogsService() {}

    public LinkedList<UploadLogs> selectAll() {
        Iterable<UploadLogs> entrySet = uploadLogsRepository.findAll();
        LinkedList<UploadLogs> entryList = new LinkedList<>();
        entrySet.forEach(entryList::add);
        return entryList;
    }

    public void createZeroProgressUpload(String url) {
        uploadLogsRepository.save(new UploadLogs(url, IN_PROGRESS, new Timestamp(System.currentTimeMillis())));
    }

    public void finishExistingProgress(String url) {
        uploadLogsRepository.save(new UploadLogs(url, COMPLETED));
    }

    public void failExistingProgress(String url) {
        uploadLogsRepository.save(new UploadLogs(url, FAILED));
    }

    public void deleteExistingProgress(String url) {
        uploadLogsRepository.deleteById(url);
    }

    public Optional<UploadLogs> selectById(String url) {
        return uploadLogsRepository.findById(url);
    }
}
