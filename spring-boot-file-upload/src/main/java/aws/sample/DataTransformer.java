package aws.sample;
// Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// The DataTransformer class transforms objects in the input S3 bucket
// and puts the transformed objects into the output S3 bucket.
public class DataTransformer {

  // STUDENT TODO 2: Set output bucket name (must be globally unique)
  public static final String OUTPUT_BUCKET_NAME = "161970126735-upload-bucket2-store";

  // Region in which student's buckets will be created
  public static final Region BUCKET_REGION = Utils.getRegion();

  // The Amazon S3 client allows you to manage buckets and objects programmatically
  public static AmazonS3Client s3ClientForStudentBuckets;

  // List used to store pre-signed URLs generated
  public static List<URL> presignedUrls = new ArrayList<>();

  // Variables used to create JSON content
  public static final String[] ATTRS = {"genericDrugName", "adverseReaction"};


  public void main() throws Exception {
    ObjectListing inputFileObjects = null;
    String fileKey = null;
    S3Object s3Object = null;
    File transformedFile = null;
    URL url = null;
    PutObjectResult response = null;

    // Create AmazonS3Client
    // The AmazonS3Client will automatically retrieve the credential profiles file at the default location (~/.aws/credentials)
    s3ClientForStudentBuckets = createS3Client(BUCKET_REGION);

   
    try {
      System.out.println("DataTransformer: Starting");


   //   do {
        // Iterate over the list of object summaries
        // Get the object key from each object summary
//        for (S3ObjectSummary objectSummary : inputFileObjects.getObjectSummaries()) {
    	  for (final File fileEntry : new File("C:\\temp").listFiles()) {

          fileKey = fileEntry.getName();
          System.out.println("DataTransformer: Transforming file: " + fileKey);

            // Retrieve the object with the specified key from the input bucket
            //s3Object = getObject(s3ClientForStudentBuckets, INPUT_BUCKET_NAME, fileKey);

            // Convert the file from CSV to JSON format
            transformedFile = transformText(fileEntry);

            // STUDENT TODO 7: Switch to enhanced file upload
            putObjectBasic(OUTPUT_BUCKET_NAME, fileKey, transformedFile);
            // response = putObjectEnhanced(OUTPUT_BUCKET_NAME, fileKey, transformedFile);

            if (response != null) {
              System.out.println("Encryption algorithm: " + response.getSSEAlgorithm());
              System.out.println(
                  "User metadata: "
                      + s3ClientForStudentBuckets
                          .getObjectMetadata(OUTPUT_BUCKET_NAME, fileKey)
                          .getUserMetadata());
            }

            // Generate a pre-signed URL for the JSON file
            url = generatePresignedUrl(OUTPUT_BUCKET_NAME, fileKey);

            if (url != null) {
              presignedUrls.add(url);
            }
          }
        
        //inputFileObjects = s3ClientForStudentBuckets.listNextBatchOfObjects(inputFileObjects);
     // } while (inputFileObjects.isTruncated());

      printPresignedUrls();
      System.out.println("DataTransformer: DONE");
    } catch (AmazonServiceException ase) {
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      System.out.println("Error Message: " + ace.getMessage());
    }
  }

  // Read the input stream of the S3 object. Transform the content to JSON format
  // Return the transformed text in a File object
  private static File transformText(S3Object s3Object) throws IOException {
    File transformedFile = new File("transformedfile.txt");
    String inputLine = null;
    StringBuffer outputStrBuf = new StringBuffer(1024);
    outputStrBuf.append("[\n");

    try (java.io.InputStream is = s3Object.getObjectContent();
        java.util.Scanner s = new java.util.Scanner(is);
        FileOutputStream fos = new FileOutputStream(transformedFile)) {
      s.useDelimiter("\n");
      while (s.hasNextLine()) {
        inputLine = s.nextLine();
        outputStrBuf.append(inputLine);
      }
    } catch (IOException e) {
      System.out.println("DataTransformer: Unable to create transformed file");
      e.printStackTrace();
    }
    return transformedFile;
  }
  
  private static File transformText(File input) throws IOException {
	    File transformedFile = new File("transformedfile.txt");
	    String inputLine = null;
	    StringBuffer outputStrBuf = new StringBuffer(1024);
	    outputStrBuf.append("[\n");

	    try (java.io.InputStream is = new FileInputStream(input);
	        java.util.Scanner s = new java.util.Scanner(is);
	        FileOutputStream fos = new FileOutputStream(transformedFile)) {
	      s.useDelimiter("\n");
	      while (s.hasNextLine()) {
	        inputLine = s.nextLine();
	        outputStrBuf.append(inputLine);
	      }
	    } catch (IOException e) {
	      System.out.println("DataTransformer: Unable to create transformed file");
	      e.printStackTrace();
	    }
	    return transformedFile;
	  }

  
  private static void printPresignedUrls() {
    System.out.println("DataTransformer: Pre-signed URLs: ");
    for (URL url : presignedUrls) {
      System.out.println(url + "\n");
    }
  }

  /**
   * Return a S3 Client
   *
   * @param bucketRegion    Region containing the buckets
   * @return                The S3 Client
   */
  private static AmazonS3Client createS3Client(Region bucketRegion) {
    // STUDENT TODO 3: Replace the solution with your own code
    return Solution.createS3Client(bucketRegion);
  }

  /**
   * Download a file from a S3 bucket
   *
   * @param s3Client      The S3 Client
   * @param bucketName    Name of the S3 bucket
   * @param fileKey       Key (path) to the file
   * @return              The file contents
   */
  private static S3Object getObject(AmazonS3Client s3Client, String bucketName, String fileKey) {
    // STUDENT TODO 4: Replace the solution with your own code
    return Solution.getObject(s3ClientForStudentBuckets, bucketName, fileKey);
  }

  /**
   * Upload a file to a S3 bucket
   *
   * @param bucketName        Name of the S3 bucket
   * @param fileKey           Key (path) to the file
   * @param transformedFile   Contents of the file
   */
  private static void putObjectBasic(String bucketName, String fileKey, File transformedFile) {
    // STUDENT TODO 5: Replace the solution with your own code
    Solution.putObjectBasic(
        s3ClientForStudentBuckets, OUTPUT_BUCKET_NAME, fileKey, transformedFile);
  }

  /**
   * Return a presigned URL to a file
   *
   * @param bucketName    Name of the S3 bucket
   * @param objectKey     Key (path) to the file
   * @return              Presigned URL
   */
  private static URL generatePresignedUrl(String bucketName, String objectKey) {
    // STUDENT TODO 6: Replace the solution with your own code
    return Solution.generatePresignedUrl(s3ClientForStudentBuckets, bucketName, objectKey);
  }

  /**
   * Upload a file to a S3 bucket using AES 256 server-side encryption
   *
   * @param bucketName        Name of the S3 bucket
   * @param fileKey           Key (path) to the file
   * @param transformedFile   Contents of the file
   * @return                  Response object for file creation
   */
  private static PutObjectResult putObjectEnhanced(
      String bucketName, String fileKey, File transformedFile) {
    // STUDENT TODO 8: Replace the solution with your own code
    return Solution.putObjectEnhanced(
        s3ClientForStudentBuckets, bucketName, fileKey, transformedFile);
  }

}
