import { AppContext } from '@app/services/app.service';
import { UserRole, ServiceInfo } from '@app/model/config-model';
import { cloneDeep } from 'lodash';
import { mockTestCasesSchema } from './testCasesSchema';

export const mockUserInfoAlert: ServiceInfo =
{
    name: 'myalert',
    type: 'alert',
    user_roles: [
        UserRole.SERVICE_USER,
        UserRole.SERVICE_ADMIN,
    ],
};

export const mockUserInfoAlertNoAdmin: ServiceInfo =
{
    name: 'myalert',
    type: 'alert',
    user_roles: [
        UserRole.SERVICE_USER,
    ],
};

export const mockUserInfoParser: ServiceInfo =
{
    name: 'myparserconfig',
    type: 'parserconfig',
    user_roles: [
        UserRole.SERVICE_USER,
    ],
};

export const mockUserServicesMap = new Map();
mockUserServicesMap.set('myalert', mockUserInfoAlert);
mockUserServicesMap.set('myparserconfig', mockUserInfoParser);

export const mockUserServicesMapNoAdmin = new Map();
mockUserServicesMapNoAdmin.set('myalert', mockUserInfoAlertNoAdmin);
mockUserServicesMapNoAdmin.set('myparserconfig', mockUserInfoParser);

export const mockAppContext = new AppContext();
mockAppContext.user = 'siembol';
mockAppContext.userServices= [mockUserInfoAlert, mockUserInfoParser];
mockAppContext.userServicesMap = mockUserServicesMap;

export const mockAppContextNoAdmin = new AppContext();
mockAppContextNoAdmin.user = 'siembol';
mockAppContextNoAdmin.userServices= [mockUserInfoAlertNoAdmin, mockUserInfoParser];
mockAppContextNoAdmin.userServicesMap = mockUserServicesMapNoAdmin;

export const mockAppContextWithTestSchema = cloneDeep(mockAppContext);
mockAppContextWithTestSchema.testCaseSchema = mockTestCasesSchema;
