/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * SangCheon Park   Aug 26, 2022		    First Draft.
 */
package io.playce.roro.common.setting;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class SettingsConstants {
    public static final String RORO_WEB_TERMINAL = "roro.web-terminal";
    public static final String ENABLE_MONITORING_SCHEDULE = "enable.monitoring.schedule";
    public static final String RORO_SSH_IP_ADDRESS = "roro.ssh.ip-address";
    public static final String RORO_SSH_PORT = "roro.ssh.port";
    public static final String RORO_SSH_USER_NAME = "roro.ssh.user-name";
    public static final String RORO_SSH_PASSWORD = "roro.ssh.password";
    public static final String RORO_SSH_PPK_FILE_PATH = "roro.ssh.ppk-file-path";

    public static final String RORO_SSH_CONNECT_TIMEOUT = "ssh.connect.timeout";
    public static final String RORO_SSH_USE_BOUNCY_CASTLE_PROVIDER = "ssh.use.bouncy.castle.provider";
    public static final String RORO_SSH_SKIP_MESSAGE = "ssh.skip.messages";

    public static final String ENABLE_SCHEDULED_SCAN = "enable.scheduled.scan";
    public static final String RORO_MIDDLEWARE_AUTO_SCAN = "roro.middleware-auto-scan";
    public static final String RORO_MIDDLEWARE_AUTO_SCAN_AFTER_SERVER_SCAN = "roro.middleware-auto-scan.after-server-scan";
    public static final String RORO_APPLICATION_AUTO_SCAN = "roro.application-auto-scan";
    public static final String APPSCAN_FILE_EXTENSIONS = "appscan.file-extensions";
    public static final String APPSCAN_COPY_ONLY_MATCHED_EXTENSIONS = "appscan.copy.only-matched-extensions";
    public static final String APPSCAN_EXCLUDE_FILENAMES = "appscan.exclude-filenames";
    public static final String APPSCAN_EXCLUDE_DOMAINS = "appscan.exclude-domains";
    public static final String APPSCAN_REMOVE_FILES_AFTER_SCAN = "appscan.remove.files-after-scan";
    public static final String APPSCAN_COPY_IGNORE_FILENAMES = "appscan.copy.ignore-filenames";
    public static final String WINDOWS_POWERSHELL_USE_OUTFILE = "windows.powershell.use-outfile";
    public static final String RORO_MIGRATION_ENABLED = "roro.migration.enabled";
    public static final String RORO_MIGRATION_DIR_REMOVE = "roro.migration.dir.remove";
    public static final String RORO_MIGRATION_BUCKET_NAME = "roro.migration.bucket.name";
    public static final String RORO_MIGRATION_BUCKET_REGION = "roro.migration.bucket.region";
    public static final String RORO_MIGRATION_BUCKET_REMOVE = "roro.migration.bucket.remove";
    public static final String RORO_MIGRATION_INCLUDE_SYSTEM_UID = "roro.migration.include.system.uid";

}