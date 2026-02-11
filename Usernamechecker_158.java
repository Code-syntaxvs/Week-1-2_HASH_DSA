import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UsernameService {

    // username -> userId mapping (O(1) lookup)
    private final ConcurrentHashMap<String, String> usernameToUserId;

    // username -> attempt frequency
    private final ConcurrentHashMap<String, AtomicInteger> attemptFrequency;

    public UsernameService() {
        usernameToUserId = new ConcurrentHashMap<>();
        attemptFrequency = new ConcurrentHashMap<>();
    }

    // Register a username
    public boolean registerUsername(String username, String userId) {
        // putIfAbsent ensures atomic check + insert
        return usernameToUserId.putIfAbsent(username, userId) == null;
    }

    // Check availability in O(1)
    public boolean checkAvailability(String username) {
        // Track attempt frequency
        attemptFrequency
                .computeIfAbsent(username, k -> new AtomicInteger(0))
                .incrementAndGet();

        return !usernameToUserId.containsKey(username);
    }

    // Suggest alternatives if username is taken
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        if (checkAvailability(username)) {
            suggestions.add(username);
            return suggestions;
        }

        // Strategy 1: Append numbers
        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (checkAvailability(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Strategy 2: Replace underscore with dot
        if (username.contains("_")) {
            String dotVersion = username.replace("_", ".");
            if (checkAvailability(dotVersion)) {
                suggestions.add(dotVersion);
            }
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String mostAttempted = null;
        int max = 0;

        for (Map.Entry<String, AtomicInteger> entry : attemptFrequency.entrySet()) {
            if (entry.getValue().get() > max) {
                max = entry.getValue().get();
                mostAttempted = entry.getKey();
            }
        }

        return mostAttempted + " (" + max + " attempts)";
    }

    // For testing
    public static void main(String[] args) {
        UsernameService service = new UsernameService();

        service.registerUsername("john_doe", "U1001");
        service.registerUsername("admin", "U0001");

        System.out.println("checkAvailability(\"john_doe\") → "
                + service.checkAvailability("john_doe"));

        System.out.println("checkAvailability(\"jane_smith\") → "
                + service.checkAvailability("jane_smith"));

        System.out.println("suggestAlternatives(\"john_doe\") → "
                + service.suggestAlternatives("john_doe"));

        // Simulate attempts
        for (int i = 0; i < 10543; i++) {
            service.checkAvailability("admin");
        }

        System.out.println("getMostAttempted() → "
                + service.getMostAttempted());
    }
}
