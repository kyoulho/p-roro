package io.playce.roro.host.scan;

import io.playce.roro.host.scan.dto.ScanResult;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface HostScanner {
    @Async("hostScanTaskExecutor")
    CompletableFuture<ScanResult> scanIpAddress(String ipAddress) throws TaskRejectedException;
}
