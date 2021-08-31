import { TestBed } from '@angular/core/testing';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { mockStore } from 'testing/store';
import { cloneDeep } from 'lodash';
import { Config, Deployment } from '@app/model';


const mockConfigsUnsorted = [
  { name: 'test2' },
  { name: 'test3' },
  { name: 'test1' },
  { name: 'test5' },
  { name: 'test4' },
];

const mockConfigsFilter = [
  { author: 'siembol', isDeployed: false, name: 'test1', tags: ['test4'], version: 2, versionFlag: -1 },
  { author: 'John', isDeployed: true, name: 'parse_alert', tags: ['test2'], version: 2, versionFlag: 3 },
  { author: 'siembol', isDeployed: true, name: 'test3', tags: ['alert', 'Test4', 'test3'], version: 2, versionFlag: -1 },
];

const mockDeploymentConfigsFilter = [
  { author: 'John', isDeployed: true, name: 'parse_alert', tags: ['test2'], version: 2, versionFlag: 3 },
  { author: 'siembol', isDeployed: true, name: 'test3', tags: ['alert', 'Test4', 'test3'], version: 2, versionFlag: -1 },
];

const mockDeployment = {
  configs: [],
  deploymentVersion:1,
};

describe('ConfigStoreStateBuilder', () => {
  let builder: ConfigStoreStateBuilder;
  let mockConfigsSorted;

  beforeEach(() => {
    builder = new ConfigStoreStateBuilder(cloneDeep(mockStore));

    mockConfigsSorted = [
      { name: 'test1' },
      { name: 'test2' },
      { name: 'test3' },
      { name: 'test4' },
      { name: 'test5' },
    ];
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should be created', () => {
    expect(builder).toBeTruthy();
  });

  describe('detectOutdatedConfigs', () => {
    it('should be outdated', () => {
      builder['state'].configs[0].version = 3;
      expect(builder['state'].deployment.configs[0].versionFlag).toEqual(-1);
      builder.detectOutdatedConfigs();
      expect(builder['state'].deployment.configs[0].versionFlag).toEqual(3);
    });

    it('should be not deployed', () => {
      expect(builder['state'].configs[1].isDeployed).toBeUndefined();
      builder.detectOutdatedConfigs();
      expect(builder['state'].configs[1].isDeployed).toEqual(false);
    });
  });

  describe('reorderConfigsByDeployment', () => {
    it('should reorder', () => {
      builder.configs(mockConfigsUnsorted as Config[]);
      mockDeployment.configs = mockConfigsSorted;
      builder.deployment(mockDeployment as Deployment);
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsSorted);
    });

    it('should not reorder', () => {
      builder.configs(mockConfigsSorted as Config[]);
      mockDeployment.configs = mockConfigsSorted;
      builder.deployment(mockDeployment as Deployment);
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsSorted);
    });

    it('should reorder with non deployed configs', () => {
      const mockConfigsUnsorted2 = cloneDeep(mockConfigsUnsorted);
      mockConfigsUnsorted2.splice(2, 0, { name: 'test6' });
      mockConfigsUnsorted2.splice(4, 0, { name: 'test7' });
      builder.configs(mockConfigsUnsorted2 as Config[]);
      mockDeployment.configs = mockConfigsSorted;
      builder.deployment(mockDeployment as Deployment);
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(
        mockConfigsSorted.concat({ name: 'test7' }, { name: 'test6' })
      );
    });

    it('should not have deployment configs so not reorder', () => {
      builder.configs(mockConfigsUnsorted as Config[]);
      mockDeployment.configs = [];
      builder.deployment(mockDeployment as Deployment);
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsUnsorted);
    });

    it('should do nothing', () => {
      builder.configs([]);
      mockDeployment.configs = [];
      builder.deployment(mockDeployment as Deployment);
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual([]);
    });
  });

  describe('computeFiltered', () => {
    beforeEach(() => {
      builder.configs(mockConfigsFilter as Config[]);
      mockDeployment.configs = mockDeploymentConfigsFilter;
      builder.deployment(mockDeployment as Deployment);
      builder.reorderConfigsByDeployment();
    });
    it('should filter undeployed', () => {
      builder.filterUndeployed(true);
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[0]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([]);
    });
    it('should filter upgradable', () => {
      builder.filterUpgradable (true);
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[1]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([mockDeploymentConfigsFilter[0]]);
    });
    it('should filter user configs', () => {
      builder.filterMyConfigs(true);
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[2], mockConfigsFilter[0]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([mockDeploymentConfigsFilter[1]]);
    });
    it('should filter for term', () => {
      builder.searchTerm('alert');
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[1], mockConfigsFilter[2]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([
        mockDeploymentConfigsFilter[0],
        mockDeploymentConfigsFilter[1],
      ]);
    });
    it('should filter for non existant term', () => {
      builder.searchTerm('xyz');
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([]);
    });
    it('should filter for term not dependant on case', () => {
      builder.searchTerm('test4');
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[2], mockConfigsFilter[0]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([mockDeploymentConfigsFilter[1]]);
    });
  });

  it("should resetEditedTestCase", () => {
    builder.resetEditedTestCase();
    expect((builder['state'] as any).editedTestCase).toBeNull();
  });

  
  it("should move config", () => {
    const mockConfigsMoved = [
      { name: 'test2' },
      { name: 'test3' },
      { name: 'test4' },
      { name: 'test5' },
      { name: 'test1' },
    ];
    mockDeployment.configs = mockConfigsSorted;
    builder.deployment(mockDeployment as Deployment);
    builder.computeFiltered("siembol");
    builder.moveConfigInDeployment(0,4);
    expect((builder['state'] as any).deployment.configs).toEqual(mockConfigsMoved);
  })

  it("should remove config in deployment", () => {
    const mockConfigsRemoved = [
      { name: 'test1' },
      { name: 'test3' },
      { name: 'test4' },
      { name: 'test5' },
    ];
    mockDeployment.configs = mockConfigsSorted;
    builder.deployment(mockDeployment as Deployment);
    builder.computeFiltered("siembol");
    builder.removeConfigFromDeployment(1);
    expect((builder['state'] as any).deployment.configs).toEqual(mockConfigsRemoved);
  })

  it("should upgrade config in deployment", () => {
    const mockConfigsUpgraded = [
      { name: 'test1' },
      { name: 'test2', version: 2 },
      { name: 'test3' },
      { name: 'test4' },
      { name: 'test5' },
    ];
    builder.configs(mockConfigsUpgraded as Config[]);
    mockDeployment.configs = mockConfigsSorted;
    builder.deployment(mockDeployment as Deployment);
    builder.reorderConfigsByDeployment();
    builder.computeFiltered("siembol");
    builder.upgradeConfigInDeployment(1);
    expect((builder['state'] as any).deployment.configs).toEqual(mockConfigsUpgraded);
  })

  it("should add config to deployment", () => {
      const mockDeploymentConfigs = [
        { name: 'test1' },
        { name: 'test2' },
        { name: 'test3' },
        { name: 'test4' },
      ];
      builder.configs(mockConfigsSorted as Config[]);
      builder.deployment({configs: mockDeploymentConfigs} as Deployment);
      builder.reorderConfigsByDeployment();
      builder.computeFiltered("siembol");
      builder.addConfigToDeployment(4);
      expect((builder['state'] as any).deployment.configs).toEqual(mockConfigsSorted);
    })

  it("should add config to deployment in position", () => {
    const mockDeploymentConfigs = [
      { name: 'test1' },
      { name: 'test3' },
      { name: 'test4' },
      { name: 'test5' },
    ];
    builder.configs(mockConfigsSorted as Config[]);
    builder.deployment({configs: mockDeploymentConfigs} as Deployment);
    builder.reorderConfigsByDeployment();
    builder.computeFiltered("siembol");
    builder.addConfigToDeploymenInPosition(4, 1);
    expect((builder['state'] as any).deployment.configs).toEqual(mockConfigsSorted);
  })
  
});
