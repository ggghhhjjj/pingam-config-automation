# PingAm Configuration Automation

A Java 11 project for automating PingAm (formerly OpenAM) site configuration using the PingAm REST API.

## Overview

This project provides a framework for automating configuration tasks in PingAm environments. It allows for:

- Creating sequences of REST API calls (workflows)
- Extracting and reusing data from responses in subsequent requests
- Conditional branching based on response validation
- Configuration via properties files

## Project Structure

- `client` - HTTP client for API interactions
- `config` - Configuration handling
- `exception` - Custom exceptions
- `model` - Request/response data models
- `workflow` - Workflow engine for orchestrating API calls

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Access to a PingAm instance

### Building the Project

```bash
mvn clean package
```

This will create a JAR file with dependencies in the `target` directory.

### Configuration

Create a properties file (e.g., `config.properties`) with your PingAm configuration:

```properties
# PingAm Server Configuration
api.baseUrl=http://your-pingam-server:8080/sso
api.version=resource=2.0,protocol=1.0

# Authentication
api.username=amAdmin
api.password=yourPassword

# Other configuration parameters...
```

### Running the Application

```bash
java -jar target/pingam-config-automation-1.0-SNAPSHOT-jar-with-dependencies.jar --config=config.properties
```

Command-line options:

- `--config`, `-c`: Path to the properties file (required)
- `--start-step`, `-s`: The workflow step to start from (default: "authenticate")
- `--verbose`, `-v`: Enable verbose output
- `--help`, `-h`: Show help message
- `--version`, `-V`: Show version information

Examples:

```bash
# Basic usage with config file
java -jar target/pingam-config-automation-1.0-SNAPSHOT-jar-with-dependencies.jar --config=config.properties

# Start from a specific workflow step
java -jar target/pingam-config-automation-1.0-SNAPSHOT-jar-with-dependencies.jar --config=config.properties --start-step=createRealm

# Enable verbose mode
java -jar target/pingam-config-automation-1.0-SNAPSHOT-jar-with-dependencies.jar --config=config.properties --verbose
```
## Extending the Project

### Creating New API Requests/Responses

1. Create request and response classes in the `model` package extending `ApiRequest` and `ApiResponse`
2. Implement the necessary methods and properties

Example:

```java
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MyNewRequest extends ApiRequest {
    @JsonProperty("propertyName")
    private String propertyName;
    
    public static MyNewRequest createDefault() {
        return MyNewRequest.builder()
                .endpoint("/your/endpoint")
                .method(HttpMethod.POST)
                .propertyName("defaultValue")
                .build()
                .withHeader("iPlanetDirectoryPro", "${auth.token}");
    }
}
```

### Adding New Workflow Steps

Extend the `registerWorkflowSteps` method in `ConfigAutomationApplication`:

```java
WorkflowStep<MyNewRequest, MyNewResponse> myNewStep = 
        new WorkflowStep<>("myNewStep", 
                MyNewRequest.createDefault(), 
                MyNewResponse.class);

myNewStep
        .withDataExtractor("extracted.property", MyNewResponse::getSomeProperty)
        .withDefaultNextStep("nextStep");

workflowEngine.registerStep(myNewStep);
```

## API Flow Examples

### Authentication Flow

1. Submit authentication request with username/password
2. Extract token from response
3. Use token in subsequent requests

### Realm Creation Flow

1. Authenticate to get token
2. Create realm with token
3. List realms to verify creation
4. Configure LDAP for the realm

## License

This project is licensed under the MIT License - see the LICENSE file for details.
