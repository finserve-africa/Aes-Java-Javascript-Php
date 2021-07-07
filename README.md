# Aes-Java-Javascript-Php


## Java 

  AesUtil aesUtil = new AesUtil();
  
  String encryptedContent = aesUtil.encrypt("8z96G01Br5IU5gZ6wBvmc61NOyWSlm", "My Plain Text");
  
  System.out.println(encryptedContent);
  
  String decryptedContent = aesUtil.decrypt("8z96G01Br5IU5gZ6wBvmc61NOyWSlm", "ccd5fd44cd908aee0438d5c6e4ebe77304116f773ce0266b3a1e77a156266f886803c3c81375d9195e9e872894e90df1yKU69H63PWUAFMFo8sjMjQ==");
  
  System.out.println(decryptedContent);
  
  
  
  ## Javascript
  
  const aes = new AesUtil();
  
  let rawData = "My Plain Text";
  
  let encrypted = aes.encrypt('8z96G01Br5IU5gZ6wBvmc61NOyWSlm', rawData);
  
  let decrypted = aes.decrypt('8z96G01Br5IU5gZ6wBvmc61NOyWSlm', "ccd5fd44cd908aee0438d5c6e4ebe77304116f773ce0266b3a1e77a156266f886803c3c81375d9195e9e872894e90df1yKU69H63PWUAFMFo8sjMjQ==");

console.log('encrypted: ' + encrypted);
console.log('decrypted: ' + decrypted);
