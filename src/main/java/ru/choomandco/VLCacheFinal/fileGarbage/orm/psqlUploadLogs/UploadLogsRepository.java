package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUploadLogs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadLogsRepository extends JpaRepository<UploadLogs, String> {}
