package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.alerts.model.MatcherDto;
import uk.co.gresearch.siembol.alerts.model.MatcherTypeDto;

import java.util.*;

import static org.mockito.Mockito.when;

public class SigmaConditionTokenTest {
    /**
     *   not     1 of filter*
     **/
    @Multiline
    private static String condition1;

    /**
     *   (     1 of filter* and not secret) or            long
     **/
    @Multiline
    private static String condition2;

    /**
     * 1 of A | 1 of B
     **/
    @Multiline
    private static String conditionUnsupported;

    /**
     * 1 of A $ 1 of B
     **/
    @Multiline
    private static String conditionUnknownToken;

    private SigmaConditionTokenNode node;
    Map<String, SigmaSearch> searches;
    SigmaSearch searchSecret;
    List<MatcherDto> secretMatchers = Collections.singletonList(new MatcherDto());
    SigmaSearch searchPublic;
    List<MatcherDto> publicMatchers = Collections.singletonList(new MatcherDto());

    @Before
    public void Setup() {
        searches = new HashMap<>();
        searchSecret = Mockito.mock(SigmaSearch.class);
        when(searchSecret.getSiembolMatchers()).thenReturn(secretMatchers);

        searchPublic = Mockito.mock(SigmaSearch.class);
        when(searchPublic.getSiembolMatchers()).thenReturn(publicMatchers);
        searches.put("secret", searchSecret);
        searches.put("public", searchPublic);
    }

