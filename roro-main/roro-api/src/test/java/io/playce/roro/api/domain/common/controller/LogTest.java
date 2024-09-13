package io.playce.roro.api.domain.common.controller;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogTest {

    static final String LOG_FILE_NAME = "playce-roro.log";
    static final String SQL_LITE_FILE_NAME = "registry";
    static final String LOG_DIRECTORY_NAME = "/Users/jhbaek/Downloads/logs";
    //    final String LOG_DIRECTORY_NAME = System.getProperty("catalina.home") + "/logs";
    static final int COMPRESS_LOG_FILE_COUNT = 2;


    public static void main(String[] args) throws IOException {
        List<File> compressLogFiles = new ArrayList<>();
        File testDir = new File(LOG_DIRECTORY_NAME);
        File[] fileNameList = testDir.listFiles((dir, name) -> name.contains(LOG_FILE_NAME) || name.contains(SQL_LITE_FILE_NAME));

        if (fileNameList != null && fileNameList.length > 0) {
            // 최근 수정된 파일순으로 정렬.
            Arrays.sort(fileNameList, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

            // 최대 3개 까지 등록한다.
            if (fileNameList.length > COMPRESS_LOG_FILE_COUNT) {
                compressLogFiles.addAll(Arrays.asList(fileNameList).subList(0, 3));
            } else {
                compressLogFiles.addAll(Arrays.asList(fileNameList));
            }

            for (File tempFile : fileNameList) {
                if (tempFile.getName().contains(SQL_LITE_FILE_NAME)) {
                    compressLogFiles.add(tempFile);
                }
            }

        }

        System.out.println(compressLogFiles);

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
        }

        // 파일 write
        File zipFile = new File("/Users/jhbaek/Downloads", "cc.zip");
        int readCount = 0;
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        FileOutputStream fos = new FileOutputStream(zipFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] outBuffer = new byte[65536];
        while ((readCount = bais.read(outBuffer)) > 0) {
            bos.write(outBuffer, 0, readCount);
        }


        System.out.println();
        System.out.println("압축 파일 생성 성공");

    }
}
