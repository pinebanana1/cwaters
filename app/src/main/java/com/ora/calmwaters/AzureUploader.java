/**----------------------------------------------------------------------------------
* Microsoft Developer & Platform Evangelism
*
* Copyright (c) Microsoft Corporation. All rights reserved.
*
* THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, 
* EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES 
* OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
*----------------------------------------------------------------------------------
* The example companies, organizations, products, domain names,	
* e-mail addresses, logos, people, places, and events depicted
* herein are fictitious.  No association with any real company,
* organization, product, domain name, email address, logo, person,
* places, or events is intended or should be inferred.
*----------------------------------------------------------------------------------
**/

package com.ora.calmwaters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class AzureUploader {
    /*
    **Only use Shared Key authentication for testing purposes!** 
    Your account name and account key, which give full read/write access to the associated Storage account, 
    will be distributed to every person that downloads your app. 
    This is **not** a good practice as you risk having your key compromised by untrusted clients. 
    Please consult following documents to understand and use Shared Access Signatures instead. 
    https://docs.microsoft.com/en-us/rest/api/storageservices/delegating-access-with-a-shared-access-signature 
    and https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1 
    */

    private static final String TAG = "OraAzure";

    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;"
            //+ "AccountName=oraphoto;"
            + "AccountName=eyecupstorage;"
            //+ "AccountKey=D3QkdD1dWkILjuQqIKW0F8/IVOQxx9vWRG42zUB86nOCC2VeHBR35B2H45kBg+3DZjjDhEMwqg9LTzt4UGbIQQ=="
            + "AccountKey=CZ29/pm/pgSGco7lYtCInJL8cp18NljtBYMQV41FQlokMApoTj3MPwxnqmBpcSyXo6hMCjziTM5f+ASt2jcnIg=="
            + ";EndpointSuffix=core.windows.net";

    private static final String testStorageConnectionString = "DefaultEndpointsProtocol=https;"
            + "AccountName=eyecupstorage;"
            + "AccountKey=CZ29/pm/pgSGco7lYtCInJL8cp18NljtBYMQV41FQlokMApoTj3MPwxnqmBpcSyXo6hMCjziTM5f+ASt2jcnIg=="
            + ";EndpointSuffix=core.windows.net";

    private static CloudBlobContainer getContainer() throws Exception {
        // Retrieve storage account from connection-string.

        CloudStorageAccount storageAccount = CloudStorageAccount
                .parse(storageConnectionString);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        String name = AzureUploader.class.getPackage().getName().substring("com.ora.".length());
        CloudBlobContainer container = blobClient.getContainerReference(name);

        return container;
    }

    private static CloudBlobContainer getTestContainer() throws Exception {
        CloudStorageAccount storageAccount = CloudStorageAccount
                .parse(testStorageConnectionString);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
//        String name = AzureUploader.class.getPackage().getName().substring("com.ora.".length());
        CloudBlobContainer container = blobClient.getContainerReference("software-dev-test");

        return container;
    }

    public static String[] ListImages() throws Exception{
        CloudBlobContainer container = getContainer();

        Iterable<ListBlobItem> blobs = container.listBlobs();

        LinkedList<String> blobNames = new LinkedList<>();
        for(ListBlobItem blob: blobs) {
            blobNames.add(((CloudBlockBlob) blob).getName());
        }

        return blobNames.toArray(new String[blobNames.size()]);
    }

    public static void GetImage(String name, OutputStream imageStream, long imageLength) throws Exception {
        CloudBlobContainer container = getContainer();

        CloudBlockBlob blob = container.getBlockBlobReference(name);

        if(blob.exists()){
            blob.downloadAttributes();

            imageLength = blob.getProperties().getLength();

            blob.download(imageStream);
        }
    }

    public static void UploadBlob(InputStream image, String blobName, int blobLength) throws Exception {
        CloudBlobContainer container = getTestContainer();

        container.createIfNotExists();

        CloudBlockBlob blob = container.getBlockBlobReference(blobName);
        blob.upload(image, blobLength);

//        return blobName;
    }

    public static void UploadImage(Context context, Uri imageUri)
    {
        try {
            final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            final int imageLength = imageStream.available();

            Thread th = new Thread(new Runnable() {
                public void run() {
                    try {
                        AzureUploader.UploadBlob(imageStream, imageUri.getLastPathSegment(), imageLength);
                    }
                    catch(Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }});
            th.start();
        }
        catch(Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public static void uploadDiary(Context context, Uri diaryUri) {
        try {
            final InputStream diaryStream = context.getContentResolver().openInputStream(diaryUri);
            final int diaryLength = diaryStream.available();

            Thread thread = new Thread(() -> {
                try {
                    AzureUploader.UploadBlob(diaryStream, diaryUri.getLastPathSegment(), diaryLength);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();

        } catch (Exception e) {
            LogHelper.e("AzureUploader - uploadDiary(): error is\n" + e.getMessage());
            e.printStackTrace();
        }
    }

}
