import { TestBed } from '@angular/core/testing';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { mockStore } from 'testing/store';
import { cloneDeep } from 'lodash';

const mockConfigsSorted = [
  { name: 'test1' },
  { name: 'test2' },
  { name: 'test3' },
  { name: 'test4' },
  { name: 'test5' },
];

const mockConfigsUnsorted = [
  { name: 'test2' },
  { name: 'test3' },
  { name: 'test1' },
  { name: 'test5' },
  { name: 'test4' },
];

const mockConfigsFilter = [
  { author: 'siembol', isDeployed: false, name: 'test1', tags: ['test'], versionFlag: -1 },
  { author: 'John', isDeployed: true, name: 'parse_alert', tags: ['test2'], versionFlag: 3 },
  { author: 'siembol', isDeployed: true, name: 'test3', tags: ['alert', 'test3'], versionFlag: -1 },
];

const mockDeploymentConfigsFilter = [
  { author: 'John', isDeployed: true, name: 'parse_alert', tags: ['test2'], versionFlag: 3 },
  { author: 'siembol', isDeployed: true, name: 'test3', tags: ['alert', 'test3'], versionFlag: -1 },
];

describe('ConfigStoreStateBuilder', () => {
  let builder: ConfigStoreStateBuilder;

  beforeEach(() => {
    builder = new ConfigStoreStateBuilder(cloneDeep(mockStore));
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
      (builder['state'] as any).configs = mockConfigsUnsorted;
      (builder['state'] as any).deployment.configs = mockConfigsSorted;
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsSorted);
    });

    it('should not reorder', () => {
      (builder['state'] as any).configs = mockConfigsSorted;
      (builder['state'] as any).deployment.configs = mockConfigsSorted;
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsSorted);
    });

    it('should reorder with non deployed configs', () => {
      const mockConfigsUnsorted2 = cloneDeep(mockConfigsUnsorted);
      mockConfigsUnsorted2.splice(2, 0, { name: 'test6' });
      mockConfigsUnsorted2.splice(4, 0, { name: 'test7' });
      (builder['state'] as any).configs = mockConfigsUnsorted2;
      (builder['state'] as any).deployment.configs = mockConfigsSorted;
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(
        mockConfigsSorted.concat({ name: 'test7' }, { name: 'test6' })
      );
    });

    it('should not have deployment configs so not reorder', () => {
      (builder['state'] as any).configs = mockConfigsUnsorted;
      (builder['state'] as any).deployment.configs = [];
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsUnsorted);
    });

    it('should do nothing', () => {
      (builder['state'] as any).configs = [];
      (builder['state'] as any).deployment.configs = [];
      builder.reorderConfigsByDeployment();
      expect((builder['state'] as any).sortedConfigs).toEqual([]);
    });
  });

  describe('computeFiltered', () => {
    beforeEach(() => {
      (builder['state'] as any).sortedConfigs = mockConfigsFilter;
      (builder['state'] as any).deployment.configs = mockDeploymentConfigsFilter;
    });
    it('should filter undeployed', () => {
      builder['state'].filterUndeployed = true;
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[0]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([]);
    });
    it('should filter upgradable', () => {
      builder['state'].filterUpgradable = true;
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[1]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([mockDeploymentConfigsFilter[0]]);
    });
    it('should filter user configs', () => {
      builder['state'].filterMyConfigs = true;
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[0], mockConfigsFilter[2]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([mockDeploymentConfigsFilter[1]]);
    });
    it('should filter for term', () => {
      builder['state'].searchTerm = 'alert';
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([mockConfigsFilter[1], mockConfigsFilter[2]]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([
        mockDeploymentConfigsFilter[0],
        mockDeploymentConfigsFilter[1],
      ]);
    });
    it('should filter for non existant term', () => {
      builder['state'].searchTerm = 'xyz';
      builder.computeFiltered('siembol');
      expect((builder['state'] as any).filteredConfigs).toEqual([]);
      expect((builder['state'] as any).filteredDeployment.configs).toEqual([]);
    });
  });
});
