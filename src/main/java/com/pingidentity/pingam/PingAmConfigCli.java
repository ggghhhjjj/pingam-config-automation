package com.pingidentity.pingam;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Command-line interface for PingAm configuration automation
 * Responsible for parsing command-line arguments and delegating to the application
 */
@Slf4j
@Command(
        name = "pingam-config",
        mixinStandardHelpOptions = true,
        version = "PingAm Config Automation 1.0",
        description = "Automates PingAm (former OpenAM) site configuration setup using REST API."
)
public class PingAmConfigCli implements Callable<Integer> {

    @Option(
            names = {"--config", "-c"},
            description = "Path to the properties file",
            required = true
    )
    private File configFile;

    @Option(
            names = {"--start-step", "-s"},
            description = "The workflow step to start from (default: authenticate)",
            defaultValue = "authenticate"
    )
    private String startStep;

    @Option(
            names = {"--verbose", "-v"},
            description = "Enable verbose output"
    )
    private boolean verbose;

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new PingAmConfigCli()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Parse command-line arguments and delegate to the application
     */
    @Override
    public Integer call() {
        try {
            if (verbose) {
                log.info("Starting PingAm configuration with config file: {}", configFile.getAbsolutePath());
            }

            // Validate config file
            if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
                log.error("Cannot read configuration file: {}", configFile.getAbsolutePath());
                return 1;
            }

            // Create configuration parameters
            ConfigurationParameters parameters = new ConfigurationParameters(
                    configFile.getAbsolutePath(),
                    startStep,
                    verbose
            );

            // Create and run the application
            ConfigAutomationApplication application = new ConfigAutomationApplication();
            return application.run(parameters);

        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return 1;
        }
    }
}