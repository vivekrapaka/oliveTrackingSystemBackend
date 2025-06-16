# oliveTrackingSystemBackend
backend application for the olivetracking system

 public static void main(String[] args) {
	        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	        String plainPassword = "your_admin_password_here"; // CHANGE THIS to your desired admin password
	        String hashedPassword = passwordEncoder.encode(plainPassword);
	        System.out.println("Plain Password: " + plainPassword);
	        System.out.println("Hashed Password: " + hashedPassword);
	    }
