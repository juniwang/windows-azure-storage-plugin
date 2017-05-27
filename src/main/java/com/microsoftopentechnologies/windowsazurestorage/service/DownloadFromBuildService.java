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
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoftopentechnologies.windowsazurestorage.AzureBlob;
import com.microsoftopentechnologies.windowsazurestorage.AzureBlobAction;
import com.microsoftopentechnologies.windowsazurestorage.Messages;
import com.microsoftopentechnologies.windowsazurestorage.exceptions.WAStorageException;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureUtils;
import com.microsoftopentechnologies.windowsazurestorage.helper.Utils;
import com.microsoftopentechnologies.windowsazurestorage.service.model.BuilderServiceData;
import hudson.EnvVars;
import hudson.matrix.MatrixBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.copyartifact.BuildFilter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class DownloadFromBuildService extends DownloadService {
    public DownloadFromBuildService(final BuilderServiceData data) {
        super(data);
    }

    @Override
    public int execute() {
        int filesDownloaded = 0;

        try {
            Job<?, ?> job = Utils.getJenkinsInstance().getItemByFullName(serviceData.getProjectName(), Job.class);
            if (job != null) {
                // Resolve download location
                final EnvVars envVars = serviceData.getRun().getEnvironment(serviceData.getTaskListener());
                BuildFilter filter = new BuildFilter();
                Run source = serviceData.getBuildSelector().getBuild(job, envVars, filter, serviceData.getRun());

                if (source instanceof MatrixBuild) {
                    for (Run r : ((MatrixBuild) source).getExactRuns()) {
                        filesDownloaded += downloadArtifacts(r);
                    }
                } else {
                    filesDownloaded += downloadArtifacts(source);
                }
            } else {
                println(Messages.AzureStorageBuilder_job_invalid(serviceData.getProjectName()));
                setRunUnstable();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(error(Messages.AzureStorageBuilder_download_err(serviceData.getStorageAccountInfo().getStorageAccName())));
            setRunUnstable();
        }
        return filesDownloaded;
    }

    private int downloadArtifacts(final Run<?, ?> source) {
        int filesDownloaded = 0;
        try {

            final AzureBlobAction action = source.getAction(AzureBlobAction.class);
            List<AzureBlob> azureBlobs = action.getIndividualBlobs();
            if (action.getZipArchiveBlob() != null && serviceData.isIncludeArchiveZips()) {
                azureBlobs.addAll(Arrays.asList(action.getZipArchiveBlob()));
            }

            filesDownloaded = downloarAzureBlobs(azureBlobs);
        } catch (WAStorageException e) {
            setRunUnstable();
        }

        return filesDownloaded;
    }

    private int downloarAzureBlobs(final List<AzureBlob> azureBlobs) throws WAStorageException {
        int filesDownloaded = 0;

        for (final AzureBlob blob : azureBlobs) {
            try {
                println(Messages.AzureStorageBuilder_downloading());

                final URL blobURL = new URL(blob.getBlobURL());
                final String filePath = blobURL.getFile();

                final CloudBlobContainer container = AzureUtils.getBlobContainerReference(serviceData.getStorageAccountInfo(),
                        filePath.split("/")[1], false, true, null);

                if (shouldDownload(serviceData.getIncludeFilesPattern(), serviceData.getExcludeFilesPattern(), blob.getBlobName(), true)) {
                    final CloudBlockBlob cbb = container.getBlockBlobReference(blob.getBlobName());
                    downloadBlob(cbb);
                    filesDownloaded++;
                }
            } catch (StorageException | URISyntaxException | IOException e) {
                throw new WAStorageException(e.getMessage(), e.getCause());
            }
        }
        return filesDownloaded;
    }
}
