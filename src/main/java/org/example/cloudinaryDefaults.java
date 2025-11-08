package org.example;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.util.Map;


public class cloudinaryDefaults {

    public static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dvpwqtobj",
            "api_key", "642298169966665",
            "api_secret", "fLRoefQsXvaVowTNUesQB720tTw"
    ));
    public static void main(String[] args) {
        try {
            // Upload first default image
            Map uploadResult1 = cloudinary.uploader().upload(
                    new File("C:/Users/User/Desktop/Crow's Nest49/Crow's Nest49/Crow's Nest5/Crow's Nest3/Crow's Nest/Crow's Nest(FrontEnd)/CrowsNestFrontEnd/src/main/resources/com/crowsnestfrontend/images/user.png"),
                    ObjectUtils.asMap("folder", "default_profiles")
            );
            System.out.println("Default 1 uploaded: " + uploadResult1.get("secure_url"));




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static  String githubToCloudinary(String url){
        String returnURL="";
        try {
            // Upload first default image
            Map uploadResult1 = cloudinary.uploader().upload(
                    url,
                    ObjectUtils.asMap("folder", "default_profiles")
            );
            returnURL=(String)uploadResult1.get("secure_url");




        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnURL;
    }


}