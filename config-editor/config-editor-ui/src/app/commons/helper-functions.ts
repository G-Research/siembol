import { FILTER_DELIMITER, FILTER_PARAM_KEY, SEARCH_PARAM_KEY, ServiceSearch } from '@app/model/config-model';
import { isEqual } from 'lodash';

export function copyTextToClipboard(text: string): boolean {
  const textArea = document.createElement('textarea');
  textArea.style.position = 'fixed';
  textArea.style.top = '0';
  textArea.style.left = '0';
  textArea.style.opacity = '0';
  textArea.value = text;
  document.body.appendChild(textArea);
  textArea.select();
  let success;
  try {
    success = document.execCommand('copy');
  } catch (err) {
    console.error('error copying to clipboard, ', err);
  }
  document.body.removeChild(textArea);

  return success;
}

export function replacer(key, value) {
  return value === null ? undefined : value;
}

export function areJsonEqual(config1: any, config2: any) {
  return isEqual(config1, config2);
}

export function parseSearchParams(search: ServiceSearch): ServiceSearch {
  const result = { [SEARCH_PARAM_KEY]: search[SEARCH_PARAM_KEY]};
  if (search[FILTER_PARAM_KEY]){
    for (const param of search[FILTER_PARAM_KEY]) {
      const [groupName, filterName] = param.split(FILTER_DELIMITER, 2);
      if (!result[groupName]) {
        result[groupName] = [];
      }
      result[groupName].push(filterName);
    }
  }
  return result;
}