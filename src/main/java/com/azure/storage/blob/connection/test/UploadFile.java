// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.azure.storage.blob.connection.test;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class UploadFile {
    private static final Logger log = LoggerFactory.getLogger(UploadFile.class);

    public static void main(String[] args) throws IOException {
        String connectionString = System.getenv("connectionString");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        // Create the container and return a container client object
        String containerName = "test-container-" + java.util.UUID.randomUUID().toString().substring(0, 6);
        BlobContainerClient containerClient = blobServiceClient.createBlobContainer(containerName);

        String fileName = "test-file-" + java.util.UUID.randomUUID() + ".txt";
        File localFile = new File(fileName);
        try (FileWriter writer = new FileWriter(localFile, true)) {
            writer.write("Upload was successful!");
        }

        // Reference to the blob
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        // Upload the blob
        log.info("Uploading to Azure Blob Storage URL: {}", blobClient.getBlobUrl());
        try {
            blobClient.uploadFromFile(fileName);
        } catch (Exception e) {
            log.error("Couldn't upload blob {}", fileName, e);
        }

        // Enumerate blob in the container
        Optional<BlobItem> blobItemOptional = containerClient.listBlobs().stream().findFirst();
        BlobItem blobItem;
        if (blobItemOptional.isPresent()) {
            blobItem = blobItemOptional.get();
            if (!fileName.equals(blobItem.getName())) {
                log.error("Blob in container was named {}, not {} as expected", blobItem.getName(), fileName);
            } else {
                log.info("Blob {} was successfully uploaded!", blobItem.getName());
            }
        } else {
            log.error("Blob {} couldn't be retrieved from container", fileName);
        }

        containerClient.delete();
        localFile.delete();
    }
}