package identity.am.automation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains configuration parameters for the application
 * This class separates the CLI parsing from the application logic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationParameters {
    private String configFilePath;
    private String startStep;
    private boolean verbose;
}