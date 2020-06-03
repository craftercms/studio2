/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.model.aws.mediaconvert;

import java.util.List;

/**
 * Holds the data for a triggered MediaConvert job
 *
 * @author joseross
 * @since 3.1.1
 */
public class MediaConvertResult {

    /**
     * The id of the triggered job
     */
    protected String jobId;

    /**
     * The full ARN of the triggered job
     */
    protected String jobArn;

    /**
     * The list of files that will be generated by the triggered job
     */
    protected List<String> urls;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public String getJobArn() {
        return jobArn;
    }

    public void setJobArn(final String jobArn) {
        this.jobArn = jobArn;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(final List<String> urls) {
        this.urls = urls;
    }

}
