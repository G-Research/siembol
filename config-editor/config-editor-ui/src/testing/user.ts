import { UserInfo, UserRole } from '@app/model/config-model';
import { mockUserInfoAlert, mockUserInfoParser } from './appContext';


export const mockUserInfo: UserInfo =
{
  services: [ mockUserInfoAlert, mockUserInfoParser],
  user_name: 'siembol',
};
