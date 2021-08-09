import { FormControl, ValidationErrors } from '@angular/forms';
import * as jp from 'jsonpath';

const DUMMY_VARIABLE_REGEX = 'a';

export function validateRegexConditional(control: FormControl): ValidationErrors {
  return !control.parent || control.parent.value?.matcher_type === 'IS_IN_SET' || !control.value ||
    (checkBrackets(control.value) && validateNamedGroups(control.value))
    ? null : {'regexConditional': true};
}

export function validateJsonPath(control: FormControl): ValidationErrors {
  try{
    jp.parse(control.value)
    return null;
  } catch(e) {
    return {"jsonpath": true};
  }
}

export function validateRegex(control: FormControl): ValidationErrors {
  return !control.value || checkBrackets(control.value) && validateNamedGroups(control.value)? null : {"regex": true};
}

export function checkBrackets(s: string): boolean {
  s = replaceEscaped(s);
  let count = 0;
  for (const c of s) {
    switch(c){
      case '(':
        count++;
        break;
      case ')':
        count--;
        if (count < 0) {
          return false;
        }
        break;
    }
  }
  if (count === 0) {
    return true;
  }
  return false;
}

export function validateNamedGroups(s: string): boolean {
  s = replaceEscapedSequences(s);
  const re = /\(\?<(?<name>[^>]+)>(\[(.*?)\]|\\.|[^)])+\)/g;
  const name_re = /^[a-zA-Z][a-zA-Z0-9:_]*$/;
  let match = s.match(re);
  while (match != null) {
    if (match.groups && !name_re.test(match.groups.name)) {
      return false;
    }
    match = re.exec(s);
  }
  return true;
}

export function replaceEscaped(s:string): string {
  s = replaceEscapedSequences(s);
  s = replaceSquaredBrackets(s);
  return replaceEscapedCharacters(s);
}

function replaceSquaredBrackets(s: string): string {
  return replace(s, /(?<!\\)\[.*?(?<!\\)\]/);
}

function replaceEscapedSequences(s: string): string {
  return replace(s, /\\Q.*?(?:\\E|$)/);
}

function replaceEscapedCharacters(s: string): string {
  return replace(s, /\\./);
}

function replace(s: string, re: RegExp): string {
  let match;
  while((match = re.exec(s)) !== null) {
    s = s.replace(match, DUMMY_VARIABLE_REGEX);
  } 
  return s;
}
