<?php 

 function encrypt($data = '', $key = NULL) {
    if($key != NULL && $data != ""){
        $method = "AES-256-CBC";
        $key1 = mb_convert_encoding($key, "UTF-8"); //Encoding to UTF-8
        $key1 = $key;
        
        //Randomly generate IV and salt
        $salt1 = random_bytes (32); 
        $IVbytes = random_bytes (16); 
        
 
        //SecretKeyFactory Instance of PBKDF2WithHmacSHA1 Java Equivalent
        $hash = openssl_pbkdf2($key1,$salt1,'256', 1989, 'SHA1'); 
      
        // Encrypt
        $encrypted = openssl_encrypt($data, $method, $hash, OPENSSL_RAW_DATA, $IVbytes);
      
        // Concatenate salt, IV and encrypted text and base64-encode the result
        $result = strtoupper(bin2hex($salt1)).strtoupper(bin2hex($IVbytes)).base64_encode($encrypted);   
        return $result;
    }else{
        return "String to encrypt, Key is required.";
    }
}
