package market.domain.store;

import jdk.jshell.spi.ExecutionControl;

import java.util.*;

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

    private HashMap<Integer, List<Integer>> ownerToAssignedManagers;

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

//        this.managers = new HashMap<>();
//        this.owners = new ArrayList<>();
        this.ownerToWhoAssignedHim = new HashMap<>();
        this.ownerToAssignedOwners = new HashMap<>();
        this.ownerToAssignedManagers = new HashMap<>();

        //assigning founder as manager and owner
        this.founderID = founderID;
        this.ownerToAssignedOwners.put(founderID, new ArrayList<>());
        this.ownerToAssignedManagers.put(founderID, new ArrayList<>());


//        this.managers.put(founderID, new ArrayList<>());
//        this.owners.add(founderID);
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
        return true;
    }



/*
retruns if 'id' is an owner of the store
 */
    public boolean isOwner(int id){
        return ownerToAssignedOwners.containsKey(id);
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


}