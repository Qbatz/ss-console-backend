package com.smartstay.console.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AgentActivityUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Convert any object to Map<String, Object>
     * Used for oldObject / newObject snapshots
     */
    public static Map<String, Object> singleObjectMap(Object obj) {
        if (obj == null) {
            return null;
        }
        return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Used for CREATE and DELETE operations
     *
     * CREATE  -> old = null, new = value
     * DELETE  -> old = value, new = null
     */
    public static Map<String, Map<String, Object>> changesMap(Object obj, boolean isCreate) {

        Map<String, Object> objectMap = singleObjectMap(obj);
        Map<String, Map<String, Object>> changes = new HashMap<>();

        if (objectMap == null || objectMap.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Map<String, Object> diff = new HashMap<>();

            if (isCreate) {
                diff.put("old", null);
                diff.put("new", entry.getValue());
            } else {
                diff.put("old", entry.getValue());
                diff.put("new", null);
            }

            changes.put(entry.getKey(), diff);
        }

        return changes;
    }

    /**
     * Used for UPDATE operations
     * Compares old and new objects and returns only changed fields
     */
    public static Map<String, Map<String, Object>> differences(Object oldObj, Object newObj) {

        Map<String, Object> oldMap = singleObjectMap(oldObj);
        Map<String, Object> newMap = singleObjectMap(newObj);

        if (oldMap == null || newMap == null) {
            return null;
        }

        Map<String, Map<String, Object>> changes = new HashMap<>();

        for (String key : newMap.keySet()) {

            Object oldVal = oldMap.get(key);
            Object newVal = newMap.get(key);

            if (!Objects.equals(oldVal, newVal)) {

                Map<String, Object> diff = new HashMap<>();
                diff.put("old", oldVal);
                diff.put("new", newVal);

                changes.put(key, diff);
            }
        }

        return changes.isEmpty() ? null : changes;
    }

    public static String buildDescription(ActivityType type,
                                          Source source) {

        if (type == null || source == null) {
            throw new IllegalArgumentException("ActivityType and Source required to build description");
        }

        return switch (type) {
            case CREATE -> getCreateDescription(source);
            case UPDATE -> getUpdateDescription(source);
            case DELETE -> getDeleteDescription(source);
            case LOGIN -> getLoginDescription(source);
            default -> type.name() + " action performed for " + source.name();
        };
    }

    private static String getCreateDescription(Source source) {
        return switch (source) {
            case AGENT -> "Created a new agent";
            case MOCK_AGENT -> "Created a new mock agent";
            case AGENT_ROLE -> "Created a new agent role";
            case SUBSCRIPTION -> "Created a new subscription";
            default -> "Created successfully";
        };
    }

    private static String getUpdateDescription(Source source) {
        return switch (source) {
            case AGENT -> "Updated an agent";
            case MOCK_AGENT -> "Updated a mock agent";
            case AGENT_ROLE -> "Updated an agent role";
            case SUBSCRIPTION -> "Updated subscription";
            case RESET_PASSWORD -> "Updated password";
            default -> "Updated successfully";
        };
    }

    private static String getDeleteDescription(Source source) {
        return switch (source) {
            case AGENT -> "Deleted an agent";
            case MOCK_AGENT -> "Deleted a mock agent";
            case AGENT_ROLE -> "Deleted an agent role";
            case SUBSCRIPTION -> "Deleted subscription";
            default -> "Deleted successfully";
        };
    }

    private static String getLoginDescription(Source source) {
        return switch (source) {
            case AGENT_LOGIN -> "Agent logged in";
            case MOCK_AGENT_LOGIN -> "Mock agent logged in";
            default -> "Login successful";
        };
    }
}
