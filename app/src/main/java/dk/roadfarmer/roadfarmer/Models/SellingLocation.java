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
    private String overallCategory;
    private String overallCategory2;
    private String overallCategory3;
    private String overallCategory4;
    private String overallCategory5;
    private String specificItem1;
    private String specificItem2;
    private String specificItem3;
    private String specificItem4;
    private String specificItem5;

    public SellingLocation()
    {

    }

    public SellingLocation(String road, String city, int no, int zip, String locationID) {
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

    public void setOverallCategory(String overallCategory) {
        this.overallCategory = overallCategory;
    }

    public void setOverallCategory2(String overallCategory2) {
        this.overallCategory2 = overallCategory2;
    }

    public void setOverallCategory3(String overallCategory3) {
        this.overallCategory3 = overallCategory3;
    }

    public void setOverallCategory4(String overallCategory4) {
        this.overallCategory4 = overallCategory4;
    }

    public void setOverallCategory5(String overallCategory5) {
        this.overallCategory5 = overallCategory5;
    }

    public String getOverallCategory2() {
        return overallCategory2;
    }

    public String getOverallCategory3() {
        return overallCategory3;
    }

    public String getOverallCategory4() {
        return overallCategory4;
    }

    public String getOverallCategory5() {
        return overallCategory5;
    }

    public void setSpecificItem1(String specificItem1) {
        this.specificItem1 = specificItem1;
    }

    public void setSpecificItem2(String specificItem2) {
        this.specificItem2 = specificItem2;
    }

    public void setSpecificItem3(String specificItem3) {
        this.specificItem3 = specificItem3;
    }

    public void setSpecificItem4(String specificItem4) {
        this.specificItem4 = specificItem4;
    }

    public void setSpecificItem5(String specificItem5) {
        this.specificItem5 = specificItem5;
    }

    public String getOverallCategory() {
        return overallCategory;
    }

    public String getSpecificItem1() {
        return specificItem1;
    }

    public String getSpecificItem2() {
        return specificItem2;
    }

    public String getSpecificItem3() {
        return specificItem3;
    }

    public String getSpecificItem4() {
        return specificItem4;
    }

    public String getSpecificItem5() {
        return specificItem5;
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