// Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using Amazon;
using Amazon.DynamoDBv2;
using Amazon.DynamoDBv2.Model;
using Amazon.Runtime;
using Amazon.S3;
using Amazon.S3.Model;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;

namespace Lab8
{
    public static class DynamoDBManager
    {
        public static readonly string PharmaTableName = "PharmaceuticalsUsage";
        public static readonly string S3BucketName = Utils.LabS3BucketName;
        public static readonly string S3BucketRegion = Utils.LabS3BucketRegion;
        public static readonly string PharmaDataFileKey = Utils.PharmaDataFileKey;

        private static AmazonDynamoDBClient dynamoDBClient = null;
        private static AmazonS3Client s3 = null;

        public static void Init()
        {
            s3 = new AmazonS3Client();
            dynamoDBClient = new AmazonDynamoDBClient();
            Setup();
        }

        private static void Setup()
        {
            Debug.WriteLine("Beginning DynamoDB setup.");
            if (!PharmaDataExists())
            {
                CreatePharmaTable();
            }
            LoadData();
            Debug.WriteLine("Completed DynamoDB setup.");
        }

        private static bool PharmaDataExists()
        {
            bool pharmaDataExists = false;
            try
            {
                dynamoDBClient.DescribeTable(new DescribeTableRequest
                {
                    TableName = PharmaTableName
                });

                pharmaDataExists = true;
            }
            catch (ResourceNotFoundException e)
            {
                Debug.WriteLine(PharmaTableName + " DynamoDB table does not exist. " + e.Message);
            }
            return pharmaDataExists;
        }

        private static void CreatePharmaTable()
        {
            var attributeDefinitions = new List<AttributeDefinition>()
            {
                {
                    new AttributeDefinition {
                      AttributeName = "DrugName",
                      AttributeType = "S",
                    }
                }
            };

            var tableKeySchema = new List<KeySchemaElement>()
            {
                {
                    new KeySchemaElement {
                      AttributeName = "DrugName",
                      KeyType = "HASH",
                    }
                }
            };

            CreateTableRequest request = new CreateTableRequest
            {
                TableName = PharmaTableName,
                ProvisionedThroughput = new ProvisionedThroughput
                {
                    ReadCapacityUnits = (long)5,
                    WriteCapacityUnits = (long)5
                },
                AttributeDefinitions = attributeDefinitions,
                KeySchema = tableKeySchema
            };

            Debug.WriteLine(String.Format("Creating {0} table. ", PharmaTableName));

            dynamoDBClient.CreateTable(request);

            string status = null;
            do
            {
                System.Threading.Thread.Sleep(5000); // Wait 5 seconds.
                try
                {
                    var res = dynamoDBClient.DescribeTable(new DescribeTableRequest
                    {
                        TableName = PharmaTableName
                    });

                    status = res.Table.TableStatus;
                }
                catch (Exception)
                {
                    throw;
                }
            } while (status != "ACTIVE");
        }

        /**
         * Loads pharmaceutical usage data from CSV file into DynamoDB table.
         */
        private static void LoadData()
        {
            string line;
            char[] splitter = { ',' };
            RegionEndpoint region = RegionEndpoint.USWest2;
            s3 = new AmazonS3Client(region);
            StreamReader reader = null;

            try
            {
                GetObjectRequest requestFromS3 = new GetObjectRequest()
                {
                    BucketName = S3BucketName,
                    Key = PharmaDataFileKey
                };

                GetObjectResponse responseFromS3 = s3.GetObject(requestFromS3);

                if (responseFromS3 != null)
                {
                    reader = new StreamReader(responseFromS3.ResponseStream);

                    reader.ReadLine();

                    while ((line = reader.ReadLine()) != null)
                    {
                        string[] pharmaDataAttrValues = line.Split(splitter);

                        try
                        {
                            if (!pharmaDataAttrValues[0].ToLower().Equals("DrugName"))
                            {
                                // CSV attributes: DrugName, Usage
                                Debug.WriteLine("\n");

                                var requestDiseaseListing = new PutItemRequest
                                {
                                    TableName = PharmaTableName,
                                    Item = new Dictionary<string, AttributeValue>()
                                    {
                                        { "DrugName", new AttributeValue { S = pharmaDataAttrValues[0] }},
                                        { "Usage", new AttributeValue { S = pharmaDataAttrValues[1] }}
                                    }
                                };

                                dynamoDBClient.PutItem(requestDiseaseListing);
                                Debug.WriteLine("Added item to table:" + line);

                            }
                        }
                        catch (Exception)
                        {
                            throw;
                        }
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                Debug.WriteLine(e.StackTrace);
            }
            catch (IOException e)
            {
                Debug.WriteLine(e.StackTrace);
            }
            finally
            {
                if (reader != null)
                {
                    try
                    {
                        reader.Close();
                    }
                    catch (IOException e)
                    {
                        Debug.WriteLine(e.StackTrace);
                    }
                }
            }

           Debug.WriteLine("DynamoDB data upload complete.");
        }

        public static string GetPharmaInfo(string drugName)
        {
            string pharmaInfo = null;

            var request = new GetItemRequest
            {
                TableName = PharmaTableName,
                Key = new Dictionary<string, AttributeValue>() { { "DrugName", new AttributeValue { S = drugName } } },
            };

            GetItemResponse response = dynamoDBClient.GetItem(request);
            pharmaInfo = response.Item["Usage"].S.ToString();
            return pharmaInfo;
        }

    }
}
