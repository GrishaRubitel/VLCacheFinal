package ru.choomandco.VLCacheFinal.fileGarbage.service.videoStreamer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlCacheStatus.CacheStatusService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUploadLogs.UploadLogsService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos.VideoService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos.Videos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@EnableAsync
public class VideoController {

    private static final String VIDEOS_PATH = "./videos/";
    private static final String DD_MM_YYYY = "ddMMyyyy";
    private static final String DOWNLOADER_ID = "downloader";
    private static final String DATA_TOPIC = "data-topic";
    private static final String PART_ZERO = "0";
    private static final String PART_TWO = "2";
    private static final String SLASH = "/";
    private static final String UNNAMED_DIR = "unnamed_dir";
    private static final String URL_REGEX = "=(\\w+)$";
    private static final String YT_DLP = "yt-dlp";
    private static final String P_FLAG = "-P";
    private static final String TRUNK_ID = "trunk-id";
    private static final String O_FLAG = "-o";
    private static final String F_FLAG = "-f";
    private static final String QUALITY_PRESET = "bv[height<=720]+ba[height<=720]";
    private static final String SNIPPET = "snippet";
    private static final String API_KEY = "AIzaSyCHLMWk5-MknRuQpUgWlI5kXgKuvzta0P8";
    private static final String APP_NAME = "VLCash";
    private static final String DELETE_ALL_OF_THEM = "DELETE ALL OF THEM";
    private static final String SPACE = " ";
    private static final String UNDERSCORE = "_";
    private static final String MP4_POINTED = ".mp4";
    private static final String MP4 = "mp4";
    public static final String URL = "url";
    public static final String USER_ID = "userId";

    private static Boolean videosExists = false;

    private static final Logger log = Logger.getLogger(VideoController.class.getName());

    @Autowired
    VideoService videoService;
    @Autowired
    UploadLogsService uploadLogsService;
    @Autowired
    CacheStatusService cacheStatusService;

    @PostConstruct
    private void createVideoDir() {
        Path path = Path.of(VIDEOS_PATH);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
                log.info("Videos's dir created!");
            } catch (IOException e) {
                log.warning("Error while creating videos's dir - " + e.getMessage());
            }
        }
    }

    @KafkaListener(id = TRUNK_ID, topicPartitions = {
            @TopicPartition(topic = DATA_TOPIC, partitions = PART_TWO)
    })
    private void trunkTable(String message) {
        if (message.equals(DELETE_ALL_OF_THEM)) {
            try {
                FileUtils.cleanDirectory(new File(VIDEOS_PATH));
            } catch (Exception e) {
                log.warning("Error while cleaning directory - " + e.getMessage());
            }
        }
    }

    @KafkaListener(id = DOWNLOADER_ID, topicPartitions = {
            @TopicPartition(topic = DATA_TOPIC, partitions = PART_ZERO)
    })
    private void videoDownloadStarter(String message) {
        prepareToDownload(message);
    }

    private void prepareToDownload(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String url = jsonNode.get(URL).asText();
        int userId = jsonNode.get(USER_ID).asInt();

        String currDate = getCurrentDate();
        createDir(VIDEOS_PATH + currDate);

        Videos newVideo = new Videos();
        downloadVideo(newVideo, url, VIDEOS_PATH + currDate, userId);
    }

    private void createDir(String path) {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectory(filePath);
                log.info("Dir Created!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getIdFromUrl(String fullUrl) {
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(fullUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return UNNAMED_DIR;
    }

    private String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MM_YYYY);
        return currentDate.format(formatter);
    }

    private void downloadVideo(Videos newVideo, String videoUrl, String directory, int userId) {
        createDir(directory);

        String videoTitle;
        try {
            videoTitle = getVideoTitle(videoUrl);
        } catch (IndexOutOfBoundsException e) {
            log.warning("Error with title extraction - " + e.getMessage());
            videoTitle = String.valueOf(System.currentTimeMillis());
        }

        newVideo = new Videos(videoUrl, userId, videoTitle, directory, new Timestamp(System.currentTimeMillis()));
        videoService.insertVideo(newVideo);
        uploadLogsService.createZeroProgressUpload(videoUrl);
        cacheStatusService.insertNewCache(videoUrl, false);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
                YT_DLP, videoUrl,
                P_FLAG, directory,
                F_FLAG, MP4,
                O_FLAG, videoTitle);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                directory = directory + SLASH + videoTitle;
                newVideo.setFilePath(directory);
                videoService.insertVideo(newVideo);
                uploadLogsService.finishExistingProgress(videoUrl);
                cacheStatusService.updateCacheState(videoUrl, true);
            } else {
                uploadLogsService.failExistingProgress(videoUrl);
            }
        } catch (IOException e) {
            log.warning("Error while downloading - " + e.getMessage());
        } catch (InterruptedException e) {
            log.warning("Error with tread while downloading - " + e.getMessage());
        }
    }

    private String getVideoTitle(String url) {
        YouTube youtube = new YouTube.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                request -> {})
                .setApplicationName(APP_NAME)
                .build();

        String videoId = getIdFromUrl(url);
        VideoListResponse response = null;
        YouTube.Videos.List request = null;
        try {
            request = youtube.videos()
                    .list(SNIPPET)
                    .setId(videoId)
                    .setKey(API_KEY);
            response = request.execute();
        } catch (IOException e) {
            log.warning("Error while geting youTube video title - " + e.getMessage());
        }
        Video video = response.getItems().get(0);

        return formaliseVideoTitle(video.getSnippet().getTitle());
    }

    private String formaliseVideoTitle(String title) {
        title = title.trim().replaceAll(SPACE, UNDERSCORE);
        return title + MP4_POINTED;
    }
}
