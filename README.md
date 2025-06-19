# oliveTrackingSystemBackend
backend application for the olivetracking system

 public static void main(String[] args) {
	        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	        String plainPassword = "your_admin_password_here"; // CHANGE THIS to your desired admin password
	        String hashedPassword = passwordEncoder.encode(plainPassword);
	        System.out.println("Plain Password: " + plainPassword);
	        System.out.println("Hashed Password: " + hashedPassword);
	    }

select * from karya.dbo.users

id	email	full_name	password	project_ids	role
1	admin@olivecrypto.com	admin	$2a$10$YqzTJvPJO2gKykBk4lKbhuGy0CJOxHN/9WVRGHGFhj1gVW/Ve7wZi	NULL	ADMIN
2	vivekkumar.rapaka@olivecrypto.com	vivek kumar	$2a$10$qzqOU1yClvhIAVcBzjIiCunn0dwfZ6Cqi6aMxgWbfUYQdGQwP4fTm	NULL	TEAMMEMBER
