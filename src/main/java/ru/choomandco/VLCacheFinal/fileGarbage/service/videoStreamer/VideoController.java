package ru.choomandco.VLCacheFinal.fileGarbage.service.videoStreamer;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import jakarta.annotation.PostConstruct;
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


/**
 * Один из двух основных микросервисов. Занимается скачиванием видео по полученной ссылке
 */
@Service
@EnableAsync
public class VideoController {

    private static final String VIDEOS_PATH = "./videos/";
    private static final String DD_MM_YYYY = "ddMMyyyy";
    private static final String DOWNLOADER_ID = "downloader";
    private static final String DATA_TOPIC = "data-topic";
    private static final String PART_ZERO = "0";
    private static final String SLASH = "/";
    private static final String UNNAMED_DIR = "unnamed_dir";
    private static final String URL_REGEX = "=(\\w+)$";
    private static final String YT_DLP = "yt-dlp";
    private static final String P_FLAG = "-P";
    private static final String O_FLAG = "-o";
    private static final String F_FLAG = "-f";
    private static final String SNIPPET = "snippet";
    private static final String API_KEY = "AIzaSyCHLMWk5-MknRuQpUgWlI5kXgKuvzta0P8";
    private static final String APP_NAME = "VLCash";
    private static final String SPACE = " ";
    private static final String UNDERSCORE = "_";
    private static final String MP4_POINTED = ".mp4";
    private static final String MP4 = "mp4";
    public static final String URL = "url";

    private static final Logger log = Logger.getLogger(VideoController.class.getName());

    @Autowired
    VideoService videoService;
    @Autowired
    UploadLogsService uploadLogsService;
    @Autowired
    CacheStatusService cacheStatusService;

    /**
     * Пост конструкт срабатывает во время поднятия микросервиса. В данном случае он проверяет, что у сервиса имеется в доступе папка, куда будут сохраняться видео
     */
    @PostConstruct
    private void createVideoDir() {
        createDir(VIDEOS_PATH);
    }

    /**
     * Автоматически поднимаемый Spring'ом Kafka Consumer. В данном случае мы слушаем топик и партицию, в которую
     * пишет свои сообщения "клиентский" микросервис, а конкретно ссылки на видеоролики и userId.
     * P.s. В данной версии проекта userId не применяется
     * @param message json с ссылкой и userId
     */
    @KafkaListener(id = DOWNLOADER_ID, topicPartitions = {
            @TopicPartition(topic = DATA_TOPIC, partitions = PART_ZERO)
    })
    private void videoDownloadStarter(String message) {
        prepareToDownload(message);
    }

    /**
     * Метод, который извлекает из сообщения, полученным при помощи KafkaListener'a, ссылку и iserId.
     * После извлеченные данные отправляются в основной метод, который занимается именно скачиванием.
     * @param url ссылка на видео
     */
    private void prepareToDownload(String url) {
        String currDate = getCurrentDate();
        createDir(VIDEOS_PATH + currDate);

        Videos newVideo = new Videos();
        downloadVideo(newVideo, url, VIDEOS_PATH + currDate);
    }

    /**
     * Метод получает на вход какой-то путь до папки и создаёт её, если она не существует
     * @param path Путь до файла
     */
    private void createDir(String path) {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectory(filePath);
                log.info("Dir Created!");
            } catch (IOException e) {
                log.warning("Error while creating dir - " + e.getMessage());
            }
        }
    }

    /**
     * Метод, при помощи регулярного выражения, извлекает из YouTube ссылки id видеоролика
     * @param fullUrl Полная ссылка на видеоролик
     * @return Извлеченный id
     */
    private String getIdFromUrl(String fullUrl) {
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(fullUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return UNNAMED_DIR;
    }

    /**
     * Метод возвращает сегодняшнюю дату для создания новой папки, в которой будут сохраняться видео, скачиваемые именно сегодня
     * @return Дату в формате ddmmyyyy
     */
    private String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MM_YYYY);
        return currentDate.format(formatter);
    }

    /**
     * Основной сервис микросервиса. Собирает и запускает дополнительный процесс, в котором будет выполнена команда
     * консольного приложения yt-dlp.
     * @param newVideo Объект класса Video, данные из которого будут записываться в базу данных
     * @param videoUrl Полная ссылка на видео, которое необходимо скачать
     * @param directory Путь до папки, в которую будет скачан видеоролик
     */
    private void downloadVideo(Videos newVideo, String videoUrl, String directory) {
        createDir(directory);

        String videoTitle;
        try {
            videoTitle = getVideoTitle(videoUrl);
        } catch (IndexOutOfBoundsException e) {
            log.warning("Error with title extraction - " + e.getMessage());
            videoTitle = String.valueOf(System.currentTimeMillis());
        }

        //Перед тем как начать  загрузку видео мы заполняем базу данных необходимыми данными.
        //На данном этапе мы задаём видео состояние загрузки.
        newVideo = new Videos(videoUrl, videoTitle, directory, new Timestamp(System.currentTimeMillis()));
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
            //Отслеживание завршения процесса. Код выхода 0 означает, что видео было упешно скачано.
            //На этом этапе мы изменяем состояние видео на стороне базы данных на "загружено"
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

    /**
     * Этот метод обращается к официальным API YouTube'a для извлечения названия скачиваемого видеролика.
     * Это нужно затем, что yt-dlp может скачать видео и назвать его как ему будет удобно. А значит мы можем занести в БД некорректный путь до видео.
     * Узнать его название мы сможем лишь прочтением всех файлов в директории.
     * Во избежании лишних вычислений сервис будт вручную задавать имя, под которым надо сохранить видео.
     * @param url Ссылка на видео
     * @return Возвращает название видео "так, как на YouTube"
     */
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

    /**
     * Метод избавляется от пробелов в названии видео, заменяя их на "_".
     * @param title Нзвание видео
     * @return Формализованное название
     */
    private String formaliseVideoTitle(String title) {
        return title.trim().replaceAll(SPACE, UNDERSCORE);
    }
}
