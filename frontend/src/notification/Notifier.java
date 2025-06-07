package market.notification;

public class Notifier implements INotifier {


    @Override
    public boolean notifyUser(String userId, String message) {
        Broadcaster.broadcast(userId, message);
        return true;
    }
}
