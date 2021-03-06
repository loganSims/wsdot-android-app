package gov.wa.wsdot.android.wsdot.ui.ferries.departures.vesselwatch;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.repository.VesselWatchRepository;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class VesselWatchViewModel extends ViewModel {

    private static String TAG = VesselWatchViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private VesselWatchRepository vesselWatchRepo;

    @Inject
    VesselWatchViewModel(VesselWatchRepository vesselWatchRepo) {
        this.mStatus = new MutableLiveData<>();
        this.vesselWatchRepo = vesselWatchRepo;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MutableLiveData<List<VesselWatchItem>> getVessels(){
        return vesselWatchRepo.getVessels();
    }

    public void refreshVessels() {
        vesselWatchRepo.refreshData(this.mStatus);
    }

}
