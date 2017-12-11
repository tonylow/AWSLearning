package com.mkyong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import aws.sample.*;


@Controller
public class UploadController {

    //Save the uploaded file to this folder
    private static String UPLOADED_FOLDER = "c://temp//";

    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @PostMapping("/upload") // //new annotation since 4.3
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws Exception {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {

            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);

            AmazonS3Client s3ClientForStudentBuckets = createS3Client(Utils.getRegion());
        DataTransformer dt = new DataTransformer();
            dt.main();
            
            ObjectListing inputFileObjects = null;
            inputFileObjects = s3ClientForStudentBuckets.listObjects("161970126735-upload-bucket2-store");
            String fileKey="";
            String filelist="";
            int count = 1;
                // Iterate over the list of object summaries
                // Get the object key from each object summary
                for (S3ObjectSummary objectSummary : inputFileObjects.getObjectSummaries()) {
            	  //for (final File f	ileEntry : new File("C:\\temp").listFiles()) {

                	filelist += count + ":" + objectSummary.getKey() + "\n";
//                System.out.println(fileKey);
                	count++;
                inputFileObjects = s3ClientForStudentBuckets.listNextBatchOfObjects(inputFileObjects);
              }
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'" + "\n" + filelist);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }
    
    public  AmazonS3Client createS3Client(Region bucketRegion) {
        System.out.printf(
            "\nRUNNING SOLUTION CODE: %s! Follow the steps in the lab guide to replace this method with your own implementation.\n",
            "createS3Client");

        AmazonS3Client s3Client = null;
        s3Client = new AmazonS3Client();
        s3Client.setRegion(bucketRegion);
        return s3Client;
      }

}