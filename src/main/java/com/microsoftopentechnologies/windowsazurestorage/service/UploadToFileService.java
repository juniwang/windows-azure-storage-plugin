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

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.*;
import com.microsoftopentechnologies.windowsazurestorage.AzureBlob;
import com.microsoftopentechnologies.windowsazurestorage.exceptions.WAStorageException;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureUtils;
import com.microsoftopentechnologies.windowsazurestorage.helper.Constants;
import com.microsoftopentechnologies.windowsazurestorage.helper.Utils;
import com.microsoftopentechnologies.windowsazurestorage.service.model.PublisherServiceData;
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

public class UploadToFileService extends UploadService {
    public UploadToFileService(PublisherServiceData serviceData) {
        super(serviceData);
    }

    @Override
    protected void uploadIndividuals(String embeddedVP, FilePath[] paths) throws WAStorageException {
        try {
            final CloudFileShare fileShare = getCloudFileShare();

            for (FilePath src : paths) {
                final String filePath = getItemPath(src, embeddedVP);
                final CloudFile cloudFile = fileShare.getRootDirectoryReference().getFileReference(filePath);
                final String hash = uploadCloudFile(cloudFile, src);
                AzureBlob azureBlob = new AzureBlob(cloudFile.getName(), cloudFile.getUri().toString().replace("http://", "https://"), hash, src.length(), Constants.FILE_STORAGE);
                serviceData.getIndividualBlobs().add(azureBlob);
            }
        } catch (URISyntaxException | StorageException | IOException | InterruptedException e) {
            throw new WAStorageException("fail to upload individual files to azure file storage", e);
        }
    }

    @Override
    protected void uploadArchive(String archiveIncludes) throws WAStorageException {
        try {
            final CloudFileShare fileShare = getCloudFileShare();

            final FilePath workspacePath = serviceData.getRemoteWorkspace();
            // Create a temp dir for the upload
            final FilePath tempDir = workspacePath.createTempDir(zipFolderName, null);
            final FilePath zipPath = tempDir.child(zipName);

            // zip included files into archive.zip file.
            final DirScanner.Glob globScanner = new DirScanner.Glob(archiveIncludes, excludedFilesAndZip());
            workspacePath.zip(zipPath.write(), globScanner);

            // When uploading the zip, do not add in the tempDir to the azure
            String azureFileName = zipPath.getName();
            if (!StringUtils.isBlank(serviceData.getVirtualPath())) {
                azureFileName = serviceData.getVirtualPath() + azureFileName;
            }

            final CloudFile cloudFile = fileShare.getRootDirectoryReference().getFileReference(azureFileName);
            String uploadedFileHash = uploadCloudFile(cloudFile, zipPath);
            // Make sure to note the new blob as an archive blob,
            // so that it can be specially marked on the azure storage page.
            AzureBlob azureBlob = new AzureBlob(cloudFile.getName(), cloudFile.getUri().toString().replace("http://", "https://"), uploadedFileHash, zipPath.length(), Constants.FILE_STORAGE);
            serviceData.getArchiveBlobs().add(azureBlob);

            tempDir.deleteRecursive();
        } catch (IOException | InterruptedException | URISyntaxException | StorageException e) {
            throw new WAStorageException("Fail to upload individual files to blob", e);
        }
    }

    private String uploadCloudFile(final CloudFile cloudFile, final FilePath localPath)
            throws WAStorageException {
        try {
            ensureDirExist(cloudFile.getParent());
            cloudFile.setMetadata(updateMetadata(cloudFile.getMetadata()));

            final MessageDigest md = DigestUtils.getMd5Digest();
            long startTime = System.currentTimeMillis();
            try (InputStream inputStream = localPath.read(); DigestInputStream digestInputStream = new DigestInputStream(inputStream, md)) {
                cloudFile.upload(digestInputStream, localPath.length(), null, new FileRequestOptions(), Utils.updateUserAgent());
            }
            long endTime = System.currentTimeMillis();

            println("Uploaded blob with uri " + cloudFile.getUri() + " in " + getTime(endTime - startTime));
            return DatatypeConverter.printHexBinary(md.digest());
        } catch (IOException | InterruptedException | URISyntaxException | StorageException e) {
            throw new WAStorageException("fail to upload file to azure file storage", e);
        }

    }

    private void ensureDirExist(final CloudFileDirectory directory)
            throws WAStorageException {
        try {
            if (!directory.exists()) {
                ensureDirExist(directory.getParent());
                directory.create();
            }
        } catch (StorageException | URISyntaxException e) {
            throw new WAStorageException("fail to create directory.", e);
        }
    }

    private CloudFileShare getCloudFileShare() throws URISyntaxException, StorageException {
        final CloudStorageAccount cloudStorageAccount = AzureUtils.getCloudStorageAccount(serviceData.getStorageAccountInfo());
        final CloudFileClient cloudFileClient = cloudStorageAccount.createCloudFileClient();
        final CloudFileShare fileShare = cloudFileClient.getShareReference(serviceData.getFileShareName());

        // Delete previous contents if cleanup is needed
        if (serviceData.isCleanUpContainerOrShare() && fileShare.exists()) {
            println("Clean up existing files in  file share " + serviceData.getFileShareName());
            deleteFiles(fileShare.getRootDirectoryReference().listFilesAndDirectories());
        }

        fileShare.createIfNotExists();
        return fileShare;
    }

    private void deleteFiles(final Iterable<ListFileItem> fileItems) throws StorageException {
        for (final ListFileItem fileItem : fileItems) {
            if (fileItem instanceof CloudFileDirectory) {
                final CloudFileDirectory directory = (CloudFileDirectory) fileItem;
                deleteFiles(directory.listFilesAndDirectories());
            } else if (fileItem instanceof CloudFile) {
                ((CloudFile) fileItem).delete();
            }
        }
    }
}
