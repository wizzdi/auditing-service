package com.flexicore.audit.rest;

import com.flexicore.annotations.IOperation;
import com.flexicore.audit.model.AuditingEvent;
import com.flexicore.audit.request.AuditingFilter;
import com.flexicore.audit.request.OperatingIdRef;
import com.flexicore.data.jsoncontainers.PaginationResponse;
import com.flexicore.init.FlexiCoreApplication;
import com.flexicore.model.Operation;
import com.flexicore.model.User;
import com.flexicore.request.AuthenticationRequest;
import com.flexicore.request.OperationFiltering;
import com.flexicore.request.UserCreate;
import com.flexicore.response.AuthenticationResponse;
import com.flexicore.rest.UserRESTService;
import com.flexicore.security.SecurityContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FlexiCoreApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class AuditRESTServiceTest {

    private Operation operation;
    @Autowired
    private TestRestTemplate restTemplate;
    private OffsetDateTime timeFrom;
    private User user;

    @BeforeAll
    private void init() {
        ResponseEntity<AuthenticationResponse> authenticationResponse = this.restTemplate.postForEntity("/FlexiCore/rest/authenticationNew/login", new AuthenticationRequest().setEmail("admin@flexicore.com").setPassword("admin"), AuthenticationResponse.class);
        String authenticationKey = authenticationResponse.getBody().getAuthenticationKey();
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("authenticationKey", authenticationKey);
                    return execution.execute(request, body);
                }));
    }

    private User createUser() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> request = new HttpEntity<>(new UserCreate().setPassword("test").setEmail(UUID.randomUUID().toString() + "@test.com").setName("test"), headers);
        ResponseEntity<User> createCategory = this.restTemplate.exchange("/FlexiCore/rest/users/createUser", HttpMethod.POST, request, User.class);
        return createCategory.getBody();

    }

    @Test
    @Order(1)
    public void setOperationAuditable() throws NoSuchMethodException {
        OperationFiltering request = new OperationFiltering();
        IOperation createUser = UserRESTService.class.getMethod("createUser", String.class, UserCreate.class, SecurityContext.class).getAnnotation(IOperation.class);
        String name = createUser.Name();
        request.setNameLike(name);
        ParameterizedTypeReference<PaginationResponse<Operation>> t = new ParameterizedTypeReference<PaginationResponse<Operation>>() {};

        ResponseEntity<PaginationResponse<Operation>> operationResponse = this.restTemplate.exchange("/FlexiCore/rest/operations/getAllOperations", HttpMethod.POST, new HttpEntity<>(request), t);
        Assertions.assertEquals(200, operationResponse.getStatusCodeValue());
        PaginationResponse<Operation> body = operationResponse.getBody();
        Assertions.assertNotNull(body);
        List<Operation> operations = body.getList();
        Assertions.assertFalse(operations.isEmpty());
        this.operation = operations.get(0);

        HttpHeaders headers = new HttpHeaders();
        headers.add("auditable", true + "");
        HttpEntity<?> updateOperationRequest = new HttpEntity<>(null, headers);
        ResponseEntity<Operation> operationUpdateResponse = this.restTemplate.exchange("/FlexiCore/rest/operations/setOperationAuditable/"+operation.getId(), HttpMethod.PUT, updateOperationRequest, Operation.class);
        Assertions.assertEquals(200, operationUpdateResponse.getStatusCodeValue());
        this.operation = operationUpdateResponse.getBody();
        Assertions.assertNotNull(this.operation);
        Assertions.assertTrue(this.operation.isAuditable());


    }

    @Test
    @Order(2)
    public void testCreateAudit() throws InterruptedException {
        timeFrom = OffsetDateTime.now();
        user=createUser();
        Thread.sleep(2000);
        AuditingFilter request = new AuditingFilter();
        request.setFromDate(timeFrom);
        request.setOperationIds(Collections.singleton(new OperatingIdRef(operation.getId())));
        ParameterizedTypeReference<PaginationResponse<AuditingEvent>> t = new ParameterizedTypeReference<PaginationResponse<AuditingEvent>>() {};

        ResponseEntity<PaginationResponse<AuditingEvent>> response = this.restTemplate.exchange("/FlexiCore/rest/audit/getAllAuditingEvents", HttpMethod.POST, new HttpEntity<>(request), t);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        PaginationResponse<AuditingEvent> body = response.getBody();
        Assertions.assertNotNull(body);
        List<AuditingEvent> auditingEvents = body.getList();
        Assertions.assertFalse(auditingEvents.isEmpty());

    }

    @Test
    @Order(3)
    public void testGetAllAuditsAuditTypeLike() throws InterruptedException {
        AuditingFilter request = new AuditingFilter();
        request.setAuditTypeLike("/ES/");
        ParameterizedTypeReference<PaginationResponse<AuditingEvent>> t = new ParameterizedTypeReference<PaginationResponse<AuditingEvent>>() {};
        ResponseEntity<PaginationResponse<AuditingEvent>> response = this.restTemplate.exchange("/FlexiCore/rest/audit/getAllAuditingEvents", HttpMethod.POST, new HttpEntity<>(request), t);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        PaginationResponse<AuditingEvent> body = response.getBody();
        Assertions.assertNotNull(body);
        List<AuditingEvent> auditingEvents = body.getList();
        Assertions.assertFalse(auditingEvents.isEmpty());

    }

    @Test
    @Order(4)
    public void testGetAllAuditsUserNameLike() throws InterruptedException {
        AuditingFilter request = new AuditingFilter();
        request.setUserNameLike("/"+user.getName().substring(1,user.getName().length()-1)+"/");
        ParameterizedTypeReference<PaginationResponse<AuditingEvent>> t = new ParameterizedTypeReference<PaginationResponse<AuditingEvent>>() {};
        ResponseEntity<PaginationResponse<AuditingEvent>> response = this.restTemplate.exchange("/FlexiCore/rest/audit/getAllAuditingEvents", HttpMethod.POST, new HttpEntity<>(request), t);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        PaginationResponse<AuditingEvent> body = response.getBody();
        Assertions.assertNotNull(body);
        List<AuditingEvent> auditingEvents = body.getList();
        Assertions.assertFalse(auditingEvents.isEmpty());

    }

    @Test
    @Order(5)
    public void testGetAllAuditsOperationNameLike() throws InterruptedException {
        AuditingFilter request = new AuditingFilter();
        request.setOperationNameLike("/"+operation.getName().substring(1,operation.getName().length()-1)+"/");
        ParameterizedTypeReference<PaginationResponse<AuditingEvent>> t = new ParameterizedTypeReference<PaginationResponse<AuditingEvent>>() {};
        ResponseEntity<PaginationResponse<AuditingEvent>> response = this.restTemplate.exchange("/FlexiCore/rest/audit/getAllAuditingEvents", HttpMethod.POST, new HttpEntity<>(request), t);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        PaginationResponse<AuditingEvent> body = response.getBody();
        Assertions.assertNotNull(body);
        List<AuditingEvent> auditingEvents = body.getList();
        Assertions.assertFalse(auditingEvents.isEmpty());

    }

}
