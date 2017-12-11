# Copyright 2017 Amazon Web Services, Inc. or its affiliates. All rights
# reserved.

import boto3
import time
import csv
from botocore.exceptions import ClientError

PHARMA_TABLE_NAME = "PharmaceuticalUsage"
BUCKET_REGION = "us-west-2"
BUCKET_NAME = "us-west-2-aws-staging"
PHARMA_DATA_FILE_KEY = "awsu-ilt/AWS-100-DEV/v2.2/binaries/input/lab-8-elastiCache/PharmaListings.csv"
FILE_KEY = "PharmaListings.csv"
DELIMITER = ","


def is_table_active(tableName=PHARMA_TABLE_NAME):
    # Is table active
    try:
        resource = boto3.resource('dynamodb')
        table = resource.Table(tableName)
        if table.table_status == 'ACTIVE':
            return True
    except ClientError as err:
        if (err.response.get('Error').get('Code')
                == 'ResourceNotFoundException'):
            print("{0} Table is not found here".format(tableName))
    except Exception as err:
        print("Error message:: {0}".format(err))
    return False


def get_info(drugName, tableName=PHARMA_TABLE_NAME):
    # Query the PharmaInfo table on the given region for a specific drug name.
    try:
        dynamodb = boto3.resource('dynamodb')
        myTable = dynamodb.Table(tableName)
        rec = myTable.get_item(Key={'drugName': drugName})
        # Retrieves from the records object returned by the query.
        if rec.get('Item'):
            return rec['Item'].get('usage')
    except Exception as err:
        print("Error message: {0}".format(err))


def load_data(
        tableName=PHARMA_TABLE_NAME,
        bucket=BUCKET_NAME,
        region=BUCKET_REGION,
        fKey=PHARMA_DATA_FILE_KEY,
        fName=FILE_KEY):
    # Add items (DrugName, usage) to the given PharmaInfo table available in
    # the given region
    try:
        # Check if the File Key is present in the bucket
        S3 = boto3.resource('s3', region)
        dynamodb = boto3.resource('dynamodb')
        myTable = dynamodb.Table(tableName)
        try:
            S3.meta.client.head_bucket(Bucket=bucket)
            myBucket = S3.Bucket(bucket)
        except ClientError as err:
            print("Bucket Not Available")
            print("Error message: {0}".format(err))
            return False
        except Exception as err:
            print("Error message: {0}".format(err))
            return False
        # Connects to S3 and get the CSV file from the given bucket
        try:
            myBucket.download_file(fKey, fName)
        except Exception as err:
            print("Error message: {0}".format(err))
            print("Downloading failed from S3 bucket")
            return False
        print("Loading pharmaceutical data from csv file to DynamoDB")
        with open(fName, newline='') as fh:
            reader = csv.DictReader(fh, delimiter=DELIMITER)
            print("Begin loading items ...")
            for row in reader:
                try:
                    resp = myTable.put_item(
                        Item={
                            'drugName': row['DrugName'],
                            'usage': row['Usage']})
                except Exception as err:
                    print("Error message: {0}".format(err))
                    numFailures += 1
            print("End loading items")
    except Exception as err:
        print("Failed creating item {0}".format(tableName))
        print("Error message: {0}".format(err))
        return False
    return True


def pharma_data_exists(tableName=PHARMA_TABLE_NAME):
    # Check if the given tableName exists and active.
    try:
        pharma_data_exists = False
        pharma_data_exists = is_table_active(tableName)
    except Exception as err:
        print("Error message:: {0}".format(err))
    return pharma_data_exists


def create_pharma_table(tableName=PHARMA_TABLE_NAME):
    # Create PharmaceuticalUsage table with the given name in the given region
    # with the following fields.
    rval = False
    dynamodb = boto3.resource('dynamodb')
    try:
        print("Creating Table")
        table = dynamodb.create_table(TableName=tableName,
                                      KeySchema=[{'AttributeName': 'drugName',
                                                  'KeyType': 'HASH'}],
                                      AttributeDefinitions=[{'AttributeName': 'drugName',
                                                             'AttributeType': 'S'}],
                                      ProvisionedThroughput={'ReadCapacityUnits': 5,
                                                             'WriteCapacityUnits': 5})
        # Table creation takes a little time. Please wait.
        time.sleep(5)
        # Checks if the given PharmaInfo table exists
        if pharma_data_exists(tableName):
            rval = True
    except Exception as err:
        print("{0} Table creation failed.".format(tableName))
        print("Error message: {0}".format(err))
    return rval


def setup():
    # Setting up DynamoDB:
    print("Begin DynamoDB Setup, Creating table")
    # Checks if the given PharmaInfo table exists
    rval = True
    if (not pharma_data_exists()):
        rval = create_pharma_table()
    print("Table created")
    if not rval:
        print("Loading failed since Table creation failed.")
        return False
    rval = load_data()
    if not rval:
        return False
    print("DynamoDB setup completed")
    return True
