package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Videos, String> {}
