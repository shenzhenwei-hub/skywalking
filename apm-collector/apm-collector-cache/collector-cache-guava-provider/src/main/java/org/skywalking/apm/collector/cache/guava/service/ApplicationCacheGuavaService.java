/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.cache.guava.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.skywalking.apm.collector.cache.service.ApplicationCacheService;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.core.util.StringUtils;
import org.skywalking.apm.collector.storage.dao.IApplicationCacheDAO;
import org.skywalking.apm.collector.storage.service.DAOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng
 */
public class ApplicationCacheGuavaService implements ApplicationCacheService {

    private final Logger logger = LoggerFactory.getLogger(ApplicationCacheGuavaService.class);

    private final Cache<String, Integer> codeCache = CacheBuilder.newBuilder().initialCapacity(100).maximumSize(1000).build();

    private final DAOService daoService;

    public ApplicationCacheGuavaService(DAOService daoService) {
        this.daoService = daoService;
    }

    public int get(String applicationCode) {
        IApplicationCacheDAO dao = (IApplicationCacheDAO)daoService.get(IApplicationCacheDAO.class);

        int applicationId = 0;
        try {
            applicationId = codeCache.get(applicationCode, () -> dao.getApplicationId(applicationCode));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        if (applicationId == 0) {
            applicationId = dao.getApplicationId(applicationCode);
            if (applicationId != 0) {
                codeCache.put(applicationCode, applicationId);
            }
        }
        return applicationId;
    }

    private final Cache<Integer, String> idCache = CacheBuilder.newBuilder().maximumSize(1000).build();

    public String get(int applicationId) {
        IApplicationCacheDAO dao = (IApplicationCacheDAO)daoService.get(IApplicationCacheDAO.class);

        String applicationCode = Const.EMPTY_STRING;
        try {
            applicationCode = idCache.get(applicationId, () -> dao.getApplicationCode(applicationId));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        if (StringUtils.isEmpty(applicationCode)) {
            applicationCode = dao.getApplicationCode(applicationId);
            if (StringUtils.isNotEmpty(applicationCode)) {
                codeCache.put(applicationCode, applicationId);
            }
        }
        return applicationCode;
    }
}
