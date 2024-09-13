package io.playce.roro.api.domain.common.service;


import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.common.property.CommonProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class LogDownloadService {
    final String LOG_FILE_NAME = "playce-roro.log";
    final String SQL_LITE_FILE_NAME = "registry";
    final String LOG_DIRECTORY_NAME = System.getProperty("catalina.home") + "/logs";
    final int COMPRESS_LOG_FILE_COUNT = 2;

    public ByteArrayInputStream getLogZipFile() {
        List<File> compressLogFiles = new ArrayList<>();
        File logDir = new File(LOG_DIRECTORY_NAME);
        File[] logFileList = logDir.listFiles((dir, name) -> name.contains(LOG_FILE_NAME));

        File registryDir = new File(CommonProperties.getWorkDir());
        File[] registryFileList = registryDir.listFiles((dir, name) -> name.contains(SQL_LITE_FILE_NAME));

        if (logFileList != null && logFileList.length > 0) {
            // 최근 수정된 파일순으로 정렬.
            Arrays.sort(logFileList, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

            // 최대 3개 까지 등록한다.
            if (logFileList.length > COMPRESS_LOG_FILE_COUNT) {
                compressLogFiles.addAll(Arrays.asList(logFileList).subList(0, 3));
            } else {
                compressLogFiles.addAll(Arrays.asList(logFileList));
            }

            // add registry file
            if (registryFileList != null && registryFileList.length > 0) {
                compressLogFiles.addAll(Arrays.asList(registryFileList));
            }

            byte[] buf = new byte[4096];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream out = new ZipOutputStream(baos)) {
                for (File logFile : compressLogFiles) {
                    try (FileInputStream in = new FileInputStream(logFile)) {
                        ZipEntry ze = new ZipEntry(logFile.getName());
                        out.putNextEntry(ze);
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE);
            }

            return new ByteArrayInputStream(baos.toByteArray());
        } else {
            throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE);
        }
    }
}
