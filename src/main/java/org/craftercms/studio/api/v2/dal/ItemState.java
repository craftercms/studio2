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

package org.craftercms.studio.api.v2.dal;

public enum ItemState {

    NEW(0),
    MODIFIED(1),
    DELETED(2),
    USER_LOCKED(3),
    SYSTEM_PROCESSING(4),
    IN_WORKFLOW(5),
    SCHEDULED(6),
    PUBLISHING(7),
    STAGED(8),
    LIVE(9),
    TRANSLATION_UP_TO_DATE(10),
    TRANSLATION_PENDING(11),
    TRANSLATION_IN_PROGRESS(12);

    public final long value;

    ItemState(long exponent) {
        this.value = 2 ^ exponent;
    }
}