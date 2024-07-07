package ru.choomandco.VLCacheFinal.clientSide;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlCacheStatus.CacheStatus;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlCacheStatus.CacheStatusService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUploadLogs.UploadLogs;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUploadLogs.UploadLogsService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUsers.Users;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUsers.UsersService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos.VideoService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos.Videos;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.logging.Logger;


/**
 * Один из двух основных микросервисов. Занимается обработкой клиентских запросов при помощи OpenAPI
 */
@RestController
@Service
@RequestMapping("/api/service-api")
public class ApiHandler {

    private static final int PART_ZERO_SEND = 0;
    private static final int PART_TWO_SEND = 2;
    private static final String DATA_TOPIC = "data-topic";
    private static final String JSON_URL_FIELD = "url";
    private static final String JSON_USER_ID_FILED = "userId";
    private static final String COMPLETED = "completed";
    private static final String DELETE_ALL_OF_THEM = "DELETE ALL OF THEM";
    private static final String LOCALHOST = "http://localhost:8101/";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = Logger.getLogger(ApiHandler.class.getName());

    @Autowired
    private CacheStatusService cacheStatusService;
    @Autowired
    private UploadLogsService uploadLogsService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Это API нужно для нескольких целей.
     * 1. Получение ссылки от пользователя и проверка состояния видео на предмет нахождения его на файловом сервере.
     * 2. Если видео уже закешировано - отдаём пользователю положительный контент
     * 3. Если видео не загружено - данный микросервис передает запрос на загрузочный микросервис для начала процесса закачки
     * @param videoUrl Ссылка на видео, которое пользователь хочет посмотреть/скачать
     * @param userId Id пользователя, в данной версии проекта не используется
     * @return Возвращает ResponseEntity с одним из нескольких HttpStatus кодовЮ согласно с состоянием закешированности видео
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/init-request")
    private ResponseEntity<String> initialRequest(
            @RequestParam(name = JSON_URL_FIELD) String videoUrl,
            @RequestParam(name = JSON_USER_ID_FILED) String userId) {
        Optional<Videos> exactVideoInfo = videoService.selectById(videoUrl);

        Optional<CacheStatus> cacheStatus = cacheStatusService.selectById(videoUrl);

        if (exactVideoInfo.isEmpty()) {
            kafkaTemplate.send(DATA_TOPIC, PART_ZERO_SEND, null, videoUrl);
            log.info("New video cacheing - " + videoUrl);
            return new ResponseEntity<>("Video is now cacheing!", HttpStatus.CREATED);
        } else {
            Optional<UploadLogs> uploadStatus = uploadLogsService.selectById(videoUrl);
            if (uploadStatus.isPresent() && uploadStatus.get().getStatus().equals(COMPLETED)) {
                log.info("Video asked - " + exactVideoInfo.toString());
                return new ResponseEntity<>(HttpStatus.FOUND);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    /**
     * API для авторизация пользователя. Принимает на вход логин и пароль клиента и проверяет на предмет нахождения этих
     * данных в базе пользователей.
     * @param name Логин пользователя
     * @param pass Пароль пользователя
     * @return Возвращает ответ в формате HttpStatus, зарегистрирован пользователь или нет
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/login")
    private ResponseEntity<String> userLogin(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "pass") String pass) {
        Optional<Users> user = usersService.selectByUserName(name);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            if (user.get().getUsername().equals(name) && user.get().getPasswordHash().equals(pass)) {
                log.info("User logged - " + user.toString());
                return new ResponseEntity<>(String.valueOf(user.get().getId()), HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>(String.valueOf(user.get().getId()), HttpStatus.UNAUTHORIZED);
            }
        }
    }


    /**
     * API для регистрации нового пользователя в сервисе.
     * @param name Логин нового пользователя
     * @param pass Пароль нового пользователя
     * @return Если пользователь уже в базе, возвращаем отрицательный HttpStatus; положительный ответ, если пользователь
     * успешно зарегистрирован
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/signup")
    private ResponseEntity<String> userSignUp(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "pass") String pass) {
        if (usersService.selectByUserName(name).isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            Users newUser = new Users(name, pass, new Timestamp(System.currentTimeMillis()));
            usersService.insertNewUser(newUser);
            log.info("New user added - " + newUser.toString());
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
    }

    /**
     * Данное API возвращает клиенту URI/ссылку/путь на видео файл, расположенный на файловом сервере
     * @param videoUrl Ссылка на видео, которое надо показать
     * @return Если видео существует на файловом сервере, возвращает положительный ответ и ссылку на файл
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/use-service")
    private ResponseEntity<String> useServiceStarter(
            @RequestParam(name = JSON_URL_FIELD) String videoUrl) {

        Optional<Videos> video = videoService.selectById(videoUrl);
        if (video.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            String response = LOCALHOST + video.get().getFilePathWithoutVideos();
            log.info("Use service request - " + response);
            return new ResponseEntity<>(response, HttpStatus.FOUND);
        }
    }
}
