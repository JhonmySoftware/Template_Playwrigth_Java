package com.example.project.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds a lightweight HTML report with direct links to screenshots, traces and
 * videos. This complements Surefire/TestNG reports with execution evidence.
 */
public final class ExecutionReportManager {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<TestExecutionRecord> RECORDS = new ArrayList<>();

    private ExecutionReportManager() {
    }

    public static synchronized void record(TestExecutionRecord record) {
        RECORDS.add(record);
    }

    public static synchronized void writeReports(Path primaryReportPath, Path runSummaryPath) {
        write(primaryReportPath, buildHtml(primaryReportPath));
        write(runSummaryPath, buildHtml(runSummaryPath));
    }

    private static String buildHtml(Path reportPath) {
        List<TestExecutionRecord> sortedRecords = RECORDS.stream()
                .sorted(Comparator.comparing(TestExecutionRecord::startedAt))
                .toList();

        long passed = sortedRecords.stream().filter(record -> record.status().equals("PASS")).count();
        long failed = sortedRecords.stream().filter(record -> record.status().equals("FAIL")).count();
        long skipped = sortedRecords.stream().filter(record -> record.status().equals("SKIP")).count();

        StringBuilder builder = new StringBuilder();
        builder.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Playwright Evidence Report</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 24px; background: #f5f7fb; color: #111827; }
                        h1 { margin-bottom: 8px; }
                        p { color: #4b5563; }
                        .summary { display: flex; gap: 12px; margin: 24px 0; flex-wrap: wrap; }
                        .tile { background: #fff; border: 1px solid #dbe2ea; border-radius: 8px; padding: 16px; min-width: 140px; }
                        .label { display: block; color: #6b7280; font-size: 12px; margin-bottom: 6px; text-transform: uppercase; }
                        .value { font-size: 24px; font-weight: 700; }
                        table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #dbe2ea; }
                        th, td { padding: 12px 14px; border-bottom: 1px solid #e5e7eb; text-align: left; vertical-align: top; }
                        th { background: #eef2ff; font-size: 12px; text-transform: uppercase; color: #374151; }
                        .status { font-weight: 700; }
                        .PASS { color: #166534; }
                        .FAIL { color: #991b1b; }
                        .SKIP { color: #92400e; }
                        .artifacts a { display: inline-block; margin-right: 12px; margin-bottom: 6px; }
                        .preview { display: grid; gap: 10px; min-width: 320px; }
                        .preview img { max-width: 320px; border: 1px solid #d1d5db; border-radius: 6px; }
                        .preview video { width: 320px; border: 1px solid #d1d5db; border-radius: 6px; background: #111827; }
                        .muted { color: #6b7280; font-size: 12px; }
                        .error { color: #991b1b; white-space: pre-wrap; }
                        code { background: #f3f4f6; padding: 2px 6px; border-radius: 4px; }
                    </style>
                </head>
                <body>
                <h1>Playwright Evidence Report</h1>
                <p>Evidence-oriented execution summary with direct access to artifacts for each test.</p>
                <div class="summary">
                """);

        appendTile(builder, "Total", String.valueOf(sortedRecords.size()));
        appendTile(builder, "Passed", String.valueOf(passed));
        appendTile(builder, "Failed", String.valueOf(failed));
        appendTile(builder, "Skipped", String.valueOf(skipped));
        builder.append("""
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Test</th>
                            <th>Status</th>
                            <th>Started</th>
                            <th>Duration</th>
                            <th>Artifacts</th>
                            <th>Preview</th>
                            <th>Details</th>
                        </tr>
                    </thead>
                    <tbody>
                """);

        for (TestExecutionRecord record : sortedRecords) {
            builder.append("<tr>");
            builder.append("<td><strong>")
                    .append(escapeHtml(record.className()))
                    .append("</strong><br><code>")
                    .append(escapeHtml(record.testName()))
                    .append("</code></td>");
            builder.append("<td class=\"status ")
                    .append(record.status())
                    .append("\">")
                    .append(record.status())
                    .append("</td>");
            builder.append("<td>")
                    .append(record.startedAt().format(DATE_TIME_FORMATTER))
                    .append("</td>");
            builder.append("<td>")
                    .append(record.duration().toSecondsPart() + (record.duration().toMinutesPart() * 60))
                    .append(".")
                    .append(String.format("%03d", record.duration().toMillisPart()))
                    .append(" s</td>");
            builder.append("<td class=\"artifacts\">");
            appendArtifactLink(builder, "Screenshot", record.screenshotPath(), reportPath);
            appendArtifactLink(builder, "Trace ZIP", record.tracePath(), reportPath);
            appendArtifactLink(builder, "Video", record.videoPath(), reportPath);
            builder.append("</td>");
            builder.append("<td class=\"preview\">");
            appendScreenshotPreview(builder, record.screenshotPath(), reportPath);
            appendVideoPreview(builder, record.videoPath(), reportPath);
            builder.append("</td>");
            builder.append("<td>");
            if (record.errorMessage() != null && !record.errorMessage().isBlank()) {
                builder.append("<div class=\"error\">")
                        .append(escapeHtml(record.errorMessage()))
                        .append("</div>");
            } else {
                builder.append("No errors.");
            }
            if (record.tracePath() != null) {
                builder.append("<div class=\"muted\">Trace: open with Playwright Trace Viewer.</div>");
            }
            builder.append("</td>");
            builder.append("</tr>");
        }

        builder.append("""
                    </tbody>
                </table>
                </body>
                </html>
                """);
        return builder.toString();
    }

    private static void appendTile(StringBuilder builder, String label, String value) {
        builder.append("<div class=\"tile\"><span class=\"label\">")
                .append(escapeHtml(label))
                .append("</span><span class=\"value\">")
                .append(escapeHtml(value))
                .append("</span></div>");
    }

    private static void appendArtifactLink(StringBuilder builder, String label, Path path, Path reportPath) {
        if (path == null) {
            builder.append("<span>").append(escapeHtml(label)).append(": n/a</span><br>");
            return;
        }

        builder.append("<a href=\"")
                .append(toRelativeHref(reportPath, path))
                .append("\" target=\"_blank\" rel=\"noopener noreferrer\">")
                .append(escapeHtml(label))
                .append("</a>");
    }

    private static void appendScreenshotPreview(StringBuilder builder, Path screenshotPath, Path reportPath) {
        if (screenshotPath == null) {
            builder.append("<span class=\"muted\">No screenshot</span>");
            return;
        }

        String href = toRelativeHref(reportPath, screenshotPath);
        builder.append("<a href=\"")
                .append(href)
                .append("\" target=\"_blank\" rel=\"noopener noreferrer\">")
                .append("<img alt=\"Screenshot preview\" src=\"")
                .append(href)
                .append("\"/>")
                .append("</a>");
    }

    private static void appendVideoPreview(StringBuilder builder, Path videoPath, Path reportPath) {
        if (videoPath == null) {
            builder.append("<span class=\"muted\">No video</span>");
            return;
        }

        String href = toRelativeHref(reportPath, videoPath);
        builder.append("<video controls preload=\"metadata\">")
                .append("<source src=\"")
                .append(href)
                .append("\" type=\"video/webm\">")
                .append("Your browser does not support embedded video.")
                .append("</video>");
    }

    private static String toRelativeHref(Path reportPath, Path targetPath) {
        Path reportDirectory = reportPath.getParent();
        Path normalizedReportDirectory = reportDirectory.toAbsolutePath().normalize();
        Path normalizedTargetPath = targetPath.toAbsolutePath().normalize();
        return normalizedReportDirectory.relativize(normalizedTargetPath).toString().replace('\\', '/');
    }

    private static void write(Path path, String html) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, html, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write execution report: " + path, exception);
        }
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String toStatusName(int statusCode) {
        return switch (statusCode) {
            case 1 -> "PASS";
            case 2 -> "FAIL";
            case 3 -> "SKIP";
            default -> "UNKNOWN";
        };
    }

    public record TestExecutionRecord(
            String className,
            String testName,
            String status,
            LocalDateTime startedAt,
            Duration duration,
            Path screenshotPath,
            Path tracePath,
            Path videoPath,
            String errorMessage
    ) {
    }
}
