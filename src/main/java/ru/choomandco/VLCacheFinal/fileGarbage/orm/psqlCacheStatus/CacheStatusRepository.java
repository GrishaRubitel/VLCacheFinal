package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlCacheStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheStatusRepository extends JpaRepository<CacheStatus, String> {}
