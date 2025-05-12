package market.domain.purchase;

import java.util.Set;

public class NotificationForPurchase {

    private static final Set<String> onlineUsers = Set.of(); // בהמשך אפשר לנהל מחוברים

    public static void notifyApprovers(Set<String> approvers, String message) {
        for (String approver : approvers) {
            System.out.println("Notification to approver " + approver + ": " + message);
        }
    }

    public static void notifyUser(String userId, String message) {
        System.out.println("Notification to user " + userId + ": " + message);
    }

    public static boolean isUserOnline(String userId) {
        return onlineUsers.contains(userId);
    }
}
