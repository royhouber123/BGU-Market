package market.domain.store;

import jdk.jshell.spi.ExecutionControl;

import java.net.StandardSocketOptions;
import java.util.*;
import java.util.stream.*;


public class Store {
    private int storeID;
    private String name;
    boolean active;

    int founderID;

    private HashMap<Integer,Integer> ownerToWhoAssignedHim;
    private HashMap<Integer, List<Integer>> ownerToAssignedOwners;
    //every mangaer has a key in the dict.
    // if manager 1 assign manager 2, so manager2 in managers[manager1]

    //TODO: implement manager interface (probably state design pattern) with appropriate permissions

    private HashMap<Integer, List<Manager>> ownerToAssignedManagers;

    // ? - do we need store type


    private List<String> products; //TODO: change to Product objects!


    public Store(int storeID,String name, int founderID) throws IllegalArgumentException {
        // ? - do we need store type

        if(!isValidName(name)){
            //LOG - error
            throw new IllegalArgumentException("market.domain.store.Store name illegal!");
        }
        this.storeID=storeID;
        this.name = name;
        this.products = new ArrayList<>();
        this.active = true;

        this.ownerToWhoAssignedHim = new HashMap<>();
        this.ownerToAssignedOwners = new HashMap<>();
        this.ownerToAssignedManagers = new HashMap<>();

        //assigning founder as manager and owner
        this.founderID = founderID;
        this.ownerToAssignedOwners.put(founderID, new ArrayList<>());
        this.ownerToAssignedManagers.put(founderID, new ArrayList<>());

    }



    /*
        validates there are no illegal characters
     */
    //TODO:
    private boolean isValidName(String name) {
        return true;
    }

    /*
    adds a new owner to the store
     */
    public boolean addNewOwner(int appointerID, int newOwnerID) throws Exception {
       if (!isOwner(appointerID))
           throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);

        if (isOwner(newOwnerID))
            throw new Exception("the user:"+appointerID+" is already a owner of the store: "+storeID);

