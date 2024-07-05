package ru.choomandco.VLCacheFinal.fileGarbage.service.dataBase;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlCacheStatus.CacheStatusService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUploadLogs.UploadLogsService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUsers.UsersService;
import ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos.VideoService;

@Service
public class DBHandlerTest implements InitializingBean {
    @Autowired
    CacheStatusService cacheStatusService;
    @Autowired
    UploadLogsService uploadLogsService;
    @Autowired
    UsersService usersService;
    @Autowired
    VideoService videoService;

    public DBHandlerTest() {}

    public void testSelectAllData() {

        for (var elem : cacheStatusService.selectAll()) {
            System.out.println(elem.toString());
        }

        for (var elem : uploadLogsService.selectAll()) {
            System.out.println(elem.toString());
        }

        for (var elem : usersService.selectAll()) {
            System.out.println(elem.toString());
        }

        for (var elem : videoService.selectAll()) {
            System.out.println(elem.toString());
        }
    }

    private static DBHandlerTest instance = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public static DBHandlerTest getInstance() {
        return instance;
    }
}
