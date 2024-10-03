package model;

public class UserVO {
    private Integer user_id;
    private String name;
    private String email;
    private String password;
    private AddressVO address;
    private String role;
    private String dateCreated;
    
    public void setDateCreated(String date)
    {
    	this.dateCreated = date;
    }
    
    public String getDateCreated()
    {
    	return dateCreated;
    }
    public Integer getId() {
    	return user_id;
    }
    public void setId(Integer id) {
    	user_id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AddressVO getAddress() {
        return address;
    }

    public void setAddress(AddressVO address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
