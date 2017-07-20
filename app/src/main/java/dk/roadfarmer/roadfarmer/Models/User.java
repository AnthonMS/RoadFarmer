package dk.roadfarmer.roadfarmer.Models;

/**
 * Created by Anthon on 19-07-2017.
 */

public class User
{
    private String fullName, email, phone;
    private String userID;

    public User() {
    }

    public User(String fullName, String email, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public User(String fullName, String email, String phone, String userID) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.userID = userID;
    }

    public boolean equals(User user)
    {

        String name1 = this.getFullName();
        String name2 = user.getFullName();
        if (name1.equals(name2))
        {
            // Names are the same, now check email
            String mail1 = this.getEmail();
            String mail2 = user.getEmail();
            if (mail1.equals(mail2))
            {
                // Zipcodes, thus also city, is the same. Final check is Phonernumber
                String ph1 = this.getPhone();
                String ph2 = user.getPhone();
                if (ph1.equals(ph2))
                {
                    // The phone numbers are the same, it's the same Customer.
                    return true;
                }
                else { return false; }
            }
            else { return false; }
        }

        return false;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
