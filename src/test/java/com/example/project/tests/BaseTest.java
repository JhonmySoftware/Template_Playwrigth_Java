package com.example.project.tests;

import com.example.project.config.FrameworkConfig;
import com.example.project.config.PlaywrightFactory;
import com.example.project.utils.ArtifactManager;
import com.example.project.utils.ExecutionReportManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.nio.file.Path;

/**
 * Central test lifecycle. Browser is expensive, so it is created once per class.
 * Context and page are isolated per test method to keep tests independent.
 */
public abstract class BaseTest {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected FrameworkConfig frameworkConfig;
    protected PlaywrightFactory playwrightFactory;
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext browserContext;
    protected Page page;
    protected ArtifactManager artifactManager;
    private LocalDateTime testStartTime;

    @BeforeClass(alwaysRun = true)
    public void setUpSuite() {
        frameworkConfig = new FrameworkConfig();
        playwrightFactory = new PlaywrightFactory(frameworkConfig);
        artifactManager = new ArtifactManager(frameworkConfig);

        try {
            playwright = playwrightFactory.createPlaywright();
            browser = playwrightFactory.createBrowser(playwright);
            logger.info("Browser started: {}", frameworkConfig.getBrowserName());
            logger.info("Execution artifacts directory: {}", artifactManager.getRunDirectory());
        } catch (RuntimeException exception) {
            tearDownSuite();
            throw new IllegalStateException("Playwright bootstrap failed.", exception);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpTest(Method method) {
        try {
            browserContext = playwrightFactory.createBrowserContext(browser);
            page = browserContext.newPage();
            page.setDefaultTimeout(frameworkConfig.getDefaultTimeoutMs());
            page.setDefaultNavigationTimeout(frameworkConfig.getNavigationTimeoutMs());

            if (frameworkConfig.isTraceEnabled()) {
                browserContext.tracing().start(new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true));
            }

            testStartTime = LocalDateTime.now();
            logger.info("Starting test: {}", method.getName());
        } catch (RuntimeException exception) {
            tearDownTest(null);
            throw new IllegalStateException("Unable to create browser context or page.", exception);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownTest(ITestResult result) {
        Path screenshotPath = captureScreenshot(result);
        Path tracePath = stopTrace(result);
        Video video = page == null ? null : page.video();

        closeQuietly(page, "page");
        page = null;

        closeQuietly(browserContext, "browser context");
        browserContext = null;

        Path videoPath = persistVideo(result, video);
        writeExecutionEvidence(result, screenshotPath, tracePath, videoPath);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownSuite() {
        closeQuietly(browser, "browser");
        browser = null;

        closeQuietly(playwright, "playwright");
        playwright = null;
    }

    private Path captureScreenshot(ITestResult result) {
        if (result == null || page == null) {
            return null;
        }

        boolean shouldCapture = result.getStatus() == ITestResult.FAILURE
                ? frameworkConfig.isScreenshotOnFailureEnabled()
                : frameworkConfig.isScreenshotOnSuccessEnabled();
        if (!shouldCapture) {
            return null;
        }

        Path screenshotPath = artifactManager.buildScreenshotPath(
                result.getTestClass().getRealClass().getSimpleName(),
                buildTestArtifactName(result)
        );

        try {
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));
            Reporter.log("Screenshot: " + screenshotPath, true);
            logger.info("Screenshot stored at {}", screenshotPath);
            return screenshotPath;
        } catch (RuntimeException exception) {
            logger.warn("Unable to capture screenshot: {}", exception.getMessage());
            return null;
        }
    }

    private Path stopTrace(ITestResult result) {
        if (!frameworkConfig.isTraceEnabled() || browserContext == null || result == null) {
            return null;
        }

        Path tracePath = artifactManager.buildTracePath(
                result.getTestClass().getRealClass().getSimpleName(),
                buildTestArtifactName(result)
        );

        try {
            browserContext.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            Reporter.log("Trace file: " + tracePath, true);
            logger.info("Trace stored at {}", tracePath);
            return tracePath;
        } catch (RuntimeException exception) {
            logger.warn("Unable to persist Playwright trace: {}", exception.getMessage());
            return null;
        }
    }

    private Path persistVideo(ITestResult result, Video video) {
        if (result == null || video == null || !frameworkConfig.isVideoEnabled()) {
            return null;
        }

        Path videoPath = artifactManager.buildVideoPath(
                result.getTestClass().getRealClass().getSimpleName(),
                buildTestArtifactName(result)
        );

        try {
            video.saveAs(videoPath);
            Reporter.log("Video file: " + videoPath, true);
            logger.info("Video stored at {}", videoPath);
            return videoPath;
        } catch (RuntimeException exception) {
            logger.warn("Unable to persist video: {}", exception.getMessage());
            return null;
        }
    }

    private void writeExecutionEvidence(ITestResult result, Path screenshotPath, Path tracePath, Path videoPath) {
        if (result == null || testStartTime == null) {
            return;
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(testStartTime, endTime);
        String errorMessage = result.getThrowable() == null ? null : result.getThrowable().getMessage();

        ExecutionReportManager.record(new ExecutionReportManager.TestExecutionRecord(
                result.getTestClass().getRealClass().getSimpleName(),
                buildDisplayName(result),
                ExecutionReportManager.toStatusName(result.getStatus()),
                testStartTime,
                duration,
                screenshotPath,
                tracePath,
                videoPath,
                errorMessage
        ));
        ExecutionReportManager.writeReports(
                artifactManager.getEvidenceReportPath(),
                artifactManager.getLatestRunSummaryPath()
        );
        logger.info("Evidence report updated: {}", artifactManager.getEvidenceReportPath());
    }

    private String buildDisplayName(ITestResult result) {
        String parameters = buildParametersSuffix(result, ", ", "[", "]");
        return result.getMethod().getMethodName() + parameters;
    }

    private String buildTestArtifactName(ITestResult result) {
        return result.getMethod().getMethodName() + buildParametersSuffix(result, "_", "_", "");
    }

    private String buildParametersSuffix(ITestResult result, String separator, String prefix, String suffix) {
        Object[] parameters = result.getParameters();
        if (parameters == null || parameters.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(prefix);
        for (int index = 0; index < parameters.length; index++) {
            Object parameter = parameters[index];
            if (parameter == null) {
                builder.append("null");
            } else {
                builder.append(parameter.toString().replaceAll("[^a-zA-Z0-9._-]", "_"));
            }

            if (index < parameters.length - 1) {
                builder.append(separator);
            }
        }
        return builder.append(suffix).toString();
    }

    private void closeQuietly(AutoCloseable resource, String resourceName) {
        if (resource == null) {
            return;
        }

        try {
            resource.close();
        } catch (Exception exception) {
            logger.warn("Failed to close {} cleanly: {}", resourceName, exception.getMessage());
        }
    }
}
