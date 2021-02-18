package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class CompositeSyncActionTest {
    private SynchronisationAction okAction;
    private SynchronisationAction errorAction;
    private CompositeSyncAction compositeSyncAction;
    ConfigEditorServiceContext context;
    CompositeSyncAction.Builder builder;


    @Before
    public void setUp() {
        builder = new CompositeSyncAction.Builder();
        context = new ConfigEditorServiceContext();
        okAction = Mockito.mock(SynchronisationAction.class);
        errorAction = Mockito.mock(SynchronisationAction.class);
        when(okAction.execute(eq(context))).thenReturn(ConfigEditorResult.fromServiceContext(context));
        when(errorAction.execute(eq(context))).thenReturn(ConfigEditorResult.fromMessage(ERROR, "error"));
    }

    @Test
    public void getOkActions() {
        compositeSyncAction = builder
                .addAction(okAction)
                .addAction(okAction)
                .addAction(okAction)
                .build();

        ConfigEditorResult result = compositeSyncAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertEquals(context, result.getAttributes().getServiceContext());

        verify(okAction, times(3)).execute(eq(context));
    }

    @Test
    public void getErrorActionFirst() {
        compositeSyncAction = builder
                .addAction(errorAction)
                .addAction(okAction)
                .addAction(okAction)
                .build();

        ConfigEditorResult result = compositeSyncAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());

        verify(errorAction, times(1)).execute(eq(context));
        verify(okAction, times(0)).execute(eq(context));
    }

    @Test
    public void getErrorActionSecond() {
        compositeSyncAction = builder
                .addAction(okAction)
                .addAction(errorAction)
                .addAction(okAction)
                .build();

        ConfigEditorResult result = compositeSyncAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());

        verify(errorAction, times(1)).execute(eq(context));
        verify(okAction, times(1)).execute(eq(context));
    }

    @Test
    public void builderEmpty() {
        Assert.assertTrue(builder.isEmpty());
        builder.addAction(okAction);
        Assert.assertFalse(builder.isEmpty());
    }
}