        ownerToAssignedOwners.get(appointerID).add(newOwnerID);
        ownerToAssignedOwners.put(newOwnerID,new ArrayList<>());
        ownerToWhoAssignedHim.put(newOwnerID,appointerID);
        ownerToAssignedManagers.put(newOwnerID,new ArrayList<>());
        return true;
    }

    public boolean addNewManager(int appointerID, int newManagerID) throws Exception {
        if (!isOwner(appointerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);

        if (isManager(newManagerID))
            throw new Exception("the user:"+appointerID+" is already a owner of the store: "+storeID);

        Manager newManager = new Manager(newManagerID, appointerID);
        ownerToAssignedManagers.get(appointerID).add(newManager);
        return true;
    }


    /*
    adds permision to manager.
    managerID has to be manager. appointerID must be the owner appointed managerID, and permmisionID has to be legal.

     */
    public boolean addPermissionToManager( int managerID, int appointerID, int permissionID) throws Exception {
        if(!isOwner(appointerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);
        if(!isManager(managerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);

        Manager manager = getManager(managerID);
        Permission p = Permission.fromCode(permissionID); //if invalid code, exception is thrown here

        manager.addPermission(p,appointerID); //if appointer is not the real one, throws here
        return true;
    }



    /*
    retruns true if 'id' is an owner of the store
     */
    public boolean isOwner(int id){
        return ownerToAssignedOwners.containsKey(id);
    }

    /*
    retruns true if 'id' is a manager of the store
     */
    public boolean isManager(int id){
        for(List<Manager> l :this.ownerToAssignedManagers.values()){
            for (Manager m : l){
                if(m.getID()==id){
                    return true;
                }
            }
        }
        return false;
    }

    private Manager getManager(int id){
        for (Manager m : getAllManagers()){
            if(m.getID()==id){
                return m;
            }
        }
        return null;
    }

    private List<Manager> getAllManagers(){
        List<Manager> managers = new ArrayList<>();
        for(List<Manager> l :this.ownerToAssignedManagers.values()){
            managers.addAll(l);
        }
        return managers;
    }

    /*
    returns the owner's id which assigned "id"
    returns -1 if no one assigned him
     */
    public int OwnerAssignedBy(int id){
        if (ownerToWhoAssignedHim.containsKey(id)){
            return ownerToWhoAssignedHim.get(id);
        }
        return -1;
    }

    public int getFounderID(){
        return founderID;
    }


    public List<Integer> getOwnerAssigments(int id){
        return ownerToAssignedOwners.get(id);
    }


    /*
    removes the owner and all the people he assigned recursivly and returns a list of all the removed people
     */
    public List<Integer> removeOwner(int id, int toRemove) throws Exception {
        List<Integer> res = new ArrayList<>();
        if (!isOwner(id)){
            throw new Exception(id +" is not a owner of store:"+ storeID);
        }
        if (!isOwner(toRemove)){
            throw new Exception(toRemove +" is not a owner of store:"+ storeID);
        }
        if (toRemove == getFounderID()){
            throw new Exception(toRemove +" is the FOUNDER of store:"+ storeID+ " can't remove him");
        }
        if (ownerToWhoAssignedHim.get(toRemove)!= id){
            throw new Exception(id +" didn't assign " + toRemove + " to be owner of store:"+ storeID);
        }
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(toRemove);
        while(!queue.isEmpty()){
            int next = queue.remove();
            //add to remove list
            res.add(next);
            List<Integer> assigments = getOwnerAssigments(next);

            //enter the owners he assigned to the queue
            for (int a:assigments){
                queue.add(a);
            }
            //remove him

            ownerToAssignedOwners.get(OwnerAssignedBy(next)).remove(Integer.valueOf(next));
            ownerToWhoAssignedHim.remove(next);


            if (ownerToAssignedManagers.get(next)!= null){
                //remove the managers he assign
                for (Manager a: ownerToAssignedManagers.get(next)){
                    res.add(a.getID());
                }
                ownerToAssignedManagers.remove(next);
            }


        }
        for (int o:res){
            if (isOwner(o)){
                ownerToAssignedOwners.remove(o);
            }
        }
        return res;
    }

    /*
    if the user called is an owner,
    returns a hashmap.
    key - worker (manager or owner)
    value - set(permissions_values).   if manager
            NULL.                      if owner
     */
    public HashMap<Integer, Set<Integer>> getPositionsInStore(Integer requestUser) throws Exception{
        if(!isOwner(requestUser))
            throw new Exception("User " + requestUser.toString() + " is not an owner!");

        HashMap<Integer, Set<Integer>> workerToPermission = new HashMap<>();
        for(Integer owner : this.ownerToWhoAssignedHim.keySet()){
            workerToPermission.put(owner, null);
        }

        for(Manager m : getAllManagers()){
            Set<Permission> ps = m.getPermissions();
            workerToPermission.put(m.getID(), ps.stream().map(Permission::getCode).collect(Collectors.toSet()));
        }
        return workerToPermission;
    }



    enum Permission{
        VIEW_ONLY(0),
        EDIT_PRODUCTS(1),
        EDIT_POLICIES(2);

        private final int code;

        Permission(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        // Reverse lookup
        public static Permission fromCode(int code) throws IllegalArgumentException {
            for (Permission p : Permission.values()) {
                if (p.getCode() == code) {
                    return p;
                }
            }
            throw new IllegalArgumentException("Invalid permission code: " + code);
        }
    }

    class Manager{

        int id;
        int apointedBy;


        private Set<Permission> permissions;

        public Manager(int id, int apointedBy){
            this.id = id;
            this.apointedBy = apointedBy;
            permissions = new HashSet<>();
            permissions.add(Permission.VIEW_ONLY);
        }

        public boolean addPermission(Permission permission, int byWho) throws Exception {
            if(byWho != apointedBy){
                throw new Exception("user " + byWho + " cant add permission to manager " + this.id + " because he is not his appointer");
            }
            permissions.add(permission);
            return true;
        }

        public int getApointedBy() {
            return apointedBy;
        }
        public int getID() {
            return id;
        }

        public boolean hasPermission(Permission permission){
            return permissions.contains(permission);
        }

        public Set<Permission> getPermissions() {
            return permissions;
        }

    }

}