    @Test
    public void getTokensOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize(condition1);
        Assert.assertEquals(3, tokens.size());
        Assert.assertEquals(SigmaConditionToken.TOKEN_NOT, tokens.get(0).getLeft());
        Assert.assertEquals("not", tokens.get(0).getRight());
        Assert.assertEquals(SigmaConditionTokenType.UNARY_OPERATOR, tokens.get(0).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ONE, tokens.get(1).getLeft());
        Assert.assertEquals("1 of", tokens.get(1).getRight());
        Assert.assertEquals(SigmaConditionTokenType.UNARY_OPERATOR, tokens.get(1).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ID, tokens.get(2).getLeft());
        Assert.assertEquals("filter*", tokens.get(2).getRight());
        Assert.assertEquals(SigmaConditionTokenType.SEARCH_ID, tokens.get(2).getKey().getType());
    }

    @Test
    public void getTokens2Ok() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize(condition2);
        Assert.assertEquals(9, tokens.size());
        Assert.assertEquals(SigmaConditionToken.TOKEN_LEFT_BRACKET, tokens.get(0).getLeft());
        Assert.assertEquals("(", tokens.get(0).getRight());
        Assert.assertEquals(SigmaConditionTokenType.AUXILIARY, tokens.get(0).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ONE, tokens.get(1).getLeft());
        Assert.assertEquals("1 of", tokens.get(1).getRight());
        Assert.assertEquals(SigmaConditionTokenType.UNARY_OPERATOR, tokens.get(1).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ID, tokens.get(2).getLeft());
        Assert.assertEquals("filter*", tokens.get(2).getRight());
        Assert.assertEquals(SigmaConditionTokenType.SEARCH_ID, tokens.get(2).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_AND, tokens.get(3).getLeft());
        Assert.assertEquals("and", tokens.get(3).getRight());
        Assert.assertEquals(SigmaConditionTokenType.BINARY_OPERATOR, tokens.get(3).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_NOT, tokens.get(4).getLeft());
        Assert.assertEquals("not", tokens.get(4).getRight());
        Assert.assertEquals(SigmaConditionTokenType.UNARY_OPERATOR, tokens.get(4).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ID, tokens.get(5).getLeft());
        Assert.assertEquals("secret", tokens.get(5).getRight());
        Assert.assertEquals(SigmaConditionTokenType.SEARCH_ID, tokens.get(5).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_RIGHT_BRACKET, tokens.get(6).getLeft());
        Assert.assertEquals(")", tokens.get(6).getRight());
        Assert.assertEquals(SigmaConditionTokenType.AUXILIARY, tokens.get(6).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_OR, tokens.get(7).getLeft());
        Assert.assertEquals("or", tokens.get(7).getRight());
        Assert.assertEquals(SigmaConditionTokenType.BINARY_OPERATOR, tokens.get(7).getKey().getType());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ID, tokens.get(8).getLeft());
        Assert.assertEquals("long", tokens.get(8).getRight());
        Assert.assertEquals(SigmaConditionTokenType.SEARCH_ID, tokens.get(8).getKey().getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenizeUnsupported() {
        SigmaConditionToken.tokenize(conditionUnsupported);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenizeUnknown() {
        SigmaConditionToken.tokenize(conditionUnknownToken);
    }

    @Test
    public void getMatchersIdOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("secret");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
    }

    @Test
    public void getMatchersIdOkWildcard() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("s*");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersIdNoSearch() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("unknown");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);

        node.getToken().getMatchersList(node);
    }

    @Test
    public void getMatchersIdThemOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("them");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(2, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(2, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
        Assert.assertEquals(matchers.get(1), matchersList.get(1).get(0));
    }

    @Test
    public void getMatchersAllOfOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("all of secret");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
    }

    @Test
    public void getMatchersAllOfOkWildcard() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("all of s*");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersAllOfNoSearch() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("all of unknown");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        node.getToken().getMatchersList(node);
    }

    @Test
    public void getMatchersAllOfThemOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("all of them");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(2, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(2, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
        Assert.assertEquals(matchers.get(1), matchersList.get(1).get(0));
    }

    @Test
    public void getMatchersOneOfOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("1 of secret");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
    }

    @Test
    public void getMatchersOneOfOkWildcard() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("1 of s*");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersOneOfNoSearch() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("1 of unknown");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        node.getToken().getMatchersList(node);
    }

    @Test
    public void getMatchersOneOfThemOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("1 of them");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(MatcherTypeDto.COMPOSITE_OR, matchersList.get(0).get(0).getType());
        Assert.assertEquals(MatcherTypeDto.COMPOSITE_OR, matchers.get(0).getType());

        Assert.assertEquals(2, matchers.get(0).getMatchers().size());
        Assert.assertEquals(2, matchersList.get(0).get(0).getMatchers().size());
    }

    @Test
    public void getMatchersNotOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("not secret");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertTrue(matchersList.get(0).get(0).getNegated());
    }


    @Test(expected = IllegalArgumentException.class)
    public void getMatchersNotNoSearch() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("not unknown");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        node.getToken().getMatchersList(node);
    }

    @Test
    public void getMatchersNotOneOfThemOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("not 1 of them");

        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));
        node.getFirstOperand().setFirstOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());

        Assert.assertEquals(MatcherTypeDto.COMPOSITE_OR, matchersList.get(0).get(0).getType());
        Assert.assertEquals(2, matchersList.get(0).get(0).getMatchers().size());
        Assert.assertTrue(matchersList.get(0).get(0).getNegated());
    }

    @Test
    public void getMatchersNotAllOfThemOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("not all of them");

        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));
        node.getFirstOperand().setFirstOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());

        Assert.assertEquals(MatcherTypeDto.COMPOSITE_AND, matchersList.get(0).get(0).getType());
        Assert.assertEquals(2, matchersList.get(0).get(0).getMatchers().size());
        Assert.assertTrue(matchersList.get(0).get(0).getNegated());
    }

    @Test
    public void getMatchersAndOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("all of secret");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(1), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
    }

    @Test
    public void getMatchersAndOkWildcard() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("secret and public");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));
        node.setSecondOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(2, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchersList.get(1).size());
        Assert.assertEquals(2, matchers.size());
        Assert.assertEquals(matchers.get(0), matchersList.get(0).get(0));
        Assert.assertEquals(matchers.get(1), matchersList.get(1).get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersAndNoSearch() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("secret and unknown");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));
        node.setSecondOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersAndNoSearch2() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("unknown and secret");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));
        node.setSecondOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        node.getToken().getMatchersList(node);
    }

    @Test
    public void getMatchersOrOk() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("secret or public");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));
        node.setSecondOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        List<List<MatcherDto>> matchersList = node.getToken().getMatchersList(node);
        List<MatcherDto> matchers = node.getToken().getMatchers(node);

        Assert.assertEquals(1, matchersList.size());
        Assert.assertEquals(1, matchersList.get(0).size());
        Assert.assertEquals(1, matchers.size());

        Assert.assertEquals(MatcherTypeDto.COMPOSITE_OR, matchers.get(0).getType());
        Assert.assertEquals(2, matchers.get(0).getMatchers().size());
        Assert.assertFalse(matchers.get(0).getNegated());


    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersOrNoSearch() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("secret or unknown");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));
        node.setSecondOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersOrNoSearch2() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("unknown or secret");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));
        node.setSecondOperand(new SigmaConditionTokenNode(tokens.get(2), searches));

        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalStateException.class)
    public void getMatchersUnsupported() {
        node = new SigmaConditionTokenNode(Pair.of(SigmaConditionToken.TOKEN_AGG, "agg"), searches);
        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersOrWrongArguments() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("secret or");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));

        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersAndWrongArguments() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("secret and");
        node = new SigmaConditionTokenNode(tokens.get(1), searches);
        node.setFirstOperand(new SigmaConditionTokenNode(tokens.get(0), searches));

        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersNotWrongArguments() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("not");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);

        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersAllOfWrongArguments() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("all of");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);

        node.getToken().getMatchersList(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatchersOneOfWrongArguments() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize("1 of");
        node = new SigmaConditionTokenNode(tokens.get(0), searches);

        node.getToken().getMatchersList(node);
    }
}
