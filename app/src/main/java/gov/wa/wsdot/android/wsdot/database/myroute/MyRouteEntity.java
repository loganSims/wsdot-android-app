package gov.wa.wsdot.android.wsdot.database.myroute;

import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "my_route")
public class MyRouteEntity {

    @ColumnInfo(name = BaseColumns._ID)
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "id")
    private Long myRouteId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "route_locations")
    private String routeLocations;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "zoom")
    private Integer zoom;

    @ColumnInfo(name = "found_cameras")
    @NonNull
    private Integer foundCameras = 0;

    @ColumnInfo(name = "found_travel_times")
    @NonNull
    private Integer foundTravelTimes = 0;

    @ColumnInfo(name = "camera_ids_json")
    @NonNull
    private String cameraIdsJSON = "[]";

    @ColumnInfo(name = "travel_time_titles_json")
    @NonNull
    private String travelTimeTitlesJSON = "[]";

    @ColumnInfo(name = "is_starred")
    @NonNull
    private Integer isStarred = 0;

    public Long getMyRouteId() {
        return myRouteId;
    }

    public void setMyRouteId(Long myRouteId) {
        this.myRouteId = myRouteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRouteLocations() {
        return routeLocations;
    }

    public void setRouteLocations(String route_locations) {
        this.routeLocations = route_locations;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getZoom() {
        return zoom;
    }

    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }

    @NonNull
    public Integer getFoundCameras() {
        return foundCameras;
    }

    public void setFoundCameras(@NonNull Integer foundCameras) {
        this.foundCameras = foundCameras;
    }

    @NonNull
    public String getCameraIdsJSON() {
        return cameraIdsJSON;
    }

    public void setCameraIdsJSON(String cameraIdsJSON){
        this.cameraIdsJSON = cameraIdsJSON;
    }

    @NonNull
    public Integer getFoundTravelTimes() {
        return foundTravelTimes;
    }

    public void setFoundTravelTimes(@NonNull Integer foundTravelTimes) {
        this.foundTravelTimes = foundTravelTimes;
    }

    @NonNull
    public String getTravelTimeTitlesJSON() {
        return this.travelTimeTitlesJSON;
    }

    public void setTravelTimeTitlesJSON(String travelTimeTitles){
        this.travelTimeTitlesJSON = travelTimeTitles;
    }

    @NonNull
    public Integer getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(@NonNull Integer isStarred) {
        this.isStarred = isStarred;
    }
}
