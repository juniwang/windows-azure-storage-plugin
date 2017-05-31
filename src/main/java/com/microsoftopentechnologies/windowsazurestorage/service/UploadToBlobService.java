/*
 Copyright 2017 Microsoft Open Technologies, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.microsoftopentechnologies.windowsazurestorage.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.microsoftopentechnologies.windowsazurestorage.AzureBlob;
import com.microsoftopentechnologies.windowsazurestorage.exceptions.WAStorageException;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureUtils;
import com.microsoftopentechnologies.windowsazurestorage.helper.Utils;
import com.microsoftopentechnologies.windowsazurestorage.service.model.PublisherServiceData;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.util.DirScanner;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Service to upload files to Windows Azure Blob Storage.
 */
public class UploadToBlobService extends UploadService {


    public UploadToBlobService(PublisherServiceData serviceData) {
        super(serviceData);
    }

    @Override
    protected void uploadArchive(final String archiveIncludes)
            throws WAStorageException {
        try {
            final CloudBlobContainer container = getCloudBlobContainer();

            final FilePath workspacePath = serviceData.getRemoteWorkspace();
            // Create a temp dir for the upload
            final FilePath tempDir = workspacePath.createTempDir(zipFolderName, null);
            final FilePath zipPath = tempDir.child(zipName);

            // zip included files into archive.zip file.
            final DirScanner.Glob globScanner = new DirScanner.Glob(archiveIncludes, excludedFilesAndZip());
            workspacePath.zip(zipPath.write(), globScanner);

            // When uploading the zip, do not add in the tempDir to the azure
            String blobURI = zipPath.getName();
            if (!StringUtils.isBlank(serviceData.getVirtualPath())) {
                blobURI = serviceData.getVirtualPath() + blobURI;
            }

            final CloudBlockBlob blob = container.getBlockBlobReference(blobURI);
            String uploadedFileHash = uploadBlob(blob, zipPath);
            // Make sure to note the new blob as an archive blob,
            // so that it can be specially marked on the azure storage page.
            AzureBlob azureBlob = new AzureBlob(blob.getName(), blob.getUri().toString().replace("http://", "https://"), uploadedFileHash, zipPath.length());
            serviceData.getArchiveBlobs().add(azureBlob);

            tempDir.deleteRecursive();
        } catch (IOException | InterruptedException | URISyntaxException | StorageException e) {
            throw new WAStorageException("Fail to upload individual files to blob", e);
        }
    }

    @Override
    protected void uploadIndividuals(final String embeddedVP, final FilePath[] paths)
            throws WAStorageException {
        try {
            final CloudBlobContainer container = getCloudBlobContainer();

            for (FilePath src : paths) {
                final String blobPath = getItemPath(src, embeddedVP);
                final CloudBlockBlob blob = container.getBlockBlobReference(blobPath);
                configureBlobPropertiesAndMetadata(blob);
                String uploadedFileHash = uploadBlob(blob, src);
                AzureBlob azureBlob = new AzureBlob(blob.getName(), blob.getUri().toString().replace("http://", "https://"), uploadedFileHash, src.length());
                serviceData.getIndividualBlobs().add(azureBlob);
            }
        } catch (IOException | InterruptedException | URISyntaxException | StorageException e) {
            throw new WAStorageException("Fail to upload archive to blob", e);
        }
    }

    private void configureBlobPropertiesAndMetadata(final CloudBlockBlob blob) throws IOException, InterruptedException {
        final EnvVars env = serviceData.getRun().getEnvironment(serviceData.getTaskListener());

        // Set blob properties
        if (serviceData.getBlobProperties() != null) {
            serviceData.getBlobProperties().configure(blob, env);
        }

        // Set blob metadata
        if (serviceData.getAzureBlobMetadata() != null) {
            blob.setMetadata(updateMetadata(blob.getMetadata()));
        }
    }

    /**
     * @param blob
     * @param src
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     * @returns Md5 hash of the uploaded file in hexadecimal encoding
     */
    private String uploadBlob(CloudBlockBlob blob, FilePath src)
            throws StorageException, IOException, InterruptedException {
        final MessageDigest md = DigestUtils.getMd5Digest();
        long startTime = System.currentTimeMillis();
        try (InputStream inputStream = src.read(); DigestInputStream digestInputStream = new DigestInputStream(inputStream, md)) {
            blob.upload(digestInputStream, src.length(), null, getBlobRequestOptions(), Utils.updateUserAgent());
        }
        long endTime = System.currentTimeMillis();

        println("Uploaded to file storage with uri " + blob.getUri() + " in " + getTime(endTime - startTime));
        return DatatypeConverter.printHexBinary(md.digest());
    }

    private CloudBlobContainer getCloudBlobContainer() throws URISyntaxException, StorageException, IOException {
        final CloudBlobContainer container = AzureUtils.getBlobContainerReference(
                serviceData.getStorageAccountInfo(), serviceData.getContainerName(), true, true, serviceData.isPubAccessible());

        // Delete previous contents if cleanup is needed
        if (serviceData.isCleanUpContainerOrShare()) {
            deleteBlobs(container.listBlobs());
        }
        return container;
    }

    /**
     * Deletes contents of container
     *
     * @param blobItems list of blobs to delete
     * @throws StorageException
     * @throws URISyntaxException
     */
    private void deleteBlobs(final Iterable<ListBlobItem> blobItems)
            throws StorageException, URISyntaxException, IOException {

        for (ListBlobItem blobItem : blobItems) {
            if (blobItem instanceof CloudBlob) {
                ((CloudBlob) blobItem).uploadProperties(null, null, Utils.updateUserAgent());
                ((CloudBlob) blobItem).delete();
            } else if (blobItem instanceof CloudBlobDirectory) {
                deleteBlobs(((CloudBlobDirectory) blobItem).listBlobs());
            }
        }
    }
}
