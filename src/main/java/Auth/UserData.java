package Auth;

public class UserData{
	Integer id = -1;
	String email;
	String password;
	String role;
	
	public static UserData u1 = new UserData( -1 , "Guest" , "" , "USER");
	
	public UserData( Integer id , String email , String password , String role)
	{
		this.id = id;
		this.email = email;
		this.password = password;
		this.role = role;
	}
}