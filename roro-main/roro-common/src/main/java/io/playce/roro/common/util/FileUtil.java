/*
 * Copyright 2020 The Playce-RoRo Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Nov 26, 2020		First Draft.
 */
package io.playce.roro.common.util;

import com.ibm.icu.text.CharsetDetector;
import io.playce.roro.common.exception.RoRoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Slf4j
public class FileUtil {

    /**
     * Download string.
     *
     * @param uri       the uri
     * @param targetDir the target dir
     *
     * @return the string
     *
     * @throws IOException the io exception
     */
    public static String download(String uri, String targetDir) throws IOException {
        uri = FilenameUtils.separatorsToSystem(uri);
        if (uri.startsWith("/")) {
            uri = "file://" + uri;
        }

        URL source = new URL(uri);
        String fileName = FilenameUtils.getName(source.getPath());
        String targetFile = FilenameUtils.separatorsToSystem(targetDir + File.separator + fileName);
        FileUtils.copyURLToFile(source, new File(targetFile));

        return targetFile;
    }

    /**
     * @param file
     *
     * @return
     *
     * @throws IOException
     */
    private static String detectCharset(File file) throws InterruptedException {
        String encoding = null;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[4096];

            UniversalDetector detector = new UniversalDetector(null);

            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();

            encoding = detector.getDetectedCharset();

            if (encoding != null && detector.isDone() && Charset.isSupported(encoding)) {
                detector.reset();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred while detect a file encoding.", e);
        }

        return encoding;
    }

    /**
     * @param file
     *
     * @return
     *
     * @throws IOException
     */
    public static String getFileContents(File file) throws InterruptedException {
        String contents = null;

        try {
            //*
            CharsetDetector detector = new CharsetDetector();

            try (InputStream input = new FileInputStream(file)) {
                byte[] data = org.apache.commons.io.IOUtils.toByteArray(input, file.length());
                detector.setDeclaredEncoding("UTF-8");
                detector.setText(data);
                detector.detectAll();

                for (com.ibm.icu.text.CharsetMatch m : detector.detectAll()) {
                    if (m.getName().toLowerCase().equals("euc-kr")) {
                        contents = m.getString();
                        break;
                    }
                }

                if (contents == null) {
                    contents = detector.detect().getString();
                }
            }
            /*/
            String charset = detectCharset(file);

            log.debug("[{}]'s charset is [{}]", file.getAbsolutePath(), charset);

            contents = org.apache.commons.io.IOUtils.toString(new FileReader(file));

            if (StringUtils.isNotEmpty(charset)) {
                if (!"UTF8".equals(charset.toUpperCase()) && !"UTF-8".equals(charset.toUpperCase())) {
                    contents = new String(contents.getBytes(charset), "UTF-8");
                }
            } else {
                CharsetDetector detector = new CharsetDetector();

                try (InputStream input = new FileInputStream(file)) {
                    byte[] data = org.apache.commons.io.IOUtils.toByteArray(input, file.length());
                    detector.setDeclaredEncoding("UTF-8");
                    detector.setText(data);
                    detector.detectAll();

                    for (com.ibm.icu.text.CharsetMatch m : detector.detectAll()) {
                        if (m.getName().toLowerCase().equals("euc-kr")) {
                            contents = m.getString();
                            break;
                        }
                    }

                    if (contents == null) {
                        contents = detector.detect().getString();
                    }
                }
            }
            //*/
        } catch (IOException e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred while get a file contents.", e);
        }

        return contents;
    }

    /**
     * Unzip string.
     *
     * @param archiveFile the archive file
     * @param targetDir   the target dir
     *
     * @return the string
     *
     * @throws Exception the exception
     */
    public static String unzip(String archiveFile, String targetDir) throws Exception {
        String fqfn = null;
        String entryName = null;

        // switching separator to current system.
        archiveFile = FilenameUtils.separatorsToSystem(archiveFile);
        targetDir = FilenameUtils.separatorsToSystem(targetDir);

        InputStream is = new FileInputStream(archiveFile);

        int seq = 0;
        if (archiveFile.toLowerCase().endsWith(".zip") || archiveFile.toLowerCase().endsWith(".ear") ||
                archiveFile.toLowerCase().endsWith(".war") || archiveFile.toLowerCase().endsWith(".jar")) {
            ZipInputStream zis = new ZipInputStream(is);
            FileOutputStream fos = null;

            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                entryName = FilenameUtils.separatorsToSystem(entry.getName());

                if (seq++ == 0) {
                    fqfn = targetDir + File.separator + entryName;
                }

                if (entry.isDirectory()) {
                    File dir = new File(targetDir, entryName);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    continue;
                }

                unzip(targetDir, entryName, zis);
            }

            IOUtils.closeQuietly(zis);
        } else if (archiveFile.toLowerCase().endsWith(".tar.gz")) {
            TarArchiveInputStream fin = new TarArchiveInputStream(new GzipCompressorInputStream(is));
            FileOutputStream fos = null;

            TarArchiveEntry entry = null;
            while ((entry = fin.getNextTarEntry()) != null) {
                entryName = FilenameUtils.separatorsToSystem(entry.getName());

                if (seq++ == 0) {
                    fqfn = targetDir + File.separator + entryName;
                }

                if (entry.isDirectory()) {
                    File dir = new File(targetDir, entryName);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    continue;
                }

                unzip(targetDir, entryName, fin);
            }

            IOUtils.closeQuietly(fin);
        } else {
            throw new Exception("Unzip only supports [tar.gz], [zip], [ear], [war] and [jar] files.");
        }

        return fqfn;
    }

    /**
     * @param targetDir
     * @param entryName
     * @param fin
     *
     * @throws Exception
     */
    public static void unzip(String targetDir, String entryName, InputStream fin) throws Exception {
        File currFile = new File(targetDir, entryName);
        String fileName = currFile.getName();

        File parent = currFile.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new Exception("Can not create a directory(" + parent.getAbsolutePath() + ").");
            }
        }

        FileOutputStream fos = new FileOutputStream(currFile);
        IOUtils.copy(fin, fos);

        if (fileName.endsWith(".sh") || fileName.endsWith(".bat")) {
            currFile.setExecutable(true, false);
        }

        IOUtils.closeQuietly(fos);
    }
}
//end of FileUtil.java