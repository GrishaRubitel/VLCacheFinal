package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Optional;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public LinkedList<Videos> selectAll() {
        Iterable<Videos> entrySet = videoRepository.findAll();
        LinkedList<Videos> entryList = new LinkedList<>();
        entrySet.forEach(entryList::add);
        return entryList;
    }

    public Optional<Videos> selectById(String url) {
        return videoRepository.findById(url);
    }

    public void insertVideo(String url, int userId, String fileName, String path, Timestamp downloadTime) {
        videoRepository.save(new Videos(url, userId, fileName, path, downloadTime));
    }

    public void insertVideo(String url, String fileName, String path, Timestamp downloadTime) {
        videoRepository.save(new Videos(url, -999, fileName, path, downloadTime));
    }

    public void insertVideo(Videos video) {
        videoRepository.save(video);
    }

    public void deleteExistingVideo(String url) {
        videoRepository.deleteById(url);
    }
}
