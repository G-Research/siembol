package uk.co.gresearch.siembol.configeditor.serviceaggregator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.common.ServiceType;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ServiceAggregatorImplTest {
    private AuthorisationProvider authProvider;
    private ConfigStore store;
    private ConfigSchemaService schemaService;
    private ServiceType serviceType = ServiceType.ALERT;
    private ServiceAggregatorImpl.Builder builder;
    private ServiceAggregator serviceAggregator;
    private UserInfo user;


    @Before
    public void setUp() {
        authProvider = Mockito.mock(AuthorisationProvider.class);
        store = Mockito.mock(ConfigStore.class);
        schemaService = Mockito.mock(ConfigSchemaService.class);
        when(schemaService.getAdminConfigurationSchema()).thenReturn(
                new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, new ConfigEditorAttributes()));
        builder = new ServiceAggregatorImpl.Builder(authProvider);
        when(authProvider.getUserAuthorisation(any(), any()))
                .thenReturn(AuthorisationProvider.AuthorisationResult.ALLOWED);
        builder.addService("a", serviceType, store, schemaService);
        builder.addService("b", serviceType, store, schemaService);
        builder.addService("c", serviceType, store, schemaService);
        user = new UserInfo();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServicesWithTheSameName() {
        builder.addService("a", serviceType, store, schemaService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noServiceException() {
        new ServiceAggregatorImpl.Builder(authProvider).build();
    }

    @Test
    public void getStoreServicesOk() {
        serviceAggregator = builder.build();
        List<ConfigStore> configStores =  serviceAggregator.getConfigStoreServices();
        Assert.assertNotNull(configStores);
        Assert.assertEquals(3, configStores.size());
        configStores.forEach(x -> Assert.assertEquals(store, x));
    }

    @Test
    public void getConfigSchemaServicesOk() {
        serviceAggregator = builder.build();
        List<ConfigSchemaService> configSchemaServices =  serviceAggregator.getConfigSchemaServices();
        Assert.assertNotNull(configSchemaServices);
        Assert.assertEquals(3, configSchemaServices.size());
        configSchemaServices.forEach(x -> Assert.assertEquals(schemaService, x));
    }

    @Test
    public void checkHealthConfigStoreUp() {
        when(store.checkHealth()).thenReturn(new Health.Builder().up().build());
        serviceAggregator = builder.build();
        Health health = serviceAggregator.checkConfigStoreServicesHealth();
        Assert.assertEquals(Status.UP, health.getStatus());
    }

    @Test
    public void checkHealthConfigStoreDown() {
        when(store.checkHealth()).thenReturn(new Health.Builder().down().build());
        serviceAggregator = builder.build();
        Health health = serviceAggregator.checkConfigStoreServicesHealth();
        Assert.assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    public void checkHealthSchemaServicesUp() {
        when(schemaService.checkHealth()).thenReturn(new Health.Builder().up().build());
        serviceAggregator = builder.build();
        Health health = serviceAggregator.checkConfigSchemaServicesHealth();
        Assert.assertEquals(Status.UP, health.getStatus());
    }

    @Test
    public void checkHealthSchemaServicesDown() {
        when(schemaService.checkHealth()).thenReturn(new Health.Builder().down().build());
        serviceAggregator = builder.build();
        Health health = serviceAggregator.checkConfigSchemaServicesHealth();
        Assert.assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    public void getConfigStoreAuthorised() {
        when(authProvider.getUserAuthorisation(eq(user), eq("a")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.ALLOWED);
        serviceAggregator = builder.build();
        ConfigStore userStore = serviceAggregator.getConfigStore(user, "a");
        Mockito.verify(authProvider, times(1))
                .getUserAuthorisation(eq(user), eq("a"));
        Assert.assertEquals(userStore, store);
    }

    @Test
    public void getConfigSchemaAuthorised() {
        when(authProvider.getUserAuthorisation(eq(user), eq("a")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.ALLOWED);
        serviceAggregator = builder.build();
        ConfigSchemaService userSchemaService = serviceAggregator.getConfigSchema(user, "a");
        Mockito.verify(authProvider, times(1))
                .getUserAuthorisation(eq(user), eq("a"));
        Assert.assertEquals(userSchemaService, schemaService);
    }

    @Test(expected = uk.co.gresearch.siembol.configeditor.common.AuthorisationException.class)
    public void getConfigStoreUnauthorised() {
        when(authProvider.getUserAuthorisation(eq(user), eq("a")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.FORBIDDEN);
        serviceAggregator = builder.build();
        ConfigStore userStore = serviceAggregator.getConfigStore(user, "a");
    }

    @Test(expected = uk.co.gresearch.siembol.configeditor.common.AuthorisationException.class)
    public void getConfigSchemaUnauthorised() {
        when(authProvider.getUserAuthorisation(eq(user), eq("a")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.FORBIDDEN);
        serviceAggregator = builder.build();
        ConfigSchemaService userSchemaService = serviceAggregator.getConfigSchema(user, "a");
    }

    @Test
    public void getConfigEditorServices() {
        when(authProvider.getUserAuthorisation(eq(user), eq("a")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.ALLOWED);
        when(authProvider.getUserAuthorisation(eq(user), eq("b")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.FORBIDDEN);
        when(authProvider.getUserAuthorisation(eq(user), eq("c")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.ALLOWED);
        serviceAggregator = builder.build();
        List<ConfigEditorService> userServices = serviceAggregator.getConfigEditorServices(user);

        Mockito.verify(authProvider, times(2))
                .getUserAuthorisation(eq(user), eq("a"));
        Mockito.verify(authProvider, times(2))
                .getUserAuthorisation(eq(user), eq("b"));
        Mockito.verify(authProvider, times(2))
                .getUserAuthorisation(eq(user), eq("c"));
        
        Assert.assertEquals(2, userServices.size());
        Assert.assertEquals("a", userServices.get(0).getName());
        Assert.assertNotNull(userServices.get(0).getUserRoles());
        Assert.assertEquals("c", userServices.get(1).getName());
        Assert.assertNotNull(userServices.get(1).getUserRoles());
        Assert.assertEquals(2, userServices.get(1).getUserRoles().size());
    }

    public void getConfigEditorServicesNoAdmins() {
        when(schemaService.getAdminConfigurationSchema()).thenReturn(
                ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, "not supported"));
        when(authProvider.getUserAuthorisation(eq(user), eq("a")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.ALLOWED);
        when(authProvider.getUserAuthorisation(eq(user), eq("b")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.FORBIDDEN);
        when(authProvider.getUserAuthorisation(eq(user), eq("c")))
                .thenReturn(AuthorisationProvider.AuthorisationResult.ALLOWED);
        serviceAggregator = builder.build();
        List<ConfigEditorService> userServices = serviceAggregator.getConfigEditorServices(user);

        Mockito.verify(authProvider, times(2))
                .getUserAuthorisation(eq(user), eq("a"));
        Mockito.verify(authProvider, times(2))
                .getUserAuthorisation(eq(user), eq("b"));
        Mockito.verify(authProvider, times(2))
                .getUserAuthorisation(eq(user), eq("c"));

        Assert.assertEquals(2, userServices.size());
        Assert.assertEquals("a", userServices.get(0).getName());
        Assert.assertNotNull(userServices.get(0).getUserRoles());
        Assert.assertEquals("c", userServices.get(1).getName());
        Assert.assertNotNull(userServices.get(1).getUserRoles());
        Assert.assertEquals(1, userServices.get(1).getUserRoles().size());
    }
}
