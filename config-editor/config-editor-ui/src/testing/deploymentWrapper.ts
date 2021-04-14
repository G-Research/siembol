import { DeploymentWrapper, Deployment } from '@app/model/config-model';
import { mockParserConfigMin } from './configs';

export const mockDeployment: Deployment =
{
    configs:[
        mockParserConfigMin,
    ],
    deploymentVersion:1,
};

export const mockDeploymentWrapper: DeploymentWrapper =
{
   deploymentHistory:[
      {
         added:40,
         author:'siembol',
         date:'2021-02-19T13:32:17',
         removed:4,
      },
      {
         added:1,
         author:'siembol',
         date:'2021-01-07T15:47:57',
         removed:1,
      },
   ],
   storedDeployment: mockDeployment,
};
