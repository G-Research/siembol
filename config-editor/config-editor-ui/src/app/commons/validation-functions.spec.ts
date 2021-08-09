import { FormControl } from "@angular/forms";
import { checkBrackets, replaceEscaped, validateJsonPath, validateNamedGroups } from "./validation-functions";

fdescribe('Test validation functions', () => {
  it('Test checkBrackets', () => {
    expect(checkBrackets('(')).toBeFalse();
    expect(checkBrackets('(?<test>(hello)')).toBeFalse();
    expect(checkBrackets('\\()')).toBeFalse();
    expect(checkBrackets('())(')).toBeFalse();
    expect(checkBrackets('([test(])')).toBeTrue();
    expect(checkBrackets('\\(')).toBeTrue();
  })

  it('Test validateNamedGroups', () => {
    expect(validateNamedGroups('(?<hi>.*)(?<hi2>.*)(?<hi3>\\)jkjkjhi)hi(?<hi4>[d)]+))dsadhjs(?<hi5>.*)')).toBeTrue();
    expect(validateNamedGroups('(?<test-1>.*)')).toBeFalse();
    expect(validateNamedGroups('(?<1test>.*)')).toBeFalse();
  })

  it('Test replaceEscaped', () => {
    expect(replaceEscaped('test\\Qtest(\\Etest')).toBe('testatest');
    expect(replaceEscaped('test\\Qtest(\\Etest\\Qtesting(')).toBe('testatesta');
    expect(replaceEscaped("test\\(\\\\test")).toBe('testaatest');
    expect(replaceEscaped("test\\Qtest\test\\Ete\\)st")).toBe('testateast');
    expect(replaceEscaped("test\\Qte[st]\test\\Ete\\)st")).toBe('testateast');
  })

  it(' Test validateJsonPath', () => {
    expect(validateJsonPath({value: "$$"} as FormControl)).toEqual({jsonpath: true});
  })
});
