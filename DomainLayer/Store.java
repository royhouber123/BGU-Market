import java.lang.reflect.Array;
import java.util.*;

public class Store {
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


    public Store(String name, int founderID) throws IllegalArgumentException {
        // ? - do we need store type

        if(!isValidName(name)){
            //LOG - error
            throw new IllegalArgumentException("Store name illegal!");
        }
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
    private boolean isValidName(String name) {
        return true;
    }
}