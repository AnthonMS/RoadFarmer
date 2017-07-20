package dk.roadfarmer.roadfarmer.Models;

/**
 * Created by Anthon on 19-07-2017.
 */

public class SellingLocation
{
    private String road, city;
    private int no, zip;
    private String sellingItem;
    private String locationID;

    public SellingLocation()
    {

    }

    public SellingLocation(String road, String city, int no, int zip, String sellingItem, String locationID) {
        this.road = road;
        this.city = city;
        this.no = no;
        this.zip = zip;
        this.sellingItem = sellingItem;
        this.locationID = locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }

    public void setSellingItem(String sellingItem) {
        this.sellingItem = sellingItem;
    }

    public String getRoad() {
        return road;
    }

    public String getCity() {
        return city;
    }

    public int getNo() {
        return no;
    }

    public int getZip() {
        return zip;
    }

    public String getSellingItem() {
        return sellingItem;
    }
}