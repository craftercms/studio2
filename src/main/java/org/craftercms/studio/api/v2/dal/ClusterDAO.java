/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.api.v2.dal;

import java.util.List;
import java.util.Map;

public interface ClusterDAO {

    /**
     * Get all cluster members from database
     *
     * @return List of cluster members
     */
    List<ClusterMember> getAllMembers();

    /**
     * Get other cluster members from database - different from member executing query
     *
     * @return List of cluster members
     */
    List<ClusterMember> getOtherMembers(Map params);

    /**
     * Update cluster member in the database
     *
     * @param member Cluster member to update
     *
     * @return number of affected rows
     */
    int updateMember(ClusterMember member);

    /**
     * Add member to cluster in the database
     *
     * @param member Member to add
     *
     * @return number of affected rows
     */
    int addMember(ClusterMember member);

    /**
     * Remove members from cluster in the database
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int removeMembers(Map params);

    /**
     * Get cluster member by id from database
     *
     * @param clusterMemberId Cluster member id
     *
     * @return Cluster member with given id
     */
    ClusterMember getMemberById(long clusterMemberId);

    /**
     * Check if cluster member exists with given url
     * @param memberUrl Member Url
     * @return 0 if member does not exist, if member exists returns value greater than 0
     */
    int memberExists(String memberUrl);

    /**
     * Count number of cluster member registrations
     *
     * @param params Parameters for SQL query
     *
     * @return Number of cluster members registered with given parameters
     */
    int countRegistrations(Map params);

    /**
     * Remove member from cluster in the database by local ip
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int removeMemberByLocalIp(Map params);
}
