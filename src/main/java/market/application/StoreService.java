package market.application;
import market.domain.store.*;
public class StoreService {
    private IStoreRepository storeRepository;
    private int storeIDs =1;

    public void createStore(String storeName, int founderId) throws Exception {
        // ? - do we need store type
        if(storeRepository.containsStore(storeName)) {
            // LOG - error
            throw new Exception("The storeName '" + storeName + "' already exists");
        }
        Store store = new Store(storeIDs,storeName, founderId);
        storeIDs++;
        storeRepository.addStore(store);
        //LOG - store added
    }




/*
appoints 'newOwner' to be an owner of 'storeID' BY 'appointerID'
assumes aggreement by 'apointerID''s appointer
 */
    public String addAdditionalStoreOwner(int appointerID, int newOwnerID, int storeID){
        try
        {
            Store s = storeRepository.getStoreByID(storeID);
            if (s==null){
                throw new Exception("store doesn't exist");
            }
            s.addNewOwner(appointerID,newOwnerID);
        }
        catch (Exception e){
            //TODO:we need to decide how to handle things here
            return e.getMessage();
    }
        return "success";
    }

    /*
    requests to add a new owner to a store.
    if the founder requests, it does that.
    if an owner requests, so its send a notification to his appointer, to allow the appointment
     */
    public String OwnerAppointmentRequest(int appointerID, int newOwnerId,int storeID){
        try{
            Store s = storeRepository.getStoreByID(storeID);
            if (s==null)
                throw new Exception("store doesn't exist");
            if (s.getFounderID()==appointerID){
                s.addNewOwner(appointerID,newOwnerId);
            }
            else{
                if (s.isOwner(newOwnerId)){
                    throw new Exception(newOwnerId + " is already an Owner of store:"+storeID);
                }
                if (!s.isOwner(appointerID)){
                    throw new Exception(appointerID + " is NOT an Owner of store:"+storeID);
                }
                int requestTO=s.OwnerAssignedBy(appointerID);
                //TODO:notify 'requestTO' that his assignee want to assign new owner
            }
        }catch (Exception e){
            //TODO:we need to decide how to handle things here
            return e.getMessage();
        }
        return "success";
    }

}
