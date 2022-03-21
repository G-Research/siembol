import { TestBed } from '@angular/core/testing';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { mockStore } from 'testing/store';
import { cloneDeep } from 'lodash';
import { Config, Release } from '@app/model';


const mockConfigsUnsorted = [
  { name: 'test2' },
  { name: 'test3' },
  { name: 'test1' },
  { name: 'test5' },
  { name: 'test4' },
];

const mockRelease = {
  configs: [],
  releaseVersion:1,
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
      expect(builder['state'].release.configs[0].versionFlag).toEqual(-1);
      builder.detectOutdatedConfigs();
      expect(builder['state'].release.configs[0].versionFlag).toEqual(3);
    });

    it('should be not released', () => {
      expect(builder['state'].configs[1].isReleased).toBeUndefined();
      builder.detectOutdatedConfigs();
      expect(builder['state'].configs[1].isReleased).toEqual(false);
    });
  });

  describe('reorderConfigsByRelease', () => {
    it('should reorder', () => {
      builder.configs(mockConfigsUnsorted as Config[]);
      mockRelease.configs = mockConfigsSorted;
      builder.release(mockRelease as Release);
      builder.reorderConfigsByRelease();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsSorted);
    });

    it('should not reorder', () => {
      builder.configs(mockConfigsSorted as Config[]);
      mockRelease.configs = mockConfigsSorted;
      builder.release(mockRelease as Release);
      builder.reorderConfigsByRelease();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsSorted);
    });

    it('should reorder with non released configs', () => {
      const mockConfigsUnsorted2 = cloneDeep(mockConfigsUnsorted);
      mockConfigsUnsorted2.splice(2, 0, { name: 'test6' });
      mockConfigsUnsorted2.splice(4, 0, { name: 'test7' });
      builder.configs(mockConfigsUnsorted2 as Config[]);
      mockRelease.configs = mockConfigsSorted;
      builder.release(mockRelease as Release);
      builder.reorderConfigsByRelease();
      expect((builder['state'] as any).sortedConfigs).toEqual(
        mockConfigsSorted.concat({ name: 'test7' }, { name: 'test6' })
      );
    });

    it('should not have release configs so not reorder', () => {
      builder.configs(mockConfigsUnsorted as Config[]);
      mockRelease.configs = [];
      builder.release(mockRelease as Release);
      builder.reorderConfigsByRelease();
      expect((builder['state'] as any).sortedConfigs).toEqual(mockConfigsUnsorted);
    });

    it('should do nothing', () => {
      builder.configs([]);
      mockRelease.configs = [];
      builder.release(mockRelease as Release);
      builder.reorderConfigsByRelease();
      expect((builder['state'] as any).sortedConfigs).toEqual([]);
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
    mockRelease.configs = mockConfigsSorted;
    builder.release(mockRelease as Release);
    builder.moveConfigInRelease('test1', 4);
    expect((builder['state'] as any).release.configs).toEqual(mockConfigsMoved);
  })

  it("should remove config in release", () => {
    const mockConfigsRemoved = [
      { name: 'test1' },
      { name: 'test3' },
      { name: 'test4' },
      { name: 'test5' },
    ];
    mockRelease.configs = mockConfigsSorted;
    builder.release(mockRelease as Release);
    builder.removeConfigFromRelease("test2");
    expect((builder['state'] as any).release.configs).toEqual(mockConfigsRemoved);
  })

  it("should upgrade config in release", () => {
    const mockConfigsUpgraded = [
      { name: 'test1' },
      { name: 'test2', version: 2 },
      { name: 'test3' },
      { name: 'test4' },
      { name: 'test5' },
    ];
    builder.configs(mockConfigsUpgraded as Config[]);
    mockRelease.configs = mockConfigsSorted;
    builder.release(mockRelease as Release);
    builder.reorderConfigsByRelease();
    builder.upgradeConfigInRelease("test2");
    expect((builder['state'] as any).release.configs).toEqual(mockConfigsUpgraded);
  })

  it("should add config to release", () => {
      const mockReleaseConfigs = [
        { name: 'test1' },
        { name: 'test2' },
        { name: 'test3' },
        { name: 'test4' },
      ];
      builder.configs(mockConfigsSorted as Config[]);
      builder.release({configs: mockReleaseConfigs} as Release);
      builder.reorderConfigsByRelease();
      builder.addConfigToRelease("test5");
      const state = builder.build();
      expect(state.release.configs).toEqual(mockConfigsSorted);
    })

  it("should compute row data", () => {
    const expectedRowData = [{ 
      author: 'siembol', 
      version: 2, 
      config_name: 'config1', 
      releasedVersion: 2, 
      configHistory: [{ added: 2, author: 'siembol', date: '2021-03-12T16:26:08', removed: 2 }],
      labels: [ 'generic', 'json_extractor' ],
      testCasesCount: 0 ,
    },
    { 
      author: 'siembol', 
      version: 0, 
      config_name: 'config1_clone', 
      releasedVersion: 0, 
      configHistory: undefined, 
      labels: undefined, 
      testCasesCount: 0,
    }];
    
    const state = builder.computeConfigManagerRowData().build();
    expect(state.configManagerRowData).toEqual(expectedRowData);
  })

  it("should increment changes in release", () => {
    const state1 = builder
      .incrementChangesInRelease()
      .incrementChangesInRelease()
      .build()
    expect(state1.countChangesInRelease).toEqual(2);

    const state2 = builder
      .resetChangesInRelease()
      .build()
    expect(state2.countChangesInRelease).toEqual(0);
  })
});
