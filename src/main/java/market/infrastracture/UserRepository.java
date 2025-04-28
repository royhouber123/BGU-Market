package market.infrastracture;

import market.domain.user.*;

import java.util.HashMap;
import java.util.Map;

public class UserRepository implements IUserRepository {

    private final Map<String, User>   userMap     = new HashMap<>();
    private final Map<String, String> passwordMap = new HashMap<>();

    public UserRepository() {
        User u1 = new User("username1");
        u1.addProductToCart(111, "productA", 1);
        userMap.put(u1.getUserName(), u1);
        passwordMap.put(u1.getUserName(), "pw1");

        User u2 = new User("username2");
        u2.addProductToCart(111, "productC", 2);
        userMap.put(u2.getUserName(), u2);
        passwordMap.put(u2.getUserName(), "pw2");
    }

    public User findById(String name)                   { return userMap.get(name); }
    public void register(String name, String pw)        { userMap.put(name,new User(name)); passwordMap.put(name,pw); }
    public void delete(String name)                     { userMap.remove(name); passwordMap.remove(name); }
    public User isExist(String name,String pw)          { return pw.equals(passwordMap.get(name)) ? userMap.get(name):null; }

    public boolean changeUserName(String oldName,String newName){
        if(!userMap.containsKey(oldName)||userMap.containsKey(newName)) return false;
        User u = userMap.remove(oldName);
        u.setUserName(newName);                 
        userMap.put(newName,u);

        String pw = passwordMap.remove(oldName);
        passwordMap.put(newName,pw);
        return true;
    }

    public boolean changePassword(String name,String newPw){
        if(!passwordMap.containsKey(name)) return false;
        passwordMap.put(name,newPw);
        return true;
    }
    public ShoppingCart getCart(String name){
        User u = userMap.get(name);
        return u!=null ? u.getShoppingCart():null;
    }
}
