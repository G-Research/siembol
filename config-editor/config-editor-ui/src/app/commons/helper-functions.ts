import { UrlInfo } from '@app/model/config-model';

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

export function parseUrl(path: string): UrlInfo {
  let url = new URL(path, location.origin);
  let paths = url.pathname.substring(1).split('/');

  let service = paths[0];
  let mode = paths[1] == 'admin' ? 'admin' : '';
  let configName = url.searchParams.get('configName');
  let testCaseName = url.searchParams.get('testCaseName');

  return { service: service, mode: mode, configName: configName, testCaseName: testCaseName };
}

export function replacer(key, value) {
  return value === null ? undefined : value;
}
