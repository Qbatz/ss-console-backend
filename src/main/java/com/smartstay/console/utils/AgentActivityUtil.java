package com.smartstay.console.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AgentActivityUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> objectWrapper(Object obj) {

        if (obj == null) {
            return null;
        }

        Map<String, Object> wrapper = new HashMap<>();

        if (obj instanceof List<?> list) {
            wrapper.put("type", "LIST");
            wrapper.put("data", mapper.convertValue(list,
                    new TypeReference<List<Map<String, Object>>>() {}));
        } else {
            wrapper.put("type", "OBJECT");
            wrapper.put("data", mapper.convertValue(obj,
                    new TypeReference<Map<String, Object>>() {}));
        }

        return wrapper;
    }

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
    public static Map<String, Map<String, Object>> changesMap(Object obj, boolean isCreate, String idName) {

        if (obj == null) {
            return null;
        }

        Map<String, Map<String, Object>> changes = new HashMap<>();

        // LIST SUPPORT
        if (obj instanceof List<?> list) {

            for (Object item : list) {

                Map<String, Object> record = singleObjectMap(item);
                String id = extractId(idName, record);

                Map<String, Object> diff = new HashMap<>();

                if (isCreate) {
                    diff.put("old", null);
                    diff.put("new", record);
                } else {
                    diff.put("old", record);
                    diff.put("new", null);
                }

                changes.put(id, diff);
            }

            return changes;
        }

        // SINGLE OBJECT
        Map<String, Object> objectMap = singleObjectMap(obj);
        String id = extractId(idName, objectMap);

        Map<String, Object> diff = new HashMap<>();

        if (isCreate) {
            diff.put("old", null);
            diff.put("new", objectMap);
        } else {
            diff.put("old", objectMap);
            diff.put("new", null);
        }

        changes.put(id, diff);

        return changes;
    }

    /**
     * Used for UPDATE operations
     * Compares old and new objects and returns only changed fields
     */
    public static Map<String, Map<String, Object>> differences(Object oldObj, Object newObj, String idName) {

        if (oldObj == null || newObj == null) {
            return null;
        }

        Map<String, Map<String, Object>> changes = new HashMap<>();

        // LIST SUPPORT
        if (oldObj instanceof List<?> oldList && newObj instanceof List<?> newList) {

            Map<String, Map<String, Object>> oldById = new HashMap<>();
            Map<String, Map<String, Object>> newById = new HashMap<>();

            for (Object o : oldList) {
                Map<String, Object> map = singleObjectMap(o);
                String id = extractId(idName, map);
                oldById.put(id, map);
            }

            for (Object o : newList) {
                Map<String, Object> map = singleObjectMap(o);
                String id = extractId(idName, map);
                newById.put(id, map);
            }

            Map<String, Map<String, Object>> result = new HashMap<>();

            for (String id : oldById.keySet()) {

                Map<String, Object> oldRecord = oldById.get(id);
                Map<String, Object> newRecord = newById.get(id);

                if (newRecord == null) {

                    Map<String, Object> diff = new HashMap<>();
                    diff.put("old", oldRecord);
                    diff.put("new", null);

                    result.put(id, diff);
                    continue;
                }

                Map<String, Object> fieldChanges = new HashMap<>();

                for (String key : oldRecord.keySet()) {

                    Object oldVal = oldRecord.get(key);
                    Object newVal = newRecord.get(key);

                    if (!Objects.equals(oldVal, newVal)) {

                        Map<String, Object> change = new HashMap<>();
                        change.put("old", oldVal);
                        change.put("new", newVal);

                        fieldChanges.put(key, change);
                    }
                }

                if (!fieldChanges.isEmpty()) {
                    result.put(id, fieldChanges);
                }
            }

            for (String id : newById.keySet()) {

                if (!oldById.containsKey(id)) {

                    Map<String, Object> diff = new HashMap<>();
                    diff.put("old", null);
                    diff.put("new", newById.get(id));

                    result.put(id, diff);
                }
            }

            return result.isEmpty() ? null : result;
        }

        // SINGLE OBJECT (existing logic)
        Map<String, Object> oldMap = singleObjectMap(oldObj);
        Map<String, Object> newMap = singleObjectMap(newObj);

        if (oldMap == null || newMap == null) {
            return null;
        }

        String id = extractId(idName, newMap);

        Map<String, Object> fieldChanges = new HashMap<>();

        for (String key : newMap.keySet()) {

            Object oldVal = oldMap.get(key);
            Object newVal = newMap.get(key);

            if (!Objects.equals(oldVal, newVal)) {

                Map<String, Object> diff = new HashMap<>();
                diff.put("old", oldVal);
                diff.put("new", newVal);

                fieldChanges.put(key, diff);
            }
        }

        if (!fieldChanges.isEmpty()) {
            changes.put(id, fieldChanges);
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
            case TENANT -> "Deleted tenant";
            case HOSTEL -> "Deleted hostel";
            case HOSTEL_EXPENSE -> "Deletes all expenses";
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

    public static <T> List<T> cloneList(List<T> source, Class<T> clazz) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        return source.stream()
                .map(item -> mapper.convertValue(item, clazz))
                .toList();
    }

    public static String getIdName(Source source) {
        return switch (source) {
            case HOSTEL_EXPENSE -> "expenseId";
            case SUBSCRIPTION -> "subscriptionId";
            case OWNERS, RESET_PASSWORD -> "userId";
            case HOSTEL -> "hostelId";
            case MOCK_AGENT_LOGIN, AGENT_LOGIN, AGENT, MOCK_AGENT -> "agentId";
            case TENANT -> "customerId";
            case AGENT_ROLE -> "roleId";
        };
    }

    public static String extractId(String idName, Map<String, Object> record) {
        return record.get(idName).toString();
    }
}
