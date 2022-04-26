import { TestBed } from '@angular/core/testing';
import { ConfigStoreStateBuilder } from './config-store-state.builder';
import { mockStore } from 'testing/store';
import { cloneDeep } from 'lodash';
import { Config, Release } from '@app/model';
import { mockUiMetadataAlert } from 'testing/uiMetadataMap';
import { ConfigStatus } from '@app/model/config-model';


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

const mockCustomFilter = {
  "test_group": {
    "box1": {
      "field": "labels",
      "pattern": "json_extractor",
    },
    "box2": {
      "field": "labels",
      "pattern": "test(3|4)",
    },
  },
}

const mockCommonFilter = {
  "general": {
    "my_edits": { 
      "field": "author",
      "pattern": "siembol", 
    },
    "unreleased": {
      "field": "status",
      "pattern": ConfigStatus.UNRELEASED,
    },
    "upgradable": {
      "field": "status",
      "pattern": ConfigStatus.UPGRADABLE,
    },
  },
}

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

  describe('serviceFilterConfig', () => {
    it("should set serviceFilterConfig with only common filters", () => {
      const mockServiceFilterConfig = mockCommonFilter;
      const state = builder.serviceFilterConfig(mockUiMetadataAlert).build();
      expect(state.serviceFilterConfig).toEqual(mockServiceFilterConfig);
    })

    it("should set serviceFilterConfig with both common and custom filters", () => {
      const mockServiceFilterConfig = {
        ...mockCustomFilter,
        ...mockCommonFilter,
      }
      const mockUiMetadataAlertWithCheckboxes = cloneDeep(mockUiMetadataAlert);
      mockUiMetadataAlertWithCheckboxes.checkboxes = mockCustomFilter;
      const state = builder.serviceFilterConfig(mockUiMetadataAlertWithCheckboxes).build()
      expect(state.serviceFilterConfig).toEqual(mockServiceFilterConfig);
    })
  });

  describe('computeConfigManagerRowData', () => {
    it("should compute row data", () => {
      const expectedRowData = [{ 
        author: 'siembol', 
        version: 2, 
        config_name: 'config1', 
        releasedVersion: 2, 
        configHistory: [{ added: 2, author: 'siembol', date: '2021-03-12T16:26:08', removed: 2 }],
        labels: [ 'generic', 'json_extractor' ],
        testCasesCount: 0 ,
        isFiltered: true,
        status: ConfigStatus.UP_TO_DATE,
      },
      { 
        author: 'siembol', 
        version: 0, 
        config_name: 'config1_clone', 
        releasedVersion: 0, 
        configHistory: undefined, 
        labels: undefined, 
        testCasesCount: 0,
        isFiltered: true,
        status: ConfigStatus.UNRELEASED,
      }];
      
      const state = builder.computeConfigManagerRowData().build();
      expect(state.isAnyFilterPresent).toEqual(false);
      expect(state.configManagerRowData).toEqual(expectedRowData);
    })

    it("should compute row data with filtered upgradable rows", () => {
      const expectedRowData = [{ 
        author: 'siembol', 
        version: 2, 
        config_name: 'config1', 
        releasedVersion: 2, 
        configHistory: [{ added: 2, author: 'siembol', date: '2021-03-12T16:26:08', removed: 2 }],
        labels: [ 'generic', 'json_extractor' ],
        testCasesCount: 0 ,
        isFiltered: false,
        status: ConfigStatus.UP_TO_DATE,
      },
      { 
        author: 'siembol', 
        version: 0, 
        config_name: 'config1_clone', 
        releasedVersion: 0, 
        configHistory: undefined, 
        labels: undefined, 
        testCasesCount: 0,
        isFiltered: false,
        status: ConfigStatus.UNRELEASED,
      }];
      
      const state = builder
        .serviceFilterConfig(mockUiMetadataAlert)
        .updateServiceFilters(["general|upgradable"])
        .computeConfigManagerRowData()
        .build();
      
      expect(state.isAnyFilterPresent).toEqual(true);
      expect(state.configManagerRowData).toEqual(expectedRowData);
    })

    it("should compute row data with filtered unreleased rows", () => {
      const expectedRowData = [{ 
        author: 'siembol', 
        version: 2, 
        config_name: 'config1', 
        releasedVersion: 2, 
        configHistory: [{ added: 2, author: 'siembol', date: '2021-03-12T16:26:08', removed: 2 }],
        labels: [ 'generic', 'json_extractor' ],
        testCasesCount: 0 ,
        isFiltered: false,
        status: ConfigStatus.UP_TO_DATE,
      },
      { 
        author: 'siembol', 
        version: 0, 
        config_name: 'config1_clone', 
        releasedVersion: 0, 
        configHistory: undefined, 
        labels: undefined, 
        testCasesCount: 0,
        isFiltered: true,
        status: ConfigStatus.UNRELEASED,
      }];
      
      const state = builder
        .serviceFilterConfig(mockUiMetadataAlert)
        .updateServiceFilters(["general|unreleased"])
        .computeConfigManagerRowData()
        .build();
      
      expect(state.isAnyFilterPresent).toEqual(true);
      expect(state.configManagerRowData).toEqual(expectedRowData);
    })

    it("should compute row data with filtered author rows", () => {
      const expectedRowData = [{ 
        author: 'siembol', 
        version: 2, 
        config_name: 'config1', 
        releasedVersion: 2, 
        configHistory: [{ added: 2, author: 'siembol', date: '2021-03-12T16:26:08', removed: 2 }],
        labels: [ 'generic', 'json_extractor' ],
        testCasesCount: 0 ,
        isFiltered: true,
        status: ConfigStatus.UP_TO_DATE,
      },
      { 
        author: 'siembol', 
        version: 0, 
        config_name: 'config1_clone', 
        releasedVersion: 0, 
        configHistory: undefined, 
        labels: undefined, 
        testCasesCount: 0,
        isFiltered: true,
        status: ConfigStatus.UNRELEASED,
      }];
      
      const state = builder
        .serviceFilterConfig(mockUiMetadataAlert)
        .updateServiceFilters(["general|my_edits"])
        .computeConfigManagerRowData()
        .build();
      
      expect(state.isAnyFilterPresent).toEqual(true);
      expect(state.configManagerRowData).toEqual(expectedRowData);
    })

    it("should compute row data with filtered rows by labels", () => {
      const mockUiMetadataAlertWithCheckboxes = cloneDeep(mockUiMetadataAlert);
      mockUiMetadataAlertWithCheckboxes.checkboxes = mockCustomFilter;
      const expectedRowData = [{ 
        author: 'siembol', 
        version: 2, 
        config_name: 'config1', 
        releasedVersion: 2, 
        configHistory: [{ added: 2, author: 'siembol', date: '2021-03-12T16:26:08', removed: 2 }],
        labels: [ 'generic', 'json_extractor' ],
        testCasesCount: 0 ,
        isFiltered: true,
        status: ConfigStatus.UP_TO_DATE,
      },
      { 
        author: 'siembol', 
        version: 0, 
        config_name: 'config1_clone', 
        releasedVersion: 0, 
        configHistory: undefined, 
        labels: undefined, 
        testCasesCount: 0,
        isFiltered: false,
        status: ConfigStatus.UNRELEASED,
      }];
      
      const state = builder
        .serviceFilterConfig(mockUiMetadataAlertWithCheckboxes)
        .updateServiceFilters(["test_group|box1"])
        .computeConfigManagerRowData()
        .build();
      
      expect(state.serviceFilters).toEqual(["test_group|box1"])
      expect(state.isAnyFilterPresent).toEqual(true);
      expect(state.configManagerRowData).toEqual(expectedRowData);
    })

    it("should compute row data with filtered rows by labels", () => {
      const mockUiMetadataAlertWithCheckboxes = cloneDeep(mockUiMetadataAlert);
      mockUiMetadataAlertWithCheckboxes.checkboxes = mockCustomFilter;
      const expectedRowData = [{ 
        author: 'siembol', 
        version: 2, 
        config_name: 'config1', 
        releasedVersion: 2, 
        configHistory: [{ added: 2, author: 'siembol', date: '2021-03-12T16:26:08', removed: 2 }],
        labels: [ 'generic', 'json_extractor' ],
        testCasesCount: 0 ,
        isFiltered: false,
        status: ConfigStatus.UP_TO_DATE,
      },
      { 
        author: 'siembol', 
        version: 0, 
        config_name: 'config1_clone', 
        releasedVersion: 0, 
        configHistory: undefined, 
        labels: undefined, 
        testCasesCount: 0,
        isFiltered: false,
        status: ConfigStatus.UNRELEASED,
      }];
      
      const state = builder
        .serviceFilterConfig(mockUiMetadataAlertWithCheckboxes)
        .updateServiceFilters(["test_group|box1","test_group|box2"])
        .computeConfigManagerRowData()
        .build();
      
      expect(state.serviceFilters).toEqual(["test_group|box1", "test_group|box2"])
      expect(state.isAnyFilterPresent).toEqual(true);
      expect(state.configManagerRowData).toEqual(expectedRowData);
    })
  });
});
