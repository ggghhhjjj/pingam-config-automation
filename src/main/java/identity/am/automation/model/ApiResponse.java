package identity.am.automation.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base class for all API responses
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ApiResponse {
    /**
     * Check if the response is valid according to business rules
     *
     * @return true if the response is valid, false otherwise
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Get a value from the response by path
     * Default implementation uses reflection to get nested properties
     *
     * @param path Path to the property, e.g. "user.id"
     * @return The value at the specified path, or null if not found
     */
    public Object getValue(String path) {
        try {
            String[] parts = path.split("\\.");
            Object current = this;

            for (String part : parts) {
                if (current == null) {
                    return null;
                }

                java.lang.reflect.Field field = current.getClass().getDeclaredField(part);
                field.setAccessible(true);
                current = field.get(current);
            }

            return current;
        } catch (Exception e) {
            return null;
        }
    }
}
