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
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoftopentechnologies.windowsazurestorage.Messages;
import com.microsoftopentechnologies.windowsazurestorage.exceptions.WAStorageException;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureUtils;
import com.microsoftopentechnologies.windowsazurestorage.service.model.DownloadServiceData;

import java.io.IOException;
import java.net.URISyntaxException;

public class DownloadFromContainerService extends DownloadService {
    public DownloadFromContainerService(DownloadServiceData data) {
        super(data);
    }

    @Override
    public int execute() {
        final DownloadServiceData serviceData = getServiceData();
        int filesNeedDownload;
        try {
            println(Messages.AzureStorageBuilder_downloading());
            final CloudBlobContainer container = AzureUtils.getBlobContainerReference(
                    serviceData.getStorageAccountInfo(),
                    serviceData.getContainerName(),
                    false,
                    true,
                    null);
            filesNeedDownload = scanBlobs(container.listBlobs());
            println(Messages.AzureStorageBuilder_files_need_download_count(filesNeedDownload));
            waitForDownloadEnd();
        } catch (StorageException | URISyntaxException | IOException | WAStorageException e) {
            e.printStackTrace(error(Messages.AzureStorageBuilder_download_err(
                    serviceData.getStorageAccountInfo().getStorageAccName())));
            setRunUnstable();
        }
        return getFilesDownloaded();
    }

    protected int scanBlobs(Iterable<ListBlobItem> blobItems)
            throws URISyntaxException, StorageException, WAStorageException {
        final DownloadServiceData serviceData = getServiceData();
        int filesNeedDownload = 0;
        for (final ListBlobItem blobItem : blobItems) {
            // If the item is a blob, not a virtual directory
            if (blobItem instanceof CloudBlob) {
                // Download the item and save it to a file with the same
                final CloudBlob blob = (CloudBlob) blobItem;

                // Check whether we should download it.
                if (shouldDownload(
                        serviceData.getIncludeFilesPattern(),
                        serviceData.getExcludeFilesPattern(),
                        blob.getName(),
                        true)) {
                    getExecutorService().submit(new DownloadThread(blob));
                    filesNeedDownload++;
                }
            } else if (blobItem instanceof CloudBlobDirectory) {
                final CloudBlobDirectory blobDirectory = (CloudBlobDirectory) blobItem;
                if (shouldDownload(
                        serviceData.getIncludeFilesPattern(),
                        serviceData.getExcludeFilesPattern(),
                        blobDirectory.getPrefix(),
                        false)) {
                    filesNeedDownload += scanBlobs(blobDirectory.listBlobs());
                }
            }
        }
        return filesNeedDownload;
    }
}
