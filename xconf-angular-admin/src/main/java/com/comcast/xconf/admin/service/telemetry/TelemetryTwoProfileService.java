/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Jeyabala Murugan
 * Created: 13/07/2020
 */
package com.comcast.xconf.admin.service.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.admin.service.telemetrytwochange.ApprovedTelemetryTwoChangeCrudService;
import com.comcast.xconf.admin.service.telemetrytwochange.TelemetryTwoChangeCrudService;
import com.comcast.xconf.admin.validator.telemetry.TelemetryTwoProfileValidator;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.telemetry.TelemetryTwoProfilePredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.google.common.base.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.comcast.xconf.admin.service.telemetrytwochange.TelemetryTwoChangeBuilders.buildToCreate;
import static com.comcast.xconf.admin.service.telemetrytwochange.TelemetryTwoChangeBuilders.buildToDelete;
import static com.comcast.xconf.admin.service.telemetrytwochange.TelemetryTwoChangeBuilders.buildToUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.comcast.xconf.admin.service.telemetrytwochange.TelemetryTwoChangeBuilders.*;

@Service
@Component
public class TelemetryTwoProfileService extends AbstractApplicationTypeAwareService<TelemetryTwoProfile> {

    @Autowired
    private CachedSimpleDao<String, TelemetryTwoProfile> telemetryTwoProfileDAO;

    @Autowired
    private CachedSimpleDao<String, TelemetryTwoRule> telemetryTwoRuleDAO;

    @Autowired
    private TelemetryPermissionService permissionService;

    @Autowired
    private TelemetryTwoProfilePredicates telemetryTwoProfilePredicates;

    @Autowired
    private TelemetryTwoProfileValidator validator;
    
    @Autowired
    private TelemetryTwoChangeCrudService<TelemetryTwoProfile> pendingChangesService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ApprovedTelemetryTwoChangeCrudService<TelemetryTwoProfile> approvedChangeCrudService;

    @Override
    public CachedSimpleDao<String, TelemetryTwoProfile> getEntityDAO() {
        return telemetryTwoProfileDAO;
    }

    @Override
    protected PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    protected List<Predicate<TelemetryTwoProfile>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return telemetryTwoProfilePredicates.getPredicates(contextOptional);
    }

    @Override
    public IValidator<TelemetryTwoProfile> getValidator() {
        return validator;
    }

    public List<TelemetryTwoProfile> getTelemetryTwoProfilesByIdList(List<String> telemetryTwoProfileIdList) {
        List<TelemetryTwoProfile> telemetryTwoProfiles = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(telemetryTwoProfileIdList)) {
            for (String telemetryTwoProfileId : telemetryTwoProfileIdList) {
                TelemetryTwoProfile telemetryTwoProfile = getEntityDAO().getOne(telemetryTwoProfileId);
                if (telemetryTwoProfile != null) {
                    telemetryTwoProfiles.add(telemetryTwoProfile);
                }
            }
        }
        return telemetryTwoProfiles;
    }
    
    public TelemetryTwoChange<TelemetryTwoProfile> writeCreateChange(TelemetryTwoProfile profile) {
        beforeCreating(profile);
        beforeSaving(profile);
        return pendingChangesService.create(buildToCreate(profile, EntityType.TELEMETRY_TWO_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
    }

    public TelemetryTwoChange<TelemetryTwoProfile> writeUpdateChange(TelemetryTwoProfile newProfile) {
        beforeUpdating(newProfile);
        beforeSaving(newProfile);
        TelemetryTwoProfile oldProfile = getOne(newProfile.getId());
        return pendingChangesService.create(buildToUpdate(oldProfile, newProfile, EntityType.TELEMETRY_TWO_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
    }

    public boolean writeUpdateChangeOrSave(TelemetryTwoProfile newProfile) {
        beforeUpdating(newProfile);
        beforeSaving(newProfile);
        TelemetryTwoProfile oldProfile = getOne(newProfile.getId());
        if (newProfile.equals(oldProfile)) {
            update(newProfile);
            return false;
        } else {
            pendingChangesService.create(buildToUpdate(oldProfile, newProfile, EntityType.TELEMETRY_TWO_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
            return true;
        }
    }

    public TelemetryTwoProfile writeDeleteChange(String id) {
        beforeRemoving(id);
        TelemetryTwoProfile profile = getOne(id);
        pendingChangesService.create(buildToDelete(profile, EntityType.TELEMETRY_TWO_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
        return profile;
    }
    
    @Override
    public TelemetryTwoProfile delete(String id) {
        TelemetryTwoProfile delete = super.delete(id);
        approvedChangeCrudService.saveToApproved(buildToDelete(delete, EntityType.TELEMETRY_TWO_PROFILE, permissionService.getWriteApplication(), authService.getUserNameOrUnknown()));
        return delete;
    }

    @Override
    protected void validateUsage(String id) {
        Iterable<TelemetryTwoRule> all = Optional.presentInstances(telemetryTwoRuleDAO.asLoadingCache().asMap().values());
        for (TelemetryTwoRule rule : all) {
            if (rule.getBoundTelemetryIds().contains(id)) {
                throw new EntityConflictException("Can't delete profile as it's used in telemetry rule: " + rule.getName());
            }
        }
        TelemetryTwoProfile profileToRemove = getOne(id);
        if (CollectionUtils.isNotEmpty(pendingChangesService.getChangesByEntityId(id))) {
            throw new EntityConflictException("There is change for " + profileToRemove.getName() + " telemetry 2.0 profile");
        }
    }
}