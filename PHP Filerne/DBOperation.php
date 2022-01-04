<?php
 
class DBOperation{
 

 
    private $host = 'fdb15.biz.nf';
    private $user = '2202395_users';
    private $db = '2202395_users';
    private $pass = 'asd12345';
    private $conn;
 
public function __construct() {
         try{
		//$conn = mysqli_connect('fdb15.biz.nf', '2202395_users', 'abcd12345', '2202395_users');
                //mysqli_set_charset($conn,'utf8');
               $this -> conn = new PDO("mysql:host=".$this -> host.";dbname=".$this -> db, $this -> user, $this -> pass);
		echo "Connected";
         } catch (PDOException $e) {
                 echo($e);
		//print "Error!: " . $e->getMessage() . "<br/>";
		//echo "Not Connected";
		die();
	}
   //$this -> conn = new PDO("mysql:host=".$this -> host.";dbname=".$this -> db, $this -> user, $this -> pass);
 
}

 /* The insertData() method is called for registration which accepts name, email and password.
	We concatenate the password with a random generated String then hash them using PHP’s default
	BCRYPT hashing algorithm and store in database.
	If the query is successful the method returns true. */
 public function insertData($username, $email,$password){

   $encrypted_password = password_hash($password, PASSWORD_BCRYPT);
    
   $sql = 'INSERT INTO User SET username =:username, email =:email, encrypted_password =:encrypted_password';
 
   $query = $this ->conn ->prepare($sql);
   $query->execute(array(':username' => $username, ':email' => $email,
     ':encrypted_password' => $encrypted_password));
 
    if ($query) {
 
        return true;
 
    } else {
 
        return false;
 
    }
 }
 //The checkLogin() method validates the registered user whether the entered email and password combination is correct.
 public function checkLogin($email, $password) {
 
    $sql = 'SELECT * FROM User WHERE email = :email';
    $query = $this -> conn -> prepare($sql);
    $query -> execute(array(':email' => $email));
    $data = $query -> fetchObject();
    $db_encrypted_password = $data -> encrypted_password;

 
    if (password_verify($password,$db_encrypted_password)) {
 
        $user["username"] = $data -> username;
        $user["email"] = $data -> email;
        return $user;
 
    } else {
 
        return false;
    }
 }
	//The changePassword() method is used to change old password with a new password.
	//Note : Here I have defined empty password for MySQL. Replace with your password.
 public function changePassword($email, $password){
 
    $hash = $this -> getHash($password);
    $encrypted_password = $hash["encrypted"];
    $salt = $hash["salt"];
 
    $sql = 'UPDATE User SET encrypted_password = :encrypted_password, salt = :salt WHERE email = :email';
    $query = $this -> conn -> prepare($sql);
    $query -> execute(array(':email' => $email, ':encrypted_password' => $encrypted_password, ':salt' => $salt));
 
    if ($query) {
 
        return true;
 
    } else {
 
        return false;
 
    }
 }
/*  
 The checkUserExist() method is used to check whether a user is already registered or not using email id.
 The getHash() method is used to return the generated hash.
 The verifyHash() method checks whether the password matches the hash using PHP’s password_verify() method.
 It is used for login.
  */
 public function checkEmailExist($email){
 
    $sql = 'SELECT COUNT(*) from User WHERE email =:email';
    $query = $this -> conn -> prepare($sql);
    $query -> execute(array('email' => $email));
 
    if($query){
 
        $row_count = $query -> fetchColumn();
 
        if ($row_count == 0){
 
            return false;
 
        } else {
 
            return true;
 
        }
    } else {
 
        return false;
    }
 }
  public function findFriends($username){

    $sql = 'select u.username, f.statusid from friends f inner join User u on (u.user_id = if(f.userid = (SELECT user_id from User WHERE username =:username), f.friendid, f.userid)) where (f.userid = (SELECT user_id from User WHERE username =:username) or f.friendid = (SELECT user_id from User WHERE username =:username))';
    $query = $this -> conn -> prepare($sql);
    $query -> execute(array(':username' => $username));
    $data = $query ->fetchAll(PDO::FETCH_ASSOC);      
        return $data;
         	
   }
    public function addFriend($username, $friendname){
    
	  $sql = 'insert into friends(userid, friendid, statusid) VALUES ((SELECT user_id FROM User WHERE username=:username),(SELECT user_id FROM User WHERE username=:friendname),0)';
			
	$query = $this -> conn -> prepare($sql);	
	$query -> execute(array(':username' => $username, ':friendname' => $friendname));
         
                       if ($query) {
                                return true;
                            } else { 
                                return false;
                            }
           
   }
    public function FriendOrNot($username, $friendname, $answer){
	  
	  $sql = 'update friends set statusid = 1 where userid = (select user_id from User where username =:friendname) and friendid = (select user_id from User where username =:username)';
			
	$query = $this -> conn -> prepare($sql);	
	 $query -> execute(array(':username' => $username, ':friendname' => $friendname));
	 if ($query) {
        return true;
    } else { 
        return false;

    }
   
}
 public function checkUsernameExist($username){
 
    $sql = 'SELECT COUNT(*) from User WHERE username =:username';
    $query = $this -> conn -> prepare($sql);
    $query -> execute(array('username' => $username));
 
    if($query){
 
        $row_count = $query -> fetchColumn();
 
        if ($row_count == 0){
 
            return false;
 
        } else {
 
            return true;
 
        }
    } else {
 
        return false;
    }
 
}
public function insertTrack($username, $time, $distance){
     $sql = 'INSERT INTO track (userid, distance, time, date) values ((SELECT user_id FROM User WHERE username=:username), :time, :distance, curdate())';
    $query = $this -> conn -> prepare($sql);
     $query -> execute(array(':username' => $username, ':distance',':time' => $time));
     
     	 if ($query) {
        return true;
    } else { 
        return false;

    }
}
public function TrackAndFriends($username){
     $sql = 'select User.username, track.distance, track.time, track.date from User inner join track on User.user_id = track.userid where User.user_id IN (select user_id from friends f inner join User u on (u.user_id = if(f.userid = (SELECT user_id from User WHERE username =:username), f.friendid, f.userid)) where (f.userid = (SELECT user_id from User WHERE username =:username) or f.friendid = (SELECT user_id from User WHERE username =:username)))';
     $query = $this -> conn -> prepare($sql);
     $query -> execute(array(':username' => $username));
     $data = $query ->fetchAll(PDO::FETCH_ASSOC);      
        return $data;
}
}