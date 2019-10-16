export const isNil = val => val === undefined || val === null;
export const isEmpty = val => val == null || !(Object.keys(val) || val).length || (Object.keys(val) || val).length === 0;
