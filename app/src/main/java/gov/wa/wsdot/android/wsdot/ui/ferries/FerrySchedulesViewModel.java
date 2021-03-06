package gov.wa.wsdot.android.wsdot.ui.ferries;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.util.AbsentLiveData;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

public class FerrySchedulesViewModel extends ViewModel {

    private LiveData<List<FerryScheduleEntity>> schedules; // Object UI expects when displaying all sailings
    private MutableLiveData<ResourceStatus> mStatus;
    private FerryScheduleRepository ferryScheduleRepo;

    @Inject
    FerrySchedulesViewModel(FerryScheduleRepository ferryScheduleRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.ferryScheduleRepo = ferryScheduleRepo;
        this.schedules = ferryScheduleRepo.loadFerrySchedules(mStatus);
    }
    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<List<FerryScheduleEntity>> getFerrySchedules(){
        if (this.schedules == null){
            return AbsentLiveData.create();
        }
        return this.schedules;
    }

    public void setIsStarredFor(Integer routeId, Integer isStarred){
        ferryScheduleRepo.setIsStarred(routeId, isStarred);
    }

    public void forceRefreshFerrySchedules() {
        ferryScheduleRepo.refreshData(mStatus, true);
    }

}
