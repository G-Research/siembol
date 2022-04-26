import { parseSearchParams } from "./helper-functions";

describe('parseSearchParams', () => {
    it('should parse search params', () => {
        const search = { "search": "search string", "filter": ["integrity|high", "integrity|low", "platform|windows"] }
        expect(parseSearchParams(search)).toEqual({ "search": "search string", "integrity": ["high","low"], "platform": ["windows"]})
    });

    it('should ignore unknown params', () => {
        const search = { "search": "search string", "filter": ["integrity|high", "integrity|low"], "test": "test" }
        expect(parseSearchParams(search)).toEqual({ "search": "search string", "integrity": ["high","low"]})
    });
});
