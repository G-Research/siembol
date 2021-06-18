package uk.co.gresearch.siembol.alerts.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.compiler.MatcherType;

import java.util.*;

import static org.mockito.Mockito.*;

public class CompositeMatcherTest {
    private Map<String, Object> event;
    List<Matcher> matchers;
    private Matcher matcher1;
    private Matcher matcher2;
    private CompositeMatcher.Builder builder;
    private CompositeMatcher compositeMatcher;

    @Before
    public void setUp() {
        event = new HashMap<>();
        matcher1 = Mockito.mock(Matcher.class);
        matcher2 = Mockito.mock(Matcher.class);
        builder = CompositeMatcher.builder();
        matchers = Arrays.asList(matcher1, matcher2);
        when(matcher1.canModifyEvent()).thenReturn(false);
        when(matcher2.canModifyEvent()).thenReturn(false);
        when(matcher1.match(event)).thenReturn(EvaluationResult.MATCH);
        when(matcher2.match(event)).thenReturn(EvaluationResult.MATCH);

        builder.matchers(matchers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingMatchers() {
        CompositeMatcher.builder()
                .isNegated(true)
                .matcherType(MatcherType.COMPOSITE_OR)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyMatchers() {
        CompositeMatcher.builder()
                .isNegated(true)
                .matcherType(MatcherType.COMPOSITE_OR)
                .matchers(new ArrayList<>())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingNegated() {
        CompositeMatcher.builder()
                .matcherType(MatcherType.COMPOSITE_OR)
                .matchers(matchers)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingType() {
        CompositeMatcher.builder()
                .isNegated(true)
                .matchers(matchers)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongType() {
        CompositeMatcher.builder()
                .isNegated(true)
                .matchers(matchers)
                .matcherType(MatcherType.REGEX_MATCH)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void canModifyEventCompositeOrException() {
        when(matcher2.canModifyEvent()).thenReturn(true);
        CompositeMatcher.builder()
                .isNegated(true)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_OR)
                .build();
        verify(matcher1, times(1)).canModifyEvent();
        verify(matcher2, times(1)).canModifyEvent();
    }

    @Test
    public void canModifyEventCompositeOrOk() {
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(true)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_OR)
                .build();
        verify(matcher1, times(1)).canModifyEvent();
        verify(matcher2, times(1)).canModifyEvent();
        Assert.assertFalse(compositeMatcher.canModifyEvent());
    }

    @Test
    public void canModifyEventCompositeAndOk() {
        when(matcher1.canModifyEvent()).thenReturn(false);
        when(matcher2.canModifyEvent()).thenReturn(true);
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(false)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_AND)
                .build();
        verify(matcher1, times(1)).canModifyEvent();
        verify(matcher2, times(1)).canModifyEvent();
        Assert.assertTrue(compositeMatcher.canModifyEvent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void canModifyEventCompositeAndNegatedFailure() {
        when(matcher1.canModifyEvent()).thenReturn(false);
        when(matcher2.canModifyEvent()).thenReturn(true);
        CompositeMatcher.builder()
                .isNegated(true)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_AND)
                .build();
    }

    @Test
    public void compositeOrMatch() {
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(false)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_OR)
                .build();
        EvaluationResult result = compositeMatcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
        verify(matcher1, times(1)).match(eq(event));
        verify(matcher2, times(0)).match(eq(event));
    }

    @Test
    public void compositeOrMatchNegated() {
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(true)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_OR)
                .build();
        EvaluationResult result = compositeMatcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
        verify(matcher1, times(1)).match(eq(event));
        verify(matcher2, times(0)).match(eq(event));
    }

    @Test
    public void compositeOrNoMatch() {
        when(matcher1.match(event)).thenReturn(EvaluationResult.NO_MATCH);
        when(matcher2.match(event)).thenReturn(EvaluationResult.NO_MATCH);
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(false)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_OR)
                .build();
        EvaluationResult result = compositeMatcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
        verify(matcher1, times(1)).match(eq(event));
        verify(matcher2, times(1)).match(eq(event));
    }

    @Test
    public void compositeAndMatch() {
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(false)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_AND)
                .build();
        EvaluationResult result = compositeMatcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
        verify(matcher1, times(1)).match(eq(event));
        verify(matcher2, times(1)).match(eq(event));
    }

    @Test
    public void compositeAndMatchNegated() {
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(true)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_AND)
                .build();
        EvaluationResult result = compositeMatcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
        verify(matcher1, times(1)).match(eq(event));
        verify(matcher2, times(1)).match(eq(event));
    }

    @Test
    public void compositeAndNoMatch() {
        when(matcher1.match(event)).thenReturn(EvaluationResult.NO_MATCH);
        compositeMatcher = CompositeMatcher.builder()
                .isNegated(false)
                .matchers(matchers)
                .matcherType(MatcherType.COMPOSITE_AND)
                .build();
        EvaluationResult result = compositeMatcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
        verify(matcher1, times(1)).match(eq(event));
        verify(matcher2, times(0)).match(eq(event));
    }

}
