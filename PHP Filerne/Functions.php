<?php
 
require_once 'DBOperation.php';
 
class Functions{
 
private $db;
 
public function __construct() {
 
      $this -> db = new DBOperation();
 
}
 
public function registerUser($username, $email, $password) {
 
   $db = $this -> db;
 
 
      if ($db -> checkEmailExist($email) ) {
 
         $response["result"] = "failure";
         $response["message"] = "Email Already Registered !";
         return json_encode($response);
 
      } 
      else if($db -> checkUsernameExist($username)){
                     
         $response["result"] = "failure";
         $response["message"] = "Username already in use!";
         return json_encode($response);
      }
      
      else {
 
         $result = $db -> insertData($username, $email, $password);
 
         if ($result) {
 
              $response["result"] = "success";
            $response["message"] = "User Registered Successfully !";
            return json_encode($response);
 
         } else {
 
            $response["result"] = "failure";
            $response["message"] = "Registration Failure";
            return json_encode($response);
 
         }
      }
   
}
 
public function loginUser($email, $password) {
 
  $db = $this -> db;
 
  if (!empty($email) && !empty($password)) {
 
    if ($db -> checkEmailExist($email)) {
 
       $result =  $db -> checkLogin($email, $password);
 
       if(!$result) {
 
        $response["result"] = "failure2";
        $response["message"] = "Invaild Login Credentials";
        return json_encode($response);
 
       } else {
 
        $response["result"] = "success";
        $response["message"] = "Login Successful";
        $response["user"] = $result;
        return json_encode($response);
 
       }
    } else {
 
      $response["result"] = "failure3";
      $response["message"] = "Invaild Login Credentials";
      return json_encode($response);
 
    }
  } else {
 
      return $this -> getMsgParamNotEmpty();
    }
}
public function searchFriends($username){
 
   $db = $this -> db;
   
	   
    $result = $db -> findFriends($username);

	$response["result"] = "success";
        $response["message"] = "Friends Retreived";
        $response["user"] = $result;
        return json_encode($response);

   
}
 
public function changePassword($email, $old_password, $new_password) {
 
  $db = $this -> db;
 
  if (!empty($email) && !empty($old_password) && !empty($new_password)) {
 
    if(!$db -> checkLogin($email, $old_password)){
 
      $response["result"] = "failure";
      $response["message"] = 'Invalid Old Password';
      return json_encode($response);
 
    } else {
 
    $result = $db -> changePassword($email, $new_password);
 
      if($result) {
 
        $response["result"] = "success";
        $response["message"] = "Password Changed Successfully";
        return json_encode($response);
 
      } else {
 
        $response["result"] = "failure";
        $response["message"] = 'Error Updating Password';
        return json_encode($response);
 
      }
    }
  } else {
 
      return $this -> getMsgParamNotEmpty();
  }
}
public function makeFriendRequest($username, $friendname){
 
   $db = $this -> db;
   
   if($db -> checkUsernameExist($friendname)){
       $result = $db -> addFriend($username, $friendname);
	if(!$result) {
	
	$response["result"] = "failure";
      $response["message"] = "Couldn't send friend request";
      return json_encode($response);  

   } else {
        $response["result"] = "success";
       $response["message"] = "Friend invitation sent";
      return json_encode($response);
   }
                     
    }else{
     $response["result"] = "failure";
       $response["message"] = "Friend doesn't exist";
       return json_encode($response);
    
    }   
}
public function GetTracks($username){
 
   $db = $this -> db;
   
   $result = $db -> TrackAndFriends($username);
   
   $response["result"] = "success";
       $response["message"] = "Friend invitation sent";
       $response["user"] = $result;
      return json_encode($response);
}

public function FriendAnswer($username, $friendname, $answer){
 
   $db = $this -> db;
   
   if(!empty($username) && !empty($friendname) && !empty($answer)){
	   
    $result = $db -> FriendOrNot($username,  $friendname, $answer);
	
        if(!$result) {
		
	$response["result"] = "failure";
        $response["message"] = "Something went wrong";
        return json_encode($response);  

   } else {
	  $response["result"] = "success";
          $response["message"] = "A friend has been made!";
          return json_encode($response);
   }
   }else {
 
      return $this -> getMsgParamNotEmpty();
  }
}
 
public function isEmailValid($email){
 
  return filter_var($email, FILTER_VALIDATE_EMAIL);
}
 
public function getMsgParamNotEmpty(){
 
  $response["result"] = "failure";
  $response["message"] = "Parameters should not be empty !";
  return json_encode($response);
 
}
 
public function getMsgInvalidParam(){
 
  $response["result"] = "failure";
  $response["message"] = "Invalid Parameters";
  return json_encode($response);
 
}
 
public function getMsgInvalidEmail(){
 
  $response["result"] = "failure";
  $response["message"] = "Invalid Email";
  return json_encode($response);
 
}
}