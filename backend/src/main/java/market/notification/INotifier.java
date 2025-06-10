package market.notification;

public interface INotifier {
    boolean notifyUser(String userId, String message);
}
