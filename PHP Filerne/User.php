<?php
 
require_once 'Functions.php';
 
$fun = new Functions();
 
if ($_SERVER['REQUEST_METHOD'] == 'POST')
{
  $data = json_decode(file_get_contents("php://input"));
 
  if(isset($data -> operation)){
 
   $operation = $data -> operation;

   if(!empty($operation)){
 
      if($operation == 'register'){
 
         if(isset($data -> user ) && !empty($data -> user) && isset($data -> user -> username)
             && isset($data -> user -> email) && isset($data -> user -> password)){
 
            $user = $data -> user;
            $username = $user -> username;
            $email = $user -> email;
            $password = $user -> password;
 
          if ($fun -> isEmailValid($email)) {
 
            echo $fun -> registerUser($username, $email, $password);
 
          } else {
 
            echo $fun -> getMsgInvalidEmail();
          }
 
         } else {
 
            echo $fun -> getMsgInvalidParam();
 
         }
 
      }else if ($operation == 'login') {
 
        if(isset($data -> user ) && !empty($data -> user) && isset($data -> user -> email) && isset($data -> user -> password)){
 
          $user = $data -> user;
          $email = $user -> email;
          $password = $user -> password;
 
          echo $fun -> loginUser($email, $password);
 
        } else {
 
          echo $fun -> getMsgInvalidParam();
 
        }
      } else if ($operation == 'chgPass') {
 
        if(isset($data -> user ) && !empty($data -> user) && isset($data -> user -> email) && isset($data -> user -> old_password)
          && isset($data -> user -> new_password)){
 
          $user = $data -> user;
          $email = $user -> email;
          $old_password = $user -> old_password;
          $new_password = $user -> new_password;
 
          echo $fun -> changePassword($email, $old_password, $new_password);
 
        } else {
 
          echo $fun -> getMsgInvalidParam();
 
        }
      } else if ($operation == 'GetFriends') {
 
        if(isset($data -> user ) && !empty($data -> user) && isset($data -> user -> username)){
 
          $user = $data -> user;
		  $username = $user -> username;

          echo $fun -> searchFriends($username);
 
        } else {
 
          echo $fun -> getMsgInvalidParam();
 
        }
		
   } else if ($operation == 'SendFriendRequest') {
 
        if(isset($data -> user ) && !empty($data -> user) && isset($data -> user -> username) && isset($data -> user -> friendname)) {
 
                  $user = $data -> user;
		  $username = $user -> username;
		  $friendname = $user -> friendname;
		  
		  echo $fun -> makeFriendRequest($username, $friendname);
 
        } else {
 
          echo "User not found";
 
        }
   } else if ($operation == 'FriendAnswer'){
           
           if(isset($data -> user ) && !empty($data -> user) && isset($data -> user -> username) && isset($data -> user -> friendname) && isset($data -> user -> answer)){
                  $user = $data -> user;
		  $username = $user -> username;
		  $friendname = $user -> friendname;
                  $answer = $user -> answer;
                  
                  echo $fun -> FriendAnswer($username, $friendname, $answer);
           } else {
 
          echo $fun -> getMsgInvalidParam();
 
        }
   }else if ($operation == 'GetTracks'){
           
           if(isset($data -> user ) && !empty($data -> user) && isset($data -> user -> username)){
                  
                  $user = $data -> user;
		  $username = $user -> username;
                  
                  echo $fun -> GetTracks($username);
           }
           else {
 
          echo $fun -> getMsgInvalidParam();
 
        }
   }
   }else{
 
      echo "hahaha";
 
   }
  } else {
 
      echo $fun -> getMsgInvalidParam();
  }
} else if ($_SERVER['REQUEST_METHOD'] == 'GET'){
 
  echo "Runner application";
 
}