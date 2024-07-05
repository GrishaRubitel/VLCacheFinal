package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlCacheStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Optional;

@Service
public class CacheStatusService {

    @Autowired
    private CacheStatusRepository cacheStatusRepository;

    public LinkedList<CacheStatus> selectAll() {
        Iterable<CacheStatus> entrySet = cacheStatusRepository.findAll();
        LinkedList<CacheStatus> entryList = new LinkedList<>();
        entrySet.forEach(entryList::add);
        return entryList;
    }

    public void insertNewCache(String url, Boolean bool) {
        cacheStatusRepository.save(new CacheStatus(url, bool, new Timestamp(System.currentTimeMillis())));
    }

    public void updateCacheState(String url, Boolean bool) {
        cacheStatusRepository.save(new CacheStatus(url, bool));
    }

    public void deleteCacgeRecord(String url) {
        cacheStatusRepository.deleteById(url);
    }

    public Optional<CacheStatus> selectById(String url) {
        return cacheStatusRepository.findById(url);
    }
}
